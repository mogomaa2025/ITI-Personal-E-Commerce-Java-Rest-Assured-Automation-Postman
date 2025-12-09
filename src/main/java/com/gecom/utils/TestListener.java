package com.gecom.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import io.qameta.allure.Allure;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.gecom.utils.Base.*;

public class TestListener implements ITestListener, IExecutionListener {

    @Override
    public void onExecutionStart() {
        // Clean previous allure-results before starting tests
        String allureFolderName = Base.ALLURE_RESULTS_DIR;
        // String logsFolderName = "logs";

        // Avoid using Logger before cleanup because log4j keeps the file handle open
        System.out.println("[TestListener] Cleaning previous Allure results");
        try {
            // delete recursively
            FileUtils.deleteDirectory(new File(allureFolderName));
            // FileUtils.deleteDirectory(new File(logsFolderName));
            logWithAllure("== Cleaned previous Allure results ==");
        } catch (IOException e) {
            logError("Failed to clean previous Allure results", e);
        }

        // refresh tokens
        try {
            loginAdminToken();
            loginUserToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onExecutionFinish() {

        logWithAllure("== Generating Allure report (results dir: " + Base.ALLURE_RESULTS_DIR + ") ==");
    }




    @Override
    public void onTestStart(ITestResult result) {
        logWithAllure("START: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logWithAllure("PASS: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logWithAllure("FAIL: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logWithAllure("SKIP: " + result.getMethod().getQualifiedName());
    }

    private static void GenerateAllureReport() {
        String cmd = "allure generate --clean -o allure-report allure-results";
        String[] command = System.getProperty("os.name").toLowerCase().contains("win")
                ? new String[] { "cmd", "/c", cmd }
                : new String[] { "/bin/sh", "-c", cmd };
        try {
            Process process = new ProcessBuilder(command).inheritIO().start();
            if (process.waitFor() == 0) {
                logWithAllure("Allure report generated successfully.");
            } else {
                logWithAllure("Allure generation failed.");
            }
        } catch (IOException | InterruptedException e) {
            logError("Failed to execute Allure command", e);
        }
    }

    private static void logWithAllure(String message) {
        Logger.info(message);
        Allure.step(message);
    }

    private static void logError(String message, Throwable throwable) {
        Logger.error(message, throwable);
        Allure.step("ERROR: " + message);
    }


    public static void loginUserToken() throws Exception {
        Logger.info("::freshAccount::ExecutionStart");
        // register first fresh
        String freshEmail = "test_" + faker.random().hex(8) + "@gmail.com";
        userPassword = "Test@123";
        String username = "Test User";
        Allure.step("Send POST with user data");
        Map<String, Object> registerBody = new HashMap<>();
        registerBody.put("email", freshEmail);
        registerBody.put("password", userPassword);
        registerBody.put("phone", "+010" + faker.number().digits(8)); // new validation
        registerBody.put("address", faker.address().fullAddress()); // new validation
        registerBody.put("name", username);
        registerBody.put("is_admin", false);
        Response registerResponse = ApiUtils.postRequest(BASE_URL + "/register", registerBody);
        Assert.assertEquals(registerResponse.getStatusCode(), 201, "Status code is 201");

        // login
        Map<String, Object> body = new HashMap<>();
        body.put("email", freshEmail);
        body.put("password", userPassword);
        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        // JsonUtility.saveValue("user", userToken, TOKEN_FILE_PATH);
        SetUserToken(response);
    }

    public void loginAdminToken() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("email", ADMIN_EMAIL);
        body.put("password", ADMIN_PASSWORD);
        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        // JsonUtility.saveValue("admin", adminToken, TOKEN_FILE_PATH);
        // JsonUtility.saveValue("refresh_token", refreshToken, TOKEN_FILE_PATH);
        SetAdminToken(response);
        SetRefreshToken(response);
    }





}
