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
 * This class contains test cases for the statistics and analytics functionalities,
 * including dashboard stats, analytics, and sales reports.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "StatsAnalyticsTest")
@Severity(SeverityLevel.CRITICAL)
public class StatsAnalyticsTest {

    /**
     * Test case for verifying that an admin can retrieve dashboard statistics.
     *
     * @throws Exception if an error occurs while reading the admin token.
     */
    @Test(description = "TC-STATS-001: Verify admin can get dashboard statistics")
    public void testAdminCanGetDashboardStatistics() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");

        Allure.step("Send GET to get dashboard statistics");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/stats", adminToken);

        Allure.step("Verify statistics retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data has required fields");
        Assert.assertTrue(response.jsonPath().get("data.total_orders") instanceof Integer, "data has total_orders");
        Assert.assertTrue(response.jsonPath().get("data.total_products") instanceof Integer, "data has total_products");
        Assert.assertTrue(response.jsonPath().get("data.total_revenue") instanceof Number, "data has total_revenue");
        Assert.assertTrue(response.jsonPath().get("data.total_reviews") instanceof Integer, "data has total_reviews");
        Assert.assertTrue(response.jsonPath().get("data.total_users") instanceof Integer, "data has total_users");
        Assert.assertTrue(response.jsonPath().get("data.pending_orders") instanceof Integer, "data has pending_orders");
        Assert.assertTrue(response.jsonPath().get("data.low_stock_products") instanceof Integer, "data has low_stock_products");
    }

    /**
     * Test case for verifying that a non-admin user cannot retrieve dashboard statistics.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-STATS-002: Verify get stats fails for non-admin user", dependsOnMethods = "testAdminCanGetDashboardStatistics")
    public void testGetStatsFailsForNonAdminUser() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET with user token");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/stats", userToken);

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

    /**
     * Test case for verifying that an admin can retrieve dashboard analytics.
     *
     * @throws Exception if an error occurs while reading the admin token.
     */
    @Test(description = "TC-STATS-003: Verify admin can get dashboard analytics", dependsOnMethods = "testGetStatsFailsForNonAdminUser")
    public void testAdminCanGetDashboardAnalytics() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");

        Allure.step("Send GET to get dashboard analytics");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/analytics/dashboard", adminToken);

        Allure.step("Verify analytics retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data has required fields");
        Assert.assertTrue(response.jsonPath().get("data.monthly_revenue") instanceof Number, "data has monthly_revenue");
        Assert.assertTrue(response.jsonPath().get("data.total_orders") instanceof Integer, "data has total_orders");
        Assert.assertTrue(response.jsonPath().get("data.total_products") instanceof Integer, "data has total_products");
        Assert.assertTrue(response.jsonPath().get("data.total_revenue") instanceof Number, "data has total_revenue");
        Assert.assertTrue(response.jsonPath().get("data.total_users") instanceof Integer, "data has total_users");

        Allure.step("Verify data has arrays");
        Assert.assertTrue(response.jsonPath().get("data.page_views_data") instanceof List, "data has page_views_data array");
        Assert.assertTrue(response.jsonPath().get("data.popular_products") instanceof List, "data has popular_products array");
        Assert.assertTrue(response.jsonPath().get("data.sales_data") instanceof List, "data has sales_data array");
        Assert.assertTrue(response.jsonPath().get("data.user_registrations") instanceof List, "data has user_registrations array");
    }

    /**
     * Test case for verifying that a non-admin user cannot retrieve dashboard analytics.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-STATS-004: Verify get dashboard analytics fails for non-admin user", dependsOnMethods = "testAdminCanGetDashboardAnalytics")
    public void testGetDashboardAnalyticsFailsForNonAdminUser() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET with user token");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/analytics/dashboard", userToken);

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

    /**
     * Test case for verifying that an admin can retrieve a sales report.
     *
     * @throws Exception if an error occurs while reading the admin token.
     */
    @Test(description = "TC-STATS-005: Verify admin can get sales report", dependsOnMethods = "testGetDashboardAnalyticsFailsForNonAdminUser")
    public void testAdminCanGetSalesReport() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token is valid String");

        Allure.step("Send GET to get sales report with query params");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("start_date", SALES_REPORT_START_DATE);
        queryParams.put("end_date", SALES_REPORT_END_DATE);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/analytics/reports/sales", queryParams, adminToken);

        Allure.step("Verify sales report retrieved");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data.orders is array");
        List<Map<String, Object>> orders = response.jsonPath().getList("data.orders");
        Assert.assertNotNull(orders, "data.orders is array");

        Allure.step("Verify data.summary has required fields");
        Assert.assertTrue(response.jsonPath().get("data.summary.total_orders") instanceof Integer, "data.summary has total_orders");
        Assert.assertTrue(response.jsonPath().get("data.summary.total_sales") instanceof Number, "data.summary has total_sales");
        Assert.assertTrue(response.jsonPath().get("data.summary.average_order_value") instanceof Number, "data.summary has average_order_value");
        Assert.assertTrue(response.jsonPath().get("data.summary.cancelled_orders") instanceof Integer, "data.summary has cancelled_orders");

        Allure.step("Verify data.period has start_date and end_date");
        Assert.assertTrue(response.jsonPath().get("data.period.start_date") instanceof String, "data.period has start_date");
        Assert.assertTrue(response.jsonPath().get("data.period.end_date") instanceof String, "data.period has end_date");

        Allure.step("Verify data.sales_by_status is object");
        Assert.assertTrue(response.jsonPath().get("data.sales_by_status") instanceof Map, "data.sales_by_status is object");

        Allure.step("Verify count of orders matches total_orders");
        int totalOrders = response.jsonPath().getInt("data.summary.total_orders");
        Assert.assertEquals(orders.size(), totalOrders, "count of orders matches total_orders");
    }

    /**
     * Test case for verifying that a non-admin user cannot retrieve a sales report.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-STATS-006: Verify get sales report fails for non-admin user", dependsOnMethods = "testAdminCanGetSalesReport")
    public void testGetSalesReportFailsForNonAdminUser() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send GET with user token");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("start_date", SALES_REPORT_START_DATE);
        queryParams.put("end_date", SALES_REPORT_END_DATE);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/analytics/reports/sales", queryParams, userToken);

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
}
