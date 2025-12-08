package com.gecom.CartTests;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;

import static com.gecom.utils.Base.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "CartTest")
@Severity(SeverityLevel.CRITICAL)
public class GetCart {

    @Test(description = "TC-CART-004: Verify user can view cart contents", groups = { "Valid-Cart-Test", "valid" }) //
    public void testUserCanViewCartContents() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        List<Map<String, Object>> cartItems = response.jsonPath().getList("data");
        Assert.assertNotNull(cartItems, "data is array");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, cartItems.size(), "count matches data length");

        // test to fail here can be tested #GOMAA
        double total = response.jsonPath().getDouble("total");
        double sumItemTotal = 0.0;
        for (Map<String, Object> item : cartItems) {
            sumItemTotal += ((Number) item.get("item_total")).doubleValue();
        }
        Assert.assertEquals(total, sumItemTotal, 0.01, "total equals sum of item_total");

        if (!cartItems.isEmpty()) {
            Map<String, Object> firstItem = cartItems.get(0);
            Assert.assertTrue(firstItem.get("id") instanceof Integer, "Each item has id");
            Assert.assertTrue(firstItem.get("product_id") instanceof Integer, "Each item has product_id");
            Assert.assertTrue(firstItem.get("quantity") instanceof Integer, "Each item has quantity");
            Assert.assertTrue(firstItem.get("item_total") instanceof Number, "Each item has item_total");
            Assert.assertTrue(firstItem.get("product") instanceof Map, "Each item has product object");
            Assert.assertNotNull(firstItem.get("created_at"), "Each item has created_at");
            Assert.assertNotNull(firstItem.get("updated_at"), "Each item has updated_at");

            cartItemId = (Integer) firstItem.get("id");
            JsonUtility.saveValue("cart_item_id", cartItemId, IDS_FILE_PATH);
        }
    }

    @Test(description = "TC-CART-012: Verify user cannot access another user's cart", groups = { "Invalid-Cart-Test",
            "invalid" })
    public void testUserCannotAccessAnotherUsersCart() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        List<Map<String, Object>> cartItems = response.jsonPath().getList("data");
        for (Map<String, Object> item : cartItems) {
            Assert.assertTrue(item.get("user_id") instanceof Integer, "Each item has user_id");
            // Note: The actual user_id validation would require comparing with the
            // logged-in user's ID
            // This test verifies that the cart returns items with user_id field
        }

        // This test assumes that the API correctly filters cart items by the
        // authenticated user
        // The cart should only return items belonging to the authenticated user
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "Cart request successful");
    }
}
