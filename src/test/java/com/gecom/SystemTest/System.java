package com.gecom.SystemTest;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import io.qameta.allure.testng.AllureTestNg;

import static com.gecom.utils.Const.*;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "SystemTest")
@Severity(SeverityLevel.CRITICAL)
public class System {

        @Test(description = "TC-SYS-001: Verify API health check returns success", groups = { "Valid-System-Test",
                        "valid" })
        public void testHealthCheckSuccessfulRetrieval() {
                Response response = ApiUtils.getRequest(BASE_URL + "/health");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
                Assert.assertNotNull(response.jsonPath(), "Response should be valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success should be true");
                Assert.assertEquals(response.jsonPath().getString("message"), "API is running",
                                "message should be 'API is running'");
                String timestamp = response.jsonPath().getString("timestamp");
                Assert.assertTrue(timestamp != null && !timestamp.isEmpty(), "timestamp is present and not empty");
                Assert.assertTrue(response.getTime() < 1000, "Response time should be under 1000ms");
        }

        @Test(description = "TC-SYS-002: Verify system health returns all data files status", groups = {
                        "Valid-System-Test", "valid" })
        public void testSystemHealthSummarySuccessfulRetrieval() {
                Response response = ApiUtils.getRequest(BASE_URL + "/system/health");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
                Assert.assertNotNull(response.jsonPath(), "Response should be valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success should be true");
                Assert.assertEquals(response.jsonPath().getString("status"), "healthy", "status should be 'healthy'");
                String timestamp = response.jsonPath().getString("timestamp");
                Assert.assertTrue(timestamp != null && !timestamp.isEmpty(), "timestamp is present and not empty");
                Assert.assertNotNull(response.jsonPath().getMap("data_files"), "data_files is Map type");
                Assert.assertTrue(response.jsonPath().getInt("metrics.total_orders") >= 0,
                                "total_orders is not valid number");
                Assert.assertTrue(response.jsonPath().getInt("metrics.total_products") >= 0,
                                "total_products is not valid number");
                Assert.assertTrue(response.jsonPath().getInt("metrics.total_users") >= 0,
                                "total_users is not valid number");
                Assert.assertTrue(response.getTime() < 1000, "Response time should be under 1000ms");
        }

        @Test(description = "TC-SYS-003: Verify API documentation returns all endpoints", groups = {
                        "Valid-System-Test", "valid" })
        public void testApiDocumentationSuccessfulRetrieval() {
                Response response = ApiUtils.getRequest(BASE_URL + "/docs");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
                Assert.assertNotNull(response.jsonPath(), "Response should be valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success should be true");
                Assert.assertEquals(response.jsonPath().getString("title"), "E-Commerce API Documentation",
                                "title should be 'E-Commerce API Documentation'");
                String version = response.jsonPath().getString("version");
                Assert.assertTrue(version != null && !version.isEmpty(), "version present");
                String description = response.jsonPath().getString("description");
                Assert.assertTrue(description != null && !description.isEmpty(), "description present");
                String baseUrl = response.jsonPath().getString("base_url");
                Assert.assertTrue(baseUrl != null && !baseUrl.isEmpty(), "base_url present and not empty");
                int totalEndpoints = response.jsonPath().getInt("total_endpoints");
                Assert.assertTrue(totalEndpoints >= 62, "total_endpoints >= 62");
                Assert.assertNotNull(response.jsonPath().getMap("endpoints"), "endpoints object has categories");
                Assert.assertFalse(response.jsonPath().getList("features").isEmpty(), "features array not empty");
        }
}
