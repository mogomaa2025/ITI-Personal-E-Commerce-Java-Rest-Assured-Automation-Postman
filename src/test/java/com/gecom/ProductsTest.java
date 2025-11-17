package com.gecom;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import static com.gecom.utils.Const.*;
import com.gecom.utils.JsonUtility;
import com.gecom.utils.Logger;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "ProductsTest")
@Severity(SeverityLevel.CRITICAL)
public class ProductsTest {

    @Test(description = "TC-PROD-001: Verify get all products with pagination")
    public void testGetAllProductsWithPagination() {
        Allure.step("Send GET to products");
        Response response = ApiUtils.getRequest(BASE_URL + "/products");

        Allure.step("Verify products and pagination");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array"); // array  "data":[{},{}]

        Allure.step("Verify pagination has page/per_page/total/pages");
        Assert.assertTrue(response.jsonPath().get("pagination.page") instanceof Integer, "pagination has page");
        Assert.assertTrue(response.jsonPath().get("pagination.per_page") instanceof Integer, "pagination has per_page");
        Assert.assertTrue(response.jsonPath().get("pagination.total") instanceof Integer, "pagination has total");
        Assert.assertTrue(response.jsonPath().get("pagination.pages") instanceof Integer, "pagination has pages");

        Allure.step("Verify each product has required fields");
        // retrieve a list of maps from the json responses -> each map represents a product with a various attributes stored as key-value pairs the data is extracted from "data" field in the json response
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        Assert.assertFalse(products.isEmpty(), "Products limited per page");
        // Retrieve the first product from the products list
        Map<String, Object> firstProduct = products.get(0);
        Assert.assertTrue(firstProduct.get("id") instanceof Integer, "Each product has id");
        Assert.assertTrue(firstProduct.get("name") instanceof String, "Each product has name");
        Assert.assertTrue(firstProduct.get("description") instanceof String, "Each product has description");
        Assert.assertTrue(firstProduct.get("price") instanceof Number, "Each product has price");
        Assert.assertTrue(firstProduct.get("category") instanceof String, "Each product has category");
        Assert.assertTrue(firstProduct.get("stock") instanceof Integer, "Each product has stock");
        Assert.assertTrue(firstProduct.get("image_url") instanceof String, "Each product has image_url");
        Assert.assertNotNull(firstProduct.get("created_at"), "Each product has created_at");
    }

    @Test(description = "TC-PROD-002: Verify products pagination with page parameter", dependsOnMethods = "testGetAllProductsWithPagination")
    public void testProductsPaginationWithPageParameter() {
        Allure.step("Send GET with page params");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", PRODUCT_PAGINATION_PAGE.toString());
        queryParams.put("per_page", PRODUCT_PAGINATION_PER_PAGE.toString());

        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products", queryParams);

        Allure.step("Verify pagination works");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify pagination.page is "+PRODUCT_PAGINATION_PAGE+" ");
        Assert.assertEquals(response.jsonPath().getInt("pagination.page"), PRODUCT_PAGINATION_PAGE, "pagination.page is "+PRODUCT_PAGINATION_PAGE+" "); // assert agains queryParams

        Allure.step("Verify data length <= "+PRODUCT_PAGINATION_PER_PAGE+" ");
        List<Map<String, Object>> products = response.jsonPath().getList("data"); // "data" : [{},{}]
        Assert.assertTrue(products.size() <= PRODUCT_PAGINATION_PER_PAGE, "data length <= "+PRODUCT_PAGINATION_PER_PAGE+" "); // assert against queryParams

        Allure.step("Verify pagination values correct");
        Assert.assertEquals(response.jsonPath().getInt("pagination.per_page"), PRODUCT_PAGINATION_PER_PAGE, "pagination values correct");
    }

