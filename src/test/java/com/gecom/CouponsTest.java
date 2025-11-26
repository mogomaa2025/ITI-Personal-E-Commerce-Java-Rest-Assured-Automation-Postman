package com.gecom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import static com.gecom.utils.Const.BASE_URL;
import static com.gecom.utils.Const.COUPON_DESCRIPTION;
import static com.gecom.utils.Const.COUPON_DISCOUNT_TYPE;
import static com.gecom.utils.Const.COUPON_DISCOUNT_VALUE;
import static com.gecom.utils.Const.COUPON_EXPIRED_CODE;
import static com.gecom.utils.Const.COUPON_INVALID_CODE;
import static com.gecom.utils.Const.COUPON_MAX_DISCOUNT;
import static com.gecom.utils.Const.COUPON_MIN_ORDER_AMOUNT;
import static com.gecom.utils.Const.COUPON_USAGE_LIMIT;
import static com.gecom.utils.Const.COUPON_VALIDATE_ORDER_AMOUNT;
import static com.gecom.utils.Const.CouponCode;
import static com.gecom.utils.Const.IDS_FILE_PATH;
import static com.gecom.utils.Const.TOKEN_FILE_PATH;
import static com.gecom.utils.Const.adminToken;
import static com.gecom.utils.Const.userToken;
import com.gecom.utils.JsonUtility;
import com.github.javafaker.Faker;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

