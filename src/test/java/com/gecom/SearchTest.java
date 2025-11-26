package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gecom.utils.Const.*;

/**
 * This class contains test cases for the search functionalities,
 * including advanced search with filters and product recommendations.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "SearchTest")
public class SearchTest {

    /**
     * Test case for verifying advanced product search with various filters.
     *
     * @throws Exception if an error occurs while saving the product ID.
     */
    @Test(description = "TC-SEARCH-001: Verify advanced product search with filters")
    public void testAdvancedSearchWithFilters() throws Exception {
        Allure.step("Prepare advanced search filters for TC-SEARCH-001");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", SEARCH_QUERY);
        queryParams.put("category", SEARCH_CATEGORY);
        queryParams.put("min_price", SEARCH_MIN_PRICE);
        queryParams.put("max_price", SEARCH_MAX_PRICE);
        queryParams.put("min_rating", SEARCH_MIN_RATING);
        queryParams.put("sort_by", SEARCH_SORT_BY);
        queryParams.put("sort_order", SEARCH_SORT_ORDER);

        Allure.step("Send GET /search/advanced with configured filters");
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/search/advanced", queryParams);

        Allure.step("Validate status code and response envelope for TC-SEARCH-001");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Object countObj = response.jsonPath().get("count");
        Assert.assertTrue(countObj instanceof Number, "count is a number");

        List<Map<String, Object>> products = response.jsonPath().getList("data");
        Assert.assertNotNull(products, "data is array");

        Map<String, Object> filtersApplied = response.jsonPath().getMap("filters_applied");
        Assert.assertNotNull(filtersApplied, "filters_applied is object");
        Assert.assertEquals(String.valueOf(filtersApplied.get("category")), SEARCH_CATEGORY, "category filter recorded");

        double minPrice = Double.parseDouble(SEARCH_MIN_PRICE);
        double maxPrice = Double.parseDouble(SEARCH_MAX_PRICE);

        if (!products.isEmpty()) {
            Map<String, Object> firstProduct = products.get(0);
            Assert.assertTrue(firstProduct.get("id") instanceof Number, "Product has id");
            Assert.assertTrue(firstProduct.get("name") instanceof String, "Product has name");
            Assert.assertTrue(firstProduct.get("price") instanceof Number, "Product has price");
            Assert.assertTrue(firstProduct.get("category") instanceof String, "Product has category");
            Assert.assertTrue(firstProduct.get("description") instanceof String, "Product has description");

            double price = ((Number) firstProduct.get("price")).doubleValue();
            Assert.assertTrue(price >= minPrice && price <= maxPrice, "Price is within requested range");

            productId = ((Number) firstProduct.get("id")).intValue();
            JsonUtility.saveValue("search_result_product_id", productId, IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying advanced product search with a combination of all available filters.
     *
     * @throws Exception if an error occurs.
     */
    @Test(description = "TC-SEARCH-002: Verify advanced search with all filter combinations", dependsOnMethods = "testAdvancedSearchWithFilters")
    public void testAdvancedSearchWithAllFilterCombinations() throws Exception {
        Allure.step("Prepare full filter combination for TC-SEARCH-002");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", SEARCH_QUERY);
        queryParams.put("category", SEARCH_COMBINED_CATEGORY);
        queryParams.put("min_price", SEARCH_COMBINED_MIN_PRICE);
        queryParams.put("max_price", SEARCH_COMBINED_MAX_PRICE);
        queryParams.put("sort_by", SEARCH_COMBINED_SORT_BY);
        queryParams.put("sort_order", SEARCH_COMBINED_SORT_ORDER);

        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/search/advanced", queryParams);

        Allure.step("Validate status code and payload for TC-SEARCH-002");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Object countObj = response.jsonPath().get("count");
        Assert.assertTrue(countObj instanceof Number, "count is a number");

        List<Map<String, Object>> products = response.jsonPath().getList("data");
        Assert.assertNotNull(products, "data is array");

        double minPrice = Double.parseDouble(SEARCH_COMBINED_MIN_PRICE);
        double maxPrice = Double.parseDouble(SEARCH_COMBINED_MAX_PRICE);
        double previousPrice = Double.NEGATIVE_INFINITY;

        for (Map<String, Object> product : products) {
            Object priceObj = product.get("price");
            Assert.assertTrue(priceObj instanceof Number, "price is number");
            double price = ((Number) priceObj).doubleValue();
            Assert.assertTrue(price >= minPrice && price <= maxPrice, "Price is within combination range");

            Object categoryObj = product.get("category");
            Assert.assertTrue(categoryObj instanceof String, "category is string");
            Assert.assertEquals(String.valueOf(categoryObj), SEARCH_COMBINED_CATEGORY, "Category matches filter");

            Assert.assertTrue(price >= previousPrice, "Results sorted ascending by price");
            previousPrice = price;
        }
    }

    /**
     * Test case for verifying that product recommendations can be retrieved for a given product.
     *
     * @throws Exception if an error occurs while saving the recommended product ID.
     */
    @Test(description = "TC-SEARCH-003: Verify get product recommendations", dependsOnMethods = "testAdvancedSearchWithAllFilterCombinations")
    public void testGetProductRecommendations() throws Exception {

        Allure.step("Send GET /recommendations/{id}");
        Response response = ApiUtils.getRequest(BASE_URL + "/recommendations/" + SEARCH_RECOMMENDATION_PRODUCT_ID);

        Allure.step("Validate status code and response body for TC-SEARCH-003");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Object countObj = response.jsonPath().get("count");
        Assert.assertTrue(countObj instanceof Number, "count is a number");

        List<Map<String, Object>> recommendations = response.jsonPath().getList("data");
        Assert.assertNotNull(recommendations, "data is array");

        if (!recommendations.isEmpty()) {
            Map<String, Object> firstRecommendation = recommendations.get(0);
            Assert.assertTrue(firstRecommendation.get("id") instanceof Number, "Recommendation has id");
            Assert.assertTrue(firstRecommendation.get("name") instanceof String, "Recommendation has name");
            Assert.assertTrue(firstRecommendation.get("price") instanceof Number, "Recommendation has price");
            Assert.assertTrue(firstRecommendation.get("category") instanceof String, "Recommendation has category");
            Assert.assertTrue(firstRecommendation.get("description") instanceof String, "Recommendation has description");

            JsonUtility.saveValue("recommendation_product_id",
                    ((Number) firstRecommendation.get("id")).intValue(),
                    IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying that retrieving recommendations for a non-existent product fails.
     */
    @Test(description = "TC-SEARCH-004: Verify recommendations for non-existent product", dependsOnMethods = "testGetProductRecommendations")
    public void testRecommendationsForNonExistentProduct() {
        Allure.step("Send GET /recommendations/{invalid_id}");
        Response response = ApiUtils.getRequest(BASE_URL + "/recommendations/" + SEARCH_INVALID_PRODUCT_ID);

        Allure.step("Validate status code and error payload for TC-SEARCH-004");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        Assert.assertTrue(response.jsonPath().get("error") instanceof String, "Error message is string");
    }
}
