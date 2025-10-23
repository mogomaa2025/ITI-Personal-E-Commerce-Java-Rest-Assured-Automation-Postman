package com.gecom.utils;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class JsonUtility {

    // Save or update a value in a JSON file
    public static void saveToken(String key, String value, String filePath) throws Exception {
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

    // Read a value from a JSON file
    public static String getToken(String key, String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) return null;

        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        if (content.trim().isEmpty()) return null;

        JSONObject json = new JSONObject(content);
        return json.optString(key, null);
    }




    // Get the last user ID from a response
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
}
