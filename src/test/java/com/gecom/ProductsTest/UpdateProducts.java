package com.gecom.ProductsTest;

import static com.gecom.utils.Const.*;
import java.util.HashMap;
import java.util.List;
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
public class UpdateProducts {

        @Test(description = "TC-PROD-009: Verify admin can update product", groups = {
                        "Valid-Products-Test", "valid" })
        public void testAdminCanUpdateProduct() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                productId = (Integer) JsonUtility.getValue("product_id", IDS_FILE_PATH);
                Assert.assertNotNull(productId, "Product ID not found");
                Map<String, Object> body = new HashMap<>();
                body.put("description", UPDATED_DESCRIPTION);
                body.put("price", UPDATED_PRICE);
                body.put("stock", UPDATED_STOCK);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/" + productId, adminToken, body);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Product updated successfully",
                                "message is 'Product updated successfully'");
                Assert.assertEquals(response.jsonPath().getString("data.description"), UPDATED_DESCRIPTION,
                                "data reflects updates");
                Assert.assertEquals(response.jsonPath().getDouble("data.price"), UPDATED_PRICE,
                                "data reflects updates");
                Assert.assertNotNull(response.jsonPath().get("data.updated_at"), "updated_at present");
        }

        @Test(description = "TC-PROD-010: Verify update product fails for non-existent product", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testUpdateProductFailsForNonExistent() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                Map<String, Object> body = new HashMap<>();
                body.put("description", UPDATED_DESCRIPTION);
                body.put("price", UPDATED_PRICE);
                body.put("stock", UPDATED_STOCK);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/99999", adminToken, body);

                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Product not found"),
                                "error indicates non existing product");
        }

        @Test(description = "TC-PROD-021: Verify admin can update product STOCK", groups = {
                        "Valid-Products-Test", "valid" })
        public void testAdminCanUpdateProductStock() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, Object> body = new HashMap<>();
                body.put("product_id", INVENTORY_PRODUCT_ID);
                body.put("stock", INVENTORY_STOCK);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/inventory/update-stock", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

                Assert.assertEquals(response.jsonPath().getString("message"), "Stock updated successfully",
                                "message is 'Stock updated successfully'");

                Assert.assertTrue(response.jsonPath().get("data.product_id") instanceof Integer, "data has product_id");
                Assert.assertTrue(response.jsonPath().get("data.old_stock") instanceof Integer, "data has old_stock");
                Assert.assertTrue(response.jsonPath().get("data.new_stock") instanceof Integer, "data has new_stock");

                Assert.assertEquals(response.jsonPath().getInt("data.new_stock"), INVENTORY_STOCK,
                                "new_stock is " + INVENTORY_STOCK + " ");
        }

        @Test(description = "TC-PROD-022: Verify update STOCK fails for non-existent product", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testUpdateStockFailsForNonExistentProduct() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, Object> body = new HashMap<>();
                body.put("product_id", 99999);
                body.put("stock", 10);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/inventory/update-stock", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        }

        @Test(description = "TC-PROD-023: Verify admin can bulk update products", groups = {
                        "Valid-Products-Test", "valid" })
        public void testAdminCanBulkUpdateProducts() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                /*
                 * send json body like this
                 * 
                 * {
                 * "updates": [
                 * {
                 * "product_id": 1,
                 * "price": 899.99,
                 * "stock": 75
                 * },
                 * {
                 * "product_id": 2,
                 * "price": 24.99,
                 * "stock": 200
                 * }
                 * ]
                 * }
                 * 
                 */
                Map<String, Object> body = new HashMap<>();
                List<Map<String, Object>> updates = List.of(
                                Map.of("product_id", BULK_UPDATE_PRODUCT_ID1, "price", BULK_UPDATE_PRICE1, "stock",
                                                BULK_UPDATE_STOCK1),
                                Map.of("product_id", BULK_UPDATE_PRODUCT_ID2, "price", BULK_UPDATE_PRICE2, "stock",
                                                BULK_UPDATE_STOCK2));
                body.put("updates", updates);
                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/bulk-update", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                String message = response.jsonPath().getString("message");
                Assert.assertTrue(message != null && message.contains("2 products updated"),
                                "message contains '2 products updated'");
                Assert.assertEquals(response.jsonPath().getInt("updated_count"), 2, "updated_count is 2");
        }

        @Test(description = "TC-PROD-024: Verify bulk update with partial failures", groups = {
                        "Valid-Products-Test", "valid" })
        public void testBulkUpdateWithPartialFailures() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, Object> body = new HashMap<>();
                List<Map<String, Object>> updates = List.of(
                                Map.of("product_id", BULK_UPDATE_PRODUCT_ID1, "price", BULK_UPDATE_PRICE1),
                                Map.of("product_id", 99999, "price", BULK_UPDATE_PRICE2));
                body.put("updates", updates);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/bulk-update", adminToken, body);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                int updatedCount = response.jsonPath().getInt("updated_count");
                Assert.assertTrue(updatedCount >= 1, "updated_count reflects successful updates");
        }

}
