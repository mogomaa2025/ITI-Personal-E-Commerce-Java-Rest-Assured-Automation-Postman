package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
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
@Test(groups = "ProductsTest")
public class ProductsTest {



    @Test
    public void testListProducts() throws Exception {
        Allure.step("Starting testListProducts...");

        Response response = ApiUtils.getRequest(BASE_URL + "/products?category=Electronics&search=&page=1&per_page=10");
        Allure.step("Get products API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Allure.step("Status code validated: 200");



        List<Map<String, Object>> dataList = response.jsonPath().getList("data");
        // Validate JSON schema: price (double) and stock (integer)
        for (Map<String, Object> product : dataList) {
            Assert.assertTrue(product.get("price") instanceof Number, "Price should be a number");
            Assert.assertTrue(product.get("stock") instanceof Integer, "Stock should be an integer");
        }
        Allure.step("JSON schema validated: price and stock types");


        Allure.step("testListProducts finished successfully.");
    }

    @Test(dependsOnMethods = "testListProducts")
    public void testCreateProductWithAdminToken() throws Exception {
        Allure.step("Starting testCreateProductWithAdminToken...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "new product1");
        body.put("description", "new product description1");
        body.put("price", 29.99);
        body.put("category", "Electronics");
        body.put("stock", 20);
        body.put("image_url", "https://ci.suez.edu.eg/wp-content/uploads/2022/08/iti-logo.png");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products", adminToken, body);
        Allure.step("Create product API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");

        // Validate JSON schema
        Assert.assertEquals(response.jsonPath().getDouble("data.price"), 29.99);
        Assert.assertEquals(response.jsonPath().getInt("data.stock"), 20);
        Allure.step("JSON schema validated: price (double) and stock (integer)");

        // Save product_id to ids.json
        productId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("product_id", productId.toString(), IDS_FILE_PATH);
        Allure.step("Product ID saved: " + productId);

        Allure.step("testCreateProductWithAdminToken finished successfully.");
    }

    @Test(dependsOnMethods = "testCreateProductWithAdminToken")
    public void testUpdateProduct() throws Exception {
        Allure.step("Starting testUpdateProduct...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");
        String productIdStr = JsonUtility.getToken("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productIdStr, "Product ID not found");
        Allure.step("Updating product with ID: " + productIdStr);

        Map<String, Object> body = new HashMap<>();
        body.put("description", "Updated description");
        body.put("price", 34.99);
        body.put("stock", 75);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/" + productIdStr, adminToken, body);
        Allure.step("Update product API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        // Validate JSON schema
        Assert.assertEquals(response.jsonPath().getDouble("data.price"), 34.99);
        Assert.assertEquals(response.jsonPath().getInt("data.stock"), 75);
        Allure.step("JSON schema validated: price and stock match request body");

        Allure.step("testUpdateProduct finished successfully.");
    }

    @Test(dependsOnMethods = "testUpdateProduct")
    public void testGetProductById() throws Exception {
        Allure.step("Starting testGetProductById...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String productIdStr = JsonUtility.getToken("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productIdStr, "Product ID not found");
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");
        Allure.step("Getting product with ID: " + productIdStr);

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/products/" + productIdStr, adminToken);
        Allure.step("Get product API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.asString().contains(productIdStr), "Response should contain product ID");
        Allure.step("testGetProductById finished successfully.");

        // Validate JSON schema
        Assert.assertEquals(response.jsonPath().getDouble("data.price"), 34.99);
        Assert.assertEquals(response.jsonPath().getInt("data.stock"), 75);
        Allure.step("JSON schema validated: price and stock match request body");
        Allure.step("testUpdateProduct finished successfully.");
    }

    @Test(dependsOnMethods = "testGetProductById")
    public void testDeleteProduct() throws Exception {
        Allure.step("Starting testDeleteProduct...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String productIdStr = JsonUtility.getToken("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productIdStr, "Product ID not found");
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");

        Allure.step("Deleting product with ID: " + productIdStr);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/" + productIdStr, adminToken);
        Allure.step("Delete product API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product deleted successfully");
        Allure.step("testDeleteProduct finished successfully.");
    }

    @Test(dependsOnMethods = "testDeleteProduct")
    public void testSearchProducts() throws Exception {
        Allure.step("Starting testSearchProducts...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");

        // Use "and" from the saved product names
        String searchQuery = "and";
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/products/search?q=" + searchQuery, adminToken);
        Allure.step("Search products API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");


        //count validation
        int count = response.jsonPath().getInt("count");
        List<Map<String, Object>> dataList = response.jsonPath().getList("data");
        Assert.assertEquals(dataList.size(), count, "Number of Search Results should match count");
        Allure.step("Validated count matches number of Search Results: " + count);

        Allure.step("testSearchProducts finished successfully.");
    }

    @Test(dependsOnMethods = "testSearchProducts")
    public void testGetProductsByCategory() throws Exception {
        Allure.step("Starting testGetProductsByCategory...");

        String category = "Electronics";
        Response response = ApiUtils.getRequest(BASE_URL + "/products/category/" + category);
        Allure.step("Get products by category API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        //count validation
        int count = response.jsonPath().getInt("count");
        List<Map<String, Object>> dataList = response.jsonPath().getList("data");
        Assert.assertEquals(dataList.size(), count, "Number of products should match count");
        Allure.step("Validated count matches number of products: " + count);


        Allure.step("testGetProductsByCategory finished successfully.");
    }

    @Test(dependsOnMethods = "testGetProductsByCategory")
    public void testGetLowStockProducts() throws Exception {
        Allure.step("Starting testGetLowStockProducts...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH); // Retrieve admin token
        Assert.assertNotNull(adminToken, "Admin token not found"); // Assert token is not null

        int threshold = 11; // Define low stock threshold
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/inventory/low-stock?threshold=" + threshold, adminToken); // Use authenticated request
        Allure.step("Get low stock products API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        //count validation
        int count = response.jsonPath().getInt("count");
        List<Map<String, Object>> dataList = response.jsonPath().getList("data");
        Assert.assertEquals(dataList.size(), count, "Number of products should match count");
        Allure.step("Validated count matches number of products: " + count);

        Allure.step("testGetLowStockProducts finished successfully.");



    }


    @Test(dependsOnMethods = "testGetLowStockProducts")
    public void testUpdateStockSingleProduct() throws Exception {
        Allure.step("Starting testUpdateStockSingleProduct...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 4);
        body.put("stock", 40);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/inventory/update-stock", adminToken, body);
        Allure.step("Update stock API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        // Validate JSON schema
        Assert.assertEquals(response.jsonPath().getInt("data.product_id"), 4);
        Assert.assertEquals(response.jsonPath().getInt("data.new_stock"), 40);
        Assert.assertEquals(response.jsonPath().getString("message"), "Stock updated successfully");
        Allure.step("Stock updated successfully for product ID 4");

        Allure.step("testUpdateStockSingleProduct finished successfully.");
    }


    @Test(dependsOnMethods = "testUpdateStockSingleProduct")
    public void testBulkUpdateStock() throws Exception {
        Allure.step("Starting testBulkUpdateStock...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");

    // body
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 4);
        body.put("price", 24.98);
        body.put("stock", 60);


        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/inventory/update-stock", adminToken, body);

        Allure.step("Bulk update stock API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");


        Allure.step("Bulk update completed: " + response.jsonPath().getString("message"));

        Allure.step("testBulkUpdateStock finished successfully.");
    }

    @Test(dependsOnMethods = "testBulkUpdateStock")
    public void testExportProducts() throws Exception {
        Allure.step("Starting testExportProducts...");

        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/export/products",adminToken);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");

        Allure.step("Export products API response status code: " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        // Validate JSON schema
        Allure.step("JSON schema validated: price and stock match request body");


        //count validation
        int totalProducts = response.jsonPath().getInt("data.total_products");
        List<Map<String, Object>> productsList = response.jsonPath().getList("data.products");
        Assert.assertEquals(productsList.size(), totalProducts, "Total products should match list size");


        Allure.step("Validated total_products matches number of products: " + totalProducts);
        // loop through each product and validate fields
        for (Map<String, Object> product : productsList) {
            String category = (String) product.get("category");
            String description = (String) product.get("description");
            String name = (String) product.get("name");

            //empty validation
            Assert.assertFalse(category.isEmpty(), "Category should have text");
            Assert.assertFalse(description.isEmpty(), "Description should have text");
            Assert.assertFalse(name.isEmpty(), "Name should have text");

            // Data Type validation
            Assert.assertTrue(product.get("price") instanceof Number, "Price should be a double");
            Assert.assertTrue(product.get("stock") instanceof Integer, "Stock should be an integer");
        }
        Allure.step("JSON schema validated for all exported products");

        Allure.step("testExportProducts finished successfully.");
    }


}






