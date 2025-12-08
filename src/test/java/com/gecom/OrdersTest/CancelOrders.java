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
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "OrdersTest")
@Severity(SeverityLevel.CRITICAL)
public class CancelOrders {

        @Test(description = "TC-ORDER-012: Verify admin can cancel order", groups = {
                        "Valid-Orders-Test", "valid" })
        public void testAdminCanCancelOrder() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
                Map<String, Object> cartBody = new HashMap<>();
                cartBody.put("product_id", CART_PRODUCT_ID);
                cartBody.put("quantity", CART_QUANTITY);
                ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

                Map<String, Object> orderBody = new HashMap<>();
                orderBody.put("shipping_address", ORDER_SHIPPING_ADDRESS);
                Response createResponse = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, orderBody);
                cancelOrderId = createResponse.jsonPath().getInt("data.id");
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token is valid String");
                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + cancelOrderId, adminToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                String message = response.jsonPath().getString("message");
                Assert.assertTrue(message != null && message.contains("Order cancelled"),
                                "message indicates order cancelled");
                Assert.assertEquals(response.jsonPath().getString("data.status"), ORDER_STATUS_CANCELLED,
                                "data.status is cancelled");
        }

        @Test(description = "TC-ORDER-013: Verify user can cancel pending order", groups = {
                        "Valid-Orders-Test", "valid" })
        public void testUserCanCancelPendingOrder() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
                Map<String, Object> cartBody = new HashMap<>();
                cartBody.put("product_id", CART_PRODUCT_ID);
                cartBody.put("quantity", CART_QUANTITY);
                ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

                Map<String, Object> orderBody = new HashMap<>();
                orderBody.put("shipping_address", ORDER_SHIPPING_ADDRESS);
                Response createResponse = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, orderBody);
                cancelOrderId = createResponse.jsonPath().getInt("data.id");
                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + cancelOrderId, userToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("data.status"), ORDER_STATUS_CANCELLED,
                                "data.status is cancelled");
        }

        @Test(description = "TC-ORDER-014: Verify cancel order fails for non-pending order", groups = {
                        "Invalid-Orders-Test", "invalid" })
        public void testCancelOrderFailsForNonPendingOrder() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Assert.assertNotNull(adminToken, "Admin token is valid String");

                ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
                Map<String, Object> cartBody = new HashMap<>();
                cartBody.put("product_id", CART_PRODUCT_ID);
                cartBody.put("quantity", CART_QUANTITY);
                ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

                Map<String, Object> orderBody = new HashMap<>();
                orderBody.put("shipping_address", ORDER_SHIPPING_ADDRESS);
                Response createResponse = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, orderBody);
                nonPendingOrderId = createResponse.jsonPath().getInt("data.id");

                Map<String, Object> statusBody = new HashMap<>();
                statusBody.put("status", ORDER_STATUS_PROCESSING);
                ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + nonPendingOrderId + "/status", adminToken,
                                statusBody);
                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + nonPendingOrderId,
                                userToken);
                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Cannot cancel"), "error indicates cannot cancel");
        }

        @Test(description = "TC-ORDER-015: Verify cancel order fails for non-existent order", groups = {
                        "Invalid-Orders-Test", "invalid" })
        public void testCancelOrderFailsForNonExistentOrder() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token is valid String");
                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + INVALID_ORDER_ID,
                                adminToken);
                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Order not found"), "error is 'Order not found'");
        }

}
