package com.gecom.HelpcenterTest;

import static com.gecom.utils.Base.*;

import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "HelpcenterTest")
@Severity(SeverityLevel.NORMAL)
public class GetHelpArticles {

    @Test(description = "TC-HELP-005: Verify get all help articles", groups = { "Valid-Help-Articles-Test", "valid" })
    public void testGetAllHelpArticles() {
        Response response = ApiUtils.getRequest(BASE_URL + "/help");

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        List<Map<String, Object>> articles = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, articles.size(), "count matches array length");

        if (!articles.isEmpty()) {
            Map<String, Object> firstArticle = articles.get(0);
            Assert.assertTrue(firstArticle.get("id") instanceof Integer, "Each article has id");
            Assert.assertTrue(firstArticle.get("question") instanceof String, "Each article has question");
            Assert.assertTrue(firstArticle.get("answer") instanceof String, "Each article has answer");
            Assert.assertTrue(firstArticle.get("category") instanceof String, "Each article has category");
            Assert.assertTrue(firstArticle.get("helpful_count") instanceof Integer, "Each article has helpful_count");
        }
    }

    @Test(description = "TC-HELP-006: Verify get help categories", groups = { "Valid-Help-Articles-Test",
            "valid" }, dependsOnMethods = "testGetAllHelpArticles")
    public void testGetHelpCategories() {
        Response response = ApiUtils.getRequest(BASE_URL + "/help/categories");

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        List<String> categories = response.jsonPath().getList("data");
        Assert.assertNotNull(categories, "data is array of categories");
    }

    @Test(description = "TC-HELP-007: Verify get specific help article from previous tests", groups = {
            "Valid-Help-Articles-Test", "valid" }, dependsOnMethods = "testGetHelpCategories")
    public void testGetSpecificHelpArticle() throws Exception {
        helpId = (Integer) JsonUtility.getValue("helpArticleId", IDS_FILE_PATH);
        Assert.assertNotNull(helpId, "Help article ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/help/" + helpId);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertNotNull(response.jsonPath().get("data"), "data contains complete article info");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "data has id");
        Assert.assertTrue(response.jsonPath().get("data.question") instanceof String, "data has question");
        Assert.assertTrue(response.jsonPath().get("data.answer") instanceof String, "data has answer");
        Assert.assertTrue(response.jsonPath().get("data.category") instanceof String, "data has category");
        Assert.assertTrue(response.jsonPath().get("data.helpful_count") instanceof Integer, "data has helpful_count");
    }

    @Test(description = "TC-HELP-008: Verify get help article fails for non-existent ID", groups = {
            "Invalid-Help-Articles-Test", "invalid" }, dependsOnMethods = "testGetSpecificHelpArticle")
    public void testGetHelpArticleFailsForNonExistentId() {
        Response response = ApiUtils.getRequest(BASE_URL + "/help/" + INVALID_HELP_ARTICLE_ID);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Article not found"), "error is 'Article not found'");
    }

}
