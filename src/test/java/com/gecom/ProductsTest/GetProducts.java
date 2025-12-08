package com.gecom.ProductsTest;

import static com.gecom.utils.Base.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import com.gecom.utils.Logger;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "ProductsTest")
@Severity(SeverityLevel.CRITICAL)
public class GetProducts {

        @Test(description = "TC-PROD-001: Verify get all products with pagination", groups = {
                        "Valid-Products-Test", "valid" })
        public void testGetAllProductsWithPagination() {
                Response response = ApiUtils.getRequest(BASE_URL + "/products");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertNotNull(response.jsonPath().getList("data"), "data is array"); // array "data":[{},{}]
                Assert.assertTrue(response.jsonPath().get("pagination.page") instanceof Integer, "pagination has page");
                Assert.assertTrue(response.jsonPath().get("pagination.per_page") instanceof Integer,
                                "pagination has per_page");
                Assert.assertTrue(response.jsonPath().get("pagination.total") instanceof Integer,
                                "pagination has total");
                Assert.assertTrue(response.jsonPath().get("pagination.pages") instanceof Integer,
                                "pagination has pages");
                // retrieve a list of maps from the json responses -> each map represents a
                // product with a various attributes stored as key-value pairs the data is
                // extracted from "data" field in the json response
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

        @Test(description = "TC-PROD-002: Verify products pagination with page parameter", groups = {
                        "Valid-Products-Test", "valid" })
        public void testProductsPaginationWithPageParameter() {
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("page", PRODUCT_PAGINATION_PAGE.toString());
                queryParams.put("per_page", PRODUCT_PAGINATION_PER_PAGE.toString());

                Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products", queryParams);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertEquals(response.jsonPath().getInt("pagination.page"), PRODUCT_PAGINATION_PAGE,
                                "pagination.page is " + PRODUCT_PAGINATION_PAGE + " "); // assert agains queryParams
                List<Map<String, Object>> products = response.jsonPath().getList("data"); // "data" : [{},{}]
                Assert.assertTrue(products.size() <= PRODUCT_PAGINATION_PER_PAGE,
                                "data length <= " + PRODUCT_PAGINATION_PER_PAGE + " "); // assert against queryParams
                Assert.assertEquals(response.jsonPath().getInt("pagination.per_page"), PRODUCT_PAGINATION_PER_PAGE,
                                "pagination values correct");
        }

        @Test(description = "TC-PROD-003: Verify filter products by CATEGORY", groups = {
                        "Valid-Products-Test", "valid" })
        public void testFilterProductsByCategory() {
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("category", FILTER_CATEGORY); // testcase17 will test all categories from ids.json
                Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products", queryParams);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                List<Map<String, Object>> products = response.jsonPath().getList("data"); // "data" : [{},{}]
                for (Map<String, Object> product : products) { // from "{}" get the value of the key "category" to
                                                               // compare it
                                                               // with "Electronics"
                        Assert.assertEquals(product.get("category"), FILTER_CATEGORY,
                                        "All products have category=" + FILTER_CATEGORY + " ");
                }
                Assert.assertNotNull(response.jsonPath().get("pagination"), "Pagination present");
                Assert.assertTrue(response.jsonPath().get("pagination.page") instanceof Integer,
                                "page is not valid Integer");
                Assert.assertTrue(response.jsonPath().get("pagination.pages") instanceof Integer,
                                "pages is not valid Integer");
                Assert.assertTrue(response.jsonPath().get("pagination.per_page") instanceof Integer,
                                "per_page is not valid Integer");
                Assert.assertTrue(response.jsonPath().get("pagination.total") instanceof Integer,
                                "total is not valid Integer");
        }

        @Test(description = "TC-PROD-004: Verify filter products by PRICE range", groups = {
                        "Valid-Products-Test", "valid" })
        public void testFilterProductsByPriceRange() {
                Map<String, String> query = new HashMap<>();
                query.put("min_price", MIN_PRICE.toString());
                query.put("max_price", MAX_PRICE.toString());
                Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products", query);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                List<Map<String, Object>> products = response.jsonPath().getList("data");
                for (Map<String, Object> product : products) {
                        double price = ((Number) product.get("price")).doubleValue();
                        Assert.assertTrue(price >= MIN_PRICE && price <= MAX_PRICE,
                                        "All products have price >= " + MIN_PRICE + " and <= " + MAX_PRICE + "");
                }
        }

