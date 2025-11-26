package com.gecom.utils;

import org.apache.logging.log4j.LogManager;

/**
 * A simple logger utility that wraps Log4j2 to provide a consistent logging interface for the test framework.
 * This class includes methods for logging at different levels (info, warn, debug, error) and for logging test steps.
 */
public final class Logger {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Logger.class);

    private Logger() {} //not used constructor

    /**
     * Logs an informational message.
     *
     * @param message The message to be logged.
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to be logged.
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Logs a debug message.
     *
     * @param message The message to be logged.
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * Logs an error message with a throwable.
     *
     * @param message   The message to be logged.
     * @param throwable The throwable to be logged.
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Logs a step in the test execution.
     *
     * @param stepName The name of the step to be logged.
     */
    public static void step(String stepName) {
        logger.info("STEP: " + stepName);
    }
}
