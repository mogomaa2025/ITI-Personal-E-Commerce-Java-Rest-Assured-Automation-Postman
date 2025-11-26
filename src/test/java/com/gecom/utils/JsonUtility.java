package com.gecom.utils;

import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility class for handling JSON data.
 * This class provides methods for saving, retrieving, and manipulating data in JSON files.
 */
public class JsonUtility {



    /**
     * Saves or updates a value in a JSON file.
     *
     * @param key      The key to save or update.
     * @param value    The value to be saved, which can be a String, int, List, Map, or JSONObject.
     * @param filePath The path to the JSON file.
     * @throws Exception If an error occurs while writing to the file.
     */
    public static void saveValue(String key, Object value, String filePath) throws Exception {

        JSONObject json = new JSONObject();
        File file = new File(filePath);

        // لو الملف موجود نقرأه
        if (file.exists()) {
            String content = new String(
                    Files.readAllBytes(Paths.get(filePath)),
                    StandardCharsets.UTF_8
            );

            if (!content.isEmpty()) {
                json = new JSONObject(content);
            }
        }

        // نضيف أو نحدّث القيمة
        json.put(key, value); // هنا يقدر يتعامل مع List<String> ويحوّلها JSONArray

        // نكتب في الملف
        Files.write(
                Paths.get(filePath),
                json.toString(4).getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Retrieves a value from a JSON file.
     *
     * @param key      The key of the value to retrieve.
     * @param filePath The path to the JSON file.
     * @return The retrieved value, which can be a String, int, boolean, JSONObject, or JSONArray.
     * @throws Exception If an error occurs while reading the file.
     */
    public static Object getValue(String key, String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        String content = new String(
                Files.readAllBytes(Paths.get(filePath)),
                StandardCharsets.UTF_8
        );

        if (content.trim().isEmpty()) {
            return null;
        }

        JSONObject json = new JSONObject(content);

        // opt بيرجع Object (ممكن يكون String, Integer, Boolean, JSONObject, JSONArray...)
        return json.opt(key);
    }


    // Getter <<< Wrappers >>> for specific types //

    /**
     * A wrapper method to get a String value from a JSON file.
     *
     * @param key      The key of the value to retrieve.
     * @param filePath The path to the JSON file.
     * @return The retrieved String value, or null if not found.
     * @throws Exception If an error occurs while reading the file.
     */
    public static String getJSONString(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value != null ? value.toString() : null;
    }

    /**
     * A wrapper method to get an Integer value from a JSON file.
     *
     * @param key      The key of the value to retrieve.
     * @param filePath The path to the JSON file.
     * @return The retrieved Integer value, or null if not found.
     * @throws Exception If an error occurs while reading the file.
     */
    public static Integer getJSONInt(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    /**
     * A wrapper method to get a Boolean value from a JSON file.
     *
     * @param key      The key of the value to retrieve.
     * @param filePath The path to the JSON file.
     * @return The retrieved Boolean value, or null if not found.
     * @throws Exception If an error occurs while reading the file.
     */
    public static Boolean getJSONBoolean(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    /**
     * A wrapper method to get a JSONObject from a JSON file.
     *
     * @param key      The key of the value to retrieve.
     * @param filePath The path to the JSON file.
     * @return The retrieved JSONObject, or null if not found.
     * @throws Exception If an error occurs while reading the file.
     */
    public static JSONObject getJSONObject(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value instanceof JSONObject ? (JSONObject) value : null;
    }









    // Deprecated METHODS xxxxxxxxxxxxxxx for education purpose only keep it


    // better use userId = (Integer) usersArray.get(usersArray.size() - 1).get("id"); // last one = size when talk about array index start with 0 so size()-1
    // Get the last user ID from a response
    //DEPRECATED use directly in test xxxxxxxxxxxxxxxxx
    @Deprecated
    public static int getLastUserId(Response response) {
        try {
            // First try to get as a list (for list responses)
            List<Map<String, Object>> data = response.jsonPath().getList("data");
            if (data != null && !data.isEmpty()) {
                Map<String, Object> lastUser = data.get(data.size() - 1);
                return (Integer) lastUser.get("id");
            }
        } catch (ClassCastException e) {
            // If it's not a list, it might be a single object
            Map<String, Object> data = response.jsonPath().getMap("data");
            if (data != null) {
                return (Integer) data.get("id");
            }
        }

        // If "data" doesn't exist or is empty, try to get directly from the response
        try {
            // Check if the response itself is a list
            List<Map<String, Object>> list = response.jsonPath().getList("$");
            if (list != null && !list.isEmpty()) {
                Map<String, Object> lastItem = list.get(list.size() - 1);
                return (Integer) lastItem.get("id");
            }
        } catch (Exception e) {
            // If not a list, try to get as a single object
            Map<String, Object> item = response.jsonPath().getMap("$");
            if (item != null) {
                return (Integer) item.get("id");
            }
        }

        Assert.fail("No ID found in the response");
        return -1; // Unreachable but required by compiler
    }



// Deprecated saveData method for Map<String, List<String>>
    @Deprecated
    public static void saveData(String filePath, Map<String, List<String>> data) throws IOException {
        File file = new File(filePath);
        JSONObject json;

        if (file.exists() && file.length() > 0) {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            json = new JSONObject(content);
        } else {
            json = new JSONObject();
        }

        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }

        Files.write(Paths.get(filePath), json.toString(4).getBytes(StandardCharsets.UTF_8));
    }

// Deprecated saveTokenold method for String key-value pairs
    @Deprecated
        public static void saveTokenold(String key, String value, String filePath) throws Exception {
        File file = new File(filePath);
        JSONObject json;

        if (file.exists() && file.length() > 0) {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            json = new JSONObject(content);
        } else {
            json = new JSONObject();
        }

        json.put(key, value);

        Files.write(Paths.get(filePath), json.toString(4).getBytes(StandardCharsets.UTF_8));
    }





}
