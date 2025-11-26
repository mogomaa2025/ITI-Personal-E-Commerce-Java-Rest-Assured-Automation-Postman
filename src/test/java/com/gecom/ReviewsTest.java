package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
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
 * This class contains test cases for the product review functionalities,
 * including creating and retrieving reviews.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "ReviewsTest")
@Severity(SeverityLevel.CRITICAL)
public class ReviewsTest {

    /**
     * Test case for verifying that a user can create a product review.
     *
     * @throws Exception if an error occurs while reading the user token or saving review data.
     */
    @Test(description = "TC-REV-001: Verify user can create product review")
    public void testUserCanCreateProductReview() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Get product ID for review");
        reviewProductId = REVIEW_PRODUCT_ID_FOR_REVIEW;

        Allure.step("Send POST with review data");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", reviewProductId);
        body.put("rating", REVIEW_RATING);
        body.put("comment", REVIEW_COMMENT);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

        Allure.step("Verify review created");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Review created successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Review created successfully", "message is 'Review created successfully'");

        Allure.step("Verify data has id/product_id/user_id/rating/comment/created_at");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.product_id") instanceof Integer, "data has product_id");
        Assert.assertTrue(response.jsonPath().get("data.user_id") instanceof Integer, "data has user_id");
        Assert.assertTrue(response.jsonPath().get("data.rating") instanceof Integer, "data has rating");
        Assert.assertTrue(response.jsonPath().get("data.comment") instanceof String, "data has comment");
        Assert.assertNotNull(response.jsonPath().get("data.created_at"), "data has created_at");

        Allure.step("Verify rating and comment match input");
        Assert.assertEquals(response.jsonPath().getInt("data.rating"), REVIEW_RATING, "rating matches input");
        Assert.assertEquals(response.jsonPath().getString("data.comment"), REVIEW_COMMENT, "comment matches input");

