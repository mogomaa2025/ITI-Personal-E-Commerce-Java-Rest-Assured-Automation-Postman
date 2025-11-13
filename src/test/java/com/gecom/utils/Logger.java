package com.gecom.utils;

import org.apache.logging.log4j.LogManager;

/**
 * Simple logger for main package classes
 * Uses log4j2 for logging. Kept minimal and focused on used APIs.
 */
public final class Logger {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Logger.class);

    private Logger() {} //not used constructor

    /**
     * Log info message
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Log warning message
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Log debug message
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * Log error message with throwable
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Create a step in logs
     */
    public static void step(String stepName) {
        logger.info("STEP: " + stepName);
    }
}
