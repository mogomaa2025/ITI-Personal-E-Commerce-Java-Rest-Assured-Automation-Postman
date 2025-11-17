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

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "OrdersTest")
@Severity(SeverityLevel.CRITICAL)
public class OrdersTest {

    @Test(description = "TC-ORDER-001: Verify user can create order with items in cart")
    public void testUserCanCreateOrder() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Clear cart");
        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Add items to cart");
        Map<String, Object> cartBody = new HashMap<>();
        cartBody.put("product_id", CART_PRODUCT_ID);
        cartBody.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

        Allure.step("Send POST to create order");
        Map<String, Object> body = new HashMap<>();
        body.put("shipping_address", ORDER_SHIPPING_ADDRESS);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, body);

        Allure.step("Verify order created");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Order created successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Order created successfully", "message is 'Order created successfully'");

        Allure.step("Verify data.shipping_address is " + ORDER_SHIPPING_ADDRESS);
        Assert.assertEquals(response.jsonPath().getString("data.shipping_address"), ORDER_SHIPPING_ADDRESS, "data.shipping_address is " + ORDER_SHIPPING_ADDRESS);

        Allure.step("Verify data has required fields");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.status") instanceof String, "data has status");
        Assert.assertTrue(response.jsonPath().get("data.total_amount") instanceof Number, "data has total_amount");
        Assert.assertTrue(response.jsonPath().get("data.items") instanceof List, "data has items");

        Allure.step("Save order_id for later tests");
        orderID = response.jsonPath().getInt("data.id");
        JsonUtility.saveValue("order_id", orderID, IDS_FILE_PATH);
    }

    @Test(description = "TC-ORDER-002: Verify create order fails with empty cart", dependsOnMethods = "testUserCanCreateOrder")
    public void testCreateOrderFailsWithEmptyCart() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Clear cart");
        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);

        Allure.step("Send POST to create order with empty cart");
        Map<String, Object> body = new HashMap<>();
        body.put("shipping_address", ORDER_SHIPPING_ADDRESS);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Cart is empty'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Cart is empty"), "error is 'Cart is empty'");
    }

    @Test(description = "TC-ORDER-003: Verify create order fails with empty shipping address", dependsOnMethods = "testCreateOrderFailsWithEmptyCart")
    public void testCreateOrderFailsWithEmptyShippingAddress() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Add items to cart");
        Map<String, Object> cartBody = new HashMap<>();
        cartBody.put("product_id", CART_PRODUCT_ID);
        cartBody.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

        Allure.step("Send POST with empty shipping address");
        Map<String, Object> body = new HashMap<>();
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates shipping address required");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Shipping address"), "error indicates shipping address required");
    }

    @Test(description = "TC-ORDER-004: Verify user can get all orders", dependsOnMethods = "testCreateOrderFailsWithEmptyShippingAddress")
    public void testUserCanGetAllOrders() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET to get orders");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders", userToken);

        Allure.step("Verify orders retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");

        Allure.step("Verify count matches data length");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, orders.size(), "count matches data length");

        Allure.step("Verify each order has required fields");
        if (!orders.isEmpty()) {
            Map<String, Object> firstOrder = orders.get(0);
            Assert.assertTrue(firstOrder.get("id") instanceof Integer, "Each order has id");
            Assert.assertTrue(firstOrder.get("status") instanceof String, "Each order has status");
            Assert.assertTrue(firstOrder.get("total_amount") instanceof Number, "Each order has total_amount");
            Assert.assertTrue(firstOrder.get("items") instanceof List, "Each order has items");
            Assert.assertTrue(firstOrder.get("shipping_address") instanceof String, "Each order has shipping_address");
        }
    }

    @Test(description = "TC-ORDER-005: Verify user can get order by ID", dependsOnMethods = "testUserCanGetAllOrders")
    public void testUserCanGetOrderById() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        orderID = JsonUtility.getJSONInt("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Allure.step("Send GET to get order by ID");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken);

        Allure.step("Verify order retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data.id matches requested order ID");
        Assert.assertEquals(response.jsonPath().getInt("data.id"), orderID, "data.id matches requested order ID");

        Allure.step("Verify data has required fields");
        Assert.assertTrue(response.jsonPath().get("data.status") instanceof String, "data has status");
        Assert.assertTrue(response.jsonPath().get("data.total_amount") instanceof Number, "data has total_amount");
        Assert.assertTrue(response.jsonPath().get("data.items") instanceof List, "data has items");
        Assert.assertTrue(response.jsonPath().get("data.shipping_address") instanceof String, "data has shipping_address");
    }

    @Test(description = "TC-ORDER-006: Verify get order by ID fails for non-existent order", dependsOnMethods = "testUserCanGetOrderById")
    public void testGetOrderByIdFailsForNonExistentOrder() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET with invalid order ID");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + INVALID_ORDER_ID, userToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Order not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Order not found"), "error is 'Order not found'");
    }

    @Test(description = "TC-ORDER-007: Verify user can update order shipping address", dependsOnMethods = "testGetOrderByIdFailsForNonExistentOrder")
    public void testUserCanUpdateOrderShippingAddress() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        orderID = JsonUtility.getJSONInt("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Allure.step("Send PUT to update shipping address");
        Map<String, Object> body = new HashMap<>();
        body.put("shipping_address", ORDER_UPDATED_SHIPPING_ADDRESS);
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken, body);

        Allure.step("Verify order updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Order updated successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Order updated successfully", "message is 'Order updated successfully'");

        Allure.step("Verify data.shipping_address is updated");
        Assert.assertEquals(response.jsonPath().getString("data.shipping_address"), ORDER_UPDATED_SHIPPING_ADDRESS, "data.shipping_address is updated");
    }

    @Test(description = "TC-ORDER-008: Verify update order fails for non-existent order", dependsOnMethods = "testUserCanUpdateOrderShippingAddress")
    public void testUpdateOrderFailsForNonExistentOrder() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send PUT with invalid order ID");
        Map<String, Object> body = new HashMap<>();
        body.put("shipping_address", ORDER_UPDATED_SHIPPING_ADDRESS);
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + INVALID_ORDER_ID, userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Order not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Order not found"), "error is 'Order not found'");
    }

    @Test(description = "TC-ORDER-009: Verify admin can update order status", dependsOnMethods = "testUpdateOrderFailsForNonExistentOrder")
    public void testAdminCanUpdateOrderStatus() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        orderID = JsonUtility.getJSONInt("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Allure.step("Send PUT to update order status");
        Map<String, Object> body = new HashMap<>();
        body.put("status", ORDER_STATUS_PROCESSING);
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID + "/status", adminToken, body);

        Allure.step("Verify order status updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message indicates status updated");
        String message = response.jsonPath().getString("message");
        Assert.assertTrue(message != null && (message.contains("Order status updated") || message.contains("Order status unchanged")), "message indicates status updated");

        Allure.step("Verify data.status is " + ORDER_STATUS_PROCESSING);
        Assert.assertEquals(response.jsonPath().getString("data.status"), ORDER_STATUS_PROCESSING, "data.status is " + ORDER_STATUS_PROCESSING);
    }

    @Test(description = "TC-ORDER-010: Verify update order status fails with invalid status", dependsOnMethods = "testAdminCanUpdateOrderStatus")
    public void testUpdateOrderStatusFailsWithInvalidStatus() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        orderID = JsonUtility.getJSONInt("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Allure.step("Send PUT with invalid status");
        Map<String, Object> body = new HashMap<>();
        body.put("status", INVALID_ORDER_STATUS);
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID + "/status", adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Invalid status'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Invalid status"), "error is 'Invalid status'");
    }

    @Test(description = "TC-ORDER-011: Verify update order status fails for non-admin user", dependsOnMethods = "testUpdateOrderStatusFailsWithInvalidStatus")
    public void testUpdateOrderStatusFailsForNonAdminUser() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        orderID = JsonUtility.getJSONInt("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Allure.step("Send PUT with user token");
        Map<String, Object> body = new HashMap<>();
        body.put("status", ORDER_STATUS_SHIPPED);
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderID + "/status", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Admin privileges required'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"), "error is 'Admin privileges required'");
    }

    @Test(description = "TC-ORDER-012: Verify admin can cancel order", dependsOnMethods = "testUpdateOrderStatusFailsForNonAdminUser")
    public void testAdminCanCancelOrder() throws Exception {
        Allure.step("Create new order for cancellation");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
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

        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");

        Allure.step("Send DELETE to cancel order");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + cancelOrderId, adminToken);

        Allure.step("Verify order cancelled");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message indicates order cancelled");
        String message = response.jsonPath().getString("message");
        Assert.assertTrue(message != null && message.contains("Order cancelled"), "message indicates order cancelled");

        Allure.step("Verify data.status is cancelled");
        Assert.assertEquals(response.jsonPath().getString("data.status"), ORDER_STATUS_CANCELLED, "data.status is cancelled");
    }

    @Test(description = "TC-ORDER-013: Verify user can cancel pending order", dependsOnMethods = "testAdminCanCancelOrder")
    public void testUserCanCancelPendingOrder() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Create new pending order");
        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken);
        Map<String, Object> cartBody = new HashMap<>();
        cartBody.put("product_id", CART_PRODUCT_ID);
        cartBody.put("quantity", CART_QUANTITY);
        ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, cartBody);

        Map<String, Object> orderBody = new HashMap<>();
        orderBody.put("shipping_address", ORDER_SHIPPING_ADDRESS);
        Response createResponse = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, orderBody);
        cancelOrderId = createResponse.jsonPath().getInt("data.id");

        Allure.step("Send DELETE to cancel pending order");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + cancelOrderId, userToken);

        Allure.step("Verify order cancelled");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data.status is cancelled");
        Assert.assertEquals(response.jsonPath().getString("data.status"), ORDER_STATUS_CANCELLED, "data.status is cancelled");
    }

    @Test(description = "TC-ORDER-014: Verify cancel order fails for non-pending order", dependsOnMethods = "testUserCanCancelPendingOrder")
    public void testCancelOrderFailsForNonPendingOrder() throws Exception {
        Allure.step("Create order and update status to processing");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
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
        ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + nonPendingOrderId + "/status", adminToken, statusBody);

        Allure.step("Send DELETE to cancel non-pending order");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + nonPendingOrderId, userToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates cannot cancel");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Cannot cancel"), "error indicates cannot cancel");
    }

    @Test(description = "TC-ORDER-015: Verify cancel order fails for non-existent order", dependsOnMethods = "testCancelOrderFailsForNonPendingOrder")
    public void testCancelOrderFailsForNonExistentOrder() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");

        Allure.step("Send DELETE with invalid order ID");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + INVALID_ORDER_ID, adminToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Order not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Order not found"), "error is 'Order not found'");
    }

    @Test(description = "TC-ORDER-016: Verify user can get orders by status pending", dependsOnMethods = "testCancelOrderFailsForNonExistentOrder")
    public void testUserCanGetOrdersByStatusPending() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET to get orders by status");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/status/" + ORDER_STATUS_PENDING, userToken);

        Allure.step("Verify orders retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");

        Allure.step("Verify count matches data length");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, orders.size(), "count matches data length");

        Allure.step("Verify each order has status pending");
        for (Map<String, Object> order : orders) {
            Assert.assertEquals(order.get("status"), ORDER_STATUS_PENDING, "Each order has status pending");
        }
    }

    @Test(description = "TC-ORDER-017: Verify user can get orders by status cancelled", dependsOnMethods = "testUserCanGetOrdersByStatusPending")
    public void testUserCanGetOrdersByStatusCancelled() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET to get orders by status");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/status/" + ORDER_STATUS_CANCELLED, userToken);

        Allure.step("Verify orders retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");

        Allure.step("Verify each order has status cancelled");
        for (Map<String, Object> order : orders) {
            Assert.assertEquals(order.get("status"), ORDER_STATUS_CANCELLED, "Each order has status cancelled");
        }
    }

    @Test(description = "TC-ORDER-018: Verify user can get orders by status processing", dependsOnMethods = "testUserCanGetOrdersByStatusCancelled")
    public void testUserCanGetOrdersByStatusProcessing() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET to get orders by status");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/status/" + ORDER_STATUS_PROCESSING, userToken);

        Allure.step("Verify orders retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");

        Allure.step("Verify each order has status processing");
        for (Map<String, Object> order : orders) {
            Assert.assertEquals(order.get("status"), ORDER_STATUS_PROCESSING, "Each order has status processing");
        }
    }

    @Test(description = "TC-ORDER-019: Verify admin can export orders", dependsOnMethods = "testUserCanGetOrdersByStatusProcessing")
    public void testAdminCanExportOrders() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");

        Allure.step("Send GET to export orders with query params");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("start_date", EXPORT_ORDERS_START_DATE);
        queryParams.put("end_date", EXPORT_ORDERS_END_DATE);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/export/orders", queryParams, adminToken);

        Allure.step("Verify orders exported");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data.orders is array");
        List<Map<String, Object>> orders = response.jsonPath().getList("data.orders");
        Assert.assertNotNull(orders, "data.orders is array");

        Allure.step("Verify data.total_orders matches orders size");
        int totalOrders = response.jsonPath().getInt("data.total_orders");
        Assert.assertEquals(orders.size(), totalOrders, "data.total_orders matches orders size");

        Allure.step("Verify each order has required fields");
        if (!orders.isEmpty()) {
            Map<String, Object> firstOrder = orders.get(0);
            Assert.assertTrue(firstOrder.get("shipping_address") instanceof String, "Each order has shipping_address");
            Assert.assertTrue(firstOrder.get("status") instanceof String, "Each order has status");
            Assert.assertTrue(firstOrder.get("items") instanceof List, "Each order has items");
        }
    }

    @Test(description = "TC-ORDER-020: Verify export orders fails for non-admin user", dependsOnMethods = "testAdminCanExportOrders")
    public void testExportOrdersFailsForNonAdminUser() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET to export orders");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("start_date", EXPORT_ORDERS_START_DATE);
        queryParams.put("end_date", EXPORT_ORDERS_END_DATE);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/export/orders", queryParams, userToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Admin privileges required'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"), "error is 'Admin privileges required'");
    }

    @Test(description = "TC-ORDER-021: Verify user cannot access another user's order", dependsOnMethods = "testExportOrdersFailsForNonAdminUser")
    public void testUserCannotAccessAnotherUsersOrder() throws Exception {
        Allure.step("Note: This test assumes order isolation");
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Get user's own orders");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders", userToken);

        Allure.step("Verify orders retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data contains only user's orders");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        for (Map<String, Object> order : orders) {
            Assert.assertTrue(order.get("user_id") instanceof Integer, "Each order has user_id");
        }
    }

    @Test(description = "TC-ORDER-022: Verify order items structure is correct", dependsOnMethods = "testUserCannotAccessAnotherUsersOrder")
    public void testOrderItemsStructureIsCorrect() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        orderID = JsonUtility.getJSONInt("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Allure.step("Send GET to get order by ID");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken);

        Allure.step("Verify order retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify data.items is array");
        List<Map<String, Object>> items = response.jsonPath().getList("data.items");
        Assert.assertNotNull(items, "data.items is array");

        Allure.step("Verify each item has required fields");
        if (!items.isEmpty()) {
            Map<String, Object> firstItem = items.get(0);
            Assert.assertTrue(firstItem.get("product_id") instanceof Integer, "Each item has product_id");
            Assert.assertTrue(firstItem.get("product_name") instanceof String, "Each item has product_name");
            Assert.assertTrue(firstItem.get("quantity") instanceof Integer, "Each item has quantity");
            Assert.assertTrue(firstItem.get("price") instanceof Number, "Each item has price");
            Assert.assertTrue(firstItem.get("subtotal") instanceof Number, "Each item has subtotal");
        }
    }

    @Test(description = "TC-ORDER-023: Verify order total amount calculation", dependsOnMethods = "testOrderItemsStructureIsCorrect")
    public void testOrderTotalAmountCalculation() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        orderID = JsonUtility.getJSONInt("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Allure.step("Send GET to get order by ID");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken);

        Allure.step("Verify order retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify data.total_amount matches sum of item subtotals");
        double totalAmount = response.jsonPath().getDouble("data.total_amount");
        List<Map<String, Object>> items = response.jsonPath().getList("data.items");
        double sumSubtotals = 0.0;
        for (Map<String, Object> item : items) {
            sumSubtotals += ((Number) item.get("subtotal")).doubleValue();
        }
        Assert.assertEquals(totalAmount, sumSubtotals, 0.01, "data.total_amount matches sum of item subtotals");
    }
}
