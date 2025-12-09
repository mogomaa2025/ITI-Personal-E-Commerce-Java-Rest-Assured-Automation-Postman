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
public class AddToCart {

    @Test(description = "TC-CART-001: Verify user can add item to cart", groups = { "Valid-Cart-Test", "valid" })
    public void testUserCanAddItemToCart() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken); // pre-condition for clean before add

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertEquals(response.jsonPath().getString("message"), "Item added to cart successfully",
                "message is 'Item added to cart successfully'");

        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        List<Map<String, Object>> cartItems = getResponse.jsonPath().getList("data");
        Assert.assertFalse(cartItems.isEmpty(), "Cart has items");
        Map<String, Object> firstItem = cartItems.get(0);
        Assert.assertEquals(firstItem.get("product_id"), CART_PRODUCT_ID, "Product ID matches");
        Assert.assertEquals(firstItem.get("quantity"), CART_QUANTITY, "Quantity is " + CART_QUANTITY);
    }

    @Test(description = "TC-CART-002: Verify add to cart fails for invalid product", groups = { "Invalid-Cart-Test",
            "invalid" }, dependsOnMethods = "testUserCanAddItemToCart")
    public void testAddToCartFailsForInvalidProduct() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", INVALID_PRODUCT_ID);
        body.put("quantity", 1);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Product not found"), "error is 'Product not found'");
    }

    @Test(description = "TC-CART-003: Verify add to cart fails with invalid quantity", groups = { "Invalid-Cart-Test",
            "invalid" }, dependsOnMethods = "testAddToCartFailsForInvalidProduct")
    public void testAddToCartFailsWithInvalidQuantity() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 1);
        body.put("quantity", INVALID_QUANTITY);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);

        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Invalid quantity"), "error indicates invalid quantity");
    }

}