/**
 * This class contains test cases for coupon management functionalities,
 * including creating, viewing, and validating coupons.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "CouponsTest")
@Severity(SeverityLevel.CRITICAL)
public class CouponsTest {

    private final Faker faker = new Faker();

    /**
     * Test case for verifying that an admin can retrieve all available coupons.
     *
     * @throws Exception if an error occurs while reading the admin token.
     */
    @Test(description = "TC-COUP-001: Verify get available coupons")
    public void testGetAvailableCoupons() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send GET to coupons");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/coupons", adminToken);

        Allure.step("Verify coupons returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify each coupon has code/discount/expiry date/conditions");
        List<Map<String, Object>> coupons = response.jsonPath().getList("data");
        if (!coupons.isEmpty()) {
            Map<String, Object> firstCoupon = coupons.get(0);
            Assert.assertTrue(firstCoupon.get("code") instanceof String, "Each coupon has code");
            Assert.assertTrue(firstCoupon.get("discount_type") instanceof String, "Each coupon has discount_type");
            Assert.assertTrue(firstCoupon.get("discount_value") instanceof Number, "Each coupon has discount_value");
            Assert.assertNotNull(firstCoupon.get("expires_at"), "Each coupon has expires_at");
            Assert.assertTrue(firstCoupon.get("min_order_amount") instanceof Number, "Each coupon has min_order_amount");
        }
    }

    /**
     * Test case for verifying that a non-admin user cannot retrieve available coupons.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-COUP-002: Verify non-admin can't get available coupons", dependsOnMethods = "testGetAvailableCoupons")
    public void testNonAdminCantGetAvailableCoupons() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send GET to coupons");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/coupons", userToken);

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify admin privilege error message appears");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"), "admin privilege error message appears");
    }

    /**
     * Test case for verifying that an admin can create a new coupon.
     *
     * @throws Exception if an error occurs while reading the admin token or saving the coupon code.
     */
    @Test(description = "TC-COUP-003: Verify admin can create coupon", dependsOnMethods = "testNonAdminCantGetAvailableCoupons")
    public void testAdminCanCreateCoupon() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Generate random code using java_Faker");
        CouponCode = faker.regexify("[a-z0-9]{8}") + "10";

        Allure.step("Send POST with coupon data");
        Map<String, Object> body = new HashMap<>();
        body.put("code", CouponCode);
        body.put("description", COUPON_DESCRIPTION);
        body.put("discount_type", COUPON_DISCOUNT_TYPE);
        body.put("discount_value", COUPON_DISCOUNT_VALUE);
        body.put("min_order_amount", COUPON_MIN_ORDER_AMOUNT);
        body.put("max_discount", COUPON_MAX_DISCOUNT);
        body.put("usage_limit", COUPON_USAGE_LIMIT);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", adminToken, body);

        Allure.step("Verify created");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Coupon created successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Coupon created successfully", "message is 'Coupon created successfully'");

        Allure.step("Verify data has coupon details matching input");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.code") instanceof String, "data has code");
        Assert.assertEquals(response.jsonPath().getString("data.code"), CouponCode, "data has coupon details matching input");
        Assert.assertTrue(response.jsonPath().get("data.description") instanceof String, "data has description");
        Assert.assertTrue(response.jsonPath().get("data.discount_type") instanceof String, "data has discount_type");
        Assert.assertTrue(response.jsonPath().get("data.discount_value") instanceof Number, "data has discount_value");
        Assert.assertTrue(response.jsonPath().get("data.min_order_amount") instanceof Number, "data has min_order_amount");
        Assert.assertTrue(response.jsonPath().get("data.max_discount") instanceof Number, "data has max_discount");
        Assert.assertTrue(response.jsonPath().get("data.usage_limit") instanceof Integer, "data has usage_limit");

        Allure.step("Save the random code using jsonutility to json ids.json");
        JsonUtility.saveValue("CouponCode", CouponCode, IDS_FILE_PATH);
    }

    /**
     * Test case for verifying that creating a coupon fails for a non-admin user.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-COUP-004: Verify create coupon fails without admin", dependsOnMethods = "testAdminCanCreateCoupon")
    public void testCreateCouponFailsWithoutAdmin() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Generate random code using java_Faker");
        String randomCode = faker.regexify("[a-z0-9]{8}") + "10";

        Allure.step("Send POST");
        Map<String, Object> body = new HashMap<>();
        body.put("code", randomCode);
        body.put("description", COUPON_DESCRIPTION);
        body.put("discount_type", COUPON_DISCOUNT_TYPE);
        body.put("discount_value", COUPON_DISCOUNT_VALUE);
        body.put("min_order_amount", COUPON_MIN_ORDER_AMOUNT);
        body.put("max_discount", COUPON_MAX_DISCOUNT);
        body.put("usage_limit", COUPON_USAGE_LIMIT);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", userToken, body);

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
    }

    /**
     * Test case for verifying that creating a coupon fails with missing data.
     *
     * @throws Exception if an error occurs while reading the admin token.
     */
    @Test(description = "TC-COUP-005: Verify create coupon fails with missing coupon data", dependsOnMethods = "testCreateCouponFailsWithoutAdmin")
    public void testCreateCouponFailsWithMissingData() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Generate random code using java_Faker");
        String randomCode = faker.regexify("[a-z0-9]{8}") + "10";

        Allure.step("Send POST with incomplete data");
        Map<String, Object> body = new HashMap<>();
        body.put("code", randomCode);
        body.put("discount", 15);
        body.put("type", "percentage");
        body.put("expiry_date", "2026-12-31");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", adminToken, body);

        Allure.step("Verify missing data message");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates missing fields");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Missing required fields"), "error indicates missing fields");
    }

    /**
     * Test case for verifying that creating a coupon fails with a duplicate code.
     *
     * @throws Exception if an error occurs while reading the admin token or coupon code.
     */
    @Test(description = "TC-COUP-006: Verify create coupon fails with duplicate code", dependsOnMethods = "testCreateCouponFailsWithMissingData")
    public void testCreateCouponFailsWithDuplicateCode() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Get existing code from jsonutility");
        CouponCode = JsonUtility.getJSONString("CouponCode", IDS_FILE_PATH);
        Assert.assertNotNull(CouponCode, "Coupon code not found");


        Allure.step("Send POST with existing code");
        Map<String, Object> body = new HashMap<>();
        body.put("code", CouponCode);
        body.put("description", COUPON_DESCRIPTION);
        body.put("discount_type", COUPON_DISCOUNT_TYPE);
        body.put("discount_value", COUPON_DISCOUNT_VALUE);
        body.put("min_order_amount", COUPON_MIN_ORDER_AMOUNT);
        body.put("max_discount", COUPON_MAX_DISCOUNT);
        body.put("usage_limit", COUPON_USAGE_LIMIT);


        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates duplicate code");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Coupon code already exists"), "error indicates duplicate code");
    }

    /**
     * Test case for verifying that a valid coupon code can be validated.
     *
     * @throws Exception if an error occurs while reading the user token or coupon code.
     */
    @Test(description = "TC-COUP-007: Verify validate valid coupon code", dependsOnMethods = "testCreateCouponFailsWithDuplicateCode")
    public void testValidateValidCouponCode() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get valid coupon using jsonutility from ids.json");
        CouponCode = JsonUtility.getJSONString("CouponCode", IDS_FILE_PATH);
        Assert.assertNotNull(CouponCode, "Coupon code not found");

        Allure.step("Send POST with valid code");
        Map<String, Object> body = new HashMap<>();
        body.put("code", CouponCode);
        body.put("order_amount", COUPON_VALIDATE_ORDER_AMOUNT);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons/validate", userToken, body);

        Allure.step("Verify validated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data has coupon discount and conditions");
        Assert.assertTrue(response.jsonPath().get("data.code") instanceof String, "data has code");
        Assert.assertTrue(response.jsonPath().get("data.discount_type") instanceof String, "data has discount_type");
        Assert.assertTrue(response.jsonPath().get("data.discount_amount") instanceof Number, "data has discount_amount");
        Assert.assertTrue(response.jsonPath().get("data.final_amount") instanceof Number, "data has final_amount");
    }

    /**
     * Test case for verifying that an invalid coupon code cannot be validated.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-COUP-008: Verify validate invalid coupon code", dependsOnMethods = "testValidateValidCouponCode")
    public void testValidateInvalidCouponCode() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST with invalid code");
        Map<String, Object> body = new HashMap<>();
        body.put("code", COUPON_INVALID_CODE);
        body.put("order_amount", COUPON_VALIDATE_ORDER_AMOUNT);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons/validate", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Invalid coupon code'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.contains("Invalid coupon code") || error.contains("Coupon not found")), "error is 'Invalid coupon code'");
    }

    /**
     * Test case for verifying that an expired coupon code cannot be validated.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-COUP-009: Verify validate expired coupon", dependsOnMethods = "testValidateInvalidCouponCode")
    public void testValidateExpiredCoupon() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST with expired coupon code");
        Map<String, Object> body = new HashMap<>();
        body.put("code", COUPON_EXPIRED_CODE);
        body.put("order_amount", COUPON_VALIDATE_ORDER_AMOUNT);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons/validate", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates expired");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("expired"), "error indicates expired");
    }
}
