package com.gecom.ContactTest;

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
@Test(groups = "ContactTest")
@Severity(SeverityLevel.NORMAL)
public class RespondContactMessages {

        @Test(description = "TC-CONT-005: Verify admin can respond to contact message", groups = {
                        "Valid-Contact-Test", "valid" })
        public void testAdminCanRespondToContactMessage() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                contactMessageId = (Integer) JsonUtility.getValue("contact_message_id", IDS_FILE_PATH);
                Assert.assertNotNull(contactMessageId, "Contact message ID not found");
                Map<String, Object> body = new HashMap<>();
                body.put("response", CONTACT_RESPONSE);

                Response response = ApiUtils
                                .postRequestWithAuth(BASE_URL + "/contact/messages/" + contactMessageId + "/respond",
                                                adminToken, body);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Response sent successfully",
                                "message is 'Response sent successfully'");
        }

        @Test(description = "TC-CONT-006: Verify respond fails for non-existent message", groups = {
                        "Invalid-Contact-Test", "invalid" })
        public void testRespondFailsForNonExistentMessage() throws Exception {
                adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
                Assert.assertNotNull(adminToken, "Admin token not found");

                Map<String, Object> body = new HashMap<>();
                body.put("response", "Test");

                Response response = ApiUtils.postRequestWithAuth(
                                BASE_URL + "/contact/messages/" + INVALID_CONTACT_MESSAGE_ID + "/respond", adminToken,
                                body);

                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Message not found"), "error is 'Message not found'");
        }

        @Test(description = "TC-CONT-007: Verify non-admin cannot respond to messages", groups = {
                        "Invalid-Contact-Test", "invalid" })
        public void testNonAdminCannotRespondToMessages() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");

                contactMessageId = (Integer) JsonUtility.getValue("contact_message_id", IDS_FILE_PATH);
                Assert.assertNotNull(contactMessageId, "Contact message ID not found");

                Map<String, Object> body = new HashMap<>();
                body.put("response", "Test");

                Response response = ApiUtils
                                .postRequestWithAuth(BASE_URL + "/contact/messages/" + contactMessageId + "/respond",
                                                userToken, body);

                Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                                "error indicates admin auth needed");
        }
}
