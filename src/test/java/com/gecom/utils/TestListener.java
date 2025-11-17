package com.gecom.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import io.qameta.allure.Allure;


/**
 * - Logs test start/success/failure/skip to stdout
 * - Generates Allure report at the end of execution (onExecutionFinish)
 * - Cleans previous Allure results at start
 */
public class TestListener implements ITestListener, IExecutionListener {

    @Override
    public void onExecutionStart() {
        // Clean previous allure-results before starting tests
        String allureFolderName = Const.ALLURE_RESULTS_DIR;
      //  String logsFolderName = "logs";

        // Avoid using Logger before cleanup because log4j keeps the file handle open
        System.out.println("[TestListener] Cleaning previous Allure results");
        try {
            // delete recursively
            FileUtils.deleteDirectory(new File(allureFolderName));
           // FileUtils.deleteDirectory(new File(logsFolderName));
            logWithAllure("✅ Cleaned previous Allure results");
        } catch (IOException e) {
            logError("Failed to clean previous Allure results", e);
        }
    }


    @Override
    public void onExecutionFinish() {


        logWithAllure("🔄 Generating Allure report (results dir: " + Const.ALLURE_RESULTS_DIR + ")");
        GenerateAllureReport();
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
                ? new String[]{"cmd", "/c", cmd}
                : new String[]{"/bin/sh", "-c", cmd};
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




}
