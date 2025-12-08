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
public class ClearCart {

    @Test(description = "TC-CART-007: Verify user can clear entire cart", groups = { "Valid-Cart-Test", "valid" })
    public void testUserCanClearEntireCart() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Map<String, Object> body1 = new HashMap<>();
        body1.put("product_id", CART_PRODUCT_ID);
        body1.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body1);

        Map<String, Object> body2 = new HashMap<>();
        body2.put("product_id", 1);
        body2.put("quantity", 1);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body2);

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertEquals(response.jsonPath().getString("message"), "Cart cleared successfully",
                "message is 'Cart cleared successfully'");

        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        List<Map<String, Object>> cartItems = getResponse.jsonPath().getList("data");
        Assert.assertTrue(cartItems.isEmpty(), "Cart is empty");
    }

    @Test(description = "TC-CART-008: Verify clear cart on already empty cart", groups = { "Valid-Cart-Test", "valid" })
    public void testClearCartOnAlreadyEmptyCart() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
    }

    @Test(description = "TC-CART-009: Verify empty cart returns empty array", groups = { "Valid-Cart-Test", "valid" })
    public void testEmptyCartReturnsEmptyArray() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        List<Map<String, Object>> cartItems = response.jsonPath().getList("data");
        Assert.assertTrue(cartItems.isEmpty(), "data is empty array");

        Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");
        Assert.assertEquals(response.jsonPath().getDouble("total"), 0.0, 0.01, "total is 0");
    }

    @Test(description = "TC-CART-010: Verify user can remove item from cart", groups = { "Valid-Cart-Test", "valid" })
    public void testUserCanRemoveItemFromCart() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        List<Map<String, Object>> cartItems = getResponse.jsonPath().getList("data");
        Assert.assertFalse(cartItems.isEmpty(), "Cart has items");
        cartItemId = (Integer) cartItems.get(0).get("id");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart/items/" + cartItemId, userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertEquals(response.jsonPath().getString("message"), "Item removed from cart successfully",
                "message is 'Item removed from cart successfully'");

        Assert.assertNotNull(response.jsonPath().get("data"), "data has removed item details");
        Response verifyResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        List<Map<String, Object>> remainingItems = verifyResponse.jsonPath().getList("data");
        boolean itemFound = false;
        for (Map<String, Object> item : remainingItems) {
            if (item.get("id").equals(cartItemId)) {
                itemFound = true;
                break;
            }
        }
        Assert.assertFalse(itemFound, "Item removed from cart");
    }

    @Test(description = "TC-CART-011: Verify remove cart item fails for non-existent item", groups = {
            "Invalid-Cart-Test", "invalid" })
    public void testRemoveCartItemFailsForNonExistentItem() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart/items/" + INVALID_CART_ITEM_ID, userToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Cart item not found"), "error is 'Cart item not found'");
    }

}
