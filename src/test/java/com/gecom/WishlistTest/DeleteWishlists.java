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
public class DeleteWishlists {

        @Test(description = "TC-WISH-006: Verify user can remove product from wishlist", groups = {
                        "Valid-Wishlist-Test", "valid" })
        public void testUserCanRemoveProductFromWishlist() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");

                userId = (Integer) JsonUtility.getValue("user_id", IDS_FILE_PATH);
                productId = (Integer) JsonUtility.getValue("product_id", IDS_FILE_PATH);

                Assert.assertNotNull(userId, "User ID is valid Integer");
                Assert.assertNotNull(productId, "Product ID is valid Integer");

                // [1]
                Map<String, Object> bodyAddWish = new HashMap<>();
                bodyAddWish.put("user_id", userId);
                bodyAddWish.put("product_id", productId);

                Response responseAddWish = ApiUtils.postRequestWithAuth(BASE_URL + "/wishlist", userToken, bodyAddWish);

                Assert.assertEquals(responseAddWish.getStatusCode(), 201, "Status code is 201");

                // [2]
                Map<String, String> queryParamsGetWishList = new HashMap<>();
                queryParamsGetWishList.put("user_id", userId.toString());
                Response responseGetWishList = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist",
                                queryParamsGetWishList,
                                userToken);

                Assert.assertEquals(responseGetWishList.getStatusCode(), 200, "Status code is 200");

                wishlistItemId = (Integer) JsonUtility.getValue("wishlist_item_id", IDS_FILE_PATH);
                Assert.assertNotNull(wishlistItemId, "Wishlist item ID is valid Integer");

                // [3]
                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/wishlist/" + wishlistItemId, userToken);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

                Assert.assertEquals(response.jsonPath().getString("message"),
                                "Product removed from wishlist successfully",
                                "message is 'Product removed from wishlist successfully'");

        }

        @Test(description = "TC-WISH-007: Verify remove from wishlist fails for non-existent item", groups = {
                        "Invalid-Wishlist-Test", "invalid" })
        public void testRemoveFromWishlistFailsForNonExistentItem() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");

                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/wishlist/" + INVALID_WISHLIST_ITEM_ID,
                                userToken);

                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

                Assert.assertEquals(response.jsonPath().getString("error"), "Wishlist item not found",
                                "error is 'Wishlist item not found'");
        }
}
