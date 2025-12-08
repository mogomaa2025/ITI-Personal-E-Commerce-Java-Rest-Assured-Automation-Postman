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
public class CreateContactMessage {

        @Test(description = "TC-CONT-001: Verify user can submit contact message", groups = { "Valid-Contact-Test",
                        "valid" })
        public void testUserCanSubmitContactMessage() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token not found");

                Map<String, Object> body = new HashMap<>();
                body.put("name", CONTACT_NAME);
                body.put("email", CONTACT_EMAIL);
                body.put("subject", CONTACT_SUBJECT);
                body.put("message", CONTACT_MESSAGE);

                Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/contact", userToken, body);
                Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"),
                                "Your message has been submitted successfully. We will get back to you soon!",
                                "message is 'Your message has been submitted successfully. We will get back to you soon!'");
        }

        @Test(description = "TC-CONT-002: Verify submit contact fails with missing required fields", groups = {
                        "Invalid-Contact-Test", "invalid" })
        public void testSubmitContactFailsWithMissingFields() {
                Map<String, Object> body = new HashMap<>();
                body.put("name", CONTACT_INCOMPLETE_NAME);

                Response response = ApiUtils.postRequest(BASE_URL + "/contact", body);

                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Name, email, and message are required"),
                                "error indicates missing fields");
        }

}
