package com.gecom.OrdersTest;

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
@Test(groups = "OrdersTest")
@Severity(SeverityLevel.CRITICAL)
public class CreateOrders {

        @Test(description = "TC-ORDER-001: Verify user can create order with items in cart", groups = {
                        "Valid-Orders-Test", "valid" })
        public void testUserCanCreateOrder() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

                Map<String, Object> cartBody = new HashMap<>();
                cartBody.put("product_id", CART_PRODUCT_ID);
                cartBody.put("quantity", CART_QUANTITY);
                ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

                Map<String, Object> body = new HashMap<>();
                body.put("shipping_address", ORDER_SHIPPING_ADDRESS);
                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, body);
                Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Order created successfully",
                                "message is 'Order created successfully'");
                Assert.assertEquals(response.jsonPath().getString("data.shipping_address"), ORDER_SHIPPING_ADDRESS,
                                "data.shipping_address is " + ORDER_SHIPPING_ADDRESS);
                Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
                Assert.assertTrue(response.jsonPath().get("data.status") instanceof String, "data has status");
                Assert.assertTrue(response.jsonPath().get("data.total_amount") instanceof Number,
                                "data has total_amount");
                Assert.assertTrue(response.jsonPath().get("data.items") instanceof List, "data has items");
                orderID = response.jsonPath().getInt("data.id");
                JsonUtility.saveValue("order_id", orderID, IDS_FILE_PATH);
        }

        @Test(description = "TC-ORDER-002: Verify create order fails with empty cart", groups = {
                        "Invalid-Orders-Test", "invalid" })
        public void testCreateOrderFailsWithEmptyCart() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
                Map<String, Object> body = new HashMap<>();
                body.put("shipping_address", ORDER_SHIPPING_ADDRESS);
                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, body);
                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Cart is empty"), "error is 'Cart is empty'");
        }

        @Test(description = "TC-ORDER-003: Verify create order fails with empty shipping address", groups = {
                        "Invalid-Orders-Test", "invalid" })
        public void testCreateOrderFailsWithEmptyShippingAddress() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                Map<String, Object> cartBody = new HashMap<>();
                cartBody.put("product_id", CART_PRODUCT_ID);
                cartBody.put("quantity", CART_QUANTITY);
                ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

                Map<String, Object> body = new HashMap<>();
                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, body);
                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Shipping address"),
                                "error indicates shipping address required");
        }

}
