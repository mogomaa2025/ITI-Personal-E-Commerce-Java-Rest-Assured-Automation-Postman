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

public class JsonUtility {



    // Save or update any value (String, int, List, Map, JSONObject...) in a JSON file
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

    // Get any value (String, int, boolean, JSONObject, JSONArray...) from JSON file
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

    // wrapper method to get String value from JSON file
    public static String getJSONString(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value != null ? value.toString() : null;
    }

    // wrapper method to get Integer value from JSON file
    public static Integer getJSONInt(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    // wrapper method to get Boolean value from JSON file
    public static Boolean getJSONBoolean(String key, String filePath) throws Exception {
        Object value = getValue(key, filePath);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    // wrapper method to get JSONObject value from JSON file
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
