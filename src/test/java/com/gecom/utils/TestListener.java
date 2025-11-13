package com.gecom.utils;

import org.apache.commons.io.FileUtils;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * - Logs test start/success/failure/skip to stdout
 * - Generates Allure report at the end of execution (onExecutionFinish)
 * - Cleans previous Allure results at start
 */
public class TestListener implements ITestListener, IExecutionListener {

    @Override
    public void onExecutionStart() {
        // Clean previous allure-results before starting tests
        String folderName = Const.ALLURE_RESULTS_DIR;

            System.out.println("Cleaning previous Allure results: " + folderName);
            try {
                // delete recursively
                FileUtils.deleteDirectory(new File(folderName));
                System.out.println("✅ Cleaned previous Allure results");
            } catch (IOException e) {
                System.err.println("Failed to clean previous Allure results: " + e.getMessage());
            }
        }


    @Override
    public void onExecutionFinish() {


        System.out.println("🔄 Generating Allure report (results dir: " + Const.ALLURE_RESULTS_DIR + ")");
        // Only generate the report — do NOT open it (pass false)
        GenerateAllureReport();
    }

    @Override
    public void onTestStart(ITestResult result) {
        Logger.info("START: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        Logger.info("PASS: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Logger.info("FAIL: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Logger.info("SKIP: " + result.getMethod().getQualifiedName());
    }



    // java
    private static void GenerateAllureReport() {
        String resultsDir = "target/allure-results";
        String cleanFlag = "--clean";
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        String allureHome = System.getenv("ALLURE_HOME");
        String allureExecutable;
        if (allureHome != null && !allureHome.isBlank()) {
            allureExecutable = allureHome + java.io.File.separator + "bin" + java.io.File.separator + (isWindows ? "allure.bat" : "allure");
        } else {
            allureExecutable = "allure"; // fallback to PATH
        }

        java.util.List<String> command = new java.util.ArrayList<>();
        if (isWindows) {
            command.add("cmd");
            command.add("/c");
            command.add(allureExecutable);
        } else {
            command.add(allureExecutable);
        }
        command.add("generate");
        command.add(resultsDir);
        command.add(cleanFlag);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            int exit = p.waitFor();
            if (exit != 0) {
                System.err.println("Allure generation failed with exit code: " + exit);
            } else {
                System.out.println("Allure report generated successfully.");
            }
        } catch (Exception e) {
            System.err.println("Failed to execute Allure command. Ensure `ALLURE_HOME` or `allure` on PATH is configured. Error: " + e.getMessage());
        }
    }




}
