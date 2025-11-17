package com.gecom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

import static com.gecom.utils.Const.*;
import static com.gecom.utils.Const.ProductIDWithoutReview;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "HelpcenterTest")
@Severity(SeverityLevel.CRITICAL)
public class HelpcenterTest {

    @Test(description = "TC-HELP-001: Verify admin can create help article")
    public void testAdminCanCreateHelpArticle() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send POST with article data");
        Map<String, Object> body = new HashMap<>();
        body.put("question", HELP_ARTICLE_QUESTION);
        body.put("answer", HELP_ARTICLE_ANSWER);
        body.put("category", HELP_ARTICLE_CATEGORY);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help", adminToken, body);

        Allure.step("Verify created");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Article created successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Article created successfully", "message is 'Article created successfully'");

    }

    @Test(description = "TC-HELP-002: Verify create help article fails without admin", dependsOnMethods = "testAdminCanCreateHelpArticle")
    public void testCreateHelpArticleFailsWithoutAdmin() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST");
        Map<String, Object> body = new HashMap<>();
        body.put("question", HELP_ARTICLE_QUESTION);
        body.put("answer", HELP_ARTICLE_ANSWER);
        body.put("category", HELP_ARTICLE_CATEGORY);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help", userToken, body);

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

    @Test(description = "TC-HELP-003: Verify admin can update help article", dependsOnMethods = "testCreateHelpArticleFailsWithoutAdmin")
    public void testAdminCanUpdateHelpArticle() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send GET to help endpoint");
        Response responseGetHelp = ApiUtils.getRequest(BASE_URL + "/help");
        Allure.step("Verify articles returned");
        Assert.assertEquals(responseGetHelp.getStatusCode(), 200, "Status code is 200");
        Allure.step("get help ID");
        List<Map<String, Object>> usersArray = responseGetHelp.jsonPath().getList("data");
        Assert.assertTrue(usersArray != null && !usersArray.isEmpty(), "data is array");
        int dataSize = responseGetHelp.jsonPath().getList("data").size();
        Map<String, Object> lastID = usersArray.get(dataSize-1); //if datasize will give out of bound #GOMAA
        helpId = (Integer) lastID.get("id"); // object to int
        Assert.assertNotNull(helpId, "help ID is valid Integer");
        System.out.println("help ID is: " + helpId + "datasize = "+dataSize+" ");
        JsonUtility.saveValue("helpArticleId", helpId, IDS_FILE_PATH);



        Allure.step("Send PUT with updates");
        Map<String, Object> body = new HashMap<>();
        body.put("question", HELP_ARTICLE_UPDATED_QUESTION);
        body.put("answer", HELP_ARTICLE_UPDATED_ANSWER);
        body.put("category", HELP_ARTICLE_UPDATED_CATEGORY);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/help/" + helpId, adminToken, body);

        Allure.step("Verify updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Article updated successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Article updated successfully", "message is 'Article updated successfully'");
    }

    @Test(description = "TC-HELP-004: Verify update help article fails for non-existent article", dependsOnMethods = "testAdminCanUpdateHelpArticle")
    public void testUpdateHelpArticleFailsForNonExistent() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send PUT with invalid article ID");
        Map<String, Object> body = new HashMap<>();
        body.put("question", HELP_ARTICLE_QUESTION);
        body.put("answer", HELP_ARTICLE_ANSWER);
        body.put("category", HELP_ARTICLE_CATEGORY);

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/help/" + INVALID_HELP_ARTICLE_ID, adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates non existing article");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Article not found"), "error indicates non existing article");
    }

    @Test(description = "TC-HELP-005: Verify get all help articles", dependsOnMethods = "testUpdateHelpArticleFailsForNonExistent")
    public void testGetAllHelpArticles() {
        Allure.step("Send GET to help endpoint");
        Response response = ApiUtils.getRequest(BASE_URL + "/help");

        Allure.step("Verify articles returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify count matches array length");
        List<Map<String, Object>> articles = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, articles.size(), "count matches array length");

        Allure.step("Verify each article has id/question/answer/category/helpful_count");
        if (!articles.isEmpty()) {
            Map<String, Object> firstArticle = articles.get(0);
            Assert.assertTrue(firstArticle.get("id") instanceof Integer, "Each article has id");
            Assert.assertTrue(firstArticle.get("question") instanceof String, "Each article has question");
            Assert.assertTrue(firstArticle.get("answer") instanceof String, "Each article has answer");
            Assert.assertTrue(firstArticle.get("category") instanceof String, "Each article has category");
            Assert.assertTrue(firstArticle.get("helpful_count") instanceof Integer, "Each article has helpful_count");
        }
    }

    @Test(description = "TC-HELP-006: Verify get help categories", dependsOnMethods = "testGetAllHelpArticles")
    public void testGetHelpCategories() {
        Allure.step("Send GET to help categories");
        Response response = ApiUtils.getRequest(BASE_URL + "/help/categories");

        Allure.step("Verify categories returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array of categories");
        List<String> categories = response.jsonPath().getList("data");
        Assert.assertNotNull(categories, "data is array of categories");
    }

    @Test(description = "TC-HELP-007: Verify get specific help article from previous tests", dependsOnMethods = "testGetHelpCategories")
    public void testGetSpecificHelpArticle() throws Exception {
        Allure.step("Get help article ID");
        helpId = JsonUtility.getJSONInt("helpArticleId", IDS_FILE_PATH);
        Assert.assertNotNull(helpId, "Help article ID not found");

        Allure.step("Send GET for specific article");
        Response response = ApiUtils.getRequest(BASE_URL + "/help/" + helpId);

        Allure.step("Verify article details");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data contains complete article info");
        Assert.assertNotNull(response.jsonPath().get("data"), "data contains complete article info");

        Allure.step("Verify article data types");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.question") instanceof String, "data has question");
        Assert.assertTrue(response.jsonPath().get("data.answer") instanceof String, "data has answer");
        Assert.assertTrue(response.jsonPath().get("data.category") instanceof String, "data has category");
        Assert.assertTrue(response.jsonPath().get("data.helpful_count") instanceof Integer, "data has helpful_count");
    }

    @Test(description = "TC-HELP-008: Verify get help article fails for non-existent ID", dependsOnMethods = "testGetSpecificHelpArticle")
    public void testGetHelpArticleFailsForNonExistentId() {
        Allure.step("Send GET with invalid article ID");
        Response response = ApiUtils.getRequest(BASE_URL + "/help/" + INVALID_HELP_ARTICLE_ID);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Article not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Article not found"), "error is 'Article not found'");
    }

    @Test(description = "TC-HELP-009: Verify user can mark article as helpful", dependsOnMethods = "testGetHelpArticleFailsForNonExistentId")
    public void testUserCanMarkArticleHelpful() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get help article ID");
        helpId = JsonUtility.getJSONInt("helpArticleId", IDS_FILE_PATH);
        Assert.assertNotNull(helpId, "Help article ID not found");

        Allure.step("Send POST to mark helpful");
        //  Map<String, Object> body = new HashMap<>();
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help/" + helpId + "/helpful", userToken, new HashMap<>()); // no body

        Allure.step("Verify helpful count increased");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Thank you for your feedback!'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Thank you for your feedback!", "message is 'Thank you for your feedback!'");
    }

    @Test(description = "TC-HELP-010: Verify user can mark article as helpful twice", dependsOnMethods = "testUserCanMarkArticleHelpful")
    public void testUserCanMarkArticleHelpfulTwice() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get help article ID");
        helpId = JsonUtility.getJSONInt("helpArticleId", IDS_FILE_PATH);
        Assert.assertNotNull(helpId, "Help article ID not found");

        Allure.step("Send POST to mark helpful again");
      //  Map<String, Object> body = new HashMap<>();
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help/" + helpId + "/helpful", userToken, new HashMap<>()); // no body

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'You have already marked this article as helpful'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("already marked this article as helpful"), "error is 'You have already marked this article as helpful'");

        Allure.step("Verify already_helpful is true");
        Assert.assertTrue(response.jsonPath().getBoolean("already_helpful"), "already_helpful is true");
    }

    @Test(description = "TC-HELP-011: Verify mark helpful fails for non-existent article", dependsOnMethods = "testUserCanMarkArticleHelpfulTwice")
    public void testMarkHelpfulFailsForNonExistentArticle() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST with invalid article ID");
        //  Map<String, Object> body = new HashMap<>();
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help/" + INVALID_HELP_ARTICLE_ID + "/helpful", userToken, new HashMap<>()); // no body

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Article not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Article not found"), "error is 'Article not found'");
    }
}
