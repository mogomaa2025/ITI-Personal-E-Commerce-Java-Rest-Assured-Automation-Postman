package com.gecom.CouponsTest;

import static com.gecom.utils.Const.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import com.github.javafaker.Faker;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "CouponsTest")
@Severity(SeverityLevel.CRITICAL)
public class CreateCoupons {

        private final Faker faker = new Faker();

        @Test(description = "TC-COUP-003: Verify admin can create coupon", groups = { "Valid-Coupons-Test", "valid" })
        public void testAdminCanCreateCoupon() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                CouponCode = faker.regexify("[a-z0-9]{8}") + "10";
                Map<String, Object> body = new HashMap<>();
                body.put("code", CouponCode);
                body.put("description", COUPON_DESCRIPTION);
                body.put("discount_type", COUPON_DISCOUNT_TYPE);
                body.put("discount_value", COUPON_DISCOUNT_VALUE);
                body.put("min_order_amount", COUPON_MIN_ORDER_AMOUNT);
                body.put("max_discount", COUPON_MAX_DISCOUNT);
                body.put("usage_limit", COUPON_USAGE_LIMIT);

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Coupon created successfully",
                                "message is 'Coupon created successfully'");
                Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
                Assert.assertTrue(response.jsonPath().get("data.code") instanceof String, "data has code");
                Assert.assertEquals(response.jsonPath().getString("data.code"), CouponCode,
                                "data has coupon details matching input");
                Assert.assertTrue(response.jsonPath().get("data.description") instanceof String,
                                "data has description");
                Assert.assertTrue(response.jsonPath().get("data.discount_type") instanceof String,
                                "data has discount_type");
                Assert.assertTrue(response.jsonPath().get("data.discount_value") instanceof Number,
                                "data has discount_value");
                Assert.assertTrue(response.jsonPath().get("data.min_order_amount") instanceof Number,
                                "data has min_order_amount");
                Assert.assertTrue(response.jsonPath().get("data.max_discount") instanceof Number,
                                "data has max_discount");
                Assert.assertTrue(response.jsonPath().get("data.usage_limit") instanceof Integer,
                                "data has usage_limit");
                JsonUtility.saveValue("CouponCode", CouponCode, IDS_FILE_PATH);
        }

        @Test(description = "TC-COUP-004: Verify create coupon fails without admin", groups = { "Invalid-Coupons-Test",
                        "invalid" })
        public void testCreateCouponFailsWithoutAdmin() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");
                String randomCode = faker.regexify("[a-z0-9]{8}") + "10";
                Map<String, Object> body = new HashMap<>();
                body.put("code", randomCode);
                body.put("description", COUPON_DESCRIPTION);
                body.put("discount_type", COUPON_DISCOUNT_TYPE);
                body.put("discount_value", COUPON_DISCOUNT_VALUE);
                body.put("min_order_amount", COUPON_MIN_ORDER_AMOUNT);
                body.put("max_discount", COUPON_MAX_DISCOUNT);
                body.put("usage_limit", COUPON_USAGE_LIMIT);

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", userToken, body);
                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        }

        @Test(description = "TC-COUP-005: Verify create coupon fails with missing coupon data", groups = {
                        "Invalid-Coupons-Test", "invalid" })
        public void testCreateCouponFailsWithMissingData() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                String randomCode = faker.regexify("[a-z0-9]{8}") + "10";
                Map<String, Object> body = new HashMap<>();
                body.put("code", randomCode);
                body.put("discount", 15);
                body.put("type", "percentage");
                body.put("expiry_date", "2026-12-31");

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Missing required fields"),
                                "error indicates missing fields");
        }

        @Test(description = "TC-COUP-006: Verify create coupon fails with duplicate code", groups = {
                        "Invalid-Coupons-Test", "invalid" })
        public void testCreateCouponFailsWithDuplicateCode() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                CouponCode = (String) JsonUtility.getValue("CouponCode", IDS_FILE_PATH);
                Assert.assertNotNull(CouponCode, "Coupon code not found");
                Map<String, Object> body = new HashMap<>();
                body.put("code", CouponCode);
                body.put("description", COUPON_DESCRIPTION);
                body.put("discount_type", COUPON_DISCOUNT_TYPE);
                body.put("discount_value", COUPON_DISCOUNT_VALUE);
                body.put("min_order_amount", COUPON_MIN_ORDER_AMOUNT);
                body.put("max_discount", COUPON_MAX_DISCOUNT);
                body.put("usage_limit", COUPON_USAGE_LIMIT);

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Coupon code already exists"),
                                "error indicates duplicate code");
        }

}
