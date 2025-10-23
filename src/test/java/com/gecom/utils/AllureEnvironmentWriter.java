package com.gecom.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class AllureEnvironmentWriter {

    // create this property file in the allure-results directory [mandatory]
    private static final Path ENVIRONMENT_FILE = Paths.get("target", "allure-results", "environment.properties");

    private AllureEnvironmentWriter() {
        throw new IllegalStateException("Utility class");
    }

    // Method to write environment properties to allure-results/environment.properties
    public static void write(Map<String, String> environment) {
        if (environment == null || environment.isEmpty()) {
            return;
        }

        try {
            Files.createDirectories(ENVIRONMENT_FILE.getParent());
            try (var writer = Files.newBufferedWriter(ENVIRONMENT_FILE, StandardCharsets.UTF_8)) {
                for (Map.Entry<String, String> entry : environment.entrySet()) {
                    writer.write(entry.getKey() + "=" + (entry.getValue() == null ? "" : entry.getValue()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to write Allure environment properties", e);
        }
    }


}
