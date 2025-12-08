package com.gecom.CouponsTest;

import static com.gecom.utils.Base.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "CouponsTest")
@Severity(SeverityLevel.CRITICAL)
public class ValidateCoupons {

    @Test(description = "TC-COUP-007: Verify validate valid coupon code", groups = { "Valid-Coupons-Test", "valid" })
    public void testValidateValidCouponCode() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        CouponCode = (String) JsonUtility.getValue("CouponCode", IDS_FILE_PATH);
        Assert.assertNotNull(CouponCode, "Coupon code not found");
        Map<String, Object> body = new HashMap<>();
        body.put("code", CouponCode);
        body.put("order_amount", COUPON_VALIDATE_ORDER_AMOUNT);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons/validate", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertTrue(response.jsonPath().get("data.code") instanceof String, "data has code");
        Assert.assertTrue(response.jsonPath().get("data.discount_type") instanceof String, "data has discount_type");
        Assert.assertTrue(response.jsonPath().get("data.discount_amount") instanceof Number,
                "data has discount_amount");
        Assert.assertTrue(response.jsonPath().get("data.final_amount") instanceof Number, "data has final_amount");
    }

    @Test(description = "TC-COUP-008: Verify validate invalid coupon code", groups = { "Invalid-Coupons-Test",
            "invalid" })
    public void testValidateInvalidCouponCode() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Map<String, Object> body = new HashMap<>();
        body.put("code", COUPON_INVALID_CODE);
        body.put("order_amount", COUPON_VALIDATE_ORDER_AMOUNT);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons/validate", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(
                error != null && (error.contains("Invalid coupon code") || error.contains("Coupon not found")),
                "error is 'Invalid coupon code'");
    }

    @Test(description = "TC-COUP-009: Verify validate expired coupon", groups = { "Invalid-Coupons-Test", "invalid" })
    public void testValidateExpiredCoupon() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Map<String, Object> body = new HashMap<>();
        body.put("code", COUPON_EXPIRED_CODE);
        body.put("order_amount", COUPON_VALIDATE_ORDER_AMOUNT);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/coupons/validate", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("expired"), "error indicates expired");
    }
}
