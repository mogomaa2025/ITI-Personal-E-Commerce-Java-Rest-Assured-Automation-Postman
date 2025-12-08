package com.gecom.utils;

import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtility {

    // file
    public static void saveValue(String key, Object value, String filePath) throws Exception {

        JSONObject json = new JSONObject();
        File file = new File(filePath);

        if (file.exists()) {
            String content = new String(
                    Files.readAllBytes(Paths.get(filePath)),
                    StandardCharsets.UTF_8);

            if (!content.isEmpty()) {
                json = new JSONObject(content);
            }
        }

        json.put(key, value);
        Files.write(
                Paths.get(filePath),
                json.toString(4).getBytes(StandardCharsets.UTF_8));
    }

    // Get any value (String, int, boolean, JSONObject, JSONArray...) from JSON file
    public static Object getValue(String key, String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        String content = new String(
                Files.readAllBytes(Paths.get(filePath)),
                StandardCharsets.UTF_8);

        if (content.trim().isEmpty()) {
            return null;
        }

        JSONObject json = new JSONObject(content);

        return json.opt(key);
    }

    // Getter <<< Wrappers >>> for specific types //
    // wrapper method to get JSONObject value from JSON file
    // we can use getValue with type cast instead
    @Deprecated
    public static JSONObject getJSONObject(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value instanceof JSONObject ? (JSONObject) value : null;
    }

}
