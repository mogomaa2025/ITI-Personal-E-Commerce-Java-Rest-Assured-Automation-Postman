package com.gecom.CouponsTest;

import static com.gecom.utils.Base.*;

import java.util.List;
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
public class ListCoupons {

    @Test(description = "TC-COUP-001: Verify get available coupons", groups = { "Valid-Coupons-Test", "valid" })
    public void testGetAvailableCoupons() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/coupons", adminToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");
        List<Map<String, Object>> coupons = response.jsonPath().getList("data");
        if (!coupons.isEmpty()) {
            Map<String, Object> firstCoupon = coupons.get(0);
            Assert.assertTrue(firstCoupon.get("code") instanceof String, "Each coupon has code");
            Assert.assertTrue(firstCoupon.get("discount_type") instanceof String, "Each coupon has discount_type");
            Assert.assertTrue(firstCoupon.get("discount_value") instanceof Number, "Each coupon has discount_value");
            Assert.assertNotNull(firstCoupon.get("expires_at"), "Each coupon has expires_at");
            Assert.assertTrue(firstCoupon.get("min_order_amount") instanceof Number,
                    "Each coupon has min_order_amount");
        }
    }

    @Test(description = "TC-COUP-002: Verify non-admin can't get available coupons", groups = {
            "Invalid-Coupons-Test", "invalid" }, dependsOnMethods = "testGetAvailableCoupons")
    public void testNonAdminCantGetAvailableCoupons() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/coupons", userToken);
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                "admin privilege error message appears");
    }

}