        @Test(description = "TC-PROD-005: Verify filter products with multiple criteria", groups = {
                        "Valid-Products-Test", "valid" })
        public void testFilterProductsWithMultipleCriteria() {
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("category", MULTIPLE_CRITERIA_CATEGORY);
                queryParams.put("min_price", MULTIPLE_CRITERIA_MIN_PRICE.toString());
                queryParams.put("max_price", MULTIPLE_CRITERIA_MAX_PRICE.toString());
                queryParams.put("search", MULTIPLE_CRITERIA_SEARCH);
                Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products", queryParams);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                List<Map<String, Object>> products = response.jsonPath().getList("data");
                // product is a map of key-value pairs in json e.g "data": [ {"category":
                for (Map<String, Object> product : products) {
                        Assert.assertEquals(product.get("category"), MULTIPLE_CRITERIA_CATEGORY,
                                        "All products match category");
                        // from object to Number #GOMAA << back again for workaround if exist
                        double price = ((Number) product.get("price")).doubleValue();
                        Assert.assertTrue(price >= MULTIPLE_CRITERIA_MIN_PRICE && price <= MULTIPLE_CRITERIA_MAX_PRICE,
                                        "All prices in range");
                        String name = product.get("name").toString().toLowerCase();
                        String description = product.get("description").toString().toLowerCase();
                        Assert.assertTrue(
                                        name.contains(MULTIPLE_CRITERIA_SEARCH)
                                                        || description.contains(MULTIPLE_CRITERIA_SEARCH),
                                        "Products contain search term");
                }
        }

        @Test(description = "TC-PROD-011: Verify get product by ID", groups = {
                        "Valid-Products-Test", "valid" })
        public void testGetProductById() throws Exception {
                productId = (Integer) JsonUtility.getValue("product_id", IDS_FILE_PATH);
                Assert.assertNotNull(productId, "Product ID not found");

                Response response = ApiUtils.getRequest(BASE_URL + "/products/" + productId);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertNotNull(response.jsonPath().get("data"), "data has complete product info");
                Assert.assertEquals(response.jsonPath().getInt("data.id"), productId.intValue(),
                                "Product ID matches request");
                Assert.assertEquals(response.jsonPath().getString("data.category"), CATEGORY,
                                "category not match created number");
                Assert.assertTrue(response.jsonPath().get("data.created_at") instanceof String,
                                "created_at is a string"); // not
                                                           // saved
                                                           // before
                Assert.assertEquals(response.jsonPath().getString("data.description"), UPDATED_DESCRIPTION,
                                "description not match created number");
                Assert.assertEquals(response.jsonPath().getString("data.image_url"), IMAGE_URL,
                                "image_url not match created number");
                Assert.assertEquals(response.jsonPath().getString("data.name"), PRODUCT_NAME,
                                "pricename not match created number");
                Assert.assertEquals(response.jsonPath().getInt("data.stock"), UPDATED_STOCK,
                                "stock not match created number");
                // Assert.assertTrue(response.jsonPath().get("data.price") instanceof Float,
                // "price is a Float"); // instanceof make error in double so use float or use
                // getDouble way
                Assert.assertEquals(response.jsonPath().getDouble("data.price"), UPDATED_PRICE,
                                "price not match created number");
                // another way use delta
                // Double pricejson = response.jsonPath().getDouble("data.PRICE");
                // Assert.assertEquals(pricejson, PRICE, 0.001, "Price value mismatch");

        }

        @Test(description = "TC-PROD-012: Verify get product fails for non-existent ID", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testGetProductFailsForNonExistentId() {
                Response response = ApiUtils.getRequest(BASE_URL + "/products/99999");
                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                Assert.assertEquals(response.jsonPath().getString("error"), "Product not found",
                                "error is 'Product not found'");
        }

        @Test(description = "TC-PROD-015: Verify search products returns matching results", groups = {
                        "Valid-Products-Test", "valid" })
        public void testSearchProductsReturnsMatchingResults() {
                Map<String, String> query = new HashMap<>();
                query.put("q", SEARCH_QUERY);
                Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/products/search", query);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");
                List<Map<String, Object>> products = response.jsonPath().getList("data");
                for (Map<String, Object> product : products) {
                        String name = product.get("name").toString().toLowerCase();
                        String description = product.get("description").toString().toLowerCase();
                        Assert.assertTrue(
                                        name.contains(SEARCH_QUERY.toLowerCase())
                                                        || description.contains(SEARCH_QUERY.toLowerCase()),
                                        "Products contain '" + SEARCH_QUERY + "' in name or description");
                }
        }

