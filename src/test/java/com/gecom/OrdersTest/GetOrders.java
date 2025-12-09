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
public class GetOrders {

    @Test(description = "TC-ORDER-004: Verify user can get all orders", groups = {
            "Valid-Orders-Test", "valid" })
    public void testUserCanGetAllOrders() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders", userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, orders.size(), "count matches data length");
        if (!orders.isEmpty()) {
            Map<String, Object> firstOrder = orders.get(0);
            Assert.assertTrue(firstOrder.get("id") instanceof Integer, "Each order has id");
            Assert.assertTrue(firstOrder.get("status") instanceof String, "Each order has status");
            Assert.assertTrue(firstOrder.get("total_amount") instanceof Number, "Each order has total_amount");
            Assert.assertTrue(firstOrder.get("items") instanceof List, "Each order has items");
            Assert.assertTrue(firstOrder.get("shipping_address") instanceof String, "Each order has shipping_address");
        }
    }

    @Test(description = "TC-ORDER-005: Verify user can get order by ID", groups = {
            "Valid-Orders-Test", "valid" }, dependsOnMethods = "testUserCanGetAllOrders")
    public void testUserCanGetOrderById() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        orderID = (Integer) JsonUtility.getValue("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertEquals(response.jsonPath().getInt("data.id"), orderID, "data.id matches requested order ID");
        Assert.assertTrue(response.jsonPath().get("data.status") instanceof String, "data has status");
        Assert.assertTrue(response.jsonPath().get("data.total_amount") instanceof Number, "data has total_amount");
        Assert.assertTrue(response.jsonPath().get("data.items") instanceof List, "data has items");
        Assert.assertTrue(response.jsonPath().get("data.shipping_address") instanceof String,
                "data has shipping_address");
    }

    @Test(description = "TC-ORDER-006: Verify get order by ID fails for non-existent order", groups = {
            "Invalid-Orders-Test", "invalid" }, dependsOnMethods = "testUserCanGetOrderById")
    public void testGetOrderByIdFailsForNonExistentOrder() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + INVALID_ORDER_ID, userToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Order not found"), "error is 'Order not found'");
    }

    @Test(description = "TC-ORDER-016: Verify user can get orders by status pending", groups = {
            "Valid-Orders-Test", "valid" }, dependsOnMethods = "testUserCanGetOrderById")
    public void testUserCanGetOrdersByStatusPending() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/status/" + ORDER_STATUS_PENDING, userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, orders.size(), "count matches data length");
        for (Map<String, Object> order : orders) {
            Assert.assertEquals(order.get("status"), ORDER_STATUS_PENDING, "Each order has status pending");
        }
    }

    @Test(description = "TC-ORDER-017: Verify user can get orders by status cancelled", groups = {
            "Valid-Orders-Test", "valid" }, dependsOnMethods = "testUserCanGetOrdersByStatusPending")
    public void testUserCanGetOrdersByStatusCancelled() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/status/" + ORDER_STATUS_CANCELLED,
                userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");
        for (Map<String, Object> order : orders) {
            Assert.assertEquals(order.get("status"), ORDER_STATUS_CANCELLED, "Each order has status cancelled");
        }
    }

    @Test(description = "TC-ORDER-018: Verify user can get orders by status processing", groups = {
            "Valid-Orders-Test", "valid" }, dependsOnMethods = "testUserCanGetOrdersByStatusCancelled")
    public void testUserCanGetOrdersByStatusProcessing() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/status/" + ORDER_STATUS_PROCESSING,
                userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "data is array");
        for (Map<String, Object> order : orders) {
            Assert.assertEquals(order.get("status"), ORDER_STATUS_PROCESSING, "Each order has status processing");
        }
    }

    @Test(description = "TC-ORDER-019: Verify admin can export orders", groups = {
            "Valid-Orders-Test", "valid" }, dependsOnMethods = "testUserCanGetOrdersByStatusProcessing")
    public void testAdminCanExportOrders() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("start_date", EXPORT_ORDERS_START_DATE);
        queryParams.put("end_date", EXPORT_ORDERS_END_DATE);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/export/orders", queryParams, adminToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        List<Map<String, Object>> orders = response.jsonPath().getList("data.orders");
        Assert.assertNotNull(orders, "data.orders is array");
        int totalOrders = response.jsonPath().getInt("data.total_orders");
        Assert.assertEquals(orders.size(), totalOrders, "data.total_orders matches orders size");
        if (!orders.isEmpty()) {
            Map<String, Object> firstOrder = orders.get(0);
            Assert.assertTrue(firstOrder.get("shipping_address") instanceof String, "Each order has shipping_address");
            Assert.assertTrue(firstOrder.get("status") instanceof String, "Each order has status");
            Assert.assertTrue(firstOrder.get("items") instanceof List, "Each order has items");
        }
    }

    @Test(description = "TC-ORDER-020: Verify export orders fails for non-admin user", groups = {
            "Invalid-Orders-Test", "invalid" }, dependsOnMethods = "testAdminCanExportOrders")
    public void testExportOrdersFailsForNonAdminUser() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("start_date", EXPORT_ORDERS_START_DATE);
        queryParams.put("end_date", EXPORT_ORDERS_END_DATE);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/export/orders", queryParams, userToken);
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                "error is 'Admin privileges required'");
    }

    @Test(description = "TC-ORDER-021: Verify user cannot access another user's order", groups = {
            "Invalid-Orders-Test", "invalid" }, dependsOnMethods = "testExportOrdersFailsForNonAdminUser")
    public void testUserCannotAccessAnotherUsersOrder() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders", userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        for (Map<String, Object> order : orders) {
            Assert.assertTrue(order.get("user_id") instanceof Integer, "Each order has user_id");
        }
    }

    @Test(description = "TC-ORDER-022: Verify order items structure is correct", groups = {
            "Valid-Orders-Test", "valid" }, dependsOnMethods = "testUserCannotAccessAnotherUsersOrder")
    public void testOrderItemsStructureIsCorrect() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        orderID = (Integer) JsonUtility.getValue("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        List<Map<String, Object>> items = response.jsonPath().getList("data.items");
        Assert.assertNotNull(items, "data.items is array");

        if (!items.isEmpty()) {
            Map<String, Object> firstItem = items.get(0);
            Assert.assertTrue(firstItem.get("product_id") instanceof Integer, "Each item has product_id");
            Assert.assertTrue(firstItem.get("product_name") instanceof String, "Each item has product_name");
            Assert.assertTrue(firstItem.get("quantity") instanceof Integer, "Each item has quantity");
            Assert.assertTrue(firstItem.get("price") instanceof Number, "Each item has price");
            Assert.assertTrue(firstItem.get("subtotal") instanceof Number, "Each item has subtotal");
        }
    }

    @Test(description = "TC-ORDER-023: Verify order total amount calculation", groups = {
            "Valid-Orders-Test", "valid" }, dependsOnMethods = "testOrderItemsStructureIsCorrect")
    public void testOrderTotalAmountCalculation() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        orderID = (Integer) JsonUtility.getValue("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(orderID, "Order ID is valid Integer");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + orderID, userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        double totalAmount = response.jsonPath().getDouble("data.total_amount");
        List<Map<String, Object>> items = response.jsonPath().getList("data.items");
        double sumSubtotals = 0.0;
        for (Map<String, Object> item : items) {
            sumSubtotals += ((Number) item.get("subtotal")).doubleValue();
        }
        Assert.assertEquals(totalAmount, sumSubtotals, 0.01, "data.total_amount matches sum of item subtotals");
    }

}
