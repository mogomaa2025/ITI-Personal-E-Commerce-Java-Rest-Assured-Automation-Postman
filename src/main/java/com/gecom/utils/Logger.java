package com.gecom.utils;

import org.apache.logging.log4j.LogManager;


public final class Logger {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Logger.class);

    private Logger() {
    } // not used constructor


    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void step(String stepName) {
        logger.info("STEP: " + stepName);
    }
}
