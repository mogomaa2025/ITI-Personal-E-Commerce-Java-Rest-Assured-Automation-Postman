package com.gecom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import static com.gecom.utils.Const.BASE_URL;
import static com.gecom.utils.Const.IDS_FILE_PATH;
import static com.gecom.utils.Const.INVALID_WISHLIST_ITEM_ID;
import static com.gecom.utils.Const.INVALID_WISHLIST_PRODUCT_ID;
import static com.gecom.utils.Const.TOKEN_FILE_PATH;
import static com.gecom.utils.Const.productId;
import static com.gecom.utils.Const.userId;
import static com.gecom.utils.Const.userToken;
import static com.gecom.utils.Const.wishlistItemId;
import com.gecom.utils.JsonUtility;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

/**
 * This class contains test cases for the wishlist functionalities,
 * including adding, viewing, and removing products from the wishlist.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "WishlistTest")
@Severity(SeverityLevel.CRITICAL)
public class WishlistTest {

    /**
     * Test case for verifying that a user can add a product to their wishlist.
     *
     * @throws Exception if an error occurs while reading user or product data.
     */
    @Test(description = "TC-WISH-001: Verify user can add product to wishlist")
    public void testUserCanAddProductToWishlist() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get user_id and product_id from ids.json");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        productId = JsonUtility.getJSONInt("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");
        Assert.assertNotNull(productId, "Product ID is valid Integer");

        Allure.step("Send POST to add to wishlist");
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", productId);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, body);

        Allure.step("Verify added");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Product added to wishlist successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product added to wishlist successfully", "message is 'Product added to wishlist successfully'");
    }

    /**
     * Test case for verifying that adding a non-existent product to the wishlist fails.
     *
     * @throws Exception if an error occurs while reading user data.
     */
    @Test(description = "TC-WISH-002: Verify add to wishlist fails for non-existent product", dependsOnMethods = "testUserCanAddProductToWishlist")
    public void testAddToWishlistFailsForNonExistentProduct() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get user_id from ids.json");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");

        Allure.step("Send POST with invalid product_id");
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", INVALID_WISHLIST_PRODUCT_ID);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Product not found'");
        Assert.assertEquals(response.jsonPath().getString("error"), "Product not found", "error is 'Product not found'");
    }

    /**
     * Test case for verifying that a user cannot add the same product to their wishlist twice.
     *
     * @throws Exception if an error occurs while reading user or product data.
     */
    @Test(description = "TC-WISH-003: Verify user cannot add same product to wishlist twice", dependsOnMethods = "testAddToWishlistFailsForNonExistentProduct")
    public void testUserCannotAddSameProductToWishlistTwice() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get user_id and product_id from ids.json");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        productId = JsonUtility.getJSONInt("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");
        Assert.assertNotNull(productId, "Product ID is valid Integer");

        Allure.step("Try adding same product again");
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", productId);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Product already in wishlist'");
        Assert.assertEquals(response.jsonPath().getString("error"), "Product already in wishlist", "error is 'Product already in wishlist'");
    }

    /**
     * Test case for verifying that a user can view their wishlist.
     *
     * @throws Exception if an error occurs while reading user data or saving the wishlist item ID.
     */
    @Test(description = "TC-WISH-004: Verify user can view their wishlist", dependsOnMethods = "testUserCannotAddSameProductToWishlistTwice")
    public void testUserCanViewTheirWishlist() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get user_id from ids.json");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");

        Allure.step("Send GET to view wishlist");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("user_id", userId.toString());
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist", queryParams, userToken);

        Allure.step("Verify wishlist returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        List<Map<String, Object>> wishlistItems = response.jsonPath().getList("data");
        Assert.assertNotNull(wishlistItems, "data is array");

        Allure.step("Verify count matches array length");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, wishlistItems.size(), "count matches array length");

        Allure.step("Verify each item contains product details");
        if (!wishlistItems.isEmpty()) {
            Map<String, Object> firstItem = wishlistItems.get(0);
            Assert.assertTrue(firstItem.get("id") instanceof Integer, "Each item has id");
            Assert.assertTrue(firstItem.get("product_id") instanceof Integer, "Each item has product_id");
            Assert.assertTrue(firstItem.get("user_id") instanceof Integer, "Each item has user_id");
            Assert.assertNotNull(firstItem.get("created_at"), "Each item has created_at");

            Allure.step("Save wishlist_item_id using jsonutility to ids.json");
            wishlistItemId = response.jsonPath().getInt("data[0].id");
            JsonUtility.saveValue("wishlist_item_id", wishlistItemId, IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying that an empty wishlist returns an empty array.
     *
     * @throws Exception if an error occurs while reading user data.
     */
    @Test(description = "TC-WISH-005: Verify empty wishlist returns empty array", dependsOnMethods = "testUserCanViewTheirWishlist")
    public void testEmptyWishlistReturnsEmptyArray() throws Exception {
        Allure.step("Login as new user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get user_id from ids.json");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");

        Allure.step("Ensure wishlist is empty by removing all items");
        // First, get current wishlist
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("user_id", userId.toString());
        Response getResponse = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist", queryParams, userToken);
        List<Map<String, Object>> items = getResponse.jsonPath().getList("data");
        
        // Remove all items if any exist
        if (items != null && !items.isEmpty()) {
            for (Map<String, Object> item : items) {
                Integer itemId = (Integer) item.get("id");
                if (itemId != null) {
                    ApiUtils.deleteRequestWithAuth(BASE_URL + "/wishlist/" + itemId, userToken);
                }
            }
        }

        Allure.step("Send GET to view empty wishlist");
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist", queryParams, userToken);

        Allure.step("Verify empty result");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is empty array");
        List<Map<String, Object>> wishlistItems = response.jsonPath().getList("data");
        Assert.assertTrue(wishlistItems.isEmpty(), "data is empty array");

        Allure.step("Verify count is 0");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, 0, "count is 0");
    }

    /**
     * Test case for verifying that a user can remove a product from their wishlist.
     *
     * @throws Exception if an error occurs while reading user or product data.
     */
    @Test(description = "TC-WISH-006: Verify user can remove product from wishlist", dependsOnMethods = "testEmptyWishlistReturnsEmptyArray")
    public void testUserCanRemoveProductFromWishlist() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Add item to wishlist first");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        productId = JsonUtility.getJSONInt("product_id", IDS_FILE_PATH);

        Assert.assertNotNull(userId, "User ID is valid Integer");
        Assert.assertNotNull(productId, "Product ID is valid Integer");

    //[1]
        Allure.step("Send POST to add to wishlist");
        Map<String, Object> bodyAddWish = new HashMap<>();
        bodyAddWish.put("user_id", userId);
        bodyAddWish.put("product_id", productId);

        Response responseAddWish = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, bodyAddWish);

        Allure.step("Verify added");
        Assert.assertEquals(responseAddWish.getStatusCode(), 201, "Status code is 201");



        //[2]
        Allure.step("Send GET to view wishlist");
        Map<String, String> queryParamsGetWishList = new HashMap<>();
        queryParamsGetWishList.put("user_id", userId.toString());
        Response responseGetWishList = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist", queryParamsGetWishList, userToken);

        Allure.step("Verify wishlist returned");
        Assert.assertEquals(responseGetWishList.getStatusCode(), 200, "Status code is 200");



        Allure.step("Use wishlist_item_id from same test requests");
        wishlistItemId = JsonUtility.getJSONInt("wishlist_item_id", IDS_FILE_PATH);
        Assert.assertNotNull(wishlistItemId, "Wishlist item ID is valid Integer");

        //[3]
        Allure.step("Send DELETE to remove item");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/wishlist/" + wishlistItemId, userToken);

        Allure.step("Verify removed");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Product removed from wishlist successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product removed from wishlist successfully", "message is 'Product removed from wishlist successfully'");

    }

    /**
     * Test case for verifying that removing a non-existent item from the wishlist fails.
     *
     * @throws Exception if an error occurs while reading user data.
     */
    @Test(description = "TC-WISH-007: Verify remove from wishlist fails for non-existent item", dependsOnMethods = "testUserCanRemoveProductFromWishlist")
    public void testRemoveFromWishlistFailsForNonExistentItem() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send DELETE with invalid wishlist item ID");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/wishlist/" + INVALID_WISHLIST_ITEM_ID, userToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Wishlist item not found'");
        Assert.assertEquals(response.jsonPath().getString("error"), "Wishlist item not found", "error is 'Wishlist item not found'");
    }
}
