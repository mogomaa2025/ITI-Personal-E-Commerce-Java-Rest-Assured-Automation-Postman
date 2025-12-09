package com.gecom.ProductsTest;

import static com.gecom.utils.Base.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gecom.utils.Logger;
import com.gecom.utils.TestListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
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
public class LikeProducts {



    @BeforeMethod(onlyForGroups = "fresh-user", alwaysRun = true)
    public void freshAccount() throws Exception {
        Logger.info("::freshAccount::groups");
        TestListener.loginUserToken();
    }


    @Test(description = "TC-PROD-026: Verify user can like a product", groups = {
            "Valid-Products-Test", "valid", "fresh-user" })
    public void testUserCanLikeProduct() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");


        Map<String, Object> body = new HashMap<>();
        body.put("product_id", LIKE_PRODUCT_ID);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products/likes", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product liked successfully",
                "message is 'Product liked successfully'");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.product_id") instanceof Integer, "data has product_id");
        Assert.assertTrue(response.jsonPath().get("data.user_id") instanceof Integer, "data has user_id");
        Assert.assertNotNull(response.jsonPath().get("data.created_at"), "data has created_at");
        Assert.assertEquals(response.jsonPath().getInt("data.product_id"), LIKE_PRODUCT_ID, "product_id matches");
        likeId = response.jsonPath().getInt("data.id");
        JsonUtility.saveValue("like_id", likeId, IDS_FILE_PATH);
    }

    @Test(description = "TC-PROD-027: Verify user cannot like same product twice", groups = {
            "Invalid-Products-Test", "invalid" }, dependsOnMethods = "testUserCanLikeProduct")
    public void testUserCannotLikeSameProductTwice() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");


        Map<String, Object> body = new HashMap<>();
        body.put("product_id", LIKE_PRODUCT_ID);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products/likes", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("already liked"),
                "error is 'You have already liked this product'");
        Assert.assertTrue(response.jsonPath().getBoolean("already_liked"), "already_liked is true");
    }

    @Test(description = "TC-PROD-028: Verify like fails for non-existent product", groups = {
            "Invalid-Products-Test", "invalid" }, dependsOnMethods = "testUserCannotLikeSameProductTwice")
    public void testLikeFailsForNonExistentProduct() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", 99999);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/products/likes", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
    }

    @Test(description = "TC-PROD-029: Verify get product likes count", groups = {
            "Valid-Products-Test", "valid" }, dependsOnMethods = "testLikeFailsForNonExistentProduct")
    public void testGetProductLikesCount() {

        Response response = ApiUtils.getRequest(BASE_URL + "/products/" + PRODUCT_ID_LIKES_TO_COUNT + "/likes");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");
        int count = response.jsonPath().getInt("count");
        List<Map<String, Object>> likes = response.jsonPath().getList("data");
        Assert.assertEquals(count, likes.size(), "count matches array length");
        if (!likes.isEmpty()) {
            Map<String, Object> like = likes.get(0);
            Assert.assertTrue(like.get("id") instanceof Integer, "Each like has id");
            Assert.assertTrue(like.get("product_id") instanceof Integer, "Each like has product_id");
            Assert.assertTrue(like.get("user_id") instanceof Integer, "Each like has user_id");
            Assert.assertNotNull(like.get("created_at"), "Each like has created_at");
        }
    }

    @Test(description = "TC-PROD-030: Verify user can check like status", groups = {
            "Valid-Products-Test", "valid" }, dependsOnMethods = "testGetProductLikesCount")
    public void testUserCanCheckLikeStatus() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils
                .getRequestWithAuth(BASE_URL + "/products/" + PRODUCT_ID_LIKES_TO_COUNT + "/likes/check", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertTrue(response.jsonPath().get("liked") instanceof Boolean, "liked is boolean");
        boolean liked = response.jsonPath().getBoolean("liked");
        if (liked) {
            Assert.assertNotNull(response.jsonPath().get("data"), "If liked=true data has like object");
        } else {
            Assert.assertNull(response.jsonPath().get("data"), "If liked=false data is null");
            Assert.assertEquals(response.jsonPath().get("data"), "null", "If liked=false data is null");
        }
    }

    @Test(description = "TC-PROD-031: Verify user can unlike product", groups = {
            "Valid-Products-Test", "valid" }, dependsOnMethods = "testUserCanCheckLikeStatus")
    public void testUserCanUnlikeProduct() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        likeId = (Integer) JsonUtility.getValue("like_id", IDS_FILE_PATH);
        Assert.assertNotNull(likeId, "Like ID not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/likes/" + likeId, userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertEquals(response.jsonPath().getString("message"), "Product unliked successfully",
                "message is 'Product unliked successfully'");
        Assert.assertNotNull(response.jsonPath().get("data"), "data has like object");

    }

    @Test(description = "TC-PROD-032: Verify unlike fails for non-existent like", groups = {
            "Invalid-Products-Test", "invalid" }, dependsOnMethods = "testUserCanUnlikeProduct")
    public void testUnlikeFailsForNonExistentLike() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/likes/99999", userToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        Assert.assertTrue(response.jsonPath().getString("error").contains("not found"), "error is 'Like not found'");
    }

    @Test(description = "TC-PROD-033: Verify user cannot unlike another user's like", groups = {
            "Invalid-Products-Test", "invalid" }, dependsOnMethods = "testUnlikeFailsForNonExistentLike")
    public void testUserCannotUnlikeAnotherUsersLike() throws Exception {

        Response likesResponse = ApiUtils.getRequest(BASE_URL + "/products/" + PRODUCT_ID_LIKES_TO_COUNT + "/likes");
        List<Map<String, Object>> likes = likesResponse.jsonPath().getList("data");

        if (!likes.isEmpty()) {
            someLikeId = (Integer) likes.get(0).get("id");

            userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
            Assert.assertNotNull(userToken, "User token not found");

            Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/products/likes/" + someLikeId, userToken);

            Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
            Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
            Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
            Response verifyResponse = ApiUtils.getRequest(BASE_URL + "/products/1/likes");
            List<Map<String, Object>> afterLikes = verifyResponse.jsonPath().getList("data");
            Assert.assertEquals(afterLikes.size(), likes.size(), "Like remains");

        }
    }
}