        Allure.step("Save review_id and product_id for later tests");
        reviewId = response.jsonPath().getInt("data.id");
        JsonUtility.saveValue("review_id", reviewId, IDS_FILE_PATH);
        JsonUtility.saveValue("review_product_id", reviewProductId, IDS_FILE_PATH);
    }

    /**
     * Test case for verifying that creating a review for a non-existent product fails.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-REV-002: Verify create review fails for non-existent product", dependsOnMethods = "testUserCanCreateProductReview")
    public void testCreateReviewFailsForNonExistentProduct() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send POST with invalid product_id");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", INVALID_PRODUCT_ID);
        body.put("rating", 5);
        body.put("comment", "Great");
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Product not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Product not found"), "error is 'Product not found'");
    }

    /**
     * Test case for verifying that a user cannot review the same product twice.
     *
     * @throws Exception if an error occurs while reading the user token or product ID.
     */
    @Test(description = "TC-REV-003: Verify user cannot review same product twice", dependsOnMethods = "testCreateReviewFailsForNonExistentProduct")
    public void testUserCannotReviewSameProductTwice() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        reviewProductId = JsonUtility.getJSONInt("review_product_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(reviewProductId, "Product ID is valid Integer");

        Allure.step("Try creating another review for same product");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", reviewProductId);
        body.put("rating", 5);
        body.put("comment", REVIEW_COMMENT_ANOTHER);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'You have already reviewed this product'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("You have already reviewed this product"), "error is 'You have already reviewed this product'");
    }

    /**
     * Test case for verifying that creating a review with an invalid rating fails.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-REV-004: Verify create review fails with invalid rating", dependsOnMethods = "testUserCannotReviewSameProductTwice")
    public void testCreateReviewFailsWithInvalidRating() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Send POST with rating > 5");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", REVIEW_PRODUCT_ID_FOR_REVIEW);
        body.put("rating", REVIEW_RATING_INVALID);
        body.put("comment", "Test");
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

        Allure.step("Verify validation error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates invalid rating");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Rating must be between 1 and 5"), "error indicates invalid rating");
    }

    /**
     * Test case for verifying that product reviews can be retrieved along with the average rating.
     *
     * @throws Exception if an error occurs while reading the product ID.
     */
    @Test(description = "TC-REV-005: Verify get product reviews with average rating", dependsOnMethods = "testCreateReviewFailsWithInvalidRating")
    public void testGetProductReviewsWithAverageRating() throws Exception {
        Allure.step("Get product ID");
        reviewProductId = JsonUtility.getJSONInt("review_product_id", IDS_FILE_PATH);
        Assert.assertNotNull(reviewProductId, "Product ID is valid Integer");

        Allure.step("Send GET for product reviews");
        Response response = ApiUtils.getRequest(BASE_URL + "/products/" + reviewProductId + "/reviews");

        Allure.step("Verify reviews and average");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        List<Map<String, Object>> reviews = response.jsonPath().getList("data");
        Assert.assertNotNull(reviews, "data is array");

        Allure.step("Verify count matches array length");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, reviews.size(), "count matches array length");

        Allure.step("Verify average_rating is calculated correctly");
        Object averageRatingObj = response.jsonPath().get("average_rating");
        Assert.assertTrue(averageRatingObj instanceof Number, "average_rating is calculated correctly");

        Allure.step("Verify each review has id/product_id/user_id/rating/comment/created_at");
        if (!reviews.isEmpty()) {
            Map<String, Object> firstReview = reviews.get(0);
            Assert.assertTrue(firstReview.get("id") instanceof Integer, "Each review has id");
            Assert.assertTrue(firstReview.get("product_id") instanceof Integer, "Each review has product_id");
            Assert.assertTrue(firstReview.get("user_id") instanceof Integer, "Each review has user_id");
            Assert.assertTrue(firstReview.get("rating") instanceof Integer, "Each review has rating");
            Assert.assertTrue(firstReview.get("comment") instanceof String, "Each review has comment");
            Assert.assertNotNull(firstReview.get("created_at"), "Each review has created_at");
        }
    }

    /**
     * Test case for verifying that an empty array is returned for a product with no reviews.
     *
     * @throws Exception if an error occurs while reading the admin token.
     */
    @Test(description = "TC-REV-006: Verify get reviews returns empty for product without reviews", dependsOnMethods = "testGetProductReviewsWithAverageRating")
    public void testGetReviewsReturnsEmptyForProductWithoutReviews() throws Exception {
        Allure.step("Get a product without reviews (use a different product ID)");

        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send POST with product data");
        Map<String, Object> productBody = new HashMap<>();
        productBody.put("name", PRODUCT_NAME);
        productBody.put("description", DESCRIPTION);
        productBody.put("price", PRICE);
        productBody.put("category", CATEGORY);
        productBody.put("stock", STOCK);
        productBody.put("image_url", IMAGE_URL);
        Response responseCreatePrdouct = ApiUtils.postRequestWithAuth(BASE_URL + "/products", adminToken, productBody);
        Allure.step("Verify created");
        Assert.assertEquals(responseCreatePrdouct.getStatusCode(), 201, "Status code is 201");
        Allure.step("Save product ID");
        ProductIDWithoutReview = responseCreatePrdouct.jsonPath().getInt("data.id");
        Assert.assertNotNull(ProductIDWithoutReview, "Product ID is valid Integer");

        Allure.step("Send GET for product without reviews");
        Response response = ApiUtils.getRequest(BASE_URL + "/products/" + ProductIDWithoutReview + "/reviews");

        Allure.step("Verify empty result");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify data is empty array");
        List<Map<String, Object>> reviews = response.jsonPath().getList("data");
        Assert.assertNotNull(reviews, "data is array");
        Assert.assertTrue(reviews.isEmpty(), "data is empty array");

        Allure.step("Verify count is 0");
        Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");

        Allure.step("Verify average_rating is 0 or null");
        Object averageRating = response.jsonPath().get("average_rating");
        Assert.assertTrue(averageRating == null || (averageRating instanceof Number && ((Number) averageRating).doubleValue() == 0.0), "average_rating is 0 or null");
    }

    /**
     * Test case for verifying that a user can check their review status for a product.
     *
     * @throws Exception if an error occurs while reading the user token or product ID.
     */
    @Test(description = "TC-REV-007: Verify user can check review status for product", dependsOnMethods = "testGetReviewsReturnsEmptyForProductWithoutReviews")
    public void testUserCanCheckReviewStatusForProduct() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        reviewProductId = JsonUtility.getJSONInt("review_product_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");
        Assert.assertNotNull(reviewProductId, "Product ID is valid Integer");

        Allure.step("Send GET to check status");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/products/" + reviewProductId + "/reviews/check", userToken);

        Allure.step("Verify status");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify has_reviewed is boolean");
        Assert.assertTrue(response.jsonPath().get("has_reviewed") instanceof Boolean, "has_reviewed is boolean");

        Allure.step("Verify review object based on has_reviewed");
        boolean hasReviewed = response.jsonPath().getBoolean("has_reviewed");
        if (hasReviewed) {
            Assert.assertTrue(response.jsonPath().get("review") instanceof Map, "If has_reviewed=true review object present");
        } else {
            Assert.assertNull(response.jsonPath().get("review"), "If has_reviewed=false review is null");
        }
    }

    /**
     * Test case for verifying the review status of a product before and after the user submits a review.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-REV-008: Verify check review status before and after reviewing", dependsOnMethods = "testUserCanCheckReviewStatusForProduct")
    public void testCheckReviewStatusBeforeAndAfterReviewing() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        Allure.step("Get a product ID for new review");
       // if(ProductIDWithoutReview == null){ ProductIDWithoutReview = 106; }
        Allure.step("Check status (should be false)");
        Response firstResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/products/" + ProductIDWithoutReview + "/reviews/check", userToken);
        Assert.assertEquals(firstResponse.getStatusCode(), 200, "First request returns 200");
        Assert.assertFalse(firstResponse.jsonPath().getBoolean("has_reviewed"), "First has_reviewed is false");

        Allure.step("Create review");
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", ProductIDWithoutReview);
        body.put("rating", REVIEW_RATING);
        body.put("comment", REVIEW_COMMENT);
        ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

        Allure.step("Check status again (should be true)");
        Response secondResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/products/" + ProductIDWithoutReview + "/reviews/check", userToken);
        Assert.assertEquals(secondResponse.getStatusCode(), 200, "Second request returns 200");
        Assert.assertTrue(secondResponse.jsonPath().getBoolean("has_reviewed"), "Second has_reviewed is true");

        Allure.step("Verify second review object present");
        Assert.assertTrue(secondResponse.jsonPath().get("review") instanceof Map, "Second review object present");
    }
}
