package com.gecom.CategoriesTests;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;

import static com.gecom.utils.Base.*;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "CategoriesTest")
@Severity(SeverityLevel.CRITICAL)
public class DeleteCategory {

    @Test(description = "TC-CAT-004: Verify admin can delete CATEGORY", groups = { "Valid-Categories-Test", "valid" })
    public void testAdminCanDeleteCategory() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        categoryId = (Integer) JsonUtility.getValue("category_id", IDS_FILE_PATH);
        Assert.assertNotNull(categoryId, "Category ID not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/categories/" + categoryId, adminToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertEquals(response.jsonPath().getString("message"), "Category deleted successfully",
                "message is 'Category deleted successfully'");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "data has name");
    }

    @Test(description = "TC-CAT-005: Verify delete CATEGORY fails for non-existent CATEGORY", groups = {
            "Invalid-Categories-Test", "invalid" })
    public void testDeleteCategoryFailsForNonExistent() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        categoryId = (Integer) JsonUtility.getValue("category_id", IDS_FILE_PATH);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/categories/" + categoryId, adminToken);

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("not found"), "error is 'Category not found'");
    }

    @Test(description = "TC-CAT-006: Verify user cannot delete CATEGORY", groups = { "Invalid-Categories-Test",
            "invalid" })
    public void testUserCannotDeleteCategory() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        categoryId = (Integer) JsonUtility.getValue("category_id", IDS_FILE_PATH);
        Assert.assertNotNull(categoryId, "Category ID not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/categories/" + categoryId, userToken);

        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                "error indicates admin required");
    }
}
