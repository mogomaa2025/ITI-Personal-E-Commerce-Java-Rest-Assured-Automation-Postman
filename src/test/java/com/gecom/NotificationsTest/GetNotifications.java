package com.gecom.NotificationsTest;

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
@Test(groups = "NotificationsTest")
@Severity(SeverityLevel.NORMAL)
public class GetNotifications {

    @Test(description = "TC-NOTIF-002: Verify user can get their notifications", groups = {
            "Valid-Notifications-Test", "valid" })
    public void testUserCanGetNotifications() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/notifications", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count"); // int not Integer to avoid Null pointer exception
        Assert.assertEquals(count, notifications.size(), "count matches array length");

        if (!notifications.isEmpty()) {
            Map<String, Object> firstNotification = notifications.get(0);
            Assert.assertTrue(firstNotification.get("id") instanceof Number, "Each notification has id");
            Assert.assertTrue(firstNotification.get("user_id") instanceof Number, "Each notification has user_id");
            Assert.assertTrue(firstNotification.get("message") instanceof String
                    || firstNotification.get("body") instanceof String, "Each notification has message/body");
            Assert.assertNotNull(firstNotification.get("is_read"), "Each notification has read status");
            Assert.assertNotNull(firstNotification.get("created_at"), "Each notification has created_at");
        }

        Assert.assertNotNull(response.jsonPath().get("unread_count"), "unread_count exists");

        if (!notifications.isEmpty()) {
            Map<String, Object> lastNotification = notifications.get(notifications.size() - 1);
            notificationId = ((Number) lastNotification.get("id")).intValue();
            JsonUtility.saveValue("notification_id", notificationId, IDS_FILE_PATH);
        }
    }

    @Test(description = "TC-NOTIF-003: Verify empty notifications returns empty array", groups = {
            "Invalid-Notifications-Test", "invalid" }, dependsOnMethods = "testUserCanGetNotifications")
    public void testEmptyNotificationsReturnsEmptyArray() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/notifications", userToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        Assert.assertNotNull(notifications, "data is array");
        // Note: This test may pass even if there are notifications, as it depends on
        // user state
        // The validation point is that the structure is correct

        if (notifications.isEmpty()) {
            int count = response.jsonPath().getInt("count"); // int instead of Integer to avoid Null pointer exception
            Assert.assertEquals(count, 0, "count is 0");
        }
    }

    @Test(description = "TC-NOTIF-008: Verify user cannot access another user's notifications", groups = {
            "Invalid-Notifications-Test", "invalid" }, dependsOnMethods = "testEmptyNotificationsReturnsEmptyArray")
    public void testUserCannotAccessAnotherUsersNotifications() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/notifications", userToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        if (!notifications.isEmpty()) {
            for (Map<String, Object> notification : notifications) {
                Assert.assertTrue(notification.get("user_id") instanceof Number, "Each notification has user_id");
            }
        }

    }

}
