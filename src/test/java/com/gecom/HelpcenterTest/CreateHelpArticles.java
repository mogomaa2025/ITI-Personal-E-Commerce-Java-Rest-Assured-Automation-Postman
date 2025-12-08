package com.gecom.HelpcenterTest;

import static com.gecom.utils.Base.*;
import java.util.HashMap;
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
public class CreateHelpArticles {

    @Test(description = "TC-HELP-001: Verify admin can create help article", groups = { "Valid-Help-Articles-Test",
            "valid" })
    public void testAdminCanCreateHelpArticle() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Map<String, Object> body = new HashMap<>();
        body.put("question", HELP_ARTICLE_QUESTION);
        body.put("answer", HELP_ARTICLE_ANSWER);
        body.put("category", HELP_ARTICLE_CATEGORY);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help", adminToken, body);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertEquals(response.jsonPath().getString("message"), "Article created successfully",
                "message is 'Article created successfully'");

    }

    @Test(description = "TC-HELP-002: Verify create help article fails without admin", groups = {
            "Invalid-Help-Articles-Test", "invalid" })
    public void testCreateHelpArticleFailsWithoutAdmin() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Map<String, Object> body = new HashMap<>();
        body.put("question", HELP_ARTICLE_QUESTION);
        body.put("answer", HELP_ARTICLE_ANSWER);
        body.put("category", HELP_ARTICLE_CATEGORY);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/help", userToken, body);

        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                "error indicates admin auth needed");
    }

}
