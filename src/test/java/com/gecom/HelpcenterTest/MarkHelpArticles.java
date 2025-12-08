package com.gecom.HelpcenterTest;

import static com.gecom.utils.Base.*;
import java.util.HashMap;

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
public class MarkHelpArticles {

        @Test(description = "TC-HELP-009: Verify user can mark article as helpful", groups = {
                        "Valid-Help-Articles-Test", "valid" })
        public void testUserCanMarkArticleHelpful() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");

                helpId = (Integer) JsonUtility.getValue("helpArticleId", IDS_FILE_PATH);
                Assert.assertNotNull(helpId, "Help article ID not found");

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help/" + helpId + "/helpful", userToken,
                                new HashMap<>()); // no body

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Thank you for your feedback!",
                                "message is 'Thank you for your feedback!'");
        }

        @Test(description = "TC-HELP-010: Verify user can mark article as helpful twice", groups = {
                        "Invalid-Help-Articles-Test", "invalid" })
        public void testUserCanMarkArticleHelpfulTwice() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");

                helpId = (Integer) JsonUtility.getValue("helpArticleId", IDS_FILE_PATH);
                Assert.assertNotNull(helpId, "Help article ID not found");

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help/" + helpId + "/helpful", userToken,
                                new HashMap<>()); // no body

                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("already marked this article as helpful"),
                                "error is 'You have already marked this article as helpful'");
                Assert.assertTrue(response.jsonPath().getBoolean("already_helpful"), "already_helpful is true");
        }

        @Test(description = "TC-HELP-011: Verify mark helpful fails for non-existent article", groups = {
                        "Invalid-Help-Articles-Test", "invalid" })
        public void testMarkHelpfulFailsForNonExistentArticle() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");
                Response response = ApiUtils.postRequestWithAuth(
                                BASE_URL + "/help/" + INVALID_HELP_ARTICLE_ID + "/helpful",
                                userToken, new HashMap<>()); // no body

                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Article not found"), "error is 'Article not found'");
        }
}
