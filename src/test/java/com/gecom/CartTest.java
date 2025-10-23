package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.gecom.utils.Const.*;

@Listeners({AllureTestNg.class})
public class CartTest {




    @Test(groups = "CartTest")
    public void testClearCart() throws Exception {
        Allure.step("Starting testClearCart...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
        Allure.step("Clear cart status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));
        Assert.assertEquals(response.jsonPath().getString("message"), "Cart cleared successfully");

        Allure.step("testClearCart finished successfully.");
    }

    @Test(groups = "CartTest" ,  dependsOnMethods = "testClearCart")
    public void testAddToCart() throws Exception {
        Allure.step("Starting testAddToCart...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 4);
        body.put("quantity", 2);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);
        Allure.step("Add to cart status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testAddToCart finished successfully.");
    }

    @Test(groups = "CartTest",  dependsOnMethods = "testAddToCart")
    public void testGetCart() throws Exception {
        Allure.step("Starting testGetCart...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/cart", userToken);
        Allure.step("Get cart status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        System.out.println("Cart Contents: " + response.asString());

        cartItemId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("cart_item_id", String.valueOf(cartItemId), IDS_FILE_PATH);
        Allure.step("cart_item_id saved: " + cartItemId);

        Allure.step("testGetCart finished successfully.");
    }

    @Test(groups = "CartTest" ,  dependsOnMethods = "testGetCart")
    public void testUpdateCartItem() throws Exception {
        Allure.step("Starting testUpdateCartItem...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        String cartItemId = JsonUtility.getToken("cart_item_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Assert.assertNotNull(cartItemId, "Cart item ID not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", 3);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/cart/items/" + cartItemId, userToken, body);
        Allure.step("Update cart item status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testUpdateCartItem finished successfully.");
    }

    @Test(groups = "CartTest" ,  dependsOnMethods = "testUpdateCartItem")
    public void testRemoveCartItem() throws Exception {
        Allure.step("Starting testRemoveCartItem...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        String cartItemId = JsonUtility.getToken("cart_item_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Assert.assertNotNull(cartItemId, "Cart item ID not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart/items/" + cartItemId, userToken);
        Allure.step("Remove cart item status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));
        Assert.assertEquals(response.jsonPath().getString("message"), "Item removed from cart successfully");

        Allure.step("testRemoveCartItem finished successfully.");
    }



}
