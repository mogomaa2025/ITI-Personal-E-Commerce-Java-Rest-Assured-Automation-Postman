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

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "HelpcenterTest")
public class HelpcenterTest {

    @Test
    public void testCreateHelpArticle() throws Exception {
        Allure.step("Starting testCreateHelpArticle...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("question", "How to track my order?");
        body.put("answer", "Use the orders page to check status.");
        body.put("category", "Orders");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help", userToken, body);
        Allure.step("Create help article response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getString("message"), "Article created successfully");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testCreateHelpArticle finished successfully.");
    }

    @Test(dependsOnMethods = "testCreateHelpArticle")
    public void testGetHelpCategories() {
        Allure.step("Starting testGetHelpCategories...");

        Response response = ApiUtils.getRequest(BASE_URL + "/help/categories");
        Allure.step("Help categories response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        //check all categories exist
        List<String> categories = response.jsonPath().getList("data");
        Assert.assertNotNull(categories, "Categories list should not be null");
        Assert.assertTrue(categories.contains("Returns"));
        Assert.assertTrue(categories.contains("General"));
        Assert.assertTrue(categories.contains("Payment"));
        Assert.assertTrue(categories.contains("Orders"));
        Assert.assertTrue(categories.contains("Shipping"));

        Allure.step("testGetHelpCategories finished successfully.");
    }

    @Test(dependsOnMethods = "testGetHelpCategories")
    public void testListHelpArticles() throws Exception {
        Allure.step("Starting testListHelpArticles...");

        Response response = ApiUtils.getRequest(BASE_URL + "/help?category="+testListHelpArticlesCaategory+"&search="+testListHelpArticlesSearch+""); //? make problem
        Allure.step("List help articles response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        //check all articles not null
        List<Map<String, Object>> articles = response.jsonPath().getList("data");
        System.out.println("testListHelpArticles: articles: " + articles);
        Assert.assertNotNull(articles, "Articles list should not be null");


        //save helpArticleId for next tests
        helpArticleId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("helpArticleId", String.valueOf(helpArticleId), IDS_FILE_PATH);
        Allure.step("helpArticleId saved: " + helpArticleId);

        Allure.step("testListHelpArticles finished successfully.");
    }

    @Test(dependsOnMethods = "testListHelpArticles")
    public void testGetHelpArticle() throws Exception {
        Allure.step("Starting testGetHelpArticle...");
        String helpId = JsonUtility.getToken("helpArticleId", IDS_FILE_PATH);
        Assert.assertNotNull(helpId, "Help article ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/help/" + helpId);
        Allure.step("Get help article response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");

        //check help article data not null
        Map<String, Object> data = response.jsonPath().getMap("data");
        System.out.println("testGetHelpArticle: data: " + data);
        Assert.assertNotNull(data, "Help article data should not be null");

        Allure.step("testGetHelpArticle finished successfully.");
    }


    @Test(dependsOnMethods = "testGetHelpArticle")
    public void testMarkHelpArticleHelpful() throws Exception {
    // Log the start of the test case in Allure report
        Allure.step("Starting testMarkHelpArticleHelpful...");
    // Retrieve user token from JSON file for authentication
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        String helpId = JsonUtility.getToken("helpArticleId", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Assert.assertNotNull(helpId, "Help article ID not found");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help/" + helpId + "/helpful", userToken, new HashMap<>());
        Allure.step("Mark helpful response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getString("message"), "Thank you for your feedback!");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testMarkHelpArticleHelpful finished successfully.");
    }

    @Test(dependsOnMethods = "testMarkHelpArticleHelpful")
    public void testUpdateHelpArticle() throws Exception {
        Allure.step("Starting testUpdateHelpArticle...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        String helpId = JsonUtility.getToken("helpArticleId", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Assert.assertNotNull(helpId, "Help article ID not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("question", "How to track my order?");
        body.put("answer", "Visit the orders page and use the tracking link.");
        body.put("category", "Orders");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/help/" + helpId, userToken, body);
        Allure.step("Update help article response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));


        Allure.step("testUpdateHelpArticle finished successfully.");
    }
}
