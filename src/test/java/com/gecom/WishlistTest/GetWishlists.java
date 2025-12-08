package com.gecom.WishlistTest;

import static com.gecom.utils.Const.*;

import java.util.HashMap;
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
@Test(groups = "WishlistTest")
@Severity(SeverityLevel.NORMAL)
public class GetWishlists {

    @Test(description = "TC-WISH-004: Verify user can view their wishlist", groups = { "Valid-Wishlist-Test", "valid" })
    public void testUserCanViewTheirWishlist() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        userId = (Integer) JsonUtility.getValue("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("user_id", userId.toString());
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist", queryParams, userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        List<Map<String, Object>> wishlistItems = response.jsonPath().getList("data");
        Assert.assertNotNull(wishlistItems, "data is array");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, wishlistItems.size(), "count matches array length");

        if (!wishlistItems.isEmpty()) {
            Map<String, Object> firstItem = wishlistItems.get(0);
            Assert.assertTrue(firstItem.get("id") instanceof Integer, "Each item has id");
            Assert.assertTrue(firstItem.get("product_id") instanceof Integer, "Each item has product_id");
            Assert.assertTrue(firstItem.get("user_id") instanceof Integer, "Each item has user_id");
            Assert.assertNotNull(firstItem.get("created_at"), "Each item has created_at");

            wishlistItemId = response.jsonPath().getInt("data[0].id");
            JsonUtility.saveValue("wishlist_item_id", wishlistItemId, IDS_FILE_PATH);
        }
    }

    @Test(description = "TC-WISH-005: Verify empty wishlist returns empty array", groups = { "Invalid-Wishlist-Test",
            "invalid" })
    public void testEmptyWishlistReturnsEmptyArray() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        userId = (Integer) JsonUtility.getValue("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID is valid Integer");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("user_id", userId.toString());
        Response getResponse = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist", queryParams, userToken);
        List<Map<String, Object>> items = getResponse.jsonPath().getList("data");

        if (items != null && !items.isEmpty()) {
            for (Map<String, Object> item : items) {
                Integer itemId = (Integer) item.get("id");
                if (itemId != null) {
                    ApiUtils.deleteRequestWithAuth(BASE_URL + "/wishlist/" + itemId, userToken);
                }
            }
        }

        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/wishlist", queryParams, userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        List<Map<String, Object>> wishlistItems = response.jsonPath().getList("data");
        Assert.assertTrue(wishlistItems.isEmpty(), "data is empty array");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, 0, "count is 0");
    }

}