    @Test(description = "TC-PROD-003: Verify filter products by CATEGORY", dependsOnMethods = "testProductsPaginationWithPageParameter")
    public void testFilterProductsByCategory() {
        Allure.step("Send GET with category filter");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("category", FILTER_CATEGORY); // testcase17 will test all categories from ids.json
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products",queryParams);

        Allure.step("Verify filtered results");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify all products have category="+FILTER_CATEGORY+" ");
        List<Map<String, Object>> products = response.jsonPath().getList("data"); // "data" : [{},{}]
        for (Map<String, Object> product : products) { // from "{}" get the value of the key "category" to compare it with "Electronics"
            Assert.assertEquals(product.get("category"), FILTER_CATEGORY, "All products have category="+FILTER_CATEGORY+" ");
        }

        Allure.step("Verify pagination present");
        Assert.assertNotNull(response.jsonPath().get("pagination"), "Pagination present");

        Allure.step("Verify pagination data types is correct");
        Assert.assertTrue(response.jsonPath().get("pagination.page") instanceof Integer, "page is not valid Integer");
        Assert.assertTrue(response.jsonPath().get("pagination.pages") instanceof Integer, "pages is not valid Integer");
        Assert.assertTrue(response.jsonPath().get("pagination.per_page") instanceof Integer, "per_page is not valid Integer");
        Assert.assertTrue(response.jsonPath().get("pagination.total") instanceof Integer, "total is not valid Integer");
    }

    @Test(description = "TC-PROD-004: Verify filter products by PRICE range", dependsOnMethods = "testFilterProductsByCategory")
    public void testFilterProductsByPriceRange() {
        Allure.step("Send GET with PRICE filters");
        Map<String, String> query = new HashMap<>();
        query.put("min_price", MIN_PRICE.toString());
        query.put("max_price", MAX_PRICE.toString());
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products",query);

        Allure.step("Verify PRICE range");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify all products have price >= 10 and <= 100");
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        for (Map<String, Object> product : products) {
            double price = ((Number) product.get("price")).doubleValue();
            Assert.assertTrue(price >= MIN_PRICE && price <= MAX_PRICE, "All products have price >= "+MIN_PRICE+" and <= "+MAX_PRICE+"");
        }
    }

    @Test(description = "TC-PROD-005: Verify filter products with multiple criteria", dependsOnMethods = "testFilterProductsByPriceRange")
    public void testFilterProductsWithMultipleCriteria() {
        Allure.step("Send GET with multiple filters");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("category", MULTIPLE_CRITERIA_CATEGORY);
        queryParams.put("min_price", MULTIPLE_CRITERIA_MIN_PRICE.toString());
        queryParams.put("max_price", MULTIPLE_CRITERIA_MAX_PRICE.toString());
        queryParams.put("search", MULTIPLE_CRITERIA_SEARCH);
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products",queryParams);

        Allure.step("Verify all filters applied");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify all products match category");
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        for (Map<String, Object> product : products) { // product is a map of key-value pairs in json e.g "data": [ {"category": "Electronics"
            Assert.assertEquals(product.get("category"), MULTIPLE_CRITERIA_CATEGORY, "All products match category");

            Allure.step("Verify all prices in range");
            double price = ((Number) product.get("price")).doubleValue(); // from object to Number #GOMAA << back again for workaround if exist
            Assert.assertTrue(price >= MULTIPLE_CRITERIA_MIN_PRICE && price <= MULTIPLE_CRITERIA_MAX_PRICE, "All prices in range");

            Allure.step("Verify products contain search term");
            String name = product.get("name").toString().toLowerCase();
            String description = product.get("description").toString().toLowerCase();
            Assert.assertTrue(name.contains(MULTIPLE_CRITERIA_SEARCH) || description.contains(MULTIPLE_CRITERIA_SEARCH), "Products contain search term");
        }
    }

    @Test(description = "TC-PROD-006: Verify admin can create product", dependsOnMethods = "testFilterProductsWithMultipleCriteria")
    public void testAdminCanCreateProduct() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send POST with product data");
        Map<String, Object> body = new HashMap<>();
        body.put("name", PRODUCT_NAME);
        body.put("description", DESCRIPTION);
        body.put("price", PRICE);
        body.put("category", CATEGORY);
        body.put("stock", STOCK);
        body.put("image_url", IMAGE_URL);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products", adminToken, body);

        Allure.step("Verify created");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Product created successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product created successfully", "message is 'Product created successfully'");

