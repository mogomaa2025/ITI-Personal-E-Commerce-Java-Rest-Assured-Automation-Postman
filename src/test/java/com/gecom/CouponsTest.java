package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.gecom.utils.Const.*;

@Listeners({AllureTestNg.class})
public class CouponsTest {

    private final Faker faker = new Faker();

    @Test(groups = "CouponsTest")
    public void testCreateCoupon() throws Exception {
        Allure.step("Starting testCreateCoupon...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        CouponCode = faker.regexify("[a-z0-9]{8}") + "10";


        //body
        Map<String, Object> body = new HashMap<>();
        body.put("code", CouponCode);
        body.put("description", "10% off first order");
        body.put("discount_type", "percentage");
        body.put("discount_value", 10);
        body.put("min_order_amount", 50);
        body.put("max_discount", 30);
        body.put("usage_limit", 100);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons", adminToken, body);
        Allure.step("Create coupon status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");
        Assert.assertEquals(response.jsonPath().getString("message"), "Coupon created successfully");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));
        Assert.assertEquals(response.jsonPath().getString("data.code"), CouponCode);

        JsonUtility.saveToken("CouponCode", CouponCode, IDS_FILE_PATH);
        Allure.step("CouponCode saved: " + CouponCode);

        Allure.step("testCreateCoupon finished successfully.");
    }

    @Test(groups = "CouponsTest",  dependsOnMethods = "testCreateCoupon")
    public void testValidateCoupon() throws Exception {
        Allure.step("Starting testValidateCoupon...");
        CouponCode = JsonUtility.getToken("CouponCode", IDS_FILE_PATH);
        Assert.assertNotNull(CouponCode, "Coupon code not found");

        Map<String, Object> body = new HashMap<>();
        body.put("code", CouponCode);
        body.put("order_amount", 120.0);

        Response response = ApiUtils.postRequest(BASE_URL + "/coupons/validate", body);
        Allure.step("Validate coupon status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));
        Assert.assertEquals(response.jsonPath().getString("data.code"), CouponCode);

        Allure.step("testValidateCoupon finished successfully.");
    }

    @Test(groups = "CouponsTest",  dependsOnMethods = "testValidateCoupon")
    public void testListCoupons() throws Exception {
        Allure.step("Starting testListCoupons...");
        CouponCode = JsonUtility.getToken("CouponCode", IDS_FILE_PATH);
        Assert.assertNotNull(CouponCode, "Coupon code not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/coupons");
        Allure.step("List coupons status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        // check last coupon code is the one we created before
        String lastCode = response.jsonPath().getString("data[-1].code");
        Assert.assertEquals(lastCode, CouponCode, "Last coupon code should match the created code");

        Allure.step("testListCoupons finished successfully.");
    }
}
