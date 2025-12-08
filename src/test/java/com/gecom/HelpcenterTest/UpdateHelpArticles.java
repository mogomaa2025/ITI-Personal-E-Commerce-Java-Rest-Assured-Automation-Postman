package com.gecom.HelpcenterTest;

import static com.gecom.utils.Base.*;
import java.util.HashMap;
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
public class UpdateHelpArticles {

        @Test(description = "TC-HELP-003: Verify admin can update help article", groups = {
                        "Valid-Help-Articles-Test", "valid" })
        public void testAdminCanUpdateHelpArticle() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Response responseGetHelp = ApiUtils.getRequest(BASE_URL + "/help");
                Assert.assertEquals(responseGetHelp.getStatusCode(), 200, "Status code is 200");
                List<Map<String, Object>> usersArray = responseGetHelp.jsonPath().getList("data");
                Assert.assertTrue(usersArray != null && !usersArray.isEmpty(), "data is array");
                int dataSize = responseGetHelp.jsonPath().getList("data").size();
                Map<String, Object> lastID = usersArray.get(dataSize - 1); // if datasize will give out of bound #GOMAA
                helpId = (Integer) lastID.get("id"); // object to int
                Assert.assertNotNull(helpId, "help ID is valid Integer");
                System.out.println("help ID is: " + helpId + "datasize = " + dataSize + " ");
                JsonUtility.saveValue("helpArticleId", helpId, IDS_FILE_PATH);

                Map<String, Object> body = new HashMap<>();
                body.put("question", HELP_ARTICLE_UPDATED_QUESTION);
                body.put("answer", HELP_ARTICLE_UPDATED_ANSWER);
                body.put("category", HELP_ARTICLE_UPDATED_CATEGORY);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/help/" + helpId, adminToken, body);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Article updated successfully",
                                "message is 'Article updated successfully'");
        }

        @Test(description = "TC-HELP-004: Verify update help article fails for non-existent article", groups = {
                        "Invalid-Help-Articles-Test", "invalid" })
        public void testUpdateHelpArticleFailsForNonExistent() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, Object> body = new HashMap<>();
                body.put("question", HELP_ARTICLE_QUESTION);
                body.put("answer", HELP_ARTICLE_ANSWER);
                body.put("category", HELP_ARTICLE_CATEGORY);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/help/" + INVALID_HELP_ARTICLE_ID,
                                adminToken,
                                body);

                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Article not found"),
                                "error indicates non existing article");
        }

}
