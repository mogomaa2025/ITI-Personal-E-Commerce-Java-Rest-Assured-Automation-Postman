package com.gecom.NotificationsTest;

import static com.gecom.utils.Const.BASE_URL;
import static com.gecom.utils.Const.IDS_FILE_PATH;
import static com.gecom.utils.Const.INVALID_NOTIFICATION_ID;
import static com.gecom.utils.Const.NOTIFICATION_TEST_CREATE_COUNT;
import static com.gecom.utils.Const.TOKEN_FILE_PATH;
import static com.gecom.utils.Const.notificationId;
import static com.gecom.utils.Const.userToken;

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
@Test(groups = "NotificationsTest")
@Severity(SeverityLevel.NORMAL)
public class CreateNotifications {

    @Test(description = "TC-NOTIF-001: Verify as tester can create test notifications", groups = {
            "Valid-Notifications-Test", "valid" })
    public void testCreateTestNotifications() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Map<String, Object> body = new HashMap<>();
        body.put("count", NOTIFICATION_TEST_CREATE_COUNT);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/notifications/test-create", userToken, body);

        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        String message = response.jsonPath().getString("message");
        Assert.assertTrue(message != null && message.contains("Created"), "message contains 'Created'");

        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        if (!notifications.isEmpty()) {
            Map<String, Object> lastNotification = notifications.get(notifications.size() - 1);
            notificationId = ((Number) lastNotification.get("id")).intValue();
            JsonUtility.saveValue("notification_id", notificationId, IDS_FILE_PATH);
        }
    }

}