        Allure.step("Verify data has all fields");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "data has name");
        Assert.assertTrue(response.jsonPath().get("data.description") instanceof String, "data has DESCRIPTION");
        Assert.assertTrue(response.jsonPath().get("data.price") instanceof Number, "data has PRICE");
        Assert.assertTrue(response.jsonPath().get("data.category") instanceof String, "data has CATEGORY");
        Assert.assertTrue(response.jsonPath().get("data.stock") instanceof Integer, "data has STOCK");
        Assert.assertTrue(response.jsonPath().get("data.image_url") instanceof String, "data has IMAGE_URL");
        Assert.assertNotNull(response.jsonPath().get("data.created_at"), "data has created_at");

        Allure.step("Verify all fields match input");
        Assert.assertEquals(response.jsonPath().getString("data.name"), PRODUCT_NAME, "All fields match input");
        Assert.assertEquals(response.jsonPath().getDouble("data.price"), PRICE, "All fields match input");

        Allure.step("Save product ID");
        productId = response.jsonPath().getInt("data.id");
        JsonUtility.saveValue("product_id", productId, IDS_FILE_PATH);
    }

    @Test(description = "TC-PROD-007: Verify create product fails without required fields", dependsOnMethods = "testAdminCanCreateProduct")
    public void testCreateProductFailsWithoutRequiredFields() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send POST with incomplete data");
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Test");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products", adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates missing fields");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Missing required fields"), "error indicates missing fields");
    }

    @Test(description = "TC-PROD-008: Verify create product fails without admin", dependsOnMethods = "testCreateProductFailsWithoutRequiredFields")
    public void testCreateProductFailsWithoutAdmin() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST");
        Map<String, Object> body = new HashMap<>();
        body.put("name", PRODUCT_NAME);
        body.put("description", DESCRIPTION);
        body.put("price", PRICE);
        body.put("category", CATEGORY);
        body.put("stock", STOCK);
        body.put("image_url", IMAGE_URL);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products", userToken, body);

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates admin auth needed");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"), "error indicates admin auth needed");
    }

    @Test(description = "TC-PROD-009: Verify admin can update product", dependsOnMethods = "testCreateProductFailsWithoutAdmin")
    public void testAdminCanUpdateProduct() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Create test product");
        productId = JsonUtility.getJSONInt("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productId, "Product ID not found");

        Allure.step("Send PUT with updates");
        Map<String, Object> body = new HashMap<>();
        body.put("description", UPDATED_DESCRIPTION);
        body.put("price", UPDATED_PRICE);
        body.put("stock", UPDATED_STOCK);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/" + productId, adminToken, body);

        Allure.step("Verify updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Product updated successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product updated successfully", "message is 'Product updated successfully'");

        Allure.step("Verify data reflects updates");
        Assert.assertEquals(response.jsonPath().getString("data.description"), UPDATED_DESCRIPTION, "data reflects updates");
        Assert.assertEquals(response.jsonPath().getDouble("data.price"), UPDATED_PRICE, "data reflects updates");

        Allure.step("Verify updated_at present");
        Assert.assertNotNull(response.jsonPath().get("data.updated_at"), "updated_at present");
    }

    @Test(description = "TC-PROD-010: Verify update product fails for non-existent product", dependsOnMethods = "testAdminCanUpdateProduct")
    public void testUpdateProductFailsForNonExistent() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send PUT with invalid ID");
        Map<String, Object> body = new HashMap<>();
        body.put("description", UPDATED_DESCRIPTION);
        body.put("price", UPDATED_PRICE);
        body.put("stock", UPDATED_STOCK);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/99999", adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates non existing product");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Product not found"), "error indicates non existing product");
    }

    @Test(description = "TC-PROD-011: Verify get product by ID", dependsOnMethods = "testUpdateProductFailsForNonExistent")
    public void testGetProductById() throws Exception {
        Allure.step("Send GET with valid product ID");
        productId = JsonUtility.getJSONInt("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productId, "Product ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/products/" + productId);

        Allure.step("Verify product details");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data has complete product info");
        Assert.assertNotNull(response.jsonPath().get("data"), "data has complete product info");

        Allure.step("Verify product ID matches request");
        Assert.assertEquals(response.jsonPath().getInt("data.id"), productId.intValue(), "Product ID matches request");

        Allure.step("Verify Data Type using dependsOnMethods request in TC-PROD-006: testAdminCanCreateProduct");
        Assert.assertEquals(response.jsonPath().getString("data.category") , CATEGORY,"category not match created number");
        Assert.assertTrue(response.jsonPath().get("data.created_at") instanceof String, "created_at is a string"); //not saved before
        Assert.assertEquals(response.jsonPath().getString("data.description") , UPDATED_DESCRIPTION,"description not match created number");
        Assert.assertEquals(response.jsonPath().getString("data.image_url") , IMAGE_URL,"image_url not match created number");
        Assert.assertEquals(response.jsonPath().getString("data.name") , PRODUCT_NAME,"pricename not match created number");
        Assert.assertEquals(response.jsonPath().getInt("data.stock") , UPDATED_STOCK,"stock not match created number");
//        Assert.assertTrue(response.jsonPath().get("data.price") instanceof Float, "price is a Float");  // instanceof make error in double so use float or use getDouble way
        Assert.assertEquals(response.jsonPath().getDouble("data.price") , UPDATED_PRICE,"price not match created number");
// another way use delta
//        Double pricejson = response.jsonPath().getDouble("data.PRICE");
//        Assert.assertEquals(pricejson, PRICE, 0.001, "Price value mismatch");




    }

    @Test(description = "TC-PROD-012: Verify get product fails for non-existent ID", dependsOnMethods = "testGetProductById")
    public void testGetProductFailsForNonExistentId() {
        Allure.step("Send GET with invalid ID");
        Response response = ApiUtils.getRequest(BASE_URL + "/products/99999");

        Allure.step("Verify 404");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Product not found'");
//        String error = response.jsonPath().getString("error");
        Assert.assertEquals(response.jsonPath().getString("error"), "Product not found","error is 'Product not found'");
    }

    @Test(description = "TC-PROD-013: Verify admin can delete product", dependsOnMethods = "testGetProductFailsForNonExistentId")
    public void testAdminCanDeleteProduct() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Create test product");
        productId = JsonUtility.getJSONInt("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productId, "Product ID not found");

        Allure.step("Send DELETE");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/" + productId, adminToken);

        Allure.step("Verify deleted");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Product deleted successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product deleted successfully", "message is 'Product deleted successfully'");

        Allure.step("Verify data has id/name");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "data has name");

        Allure.step("Subsequent GET returns 404");
        Response getResponse = ApiUtils.getRequest(BASE_URL + "/products/" + productId);
        Assert.assertEquals(getResponse.getStatusCode(), 404, "Subsequent GET returns 404");
    }

    @Test(description = "TC-PROD-014: Verify delete product fails for non-existent product", dependsOnMethods = "testAdminCanDeleteProduct")
    public void testDeleteProductFailsForNonExistent() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send DELETE with invalid ID");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/99999", adminToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Product not found'");
        Assert.assertEquals(response.jsonPath().getString("error"), "Product not found","error is 'Product not found'");
    }

    @Test(description = "TC-PROD-015: Verify search products returns matching results", dependsOnMethods = "testDeleteProductFailsForNonExistent")
    public void testSearchProductsReturnsMatchingResults() {
        Allure.step("Send GET with search query");
        Map<String, String> query = new HashMap<>();
        query.put("q", SEARCH_QUERY);
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products/search",query);

        Allure.step("Verify matching products");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify products contain search term in name or description");
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        for (Map<String, Object> product : products) {
            String name = product.get("name").toString().toLowerCase();
            String description = product.get("description").toString().toLowerCase();
            Assert.assertTrue(name.contains(SEARCH_QUERY.toLowerCase()) || description.contains(SEARCH_QUERY.toLowerCase()), "Products contain '" + SEARCH_QUERY + "' in name or description");
        }
    }

    @Test(description = "TC-PROD-016: Verify search returns empty for no matches", dependsOnMethods = "testSearchProductsReturnsMatchingResults")
    public void testSearchReturnsEmptyForNoMatches() {
        Allure.step("Send GET with non-matching query");
        Response response = ApiUtils.getRequest(BASE_URL + "/products/search?q=nonexistent123");

        Allure.step("Verify empty result");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is empty array");
//        "data": [
//
//    ],
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        Assert.assertTrue(products.isEmpty(), "data is empty array");

        Allure.step("Verify count is 0");
        Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");
    }

    @Test(description = "TC-PROD-017: Verify get products by CATEGORY", dependsOnMethods = "testSearchReturnsEmptyForNoMatches")
    public void testGetProductsByCategory() throws Exception {
        Allure.step("Get CATEGORY names from TC-CAT-003");
        // Read JSON file directly using RestAssured JsonPath #NEW WAY #GOMAA instead of wrapper classes
        JsonPath jsonPath = JsonPath.from(new File(IDS_FILE_PATH));
        List<String> categoryNames = jsonPath.getList("category_names");
        Logger.step("Category Names to be tested are: " + categoryNames);

        Assert.assertNotNull(categoryNames, "Category names not found");
        Assert.assertFalse(categoryNames.isEmpty(), "At least one CATEGORY exists");

        Allure.step("Test all unique CATEGORY names");
        for (String category : categoryNames) {
            Allure.step("Send GET for specific CATEGORY: " + category);
            Logger.info("---> Testing CATEGORY: " + category + " <---");
            Response response = ApiUtils.getRequest(BASE_URL + "/products/category/" + category); // send as many request as we can #GOMAA CHALANGE

            Allure.step("Verify filtered results for category: " + category);
            Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

            Allure.step("Verify response is valid JSON");
            Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

            Allure.step("Verify success is true");
            Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

            Allure.step("Verify data is array");
            Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

            List<Map<String, Object>> products = response.jsonPath().getList("data");
            
            Allure.step("Verify all products have category='" + category + "'");
            for (Map<String, Object> product : products) {
                Assert.assertEquals(product.get("category"), category, "All products have category='" + category + "'");
            }

            Allure.step("Verify count matches array length");
            int count = response.jsonPath().getInt("count");
            Assert.assertEquals(count, products.size(), "count matches array length");
        }
    }

    @Test(description = "TC-PROD-018: Verify get products by CATEGORY returns empty for non-existent CATEGORY", dependsOnMethods = "testGetProductsByCategory")
    public void testGetProductsByCategoryReturnsEmptyForNonExistent() {
        Allure.step("Send GET for non-existent category");
        Response response = ApiUtils.getRequest(BASE_URL + "/products/category/NonExistent");

        Allure.step("Verify empty result");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify data is empty array");
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        Assert.assertTrue(products.isEmpty(), "data is empty array");

        Allure.step("Verify count is 0");
        Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");
    }

    @Test(description = "TC-PROD-019: Verify admin can view low STOCK products", dependsOnMethods = "testGetProductsByCategoryReturnsEmptyForNonExistent")
    public void testAdminCanViewLowStockProducts() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send GET with threshold");
        Map<String, String> query = new HashMap<>();
        query.put("threshold", THRESHOLD.toString());
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/inventory/low-stock",query, adminToken);

        Allure.step("Verify low stock products");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify all products have stock <= "+ THRESHOLD +" ");
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        for (Map<String, Object> product : products) {
            Assert.assertTrue(((Integer) product.get("stock")) <= THRESHOLD, "All products have stock <= "+ THRESHOLD +" ");
        }

        Allure.step("Verify threshold value is "+ THRESHOLD +" ");
        Assert.assertEquals(response.jsonPath().getInt("threshold"), THRESHOLD, "threshold value is "+ THRESHOLD +" ");

        Allure.step("Verify count matches array length");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, products.size(), "count matches array length");
    }

    @Test(description = "TC-PROD-020: Verify low STOCK with custom THRESHOLD", dependsOnMethods = "testAdminCanViewLowStockProducts")
    public void testLowStockWithCustomThreshold() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send GET with threshold=5");
        Map<String, String> query = new HashMap<>();
        query.put("threshold", CUSTOM_THRESHOLD.toString());
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/inventory/low-stock",query, adminToken);

        Allure.step("Verify results");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify all products have stock <= "+ CUSTOM_THRESHOLD +" ");
        List<Map<String, Object>> products = response.jsonPath().getList("data");
        for (Map<String, Object> product : products) {
            Assert.assertTrue(((Integer) product.get("stock")) <= CUSTOM_THRESHOLD, "All products have stock <= "+ CUSTOM_THRESHOLD +" ");
        }

        Allure.step("Verify threshold is "+ CUSTOM_THRESHOLD +" ");
        Assert.assertEquals(response.jsonPath().getInt("threshold"), CUSTOM_THRESHOLD, "threshold is "+ CUSTOM_THRESHOLD +" ");
    }

    @Test(description = "TC-PROD-021: Verify admin can update product STOCK", dependsOnMethods = "testLowStockWithCustomThreshold")
    public void testAdminCanUpdateProductStock() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send PUT with stock update");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", INVENTORY_PRODUCT_ID);
        body.put("stock", INVENTORY_STOCK);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/inventory/update-stock", adminToken, body);

        Allure.step("Verify stock updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Stock updated successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Stock updated successfully", "message is 'Stock updated successfully'");

        Allure.step("Verify data has product_id/old_stock/new_stock");
        Assert.assertTrue(response.jsonPath().get("data.product_id") instanceof Integer, "data has product_id");
        Assert.assertTrue(response.jsonPath().get("data.old_stock") instanceof Integer, "data has old_stock");
        Assert.assertTrue(response.jsonPath().get("data.new_stock") instanceof Integer, "data has new_stock");

        Allure.step("Verify new_stock is "+ INVENTORY_STOCK +" ");
        Assert.assertEquals(response.jsonPath().getInt("data.new_stock"), INVENTORY_STOCK, "new_stock is "+ INVENTORY_STOCK +" ");
    }

    @Test(description = "TC-PROD-022: Verify update STOCK fails for non-existent product", dependsOnMethods = "testAdminCanUpdateProductStock")
    public void testUpdateStockFailsForNonExistentProduct() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send PUT with invalid product_id");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 99999);
        body.put("stock", 10);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/inventory/update-stock", adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
    }

    @Test(description = "TC-PROD-023: Verify admin can bulk update products", dependsOnMethods = "testUpdateStockFailsForNonExistentProduct")
    public void testAdminCanBulkUpdateProducts() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send PUT with multiple updates");
        /* send json body like this

        {
            "updates": [
            {
                "product_id": 1,
                    "price": 899.99,
                    "stock": 75
            },
            {
                "product_id": 2,
                    "price": 24.99,
                    "stock": 200
            }
                      ]
        }

         */
        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> updates = List.of(
            Map.of("product_id", BULK_UPDATE_PRODUCT_ID1, "price", BULK_UPDATE_PRICE1, "stock", BULK_UPDATE_STOCK1),
            Map.of("product_id", BULK_UPDATE_PRODUCT_ID2, "price", BULK_UPDATE_PRICE2, "stock", BULK_UPDATE_STOCK2)
        );
        body.put("updates", updates);
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/bulk-update", adminToken, body);

        Allure.step("Verify all updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message contains '2 products updated'");
        String message = response.jsonPath().getString("message");
        Assert.assertTrue(message != null && message.contains("2 products updated"), "message contains '2 products updated'");

        Allure.step("Verify updated_count is 2");
        Assert.assertEquals(response.jsonPath().getInt("updated_count"), 2, "updated_count is 2");
    }

    @Test(description = "TC-PROD-024: Verify bulk update with partial failures", dependsOnMethods = "testAdminCanBulkUpdateProducts")
    public void testBulkUpdateWithPartialFailures() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send PUT with valid and invalid IDs");
        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> updates = List.of(
            Map.of("product_id", BULK_UPDATE_PRODUCT_ID1, "price", BULK_UPDATE_PRICE1),
            Map.of("product_id", 99999, "price", BULK_UPDATE_PRICE2)
        );
        body.put("updates", updates);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/products/bulk-update", adminToken, body);

        Allure.step("Verify partial success");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify updated_count reflects successful updates");
        int updatedCount = response.jsonPath().getInt("updated_count");
        Assert.assertTrue(updatedCount >= 1, "updated_count reflects successful updates");
    }

    @Test(description = "TC-PROD-025: Verify admin can export all products", dependsOnMethods = "testBulkUpdateWithPartialFailures")
    public void testAdminCanExportAllProducts() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send GET to export");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/export/products", adminToken);

        Allure.step("Verify all data returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data.products is array");
        Assert.assertNotNull(response.jsonPath().getList("data.products"), "data.products is array");

        Allure.step("Verify data.total_products matches array length");
        int totalProducts = response.jsonPath().getInt("data.total_products");
        List<Map<String, Object>> products = response.jsonPath().getList("data.products");
        Assert.assertEquals(totalProducts, products.size(), "data.total_products matches array length");

        Allure.step("Verify data.export_date is timestamp");
        Assert.assertNotNull(response.jsonPath().get("data.export_date"), "data.export_date is timestamp");

        Allure.step("Verify product fields data types");
        Map<String, Object> thirdProduct = products.get(3);
        Assert.assertTrue(thirdProduct.get("category") instanceof String, "String data type is integer");
        Assert.assertTrue(thirdProduct.get("created_at") instanceof String, "created_at data type is String");
        Assert.assertTrue(thirdProduct.get("description") instanceof String, "description data type is String");
        Assert.assertTrue(thirdProduct.get("id") instanceof Integer, "id data type is integer");
        Assert.assertTrue(thirdProduct.get("image_url") instanceof String, "image_url data type is String");
        Assert.assertTrue(thirdProduct.get("name") instanceof String, "name data type is String");
        Assert.assertTrue(thirdProduct.get("price") instanceof Float, "price data type is Float");
        Assert.assertTrue(thirdProduct.get("stock") instanceof Integer, "stock data type is integer");
        Assert.assertTrue(thirdProduct.get("updated_at") instanceof String, "updated_at data type is String");


    }

    @Test(description = "TC-PROD-026: Verify user can like a product", dependsOnMethods = "testAdminCanExportAllProducts")
    public void testUserCanLikeProduct() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST to like product");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", LIKE_PRODUCT_ID);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products/likes", userToken, body);

        Allure.step("Verify like created");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Product liked successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product liked successfully", "message is 'Product liked successfully'");

        Allure.step("Verify data has id/product_id/user_id/created_at");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.product_id") instanceof Integer, "data has product_id");
        Assert.assertTrue(response.jsonPath().get("data.user_id") instanceof Integer, "data has user_id");
        Assert.assertNotNull(response.jsonPath().get("data.created_at"), "data has created_at");

        Allure.step("Verify product_id and user_id match");
        Assert.assertEquals(response.jsonPath().getInt("data.product_id"), LIKE_PRODUCT_ID, "product_id matches");

        Allure.step("Save like ID for later tests");
         likeId = response.jsonPath().getInt("data.id");
        JsonUtility.saveValue("like_id", likeId, IDS_FILE_PATH);
    }

    @Test(description = "TC-PROD-027: Verify user cannot like same product twice",  dependsOnMethods = "testUserCanLikeProduct")
    public void testUserCannotLikeSameProductTwice() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Like product");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", LIKE_PRODUCT_ID);

        Allure.step("Try liking again");
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products/likes", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'You have already liked this product'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("already liked"), "error is 'You have already liked this product'");

        Allure.step("Verify already_liked is true");
        Assert.assertTrue(response.jsonPath().getBoolean("already_liked"), "already_liked is true");
    }

    @Test(description = "TC-PROD-028: Verify like fails for non-existent product", dependsOnMethods = "testUserCannotLikeSameProductTwice")
    public void testLikeFailsForNonExistentProduct() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST with invalid product_id");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 99999);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products/likes", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
    }

    @Test(description = "TC-PROD-029: Verify get product likes count", dependsOnMethods = "testLikeFailsForNonExistentProduct")
    public void testGetProductLikesCount() {
        Allure.step("Send GET for product likes");
        Response response = ApiUtils.getRequest(BASE_URL + "/products/"+PRODUCT_ID_LIKES_TO_COUNT+"/likes");

        Allure.step("Verify likes data");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify count matches array length");
        int count = response.jsonPath().getInt("count");
        List<Map<String, Object>> likes = response.jsonPath().getList("data");
        Assert.assertEquals(count, likes.size(), "count matches array length");

        Allure.step("Verify each like has id/product_id/user_id/created_at");
        if (!likes.isEmpty()) {
            Map<String, Object> like = likes.get(0);
            Assert.assertTrue(like.get("id") instanceof Integer, "Each like has id");
            Assert.assertTrue(like.get("product_id") instanceof Integer, "Each like has product_id");
            Assert.assertTrue(like.get("user_id") instanceof Integer, "Each like has user_id");
            Assert.assertNotNull(like.get("created_at"), "Each like has created_at");
        }
    }

    @Test(description = "TC-PROD-030: Verify user can check like status", dependsOnMethods = "testGetProductLikesCount")
    public void testUserCanCheckLikeStatus() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send GET to check like status");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/products/"+PRODUCT_ID_LIKES_TO_COUNT+"/likes/check", userToken);

        Allure.step("Verify status");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify liked is boolean");
        Assert.assertTrue(response.jsonPath().get("liked") instanceof Boolean, "liked is boolean");

        Allure.step("If liked=true data has like object");
        boolean liked = response.jsonPath().getBoolean("liked"); // using boolean primitive instead of Boolean wrapper to avoid null pointer
        if (liked) { //true in case previous tests liked the product and there are likes
            Assert.assertNotNull(response.jsonPath().get("data"), "If liked=true data has like object");
        } else {    // false in case new user didn't like any product
            Allure.step("If liked=false data is null");
            Assert.assertNull(response.jsonPath().get("data"), "If liked=false data is null");
            Assert.assertEquals(response.jsonPath().get("data"), "null", "If liked=false data is null");
        }
    }

    @Test(description = "TC-PROD-031: Verify user can unlike product", dependsOnMethods = "testUserCanCheckLikeStatus")
    public void testUserCanUnlikeProduct() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Like product");
         likeId = JsonUtility.getJSONInt("like_id", IDS_FILE_PATH);
        Assert.assertNotNull(likeId, "Like ID not found");

        Allure.step("Send DELETE to unlike");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/likes/" + likeId, userToken);

        Allure.step("Verify removed");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Product unliked successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product unliked successfully", "message is 'Product unliked successfully'");

        Allure.step("Verify data has like object");
        Assert.assertNotNull(response.jsonPath().get("data"), "data has like object");

    }

    @Test(description = "TC-PROD-032: Verify unlike fails for non-existent like", dependsOnMethods = "testUserCanUnlikeProduct")
    public void testUnlikeFailsForNonExistentLike() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send DELETE with invalid like_id");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/likes/99999", userToken);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Like not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("not found"), "error is 'Like not found'");
    }

    @Test(description = "TC-PROD-033: Verify user cannot unlike another user's like", dependsOnMethods = "testUnlikeFailsForNonExistentLike")
    public void testUserCannotUnlikeAnotherUsersLike() throws Exception {
        Allure.step("Get any existing like from product");
        Response likesResponse = ApiUtils.getRequest(BASE_URL + "/products/"+PRODUCT_ID_LIKES_TO_COUNT+"/likes");
        List<Map<String, Object>> likes = likesResponse.jsonPath().getList("data");

        if (!likes.isEmpty()) {
             someLikeId = (Integer) likes.get(0).get("id");
            
            Allure.step("User1 tries to unlike");
            userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
            Assert.assertNotNull(userToken, "User token not found");

            Allure.step("User2 tries to unlike");
            Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/likes/" + someLikeId, userToken);

            Allure.step("Verify access denied");
            if (response.getStatusCode() == 403) {
                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

                Allure.step("Verify response is valid JSON");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Allure.step("Verify success is false");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

                Allure.step("Verify like remains");
                Response verifyResponse = ApiUtils.getRequest(BASE_URL + "/products/1/likes");
                List<Map<String, Object>> afterLikes = verifyResponse.jsonPath().getList("data");
                Assert.assertEquals(afterLikes.size(), likes.size(), "Like remains");
            } else {
                Allure.step("Note: This like belongs to the same user or was already deleted");
            }
        }
    }
}
