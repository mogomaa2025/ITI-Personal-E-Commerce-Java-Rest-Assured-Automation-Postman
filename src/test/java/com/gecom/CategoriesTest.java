package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gecom.utils.Const.*;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "CategoriesTest")
@Severity(SeverityLevel.CRITICAL)
public class CategoriesTest {

    @Test(description = "TC-CAT-001: Verify admin can create CATEGORY")
    public void testAdminCanCreateCategory() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send POST with category data");
        Map<String, Object> body = new HashMap<>();
        body.put("name", "TestCategory");
        body.put("description", "Test Description");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/categories", adminToken, body);

        Allure.step("Verify created");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Validate response");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Category created successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Category created successfully", "message is 'Category created successfully'");

        Allure.step("Verify data.id is number");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");

        Allure.step("Verify data.name matches input");
        Assert.assertEquals(response.jsonPath().getString("data.name"), "TestCategory", "data.name matches input");

        Allure.step("Verify data.description matches input");
        Assert.assertEquals(response.jsonPath().getString("data.description"), "Test Description", "data.description matches input");

        Allure.step("Verify data.created_at is timestamp");
        String createdAt = response.jsonPath().getString("data.created_at");
        Assert.assertTrue(createdAt != null && !createdAt.isEmpty(), "data has created_at");

        Allure.step("Save CATEGORY ID");
        categoryId = response.jsonPath().getInt("data.id");
        JsonUtility.saveValue("category_id", categoryId, IDS_FILE_PATH);
    }

    @Test(description = "TC-CAT-002: Verify create CATEGORY fails without admin", dependsOnMethods = "testAdminCanCreateCategory")
    public void testCreateCategoryFailsWithoutAdmin() throws Exception {
        Allure.step("Login as regular user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST");
        Map<String, Object> body = new HashMap<>();
        body.put("name", "TestCategory");
        body.put("description", "Test Category");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/categories", userToken, body);

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates admin required");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"), "error indicates admin required");
    }

    @Test(description = "TC-CAT-003: Verify user can view all categories", dependsOnMethods = "testCreateCategoryFailsWithoutAdmin")
    public void testUserCanViewAllCategories() throws Exception {
        Allure.step("Send GET to categories");
        Response response = ApiUtils.getRequest(BASE_URL + "/categories");

        Allure.step("Verify categories returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify count matches data length");
        int count = response.jsonPath().getInt("count");
        int dataSize = response.jsonPath().getList("data").size();
        Assert.assertEquals(count, dataSize, "count matches data length");

        Allure.step("Verify at least one CATEGORY exists");
        Assert.assertTrue(dataSize > 0, "At least one CATEGORY exists");

        Allure.step("Verify each category has id/name/description/created_at");
        List<Map<String, Object>> categories = response.jsonPath().getList("data");
        for (Map<String, Object> category : categories) {
            Assert.assertTrue(category.get("id") instanceof Integer, "Each category has id");
            Assert.assertTrue(category.get("name") instanceof String, "Each category has name");
            Assert.assertTrue(category.get("description") instanceof String, "Each category has description");
            Assert.assertTrue(category.get("created_at") instanceof String, "Each category has created_at");
        }

        Allure.step("Save Last Category id");
        categoryId = (Integer) categories.get(dataSize - 1).get("id");
        JsonUtility.saveValue("category_id", categoryId, IDS_FILE_PATH);

        Allure.step("Save all unique CATEGORY names for TC-PROD-017");
        List<String> categoryNames = response.jsonPath().getList("data.name");
        List<String> uniqueCategoryNames = categoryNames.stream().distinct().collect(java.util.stream.Collectors.toList());
        JsonUtility.saveValue("category_names", uniqueCategoryNames, IDS_FILE_PATH);
    }

    @Test(description = "TC-CAT-004: Verify admin can delete CATEGORY", dependsOnMethods = "testUserCanViewAllCategories")
    public void testAdminCanDeleteCategory() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Get test CATEGORY saved in previous test case");
        categoryId = JsonUtility.getJSONInt("category_id", IDS_FILE_PATH);
        Assert.assertNotNull(categoryId, "Category ID not found");

        Allure.step("Send DELETE");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/categories/" + categoryId, adminToken);

        Allure.step("Verify deleted");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Category deleted successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Category deleted successfully", "message is 'Category deleted successfully'");

        Allure.step("Verify data has id");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");

        Allure.step("Verify data has name");
        Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "data has name");
    }

    @Test(description = "TC-CAT-005: Verify delete CATEGORY fails for non-existent CATEGORY", dependsOnMethods = "testAdminCanDeleteCategory")
    public void testDeleteCategoryFailsForNonExistent() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send DELETE with invalid ID");
        categoryId = JsonUtility.getJSONInt("category_id", IDS_FILE_PATH);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/categories/" + categoryId, adminToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Category not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("not found"), "error is 'Category not found'");
    }

    @Test(description = "TC-CAT-006: Verify user cannot delete CATEGORY", dependsOnMethods = "testDeleteCategoryFailsForNonExistent")
    public void testUserCannotDeleteCategory() throws Exception {
        Allure.step("Login as regular user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get test CATEGORY saved in previous test case");
        categoryId = JsonUtility.getJSONInt("category_id", IDS_FILE_PATH);
        Assert.assertNotNull(categoryId, "Category ID not found");

        Allure.step("Send DELETE request");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/categories/" + categoryId, userToken);

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates admin required");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"), "error indicates admin required");
    }
}
