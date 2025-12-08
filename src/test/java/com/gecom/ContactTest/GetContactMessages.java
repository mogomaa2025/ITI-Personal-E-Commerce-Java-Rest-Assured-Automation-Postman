package com.gecom.ContactTest;

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
@Test(groups = "ContactTest")
@Severity(SeverityLevel.NORMAL)
public class GetContactMessages {

    @Test(description = "TC-CONT-003: Verify admin can view all contact messages", groups = { "Valid-Contact-Test",
            "valid" })
    public void testAdminCanViewAllContactMessages() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", CONTACT_STATUS_PENDING);
        Response response = ApiUtils.getRequestWithAuthQuery(BASE_URL + "/contact/messages", queryParams, adminToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        List<Map<String, Object>> messages = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, messages.size(), "count matches array length");
        if (!messages.isEmpty()) {
            Map<String, Object> lastMessage = messages.get(messages.size() - 1);
            Assert.assertTrue(lastMessage.get("id") instanceof Integer, "Each message has id");
            Assert.assertTrue(lastMessage.get("name") instanceof String, "Each message has name");
            Assert.assertTrue(lastMessage.get("email") instanceof String, "Each message has email");
            Assert.assertTrue(lastMessage.get("subject") instanceof String, "Each message has subject");
            Assert.assertTrue(lastMessage.get("message") instanceof String, "Each message has message");
            Assert.assertTrue(lastMessage.get("status") instanceof String, "Each message has status");
            Assert.assertNotNull(lastMessage.get("created_at"), "Each message has created_at");
            contactMessageId = (Integer) lastMessage.get("id"); // from object to int
            JsonUtility.saveValue("contact_message_id", contactMessageId, IDS_FILE_PATH);
        }
    }

    @Test(description = "TC-CONT-004: Verify non-admin cannot view contact messages", groups = {
            "Invalid-Contact-Test", "invalid" })
    public void testNonAdminCannotViewContactMessages() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/contact/messages", userToken);
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Admin privileges required"),
                "error indicates admin auth needed");
    }

}
