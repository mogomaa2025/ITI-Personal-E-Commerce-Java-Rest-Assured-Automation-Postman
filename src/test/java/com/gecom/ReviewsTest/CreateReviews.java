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
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "ReviewsTest")
@Severity(SeverityLevel.CRITICAL)
public class CreateReviews {

        @Test(description = "TC-REV-001: Verify user can create product review", groups = {
                        "Valid-Reviews-Test", "valid" })
        public void testUserCanCreateProductReview() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                reviewProductId = REVIEW_PRODUCT_ID_FOR_REVIEW;

                Map<String, Object> body = new HashMap<>();
                body.put("product_id", reviewProductId);
                body.put("rating", REVIEW_RATING);
                body.put("comment", REVIEW_COMMENT);
                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

                Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Review created successfully",
                                "message is 'Review created successfully'");

                Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
                Assert.assertTrue(response.jsonPath().get("data.product_id") instanceof Integer, "data has product_id");
                Assert.assertTrue(response.jsonPath().get("data.user_id") instanceof Integer, "data has user_id");
                Assert.assertTrue(response.jsonPath().get("data.rating") instanceof Integer, "data has rating");
                Assert.assertTrue(response.jsonPath().get("data.comment") instanceof String, "data has comment");
                Assert.assertNotNull(response.jsonPath().get("data.created_at"), "data has created_at");

                Assert.assertEquals(response.jsonPath().getInt("data.rating"), REVIEW_RATING, "rating matches input");
                Assert.assertEquals(response.jsonPath().getString("data.comment"), REVIEW_COMMENT,
                                "comment matches input");

                reviewId = response.jsonPath().getInt("data.id");
                JsonUtility.saveValue("review_id", reviewId, IDS_FILE_PATH);
                JsonUtility.saveValue("review_product_id", reviewProductId, IDS_FILE_PATH);
        }

        @Test(description = "TC-REV-002: Verify create review fails for non-existent product", groups = {
                        "Invalid-Reviews-Test", "invalid" })
        public void testCreateReviewFailsForNonExistentProduct() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                Map<String, Object> body = new HashMap<>();
                body.put("product_id", INVALID_PRODUCT_ID);
                body.put("rating", 5);
                body.put("comment", "Great");
                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Product not found"), "error is 'Product not found'");
        }

        @Test(description = "TC-REV-003: Verify user cannot review same product twice", groups = {
                        "Invalid-Reviews-Test", "invalid" })
        public void testUserCannotReviewSameProductTwice() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                reviewProductId = (Integer) JsonUtility.getValue("review_product_id", IDS_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");
                Assert.assertNotNull(reviewProductId, "Product ID is valid Integer");
                Map<String, Object> body = new HashMap<>();
                body.put("product_id", reviewProductId);
                body.put("rating", 5);
                body.put("comment", REVIEW_COMMENT_ANOTHER);
                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("You have already reviewed this product"),
                                "error is 'You have already reviewed this product'");
        }

        @Test(description = "TC-REV-004: Verify create review fails with invalid rating", groups = {
                        "Invalid-Reviews-Test", "invalid" })
        public void testCreateReviewFailsWithInvalidRating() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                Map<String, Object> body = new HashMap<>();
                body.put("product_id", REVIEW_PRODUCT_ID_FOR_REVIEW);
                body.put("rating", REVIEW_RATING_INVALID);
                body.put("comment", "Test");
                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);

                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Rating must be between 1 and 5"),
                                "error indicates invalid rating");
        }

}
