package com.gecom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import static com.gecom.utils.Const.BASE_URL;
import static com.gecom.utils.Const.CONTACT_EMAIL;
import static com.gecom.utils.Const.CONTACT_INCOMPLETE_NAME;
import static com.gecom.utils.Const.CONTACT_MESSAGE;
import static com.gecom.utils.Const.CONTACT_NAME;
import static com.gecom.utils.Const.CONTACT_RESPONSE;
import static com.gecom.utils.Const.CONTACT_STATUS_PENDING;
import static com.gecom.utils.Const.CONTACT_SUBJECT;
import static com.gecom.utils.Const.IDS_FILE_PATH;
import static com.gecom.utils.Const.INVALID_CONTACT_MESSAGE_ID;
import static com.gecom.utils.Const.TOKEN_FILE_PATH;
import static com.gecom.utils.Const.adminToken;
import static com.gecom.utils.Const.contactMessageId;
import static com.gecom.utils.Const.userToken;
import com.gecom.utils.JsonUtility;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

/**
 * This class contains test cases for the contact functionalities,
 * including submitting contact messages and admin actions for viewing and responding to them.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "ContactTest")
@Severity(SeverityLevel.CRITICAL)
public class ContactTest {

    /**
     * Test case for verifying that a user can submit a contact message.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CONT-001: Verify user can submit contact message")
    public void testUserCanSubmitContactMessage() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST with contact data");
        Map<String, Object> body = new HashMap<>();
        body.put("name", CONTACT_NAME);
        body.put("email", CONTACT_EMAIL);
        body.put("subject", CONTACT_SUBJECT);
        body.put("message", CONTACT_MESSAGE);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/contact", userToken, body);

        Allure.step("Verify submitted");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Your message has been submitted successfully. We will get back to you soon!'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Your message has been submitted successfully. We will get back to you soon!", "message is 'Your message has been submitted successfully. We will get back to you soon!'");
    }

    /**
     * Test case for verifying that submitting a contact message fails with missing required fields.
     */
    @Test(description = "TC-CONT-002: Verify submit contact fails with missing required fields", dependsOnMethods = "testUserCanSubmitContactMessage")
    public void testSubmitContactFailsWithMissingFields() {
        Allure.step("Send POST with incomplete data");
        Map<String, Object> body = new HashMap<>();
        body.put("name", CONTACT_INCOMPLETE_NAME);

        Response response = ApiUtils.postRequest(BASE_URL + "/contact", body);

        Allure.step("Verify validation error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates missing fields");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Name, email, and message are required"), "error indicates missing fields");
    }

    /**
     * Test case for verifying that an admin can view all contact messages.
     *
     * @throws Exception if an error occurs while reading the admin token or saving the message ID.
     */
    @Test(description = "TC-CONT-003: Verify admin can view all contact messages", dependsOnMethods = "testSubmitContactFailsWithMissingFields")
    public void testAdminCanViewAllContactMessages() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send GET to messages");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", CONTACT_STATUS_PENDING);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/contact/messages", queryParams, adminToken);

        Allure.step("Verify messages returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify count matches array length");
        List<Map<String, Object>> messages = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, messages.size(), "count matches array length");

        Allure.step("Verify each message has id/name/email/subject/message/status/created_at");
        if (!messages.isEmpty()) {
            Map<String, Object> lastMessage = messages.get(messages.size() - 1);
            Assert.assertTrue(lastMessage.get("id") instanceof Integer, "Each message has id");
            Assert.assertTrue(lastMessage.get("name") instanceof String, "Each message has name");
            Assert.assertTrue(lastMessage.get("email") instanceof String, "Each message has email");
            Assert.assertTrue(lastMessage.get("subject") instanceof String, "Each message has subject");
            Assert.assertTrue(lastMessage.get("message") instanceof String, "Each message has message");
            Assert.assertTrue(lastMessage.get("status") instanceof String, "Each message has status");
            Assert.assertNotNull(lastMessage.get("created_at"), "Each message has created_at");

            Allure.step("Save Last Message ID");
            contactMessageId = (Integer) lastMessage.get("id"); // from object to int
            JsonUtility.saveValue("contact_message_id", contactMessageId, IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying that a non-admin user cannot view contact messages.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-CONT-004: Verify non-admin cannot view contact messages", dependsOnMethods = "testAdminCanViewAllContactMessages")
    public void testNonAdminCannotViewContactMessages() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send GET to messages");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/contact/messages", userToken);

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

    /**
     * Test case for verifying that an admin can respond to a contact message.
     *
     * @throws Exception if an error occurs while reading the admin token or message ID.
     */
    @Test(description = "TC-CONT-005: Verify admin can respond to contact message", dependsOnMethods = "testNonAdminCannotViewContactMessages")
    public void testAdminCanRespondToContactMessage() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Get message ID");
        contactMessageId = JsonUtility.getJSONInt("contact_message_id", IDS_FILE_PATH);
        Assert.assertNotNull(contactMessageId, "Contact message ID not found");

        Allure.step("Send POST with response");
        Map<String, Object> body = new HashMap<>();
        body.put("response", CONTACT_RESPONSE);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/contact/messages/" + contactMessageId + "/respond", adminToken, body);

        Allure.step("Verify sent");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Response sent successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Response sent successfully", "message is 'Response sent successfully'");
    }

    /**
     * Test case for verifying that responding to a non-existent message fails.
     *
     * @throws Exception if an error occurs while reading the admin token.
     */
    @Test(description = "TC-CONT-006: Verify respond fails for non-existent message", dependsOnMethods = "testAdminCanRespondToContactMessage")
    public void testRespondFailsForNonExistentMessage() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Allure.step("Send POST with invalid message ID");
        Map<String, Object> body = new HashMap<>();
        body.put("response", "Test");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/contact/messages/" + INVALID_CONTACT_MESSAGE_ID + "/respond", adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Message not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Message not found"), "error is 'Message not found'");
    }

    /**
     * Test case for verifying that a non-admin user cannot respond to messages.
     *
     * @throws Exception if an error occurs while reading the user token or message ID.
     */
    @Test(description = "TC-CONT-007: Verify non-admin cannot respond to messages", dependsOnMethods = "testRespondFailsForNonExistentMessage")
    public void testNonAdminCannotRespondToMessages() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get message ID");
        contactMessageId = JsonUtility.getJSONInt("contact_message_id", IDS_FILE_PATH);
        Assert.assertNotNull(contactMessageId, "Contact message ID not found");

        Allure.step("Send POST to respond");
        Map<String, Object> body = new HashMap<>();
        body.put("response", "Test");

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/contact/messages/" + contactMessageId + "/respond", userToken, body);

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
}
