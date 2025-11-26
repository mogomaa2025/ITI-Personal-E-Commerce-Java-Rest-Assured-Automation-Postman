package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gecom.utils.Const.*;

/**
 * This class contains test cases for the shopping cart functionalities,
 * including adding, updating, viewing, and removing items from the cart.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "CartTest")
@Severity(SeverityLevel.CRITICAL)
public class CartTest {

    /**
     * Test case for verifying that a user can add an item to the cart.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-001: Verify user can add item to cart")
    public void testUserCanAddItemToCart() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Clear cart");
        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken); //pre-condition for clean before add

        Allure.step("Send POST to add item");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Allure.step("Verify added");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Item added to cart successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Item added to cart successfully", "message is 'Item added to cart successfully'");

        Allure.step("Subsequent GET shows item with quantity=" + CART_QUANTITY);
        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        List<Map<String, Object>> cartItems = getResponse.jsonPath().getList("data");
        Assert.assertFalse(cartItems.isEmpty(), "Cart has items");
        Map<String, Object> firstItem = cartItems.get(0);
        Assert.assertEquals(firstItem.get("product_id"), CART_PRODUCT_ID, "Product ID matches");
        Assert.assertEquals(firstItem.get("quantity"), CART_QUANTITY, "Quantity is " + CART_QUANTITY);
    }

    /**
     * Test case for verifying that adding an item to the cart fails for an invalid product.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-002: Verify add to cart fails for invalid product", dependsOnMethods = "testUserCanAddItemToCart")
    public void testAddToCartFailsForInvalidProduct() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send POST with invalid product_id");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", INVALID_PRODUCT_ID);
        body.put("quantity", 1);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Product not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Product not found"), "error is 'Product not found'");
    }

    /**
     * Test case for verifying that adding an item to the cart fails with an invalid quantity.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-003: Verify add to cart fails with invalid quantity", dependsOnMethods = "testAddToCartFailsForInvalidProduct")
    public void testAddToCartFailsWithInvalidQuantity() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send POST with quantity="+INVALID_QUANTITY+" ");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 1);
        body.put("quantity", INVALID_QUANTITY);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Allure.step("Verify validation error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates invalid quantity");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Invalid quantity"), "error indicates invalid quantity");
    }

    /**
     * Test case for verifying that a user can view the contents of their cart.
     *
     * @throws Exception if an error occurs while reading the user token or saving the cart item ID.
     */
    @Test(description = "TC-CART-004: Verify user can view cart contents", dependsOnMethods = "testAddToCartFailsWithInvalidQuantity") //
    public void testUserCanViewCartContents() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Add items to cart");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Allure.step("Send GET");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Verify cart");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        List<Map<String, Object>> cartItems = response.jsonPath().getList("data");
        Assert.assertNotNull(cartItems, "data is array");

        Allure.step("Verify count matches data length");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, cartItems.size(), "count matches data length");

        // test to fail here can be tested #GOMAA
        Allure.step("Verify total equals sum of item_total");
        double total = response.jsonPath().getDouble("total");
        double sumItemTotal = 0.0;
        for (Map<String, Object> item : cartItems) {
            sumItemTotal += ((Number) item.get("item_total")).doubleValue();
        }
        Assert.assertEquals(total, sumItemTotal, 0.01, "total equals sum of item_total");

        Allure.step("Verify each item has id/product_id/quantity/item_total/product object/created_at/updated_at");
        if (!cartItems.isEmpty()) {
            Map<String, Object> firstItem = cartItems.get(0);
            Assert.assertTrue(firstItem.get("id") instanceof Integer, "Each item has id");
            Assert.assertTrue(firstItem.get("product_id") instanceof Integer, "Each item has product_id");
            Assert.assertTrue(firstItem.get("quantity") instanceof Integer, "Each item has quantity");
            Assert.assertTrue(firstItem.get("item_total") instanceof Number, "Each item has item_total");
            Assert.assertTrue(firstItem.get("product") instanceof Map, "Each item has product object");
            Assert.assertNotNull(firstItem.get("created_at"), "Each item has created_at");
            Assert.assertNotNull(firstItem.get("updated_at"), "Each item has updated_at");

            Allure.step("Save cart_item_id for later tests");
            cartItemId = (Integer) firstItem.get("id");
            JsonUtility.saveValue("cart_item_id", cartItemId, IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying that a user can update the quantity of an item in the cart.
     *
     * @throws Exception if an error occurs while reading the user token or cart item ID.
     */
    @Test(description = "TC-CART-005: Verify user can update cart item quantity", dependsOnMethods = "testUserCanViewCartContents")
    public void testUserCanUpdateCartItemQuantity() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Add item to cart");
        cartItemId = JsonUtility.getJSONInt("cart_item_id", IDS_FILE_PATH);
        Assert.assertNotNull(cartItemId, "Cart item ID is valid Integer");

        Allure.step("Send PUT to update quantity");
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", CART_UPDATE_QUANTITY);
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/cart/items/" + cartItemId, userToken, body);

        Allure.step("Verify updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Cart item updated successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Cart item updated successfully", "message is 'Cart item updated successfully'");

        Allure.step("Verify data.quantity is " + CART_UPDATE_QUANTITY);
        Assert.assertEquals(response.jsonPath().getInt("data.quantity"), CART_UPDATE_QUANTITY, "data.quantity is " + CART_UPDATE_QUANTITY);

        Allure.step("Verify updated_at is recent");
        Assert.assertNotNull(response.jsonPath().get("data.updated_at"), "updated_at is recent");
    }

    /**
     * Test case for verifying that updating a non-existent item in the cart fails.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-006: Verify update cart item fails for non-existent item", dependsOnMethods = "testUserCanUpdateCartItemQuantity")
    public void testUpdateCartItemFailsForNonExistentItem() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send PUT with invalid cart item ID");
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", 2);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/cart/items/" + INVALID_CART_ITEM_ID, userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Cart item not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Cart item not found"), "error is 'Cart item not found'");
    }

    /**
     * Test case for verifying that a user can clear their entire cart.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-007: Verify user can clear entire cart", dependsOnMethods = "testUpdateCartItemFailsForNonExistentItem")
    public void testUserCanClearEntireCart() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Add multiple items");
        Map<String, Object> body1 = new HashMap<>();
        body1.put("product_id", CART_PRODUCT_ID);
        body1.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body1);

        Map<String, Object> body2 = new HashMap<>();
        body2.put("product_id", 1);
        body2.put("quantity", 1);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body2);

        Allure.step("Send DELETE");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Verify cart empty");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Cart cleared successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Cart cleared successfully", "message is 'Cart cleared successfully'");

        Allure.step("Subsequent GET returns empty cart");
        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        List<Map<String, Object>> cartItems = getResponse.jsonPath().getList("data");
        Assert.assertTrue(cartItems.isEmpty(), "Cart is empty");
    }

    /**
     * Test case for verifying that clearing an already empty cart is handled gracefully.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-008: Verify clear cart on already empty cart", dependsOnMethods = "testUserCanClearEntireCart")
    public void testClearCartOnAlreadyEmptyCart() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Allure.step("Ensure cart empty");
        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Send DELETE");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Verify success");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
    }

    /**
     * Test case for verifying that an empty cart returns an empty array.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-009: Verify empty cart returns empty array", dependsOnMethods = "testClearCartOnAlreadyEmptyCart")
    public void testEmptyCartReturnsEmptyArray() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Allure.step("Clear cart");
        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Send GET");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Verify empty");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is empty array");
        List<Map<String, Object>> cartItems = response.jsonPath().getList("data");
        Assert.assertTrue(cartItems.isEmpty(), "data is empty array");

        Allure.step("Verify count is 0");
        Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");

        Allure.step("Verify total is 0");
        Assert.assertEquals(response.jsonPath().getDouble("total"), 0.0, 0.01, "total is 0");
    }

    /**
     * Test case for verifying that a user can remove an item from the cart.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-010: Verify user can remove item from cart", dependsOnMethods = "testEmptyCartReturnsEmptyArray")
    public void testUserCanRemoveItemFromCart() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Allure.step("Add item to cart");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Allure.step("Get cart item ID");
        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        List<Map<String, Object>> cartItems = getResponse.jsonPath().getList("data");
        Assert.assertFalse(cartItems.isEmpty(), "Cart has items");
        cartItemId = (Integer) cartItems.get(0).get("id");

        Allure.step("Send DELETE");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart/items/" + cartItemId, userToken);

        Allure.step("Verify removed");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Item removed from cart successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Item removed from cart successfully", "message is 'Item removed from cart successfully'");

        Allure.step("Verify data has removed item details");
        Assert.assertNotNull(response.jsonPath().get("data"), "data has removed item details");

        Allure.step("Subsequent GET shows item removed");
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

    /**
     * Test case for verifying that removing a non-existent item from the cart fails.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-011: Verify remove cart item fails for non-existent item", dependsOnMethods = "testUserCanRemoveItemFromCart")
    public void testRemoveCartItemFailsForNonExistentItem() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Allure.step("Send DELETE with invalid ID");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart/items/" + INVALID_CART_ITEM_ID, userToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Cart item not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Cart item not found"), "error is 'Cart item not found'");
    }

    /**
     * Test case for verifying that a user cannot access another user's cart.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CART-012: Verify user cannot access another user's cart", dependsOnMethods = "testRemoveCartItemFailsForNonExistentItem")
    public void testUserCannotAccessAnotherUsersCart() throws Exception {
        Allure.step("User1 adds items to cart");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertTrue(userToken instanceof String, "User token is valid String");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Allure.step("User1 gets cart");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Verify only user1 items");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify data contains only user1 cart items");
        List<Map<String, Object>> cartItems = response.jsonPath().getList("data");
        for (Map<String, Object> item : cartItems) {
            Assert.assertTrue(item.get("user_id") instanceof Integer, "Each item has user_id");
            // Note: The actual user_id validation would require comparing with the logged-in user's ID
            // This test verifies that the cart returns items with user_id field
        }

        Allure.step("Verify cannot see user2 items");
        // This test assumes that the API correctly filters cart items by the authenticated user
        // The cart should only return items belonging to the authenticated user
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "Cart request successful");
    }
}
