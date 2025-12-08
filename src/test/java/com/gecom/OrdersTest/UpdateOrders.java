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
public class UpdateOrders {

        @Test(description = "TC-ORDER-007: Verify user can update order shipping address", groups = {
                        "Valid-Orders-Test", "valid" })
        public void testUserCanUpdateOrderShippingAddress() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                orderID = (Integer) JsonUtility.getValue("order_id", IDS_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Assert.assertNotNull(orderID, "Order ID is valid Integer");
                Map<String, Object> body = new HashMap<>();
                body.put("shipping_address", ORDER_UPDATED_SHIPPING_ADDRESS);
                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken, body);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Order updated successfully",
                                "message is 'Order updated successfully'");
                Assert.assertEquals(response.jsonPath().getString("data.shipping_address"),
                                ORDER_UPDATED_SHIPPING_ADDRESS,
                                "data.shipping_address is updated");
        }

        @Test(description = "TC-ORDER-008: Verify update order fails for non-existent order", groups = {
                        "Invalid-Orders-Test", "invalid" })
        public void testUpdateOrderFailsForNonExistentOrder() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Map<String, Object> body = new HashMap<>();
                body.put("shipping_address", ORDER_UPDATED_SHIPPING_ADDRESS);
                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + INVALID_ORDER_ID, userToken,
                                body);
                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Order not found"), "error is 'Order not found'");
        }

        @Test(description = "TC-ORDER-009: Verify admin can update order status", groups = {
                        "Valid-Orders-Test", "valid" })
        public void testAdminCanUpdateOrderStatus() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                orderID = (Integer) JsonUtility.getValue("order_id", IDS_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token is valid String");
                Assert.assertNotNull(orderID, "Order ID is valid Integer");
                Map<String, Object> body = new HashMap<>();
                body.put("status", ORDER_STATUS_PROCESSING);
                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID + "/status", adminToken,
                                body);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                String message = response.jsonPath().getString("message");
                Assert.assertTrue(
                                message != null
                                                && (message.contains("Order status updated")
                                                                || message.contains("Order status unchanged")),
                                "message indicates status updated");
                Assert.assertEquals(response.jsonPath().getString("data.status"), ORDER_STATUS_PROCESSING,
                                "data.status is " + ORDER_STATUS_PROCESSING);
        }

        @Test(description = "TC-ORDER-010: Verify update order status fails with invalid status", groups = {
                        "Invalid-Orders-Test", "invalid" })
        public void testUpdateOrderStatusFailsWithInvalidStatus() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                orderID = (Integer) JsonUtility.getValue("order_id", IDS_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token is valid String");
                Assert.assertNotNull(orderID, "Order ID is valid Integer");
                Map<String, Object> body = new HashMap<>();
                body.put("status", INVALID_ORDER_STATUS);
                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID + "/status", adminToken,
                                body);
                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Invalid status"), "error is 'Invalid status'");
        }

        @Test(description = "TC-ORDER-011: Verify update order status fails for non-admin user", groups = {
                        "Invalid-Orders-Test", "invalid" })
        public void testUpdateOrderStatusFailsForNonAdminUser() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                orderID = (Integer) JsonUtility.getValue("order_id", IDS_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Assert.assertNotNull(orderID, "Order ID is valid Integer");
                Map<String, Object> body = new HashMap<>();
                body.put("status", ORDER_STATUS_SHIPPED);
                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID + "/status", userToken,
                                body);
                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                                "error is 'Admin privileges required'");
        }
}
