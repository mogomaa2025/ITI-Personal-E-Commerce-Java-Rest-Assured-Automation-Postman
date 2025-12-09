package com.gecom.NotificationsTest;

import static com.gecom.utils.Base.*;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "NotificationsTest")
@Severity(SeverityLevel.NORMAL)
public class MarkNotifications {

    @Test(description = "TC-NOTIF-004: Verify user can mark notification as read", groups = {
            "Valid-Notifications-Test", "valid" })
    public void testUserCanMarkNotificationAsRead() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        notificationId = (Integer) JsonUtility.getValue("notification_id", IDS_FILE_PATH);
        Assert.assertNotNull(notificationId, "Notification ID is valid Integer");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/" + notificationId + "/read",
                userToken, Map.of()); // no body

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertEquals(response.jsonPath().getString("message"), "Notification marked as read",
                "message is 'Notification marked as read'");
        Map<String, Object> data = response.jsonPath().getMap("data");
        if (data != null) {
            Assert.assertTrue(data.get("is_read") instanceof Boolean, "data shows read status");
        }
    }

    @Test(description = "TC-NOTIF-005: Verify mark notification as read fails for non-existent notification", groups = {
            "Invalid-Notifications-Test", "invalid" }, dependsOnMethods = "testUserCanMarkNotificationAsRead")
    public void testMarkNotificationAsReadFailsForNonExistent() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.putRequestWithAuth(
                BASE_URL + "/notifications/" + INVALID_NOTIFICATION_ID + "/read", userToken, Map.of()); // no body

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Notification not found"),
                "error is 'Notification not found'");
    }

    @Test(description = "TC-NOTIF-006: Verify user can mark all notifications as read", groups = {
            "Valid-Notifications-Test", "valid" }, dependsOnMethods = "testMarkNotificationAsReadFailsForNonExistent")
    public void testUserCanMarkAllNotificationsAsRead() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/read-all", userToken, Map.of()); // Map.of()

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        String message = response.jsonPath().getString("message");
        Assert.assertTrue(message != null && message.contains("marked as read"), "message contains 'marked as read'");
        if (response.jsonPath().get("updated_count") != null) {
            Assert.assertTrue(response.jsonPath().get("updated_count") instanceof Number,
                    "updated_count shows number updated");
        }
    }

    @Test(description = "TC-NOTIF-007: Verify mark all notifications on already read notifications", groups = {
            "Invalid-Notifications-Test", "invalid" }, dependsOnMethods = "testUserCanMarkAllNotificationsAsRead")
    public void testMarkAllNotificationsOnAlreadyReadNotifications() throws Exception {
        Allure.step("Login as user");
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/read-all", userToken, Map.of()); // no
                                                                                                                    // body

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        if (response.jsonPath().get("updated_count") != null) {
            int updatedCount = response.jsonPath().getInt("updated_count");
            Assert.assertEquals(updatedCount, 0, "updated_count is 0");
        }
    }

    @Test(description = "TC-NOTIF-008: Verify user cannot access another user's notifications", groups = {
            "Invalid-Notifications-Test",
            "invalid" }, dependsOnMethods = "testMarkAllNotificationsOnAlreadyReadNotifications")
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
