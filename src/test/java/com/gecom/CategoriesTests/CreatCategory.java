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

import java.util.HashMap;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "CategoriesTest")
@Severity(SeverityLevel.CRITICAL)
public class CreatCategory {

        @Test(description = "TC-CAT-001: Verify admin can create CATEGORY", groups = { "Valid-Categories-Test",
                        "valid" })
        public void testAdminCanCreateCategory() throws Exception {
                adminToken = GetAdminToken();
                Assert.assertNotNull(adminToken, "Admin token not found");
                Map<String, Object> body = new HashMap<>();
                body.put("name", "TestCategory");
                body.put("description", "Test Description");

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/categories", adminToken, body);

                Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

                Assert.assertEquals(response.jsonPath().getString("message"), "Category created successfully",
                                "message is 'Category created successfully'");

                Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
                Assert.assertEquals(response.jsonPath().getString("data.name"), "TestCategory",
                                "data.name matches input");
                Assert.assertEquals(response.jsonPath().getString("data.description"), "Test Description",
                                "data.description matches input");
                String createdAt = response.jsonPath().getString("data.created_at");
                Assert.assertTrue(createdAt != null && !createdAt.isEmpty(), "data has created_at");

                setCategoryID(response);
        }

        @Test(description = "TC-CAT-002: Verify create CATEGORY fails without admin", groups = {
                        "Invalid-Categories-Test", "invalid" }, dependsOnMethods = "testAdminCanCreateCategory")
        public void testCreateCategoryFailsWithoutAdmin() throws Exception {
                userToken = GetUserToken();
                Assert.assertNotNull(userToken, "User token not found");

                Map<String, Object> body = new HashMap<>();
                body.put("name", "TestCategory");
                body.put("description", "Test Category");

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/categories", userToken, body);

                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                                "error indicates admin required");
        }

}
