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
import java.util.Map;

import static com.gecom.utils.Const.BASE_URL;
import static com.gecom.utils.Const.IDS_FILE_PATH;
import static com.gecom.utils.Const.TOKEN_FILE_PATH;

@Listeners({AllureTestNg.class})
public class ContactTest {

    @Test(groups = "testSubmitContactMessage")
    public void testSubmitContactMessage() throws Exception {
        Allure.step("Starting testSubmitContactMessage...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Jane Doe");
        body.put("email", "jane@example.com");
        body.put("subject", "Order issue");
        body.put("message", "I need help with my order.");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/contact", userToken, body);
        Allure.step("Submit contact message status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 201, "Should return 201 Created");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testSubmitContactMessage finished successfully.");
    }

    @Test(groups = "testSubmitContactMessage",  dependsOnMethods = "testSubmitContactMessage")
    public void testListContactMessages() throws Exception {
        Allure.step("Starting testListContactMessages...");

        Response response = ApiUtils.getRequest(BASE_URL + "/contact/messages?status=pending");
        Allure.step("List contact messages status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        int contactMessageId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("contact_message_id", String.valueOf(contactMessageId), IDS_FILE_PATH);
        Allure.step("contact_message_id saved: " + contactMessageId);

        Allure.step("testListContactMessages finished successfully.");
    }

    @Test(groups = "testSubmitContactMessage",  dependsOnMethods = "testListContactMessages")
    public void testRespondToContactMessage() throws Exception {
        Allure.step("Starting testRespondToContactMessage...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String contactMessageId = JsonUtility.getToken("contact_message_id", IDS_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");
        Assert.assertNotNull(contactMessageId, "Contact message ID not found");

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("response", "Thanks for reaching out. Your issue is resolved.");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/contact/messages/" + contactMessageId + "/respond", adminToken, body);
        Allure.step("Respond to contact message status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testRespondToContactMessage finished successfully.");
    }
}
