package com.gecom.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import io.qameta.allure.Allure;


/**
 * A listener class that handles TestNG events to provide logging and reporting functionality.
 * This class is responsible for:
 * - Logging the start, success, failure, and skipping of tests.
 * - Generating an Allure report at the end of the test execution.
 * - Cleaning up previous Allure results before a new test run.
 */
public class TestListener implements ITestListener, IExecutionListener {

    /**
     * This method is invoked before the TestNG execution starts.
     * It cleans up the Allure results directory to ensure a fresh report is generated.
     */
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


    /**
     * This method is invoked after the TestNG execution finishes.
     * It generates the Allure report.
     */
    @Override
    public void onExecutionFinish() {


        logWithAllure("🔄 Generating Allure report (results dir: " + Const.ALLURE_RESULTS_DIR + ")");
        GenerateAllureReport();
    }

    /**
     * This method is invoked when a test starts.
     *
     * @param result The result of the test.
     */
    @Override
    public void onTestStart(ITestResult result) {
        logWithAllure("START: " + result.getMethod().getQualifiedName());
    }

    /**
     * This method is invoked when a test succeeds.
     *
     * @param result The result of the test.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        logWithAllure("PASS: " + result.getMethod().getQualifiedName());
    }

    /**
     * This method is invoked when a test fails.
     *
     * @param result The result of the test.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        logWithAllure("FAIL: " + result.getMethod().getQualifiedName());
    }

    /**
     * This method is invoked when a test is skipped.
     *
     * @param result The result of the test.
     */
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
