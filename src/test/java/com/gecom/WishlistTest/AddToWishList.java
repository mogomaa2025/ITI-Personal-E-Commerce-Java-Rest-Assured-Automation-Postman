package com.gecom.WishlistTest;

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
@Test(groups = "WishlistTest")
@Severity(SeverityLevel.NORMAL)
public class AddToWishList {

    @Test(description = "TC-WISH-001: Verify user can add product to wishlist", groups = { "Valid-Wishlist-Test",
            "valid" })
    public void testUserCanAddProductToWishlist() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        userId = (Integer) JsonUtility.getValue("user_id", IDS_FILE_PATH);
        productId = (Integer) JsonUtility.getValue("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");
        Assert.assertNotNull(productId, "Product ID is valid Integer");

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", productId);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, body);

        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertEquals(response.jsonPath().getString("message"), "Product added to wishlist successfully");
    }

    @Test(description = "TC-WISH-002: Verify add to wishlist fails for non-existent product", groups = {
            "Invalid-Wishlist-Test", "invalid" })
    public void testAddToWishlistFailsForNonExistentProduct() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        userId = (Integer) JsonUtility.getValue("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", INVALID_WISHLIST_PRODUCT_ID);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, body);

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Assert.assertEquals(response.jsonPath().getString("error"), "Product not found");
    }

    @Test(description = "TC-WISH-003: Verify user cannot add same product to wishlist twice", groups = {
            "Invalid-Wishlist-Test", "invalid" })
    public void testUserCannotAddSameProductToWishlistTwice() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        userId = (Integer) JsonUtility.getValue("user_id", IDS_FILE_PATH);
        productId = (Integer) JsonUtility.getValue("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");
        Assert.assertNotNull(productId, "Product ID is valid Integer");

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", productId);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, body);

        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Assert.assertEquals(response.jsonPath().getString("error"), "Product already in wishlist");
    }

}
