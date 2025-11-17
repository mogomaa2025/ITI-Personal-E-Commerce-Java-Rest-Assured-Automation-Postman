package com.gecom;

import com.gecom.utils.ApiUtils;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


import static com.gecom.utils.Const.*;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "SystemTest")
@Severity(SeverityLevel.CRITICAL)
public class SystemTest {

    @Test(description = "TC-SYS-001: Verify API health check returns success")
    public void testHealthCheckSuccessfulRetrieval() {
        Allure.step("Send GET request to /health");
        Response response = ApiUtils.getRequest(BASE_URL + "/health");

        Allure.step("Verify response contains expected data");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response should be valid JSON");

        Allure.step("Verify success field is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success should be true");

        Allure.step("Verify message field");
        Assert.assertEquals(response.jsonPath().getString("message"), "API is running", "message should be 'API is running'");

        Allure.step("Verify timestamp is present and not empty");
        String timestamp = response.jsonPath().getString("timestamp");
        Assert.assertTrue(timestamp != null && !timestamp.isEmpty(), "timestamp is present and not empty");

        Allure.step("Verify response time is under 1000ms");
        Assert.assertTrue(response.getTime() < 1000, "Response time should be under 1000ms");

        Allure.step("Data retrieved successfully with valid structure");
    }

    @Test(description = "TC-SYS-002: Verify system health returns all data files status", dependsOnMethods = "testHealthCheckSuccessfulRetrieval")
    public void testSystemHealthSummarySuccessfulRetrieval() {
        Allure.step("Send GET request to /system/health");
        Response response = ApiUtils.getRequest(BASE_URL + "/system/health");

        Allure.step("Verify response contains expected data");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response should be valid JSON");

        Allure.step("Verify success field is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success should be true");

        Allure.step("Verify status field is 'healthy'");
        Assert.assertEquals(response.jsonPath().getString("status"), "healthy", "status should be 'healthy'");

        Allure.step("Verify timestamp is present and not empty");
        String timestamp = response.jsonPath().getString("timestamp");
        Assert.assertTrue(timestamp != null && !timestamp.isEmpty(), "timestamp is present and not empty");

        Allure.step("Verify data_files is Map type"); // map mean "key:value" << Justification
        Assert.assertNotNull(response.jsonPath().getMap("data_files"), "data_files is Map type");

        //        Assert.assertTrue(response.jsonPath().get("metrics.total_orders") instanceof Number , "total_orders is not valid number");
//        Assert.assertTrue(response.jsonPath().get("metrics.total_products") instanceof Number, "total_products is not valid number");
//        Assert.assertTrue(response.jsonPath().get("metrics.total_users") instanceof Number, "total_users is not valid number");
        // using this way better than instanceof for >= 0 check in one line
        Allure.step("Verify metrics contains total_orders/total_products/total_users");
        Assert.assertTrue(response.jsonPath().getInt("metrics.total_orders") >= 0, "total_orders is not valid number");
        Assert.assertTrue(response.jsonPath().getInt("metrics.total_products") >= 0, "total_products is not valid number");
        Assert.assertTrue(response.jsonPath().getInt("metrics.total_users") >= 0, "total_users is not valid number");




        Allure.step("Verify response time is under 1000ms");
        Assert.assertTrue(response.getTime() < 1000, "Response time should be under 1000ms");

        Allure.step("Data retrieved successfully with valid structure");
    }

    @Test(description = "TC-SYS-003: Verify API documentation returns all endpoints", dependsOnMethods = "testSystemHealthSummarySuccessfulRetrieval")
    public void testApiDocumentationSuccessfulRetrieval() {
        Allure.step("Send GET request to /docs");
        Response response = ApiUtils.getRequest(BASE_URL + "/docs");

        Allure.step("Verify response contains expected data");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response should be valid JSON");

        Allure.step("Verify success field is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success should be true");

        Allure.step("Verify title field");
        Assert.assertEquals(response.jsonPath().getString("title"), "E-Commerce API Documentation", "title should be 'E-Commerce API Documentation'");

        Allure.step("Verify version present");
        String version = response.jsonPath().getString("version");
        Assert.assertTrue(version != null && !version.isEmpty(), "version present");

        Allure.step("Verify DESCRIPTION present");
        String description = response.jsonPath().getString("description");
        Assert.assertTrue(description != null && !description.isEmpty(), "description present");

        Allure.step("Verify base_url present and not empty");
        String baseUrl = response.jsonPath().getString("base_url");
        Assert.assertTrue(baseUrl != null && !baseUrl.isEmpty(), "base_url present and not empty");

        Allure.step("Verify total_endpoints >= 62");
        int totalEndpoints = response.jsonPath().getInt("total_endpoints");
        Assert.assertTrue(totalEndpoints >= 62, "total_endpoints >= 62");

        Allure.step("Verify endpoints object has categories : admin, products, users, orders, cart ....etc");
        Assert.assertNotNull(response.jsonPath().getMap("endpoints"), "endpoints object has categories");

        Allure.step("Verify features array not empty : e.g : User Authentication & Management, Product Catalog, Order Processing ....etc");
        Assert.assertFalse(response.jsonPath().getList("features").isEmpty(), "features array not empty");

        Allure.step("Data retrieved successfully with valid structure");
    }
}
