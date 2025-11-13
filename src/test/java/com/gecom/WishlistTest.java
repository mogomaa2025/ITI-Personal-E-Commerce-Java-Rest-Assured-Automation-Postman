package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.gecom.utils.Const.BASE_URL;
import static com.gecom.utils.Const.IDS_FILE_PATH;
import static com.gecom.utils.Const.TOKEN_FILE_PATH;
import static com.gecom.utils.Const.wishlistItemId;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "WishlistTest")
public class WishlistTest {

    @Test
    public void testAddToWishlist() throws Exception {
        Allure.step("Starting testAddToWishlist...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        String userIdStr = JsonUtility.getToken("user_id", IDS_FILE_PATH);
        String productIdStr = JsonUtility.getToken("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(userIdStr, "User ID not found");
        Assert.assertNotNull(productIdStr, "Product ID not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", Integer.parseInt(userIdStr));
        body.put("product_id", Integer.parseInt(productIdStr));

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, body);
        Allure.step("Add to wishlist status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product added to wishlist successfully");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testAddToWishlist finished successfully.");
    }


    @Test(dependsOnMethods = "testAddToWishlist")
    public void testGetWishlist() throws Exception {
        Allure.step("Starting testGetWishlist...");

        String userIdStr = JsonUtility.getToken("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userIdStr, "User ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/wishlist?user_id=" + userIdStr);
        Allure.step("Get wishlist status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        //store wishlist item id to ids.json
         wishlistItemId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("wishlist_item_id", String.valueOf(wishlistItemId), IDS_FILE_PATH);
        Allure.step("wishlist_item_id saved: " + wishlistItemId);

        Allure.step("testGetWishlist finished successfully.");
    }

    @Test(dependsOnMethods = "testGetWishlist")
    public void testRemoveWishlistItem() throws Exception {
        Allure.step("Starting testRemoveWishlistItem...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String wishlistItemId = JsonUtility.getToken("wishlist_item_id", IDS_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");
        Assert.assertNotNull(wishlistItemId, "Wishlist item ID not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/wishlist/" + wishlistItemId, adminToken);
        Allure.step("Remove wishlist item status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product removed from wishlist successfully");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testRemoveWishlistItem finished successfully.");
    }
}
