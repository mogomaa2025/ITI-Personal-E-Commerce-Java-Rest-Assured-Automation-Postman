package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gecom.utils.Const.*;

@Listeners({AllureTestNg.class})
public class OrdersTest {




    @Test(groups = "OrdersTest")
    public void testAddToCartDuplicate() throws Exception {
        Allure.step("Starting testAddToCartDuplicate...");
        userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 5);
        body.put("quantity", 3);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);
        Allure.step("Add to cart API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");
        Assert.assertEquals(response.jsonPath().getBoolean("success"), true);
        Assert.assertEquals(response.jsonPath().getString("message"), "Item added to cart successfully");

        Allure.step("testAddToCartDuplicate finished successfully.");
    }

    @Test(groups = "OrdersTest",  dependsOnMethods = "testAddToCartDuplicate")
    public void testCreateOrder() throws Exception {
        Allure.step("Starting testCreateOrder...");
        userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Map<String, Object> body = new HashMap<>();
        body.put("shipping_address", "123 Test Street");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/orders", userToken, body);
        Allure.step("Create order API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");
        Assert.assertEquals(response.jsonPath().getBoolean("success"), true);
        Assert.assertEquals(response.jsonPath().getString("message"), "Order created successfully");
        Assert.assertEquals(response.jsonPath().getString("data.shipping_address"), "123 Test Street");

        List<Map<String, Object>> items = response.jsonPath().getList("data.items");
        Assert.assertNotNull(items, "Items list should not be null");
        Assert.assertFalse(items.isEmpty(), "Items list should not be empty");

        Map<String, Object> firstItem = items.get(0);
        Assert.assertTrue(firstItem.get("price") instanceof Number, "Price should be a number");
        Assert.assertTrue(firstItem.get("product_name") instanceof String, "Product name should be a string");

        orderID = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("order_id", String.valueOf(orderID), IDS_FILE_PATH);
        Allure.step("Order ID saved: " + orderID);

        Allure.step("testCreateOrder finished successfully.");
    }

    @Test(groups = "OrdersTest",  dependsOnMethods = "testCreateOrder")
    public void testListOrders() throws Exception {
        Allure.step("Starting testListOrders...");
        userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders", userToken);
        Allure.step("List orders API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getBoolean("success"), true);

        List<Map<String, Object>> orders = response.jsonPath().getList("data");
        Assert.assertNotNull(orders, "Orders list should not be null");

        // extract order IDs and save to ids.json
        List<String> orderIds = new ArrayList<>();
        for (Map<String, Object> order : orders) {
            Number id = (Number) order.get("id");
            orderIds.add(String.valueOf(id.longValue()));
        }

        // Save orderID to ids.json
        orderID = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("orderID", orderID.toString(), IDS_FILE_PATH);
        Allure.step("orderID ID saved: " + orderID);


        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, orderIds.size(), "Count should match number of orders");

        Allure.step("testListOrders finished successfully.");
    }

    @Test(groups = "OrdersTest",  dependsOnMethods = "testListOrders")
    public void testGetOrderById() throws Exception {
        Allure.step("Starting testGetOrderById...");
        userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        String orderId = JsonUtility.getToken("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Assert.assertNotNull(orderId, "Order ID not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/" + orderId, userToken);
        Allure.step("Get order API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        // validate response for specific order
        Assert.assertEquals(JsonUtility.getLastUserId(response), Integer.parseInt(orderId));

        Allure.step("testGetOrderById finished successfully.");
    }

    @Test(groups = "OrdersTest",  dependsOnMethods = "testGetOrderById")
    public void testCancelOrder() throws Exception {
        Allure.step("Starting testCancelOrder...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String orderId = JsonUtility.getToken("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");
        Assert.assertNotNull(orderId, "Order ID not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/orders/" + orderId, adminToken);
        Allure.step("Cancel order API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        // validate response
        Assert.assertEquals(response.jsonPath().getString("message"), "Order cancelled successfully");
        Assert.assertEquals(response.jsonPath().getString("data.status"), "cancelled");

        Allure.step("testCancelOrder finished successfully.");
    }

    @Test(groups = "OrdersTest",  dependsOnMethods = "testCancelOrder")
    public void testUpdateOrderStatus() throws Exception {
        Allure.step("Starting testUpdateOrderStatus...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String orderId = JsonUtility.getToken("order_id", IDS_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");
        Assert.assertNotNull(orderId, "Order ID not found");

        Map<String, Object> body = new HashMap<>();
        body.put("status", "shipped");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/orders/" + orderId + "/status", adminToken, body);
        Allure.step("Update status API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        // validate response
        Assert.assertEquals(response.jsonPath().getString("message"), "Order status updated successfully");
        Assert.assertEquals(response.jsonPath().getString("data.status"), "shipped");

        Allure.step("testUpdateOrderStatus finished successfully.");
    }

    @Test(groups = "OrdersTest",  dependsOnMethods = "testUpdateOrderStatus")
    public void testOrdersByStatusPending() throws Exception {
        Allure.step("Starting testOrdersByStatusPending...");
        userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/orders/status/pending", userToken);
        Allure.step("Orders by status API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getBoolean("success"), true);

        // validate pending orders structure
        List<Map<String, Object>> pendingOrders = response.jsonPath().getList("data");
        Assert.assertNotNull(pendingOrders, "Pending orders list should not be null");

        // validate count matches list size
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, pendingOrders.size(), "Count should match pending orders size");

        // validate each order status
        for (Map<String, Object> order : pendingOrders) {
            Assert.assertEquals(order.get("status"), "pending");
        }

        Allure.step("testOrdersByStatusPending finished successfully.");
    }

    @Test(groups = "OrdersTest",  dependsOnMethods = "testUpdateOrderStatus")
    public void testExportOrders() throws Exception {
        Allure.step("Starting testExportOrders...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

       // valid date range to ensure orders exist
        String endpoint = BASE_URL + "/export/orders?start_date=2024-01-01&end_date=2025-12-31";
        Response response = ApiUtils.getRequestWithAuth(endpoint, adminToken);
        Allure.step("Export orders API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getBoolean("success"), true);

        // validate exported orders structure
        List<Map<String, Object>> orders = response.jsonPath().getList("data.orders");
        Assert.assertNotNull(orders, "Exported orders list should not be null");


        // validate total orders count
        int totalOrders = response.jsonPath().getInt("data.total_orders");
        Assert.assertEquals(orders.size(), totalOrders, "Total orders should match exported list size");



        // check structure of each order
        for (Map<String, Object> order : orders) {
            Assert.assertTrue(order.get("shipping_address") instanceof String, "Shipping address should be a string");
            Assert.assertTrue(order.get("status") instanceof String, "Status should be a string");
            Assert.assertTrue(order.get("items") instanceof List, "Items should be a list");
        }

        Allure.step("testExportOrders finished successfully.");
    }
}
