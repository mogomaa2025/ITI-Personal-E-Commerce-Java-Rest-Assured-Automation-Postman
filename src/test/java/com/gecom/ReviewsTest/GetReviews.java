package com.gecom.ReviewsTest;

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
@Test(groups = "ReviewsTest")
@Severity(SeverityLevel.CRITICAL)
public class GetReviews {

        @Test(description = "TC-REV-005: Verify get product reviews with average rating", groups = {
                        "Valid-Reviews-Test", "valid" })
        public void testGetProductReviewsWithAverageRating() throws Exception {
                reviewProductId = (Integer) JsonUtility.getValue("review_product_id", IDS_FILE_PATH);
                Assert.assertNotNull(reviewProductId, "Product ID is valid Integer");

                Response response = ApiUtils.getRequest(BASE_URL + "/products/" + reviewProductId + "/reviews");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

                List<Map<String, Object>> reviews = response.jsonPath().getList("data");
                Assert.assertNotNull(reviews, "data is array");

                int count = response.jsonPath().getInt("count");
                Assert.assertEquals(count, reviews.size(), "count matches array length");

                Object averageRatingObj = response.jsonPath().get("average_rating");
                Assert.assertTrue(averageRatingObj instanceof Number, "average_rating is calculated correctly");

                if (!reviews.isEmpty()) {
                        Map<String, Object> firstReview = reviews.get(0);
                        Assert.assertTrue(firstReview.get("id") instanceof Integer, "Each review has id");
                        Assert.assertTrue(firstReview.get("product_id") instanceof Integer,
                                        "Each review has product_id");
                        Assert.assertTrue(firstReview.get("user_id") instanceof Integer, "Each review has user_id");
                        Assert.assertTrue(firstReview.get("rating") instanceof Integer, "Each review has rating");
                        Assert.assertTrue(firstReview.get("comment") instanceof String, "Each review has comment");
                        Assert.assertNotNull(firstReview.get("created_at"), "Each review has created_at");
                }
        }

        @Test(description = "TC-REV-006: Verify get reviews returns empty for product without reviews", groups = {
                        "Valid-Reviews-Test", "valid" })
        public void testGetReviewsReturnsEmptyForProductWithoutReviews() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, Object> productBody = new HashMap<>();
                productBody.put("name", PRODUCT_NAME);
                productBody.put("description", DESCRIPTION);
                productBody.put("price", PRICE);
                productBody.put("category", CATEGORY);
                productBody.put("stock", STOCK);
                productBody.put("image_url", IMAGE_URL);
                Response responseCreatePrdouct = ApiUtils.postRequestWithAuth(BASE_URL + "/products", adminToken,
                                productBody);
                Assert.assertEquals(responseCreatePrdouct.getStatusCode(), 201, "Status code is 201");
                ProductIDWithoutReview = responseCreatePrdouct.jsonPath().getInt("data.id");
                Assert.assertNotNull(ProductIDWithoutReview, "Product ID is valid Integer");

                Response response = ApiUtils.getRequest(BASE_URL + "/products/" + ProductIDWithoutReview + "/reviews");
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                List<Map<String, Object>> reviews = response.jsonPath().getList("data");
                Assert.assertNotNull(reviews, "data is array");
                Assert.assertTrue(reviews.isEmpty(), "data is empty array");

                Assert.assertEquals(response.jsonPath().getInt("count"), 0, "count is 0");

                Object averageRating = response.jsonPath().get("average_rating");
                Assert.assertTrue(
                                averageRating == null
                                                || (averageRating instanceof Number
                                                                && ((Number) averageRating).doubleValue() == 0.0),
                                "average_rating is 0 or null");
        }

        @Test(description = "TC-REV-007: Verify user can check review status for product", groups = {
                        "Valid-Reviews-Test", "valid" })
        public void testUserCanCheckReviewStatusForProduct() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                reviewProductId = (Integer) JsonUtility.getValue("review_product_id", IDS_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Assert.assertNotNull(reviewProductId, "Product ID is valid Integer");
                Response response = ApiUtils.getRequestWithAuth(
                                BASE_URL + "/products/" + reviewProductId + "/reviews/check",
                                userToken);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertTrue(response.jsonPath().get("has_reviewed") instanceof Boolean,
                                "has_reviewed is boolean");

                boolean hasReviewed = response.jsonPath().getBoolean("has_reviewed");
                if (hasReviewed) {
                        Assert.assertTrue(response.jsonPath().get("review") instanceof Map,
                                        "If has_reviewed=true review object present");
                } else {
                        Assert.assertNull(response.jsonPath().get("review"), "If has_reviewed=false review is null");
                }
        }

        @Test(description = "TC-REV-008: Verify check review status before and after reviewing", groups = {
                        "Valid-Reviews-Test", "valid" })
        public void testCheckReviewStatusBeforeAndAfterReviewing() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Response firstResponse = ApiUtils
                                .getRequestWithAuth(BASE_URL + "/products/" + ProductIDWithoutReview + "/reviews/check",
                                                userToken);
                Assert.assertEquals(firstResponse.getStatusCode(), 200, "First request returns 200");
                Assert.assertFalse(firstResponse.jsonPath().getBoolean("has_reviewed"), "First has_reviewed is false");
                Map<String, Object> body = new HashMap<>();
                body.put("product_id", ProductIDWithoutReview);
                body.put("rating", REVIEW_RATING);
                body.put("comment", REVIEW_COMMENT);
                ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

                Response secondResponse = ApiUtils
                                .getRequestWithAuth(BASE_URL + "/products/" + ProductIDWithoutReview + "/reviews/check",
                                                userToken);
                Assert.assertEquals(secondResponse.getStatusCode(), 200, "Second request returns 200");
                Assert.assertTrue(secondResponse.jsonPath().getBoolean("has_reviewed"), "Second has_reviewed is true");
                Assert.assertTrue(secondResponse.jsonPath().get("review") instanceof Map,
                                "Second review object present");
        }
}
