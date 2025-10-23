package com.gecom;

import com.gecom.utils.ApiUtils;
import io.restassured.response.Response;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.gecom.utils.Const.*;
import static com.gecom.utils.RemoveAllureResult.deleteFolder;

@Listeners({AllureTestNg.class})

public class SystemTest {
    @BeforeSuite(alwaysRun = true)
    public void CleanAllure() throws Exception  {
        deleteFolder("target/allure-results");
        System.out.println("Directory deleted successfully");
    }

    @Test(groups = "SystemTest", description = "testHealthCheck" )
    public void testHealthCheck() {
        Allure.step("Send GET request to /health endpoint");
        Response response = ApiUtils.getRequest(BASE_URL + "/health");

        Allure.step("Verify HTTP status code is 200");
        Assert.assertEquals(response.getStatusCode(), 200);

        Allure.step("Check if API is healthy");
        Assert.assertTrue(response.asString().contains("running"));
        Allure.step("Health check test passed successfully.");
    }

    @Test(groups = "SystemTest", dependsOnMethods = "testHealthCheck" )
    public void testSystemHealthSummary() {
        Allure.step("Send GET request to /system/health endpoint");
        Response response = ApiUtils.getRequest(BASE_URL + "/system/health");

        Allure.step("Verify HTTP status code is 200");
        Assert.assertEquals(response.getStatusCode(), 200);

        Allure.step("Check if system health summary is 'healthy'");
        Assert.assertTrue(response.asString().contains("\"status\":\"healthy\""));
        Allure.step("System health summary test passed successfully.");
    }

    @Test(groups = "SystemTest" ,dependsOnMethods = "testSystemHealthSummary")
    public void testApiDocs() {
        Allure.step("Send GET request to /docs endpoint");
        Response response = ApiUtils.getRequest(BASE_URL + "/docs");

        Allure.step("Verify HTTP status code is 200");
        Assert.assertEquals(response.getStatusCode(), 200);

        Allure.step("Check response contains API documentation structure");
        Assert.assertTrue(response.asString().contains("\"title\""));
        Assert.assertTrue(response.asString().contains("\"version\""));
        Assert.assertTrue(response.asString().contains("\"endpoints\""));
        Allure.step("API docs test passed successfully.");
    }
}
