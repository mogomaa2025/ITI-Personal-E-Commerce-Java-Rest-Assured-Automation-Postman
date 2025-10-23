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

@Listeners({AllureTestNg.class})
public class ReviewsTest {


    @Test(groups = "ReviewsTest")
    public void testCreateProductDuplicate() throws Exception {
        Allure.step("Starting testCreateProductDuplicate...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        //body
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

        productId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("product_id", String.valueOf(productId), IDS_FILE_PATH);
        Allure.step("Product ID saved: " + productId);

        Allure.step("testCreateProductDuplicate finished successfully.");
    }

    @Test(groups = "ReviewsTest",  dependsOnMethods = "testCreateProductDuplicate")
    public void testCreateReview() throws Exception {
        Allure.step("Starting testCreateReview...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        String productId = JsonUtility.getToken("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productId, "Product ID not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("product_id", Integer.parseInt(productId));
        body.put("rating", 3);
        body.put("comment", "Excellent product2!");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/reviews", userToken, body);
        Allure.step("Create review API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");

        Allure.step("testCreateReview finished successfully.");
    }

    @Test(groups = "ReviewsTest",  dependsOnMethods = "testCreateReview")
    public void testGetProductReviews() throws Exception {
        Allure.step("Starting testGetProductReviews...");
        String productId = JsonUtility.getToken("product_id", IDS_FILE_PATH);
        Assert.assertNotNull(productId, "Product ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/products/" + productId + "/reviews");
        Allure.step("Get product reviews API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success")); // checks if "success" is true


        //check average rating is double and not null
        Double averageRating = response.jsonPath().getDouble("average_rating");
        Assert.assertNotNull(averageRating, "Average rating should be present");
        System.out.println(averageRating);


        //check each review
        List<Map<String, Object>> reviews = response.jsonPath().getList("data");
        System.out.println("Reviews: " + reviews); //log response data
        for (Map<String, Object> review : reviews) {
            Assert.assertEquals(review.get("product_id"), Integer.parseInt(productId));
            Assert.assertTrue(review.get("rating") instanceof Number, "Rating should be numeric");
            Assert.assertTrue(review.get("comment") instanceof String, "Comment should be a string");
        }

        Allure.step("testGetProductReviews finished successfully.");
    }
}
