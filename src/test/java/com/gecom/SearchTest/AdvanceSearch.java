package com.gecom.SearchTest;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;

import static com.gecom.utils.Base.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "SearchTest")
@Severity(SeverityLevel.NORMAL)
public class AdvanceSearch {

    @Test(description = "TC-SEARCH-001: Verify advanced product search with filters", groups = {
            "Valid-Search-Test", "valid" })
    public void testAdvancedSearchWithFilters() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", SEARCH_QUERY);
        queryParams.put("category", SEARCH_CATEGORY);
        queryParams.put("min_price", SEARCH_MIN_PRICE);
        queryParams.put("max_price", SEARCH_MAX_PRICE);
        queryParams.put("min_rating", SEARCH_MIN_RATING);
        queryParams.put("sort_by", SEARCH_SORT_BY);
        queryParams.put("sort_order", SEARCH_SORT_ORDER);

        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/search/advanced", queryParams);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Object countObj = response.jsonPath().get("count");
        Assert.assertTrue(countObj instanceof Number, "count is a number");

        List<Map<String, Object>> products = response.jsonPath().getList("data");
        Assert.assertNotNull(products, "data is array");

        Map<String, Object> filtersApplied = response.jsonPath().getMap("filters_applied");
        Assert.assertNotNull(filtersApplied, "filters_applied is object");
        Assert.assertEquals(String.valueOf(filtersApplied.get("category")), SEARCH_CATEGORY,
                "category filter recorded");

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

    @Test(description = "TC-SEARCH-002: Verify advanced search with all filter combinations", groups = {
            "Valid-Search-Test", "vlaid" })
    public void testAdvancedSearchWithAllFilterCombinations() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", SEARCH_QUERY);
        queryParams.put("category", SEARCH_COMBINED_CATEGORY);
        queryParams.put("min_price", SEARCH_COMBINED_MIN_PRICE);
        queryParams.put("max_price", SEARCH_COMBINED_MAX_PRICE);
        queryParams.put("sort_by", SEARCH_COMBINED_SORT_BY);
        queryParams.put("sort_order", SEARCH_COMBINED_SORT_ORDER);

        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/search/advanced", queryParams);

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

    @Test(description = "TC-SEARCH-003: Verify get product recommendations", groups = {
            "Valid-Search-Test", "valid" })
    public void testGetProductRecommendations() throws Exception {

        Response response = ApiUtils.getRequest(BASE_URL + "/recommendations/" + SEARCH_RECOMMENDATION_PRODUCT_ID);

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
            Assert.assertTrue(firstRecommendation.get("description") instanceof String,
                    "Recommendation has description");

            JsonUtility.saveValue("recommendation_product_id",
                    ((Number) firstRecommendation.get("id")).intValue(),
                    IDS_FILE_PATH);
        }
    }

    @Test(description = "TC-SEARCH-004: Verify recommendations for non-existent product", groups = {
            "Invalid-Search-Test", "invalid" })
    public void testRecommendationsForNonExistentProduct() {
        Response response = ApiUtils.getRequest(BASE_URL + "/recommendations/" + SEARCH_INVALID_PRODUCT_ID);

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        Assert.assertTrue(response.jsonPath().get("error") instanceof String, "Error message is string");
    }
}
