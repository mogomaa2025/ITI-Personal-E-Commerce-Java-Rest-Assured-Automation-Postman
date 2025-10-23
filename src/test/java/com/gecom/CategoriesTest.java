package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.restassured.response.Response;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static com.gecom.utils.Const.*;

@Listeners({AllureTestNg.class})
public class CategoriesTest {


    @Test(groups = "CategoriesTest")
    public void testCreateCategory() throws Exception {
        Allure.step("Starting testCreateCategory...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testCreateCategory");

        Map<String, String> body = new HashMap<>();
        body.put("name", "Accessories");
        body.put("description", "Accessory items");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/categories", adminToken, body);
        Allure.step("Create category API response status code: " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");
        Assert.assertEquals(response.jsonPath().getString("message"), "Category created successfully");

        // Save the created category ID for later tests
        int categoryId = JsonUtility.getLastUserId(response);
        Allure.step("Created category with ID: " + categoryId);
        JsonUtility.saveToken("category_id", String.valueOf(categoryId), IDS_FILE_PATH);
        Allure.step("testCreateCategory finished successfully.");
    }

    @Test(groups = "CategoriesTest",  dependsOnMethods = "testCreateCategory")
    public void testGetCategories() {
        Allure.step("Starting testGetCategories...");
        Response response = ApiUtils.getRequest(BASE_URL + "/categories");
        Allure.step("Get categories API response status code: " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        response.then().assertThat().body("data.name", everyItem(not(emptyOrNullString())));
        response.then().assertThat().body("data.description", everyItem(not(emptyOrNullString())));

        int count = response.jsonPath().getInt("count");
        int dataSize = response.jsonPath().getList("data").size();
        Assert.assertEquals(count, dataSize, "Count should match the number of categories");

        List<String> categoryNames = response.jsonPath().getList("data.name");
        Map<String, List<String>> data = new HashMap<>();
        data.put("category_names", categoryNames);


        // extra step to save category names to a file
        try {
            JsonUtility.saveData(IDS_FILE_PATH, data);
            Allure.step("Successfully saved category names to " + IDS_FILE_PATH);
        } catch (IOException e) {
            Allure.step("Unable to save category names to " + IDS_FILE_PATH);
            Assert.fail("Unable to save category names to " + IDS_FILE_PATH, e);
        }

        Allure.step("testGetCategories finished successfully.");
    }

    @Test(groups = "CategoriesTest",  dependsOnMethods = "testGetCategories")
    public void testDeleteCategory() throws Exception {
        Allure.step("Starting testDeleteCategory...");

        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String categoryId = JsonUtility.getToken("category_id", IDS_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");
        Assert.assertNotNull(categoryId, "Category ID not found for testDeleteCategory");

        Allure.step("Deleting category with ID: " + categoryId);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/categories/" + categoryId, adminToken);
        Allure.step("Delete category API response status code: " + response.getStatusCode());
        Assert.assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 204, "Should return 200 OK or 204 No Content");

        // If 200 OK, verify the success message
        Assert.assertEquals(response.jsonPath().getString("message"), "Category deleted successfully");

        Allure.step("testDeleteCategory finished successfully.");
    }


}
