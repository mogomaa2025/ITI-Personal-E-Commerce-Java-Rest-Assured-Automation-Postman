package com.gecom.ProductsTest;

import static com.gecom.utils.Base.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "ProductsTest")
@Severity(SeverityLevel.CRITICAL)
public class CreateProducts {

        @Test(description = "TC-PROD-006: Verify admin can create product", groups = {
                        "Valid-Products-Test", "valid" })
        public void testAdminCanCreateProduct() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                Map<String, Object> body = new HashMap<>();
                body.put("name", PRODUCT_NAME);
                body.put("description", DESCRIPTION);
                body.put("price", PRICE);
                body.put("category", CATEGORY);
                body.put("stock", STOCK);
                body.put("image_url", IMAGE_URL);

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Product created successfully",
                                "message is 'Product created successfully'");
                Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
                Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "data has name");
                Assert.assertTrue(response.jsonPath().get("data.description") instanceof String,
                                "data has DESCRIPTION");
                Assert.assertTrue(response.jsonPath().get("data.price") instanceof Number, "data has PRICE");
                Assert.assertTrue(response.jsonPath().get("data.category") instanceof String, "data has CATEGORY");
                Assert.assertTrue(response.jsonPath().get("data.stock") instanceof Integer, "data has STOCK");
                Assert.assertTrue(response.jsonPath().get("data.image_url") instanceof String, "data has IMAGE_URL");
                Assert.assertNotNull(response.jsonPath().get("data.created_at"), "data has created_at");
                Assert.assertEquals(response.jsonPath().getString("data.name"), PRODUCT_NAME, "All fields match input");
                Assert.assertEquals(response.jsonPath().getDouble("data.price"), PRICE, "All fields match input");
                productId = response.jsonPath().getInt("data.id");
                JsonUtility.saveValue("product_id", productId, IDS_FILE_PATH);
        }

        @Test(description = "TC-PROD-007: Verify create product fails without required fields", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testCreateProductFailsWithoutRequiredFields() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                Map<String, Object> body = new HashMap<>();
                body.put("name", "Test");

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products", adminToken, body);

                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Missing required fields"),
                                "error indicates missing fields");
        }

        @Test(description = "TC-PROD-008: Verify create product fails without admin", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testCreateProductFailsWithoutAdmin() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");
                Map<String, Object> body = new HashMap<>();
                body.put("name", PRODUCT_NAME);
                body.put("description", DESCRIPTION);
                body.put("price", PRICE);
                body.put("category", CATEGORY);
                body.put("stock", STOCK);
                body.put("image_url", IMAGE_URL);

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products", userToken, body);

                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                                "error indicates admin auth needed");
        }
}
