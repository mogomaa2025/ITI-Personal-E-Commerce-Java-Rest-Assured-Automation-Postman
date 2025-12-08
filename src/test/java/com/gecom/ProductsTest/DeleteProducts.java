package com.gecom.ProductsTest;

import static com.gecom.utils.Base.*;
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
public class DeleteProducts {

        @Test(description = "TC-PROD-013: Verify admin can delete product", groups = {
                        "Valid-Products-Test", "valid" })
        public void testAdminCanDeleteProduct() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                productId = (Integer) JsonUtility.getValue("product_id", IDS_FILE_PATH);
                Assert.assertNotNull(productId, "Product ID not found");
                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/" + productId, adminToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Product deleted successfully",
                                "message is 'Product deleted successfully'");
                Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
                Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "data has name");
                Response getResponse = ApiUtils.getRequest(BASE_URL + "/products/" + productId);
                Assert.assertEquals(getResponse.getStatusCode(), 404, "Subsequent GET returns 404");
        }

        @Test(description = "TC-PROD-014: Verify delete product fails for non-existent product", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testDeleteProductFailsForNonExistent() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");
                Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/99999", adminToken);
                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                Assert.assertEquals(response.jsonPath().getString("error"), "Product not found",
                                "error is 'Product not found'");
        }

}