        @Test(description = "TC-PROD-016: Verify search returns empty for no matches", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testSearchReturnsEmptyForNoMatches() {
                Response response = ApiUtils.getRequest(BASE_URL + "/products/search?q=nonexistent123");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                // "data": [
                //
                // ],
                List<Map<String, Object>> products = response.jsonPath().getList("data");
                Assert.assertTrue(products.isEmpty(), "data is empty array");
                Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");
        }

        @Test(description = "TC-PROD-017: Verify get products by CATEGORY", groups = {
                        "Valid-Products-Test", "valid" })
        public void testGetProductsByCategory() throws Exception {
                JsonPath jsonPath = JsonPath.from(new File(IDS_FILE_PATH));
                List<String> categoryNames = jsonPath.getList("category_names");
                Logger.step("Category Names to be tested are: " + categoryNames);

                Assert.assertNotNull(categoryNames, "Category names not found");
                Assert.assertFalse(categoryNames.isEmpty(), "At least one CATEGORY exists");

                for (String category : categoryNames) {
                        Logger.info("---> Testing CATEGORY: " + category + " <---");
                        Response response = ApiUtils.getRequest(BASE_URL + "/products/category/" + category);
                        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

                        List<Map<String, Object>> products = response.jsonPath().getList("data");

                        for (Map<String, Object> product : products) {
                                Assert.assertEquals(product.get("category"), category,
                                                "All products have category='" + category + "'");
                        }

                        int count = response.jsonPath().getInt("count");
                        Assert.assertEquals(count, products.size(), "count matches array length");
                }
        }

        @Test(description = "TC-PROD-018: Verify get products by CATEGORY returns empty for non-existent CATEGORY", groups = {
                        "Invalid-Products-Test", "invalid" })
        public void testGetProductsByCategoryReturnsEmptyForNonExistent() {
                Response response = ApiUtils.getRequest(BASE_URL + "/products/category/NonExistent");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                List<Map<String, Object>> products = response.jsonPath().getList("data");
                Assert.assertTrue(products.isEmpty(), "data is empty array");
                Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");
        }

        @Test(description = "TC-PROD-019: Verify admin can view low STOCK products", groups = {
                        "Valid-Products-Test", "valid" })
        public void testAdminCanViewLowStockProducts() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, String> query = new HashMap<>();
                query.put("threshold", THRESHOLD.toString());
                Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/inventory/low-stock", query,
                                adminToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

                List<Map<String, Object>> products = response.jsonPath().getList("data");
                for (Map<String, Object> product : products) {
                        Assert.assertTrue(((Integer) product.get("stock")) <= THRESHOLD,
                                        "All products have stock <= " + THRESHOLD + " ");
                }

                Assert.assertEquals(response.jsonPath().getInt("threshold"), THRESHOLD,
                                "threshold value is " + THRESHOLD + " ");

                int count = response.jsonPath().getInt("count");
                Assert.assertEquals(count, products.size(), "count matches array length");
        }

        @Test(description = "TC-PROD-020: Verify low STOCK with custom THRESHOLD", groups = {
                        "Valid-Products-Test", "valid" })
        public void testLowStockWithCustomThreshold() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, String> query = new HashMap<>();
                query.put("threshold", CUSTOM_THRESHOLD.toString());
                Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/inventory/low-stock", query,
                                adminToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                List<Map<String, Object>> products = response.jsonPath().getList("data");
                for (Map<String, Object> product : products) {
                        Assert.assertTrue(((Integer) product.get("stock")) <= CUSTOM_THRESHOLD,
                                        "All products have stock <= " + CUSTOM_THRESHOLD + " ");
                }

                Assert.assertEquals(response.jsonPath().getInt("threshold"), CUSTOM_THRESHOLD,
                                "threshold is " + CUSTOM_THRESHOLD + " ");
        }

        @Test(description = "TC-PROD-025: Verify admin can export all products", groups = {
                        "Valid-Products-Test", "valid" })
        public void testAdminCanExportAllProducts() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/export/products", adminToken);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertNotNull(response.jsonPath().getList("data.products"), "data.products is array");
                int totalProducts = response.jsonPath().getInt("data.total_products");
                List<Map<String, Object>> products = response.jsonPath().getList("data.products");
                Assert.assertEquals(totalProducts, products.size(), "data.total_products matches array length");
                Assert.assertNotNull(response.jsonPath().get("data.export_date"), "data.export_date is timestamp");
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

}
