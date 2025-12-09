package com.gecom.StatsAnalyticsTest;

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
@Test(groups = "StatsAnalyticsTest")
@Severity(SeverityLevel.MINOR)
public class StatsAnalytics {

        @Test(description = "TC-STATS-001: Verify admin can get dashboard statistics", groups = { "Valid-Stats-Test",
                        "valid" })
        public void testAdminCanGetDashboardStatistics() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token is valid String");

                Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/stats", adminToken);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertTrue(response.jsonPath().get("data.total_orders") instanceof Integer,
                                "data has total_orders");
                Assert.assertTrue(response.jsonPath().get("data.total_products") instanceof Integer,
                                "data has total_products");
                Assert.assertTrue(response.jsonPath().get("data.total_revenue") instanceof Number,
                                "data has total_revenue");
                Assert.assertTrue(response.jsonPath().get("data.total_reviews") instanceof Integer,
                                "data has total_reviews");
                Assert.assertTrue(response.jsonPath().get("data.total_users") instanceof Integer,
                                "data has total_users");
                Assert.assertTrue(response.jsonPath().get("data.pending_orders") instanceof Integer,
                                "data has pending_orders");
                Assert.assertTrue(response.jsonPath().get("data.low_stock_products") instanceof Integer,
                                "data has low_stock_products");
        }

        @Test(description = "TC-STATS-002: Verify get stats fails for non-admin user", groups = {
                        "Invalid-Stats-Test", "invalid" }, dependsOnMethods = "testAdminCanGetDashboardStatistics")
        public void testGetStatsFailsForNonAdminUser() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/stats", userToken);
                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                                "error is 'Admin privileges required'");
        }

        @Test(description = "TC-STATS-003: Verify admin can get dashboard analytics", groups = { "Valid-Stats-Test",
                        "valid" }, dependsOnMethods = "testGetStatsFailsForNonAdminUser")
        public void testAdminCanGetDashboardAnalytics() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token is valid String");
                Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/analytics/dashboard", adminToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertTrue(response.jsonPath().get("data.monthly_revenue") instanceof Number,
                                "data has monthly_revenue");
                Assert.assertTrue(response.jsonPath().get("data.total_orders") instanceof Integer,
                                "data has total_orders");
                Assert.assertTrue(response.jsonPath().get("data.total_products") instanceof Integer,
                                "data has total_products");
                Assert.assertTrue(response.jsonPath().get("data.total_revenue") instanceof Number,
                                "data has total_revenue");
                Assert.assertTrue(response.jsonPath().get("data.total_users") instanceof Integer,
                                "data has total_users");
                Assert.assertTrue(response.jsonPath().get("data.page_views_data") instanceof List,
                                "data has page_views_data array");
                Assert.assertTrue(response.jsonPath().get("data.popular_products") instanceof List,
                                "data has popular_products array");
                Assert.assertTrue(response.jsonPath().get("data.sales_data") instanceof List,
                                "data has sales_data array");
                Assert.assertTrue(response.jsonPath().get("data.user_registrations") instanceof List,
                                "data has user_registrations array");
        }

        @Test(description = "TC-STATS-004: Verify get dashboard analytics fails for non-admin user", groups = {
                        "Invalid-Stats-Test", "invalid" }, dependsOnMethods = "testAdminCanGetDashboardAnalytics")
        public void testGetDashboardAnalyticsFailsForNonAdminUser() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/analytics/dashboard", userToken);
                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                                "error is 'Admin privileges required'");
        }

        @Test(description = "TC-STATS-005: Verify admin can get sales report", groups = { "Valid-Stats-Test",
                        "valid" }, dependsOnMethods = "testGetDashboardAnalyticsFailsForNonAdminUser")
        public void testAdminCanGetSalesReport() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token is valid String");
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("start_date", SALES_REPORT_START_DATE);
                queryParams.put("end_date", SALES_REPORT_END_DATE);
                Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/analytics/reports/sales", queryParams,
                                adminToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                List<Map<String, Object>> orders = response.jsonPath().getList("data.orders");
                Assert.assertNotNull(orders, "data.orders is array");
                Assert.assertTrue(response.jsonPath().get("data.summary.total_orders") instanceof Integer,
                                "data.summary has total_orders");
                Assert.assertTrue(response.jsonPath().get("data.summary.total_sales") instanceof Number,
                                "data.summary has total_sales");
                Assert.assertTrue(response.jsonPath().get("data.summary.average_order_value") instanceof Number,
                                "data.summary has average_order_value");
                Assert.assertTrue(response.jsonPath().get("data.summary.cancelled_orders") instanceof Integer,
                                "data.summary has cancelled_orders");
                Assert.assertTrue(response.jsonPath().get("data.period.start_date") instanceof String,
                                "data.period has start_date");
                Assert.assertTrue(response.jsonPath().get("data.period.end_date") instanceof String,
                                "data.period has end_date");
                Assert.assertTrue(response.jsonPath().get("data.sales_by_status") instanceof Map,
                                "data.sales_by_status is object");
                int totalOrders = response.jsonPath().getInt("data.summary.total_orders");
                Assert.assertEquals(orders.size(), totalOrders, "count of orders matches total_orders");
        }

        @Test(description = "TC-STATS-006: Verify get sales report fails for non-admin user", groups = {
                        "Invalid-Stats-Test", "invalid" }, dependsOnMethods = "testAdminCanGetSalesReport")
        public void testGetSalesReportFailsForNonAdminUser() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("start_date", SALES_REPORT_START_DATE);
                queryParams.put("end_date", SALES_REPORT_END_DATE);
                Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/analytics/reports/sales", queryParams,
                                userToken);
                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                                "error is 'Admin privileges required'");
        }
}
