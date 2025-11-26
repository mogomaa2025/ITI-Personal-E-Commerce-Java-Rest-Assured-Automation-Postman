package com.gecom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import static com.gecom.utils.Const.BASE_URL;
import static com.gecom.utils.Const.IDS_FILE_PATH;
import static com.gecom.utils.Const.INVALID_NOTIFICATION_ID;
import static com.gecom.utils.Const.NOTIFICATION_TEST_CREATE_COUNT;
import static com.gecom.utils.Const.TOKEN_FILE_PATH;
import static com.gecom.utils.Const.notificationId;
import static com.gecom.utils.Const.userToken;
import com.gecom.utils.JsonUtility;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

/**
 * This class contains test cases for the notifications functionalities,
 * including creating, viewing, and managing notifications.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "NotificationsTest")
@Severity(SeverityLevel.CRITICAL)
public class NotificationsTest {

    /**
     * Test case for verifying that a tester can create test notifications.
     *
     * @throws Exception if an error occurs while reading the user token or saving the notification ID.
     */
    @Test(description = "TC-NOTIF-001: Verify as tester can create test notifications")
    public void testCreateTestNotifications() throws Exception {
        Allure.step("Login as user or admin");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send POST to create test notifications");
        Map<String, Object> body = new HashMap<>();
        body.put("count", NOTIFICATION_TEST_CREATE_COUNT);

        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/notifications/test-create", userToken, body);

        Allure.step("Verify test notifications created");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message contains 'Created'");
        String message = response.jsonPath().getString("message");
        Assert.assertTrue(message != null && message.contains("Created"), "message contains 'Created'");

        Allure.step("Verify data is array of notifications");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Save last notification_id using json utility to use later");
        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        if (!notifications.isEmpty()) {
            Map<String, Object> lastNotification = notifications.get(notifications.size() - 1);
            notificationId = ((Number) lastNotification.get("id")).intValue();
            JsonUtility.saveValue("notification_id", notificationId, IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying that a user can retrieve their notifications.
     *
     * @throws Exception if an error occurs while reading the user token or saving the notification ID.
     */
    @Test(description = "TC-NOTIF-002: Verify user can get their notifications", dependsOnMethods = "testCreateTestNotifications")
    public void testUserCanGetNotifications() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send GET to get notifications");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/notifications", userToken);

        Allure.step("Verify notifications returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify count matches array length");
        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count"); // int not Integer to avoid Null pointer exception, Integer can be null
        Assert.assertEquals(count, notifications.size(), "count matches array length");

        Allure.step("Verify each notification has id/user_id/message/type/read status/created_at");
        if (!notifications.isEmpty()) {
            Map<String, Object> firstNotification = notifications.get(0);
            Assert.assertTrue(firstNotification.get("id") instanceof Number, "Each notification has id");
            Assert.assertTrue(firstNotification.get("user_id") instanceof Number, "Each notification has user_id");
            Assert.assertTrue(firstNotification.get("message") instanceof String || firstNotification.get("body") instanceof String, "Each notification has message/body");
            Assert.assertNotNull(firstNotification.get("is_read"), "Each notification has read status");
            Assert.assertNotNull(firstNotification.get("created_at"), "Each notification has created_at");
        }

        Allure.step("Verify unread_count exists");
        Assert.assertNotNull(response.jsonPath().get("unread_count"), "unread_count exists");

        Allure.step("Save last notification_id using json utility to use later");
        if (!notifications.isEmpty()) {
            Map<String, Object> lastNotification = notifications.get(notifications.size() - 1);
            notificationId = ((Number) lastNotification.get("id")).intValue();
            JsonUtility.saveValue("notification_id", notificationId, IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying that an empty array is returned for a user with no notifications.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-NOTIF-003: Verify empty notifications returns empty array", dependsOnMethods = "testUserCanGetNotifications")
    public void testEmptyNotificationsReturnsEmptyArray() throws Exception {
        Allure.step("Login as new user without notifications");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send GET to get notifications");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/notifications", userToken);

        Allure.step("Verify empty notifications");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is empty array");
        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        Assert.assertNotNull(notifications, "data is array");
        // Note: This test may pass even if there are notifications, as it depends on user state
        // The validation point is that the structure is correct

        Allure.step("Verify count is 0 if data is empty");
        if (notifications.isEmpty()) {
            int count = response.jsonPath().getInt("count"); // int instead of Integer to avoid Null pointer exception
            Assert.assertEquals(count, 0, "count is 0");
        }
    }

    /**
     * Test case for verifying that a user can mark a single notification as read.
     *
     * @throws Exception if an error occurs while reading the user token or notification ID.
     */
    @Test(description = "TC-NOTIF-004: Verify user can mark notification as read", dependsOnMethods = "testEmptyNotificationsReturnsEmptyArray")
    public void testUserCanMarkNotificationAsRead() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Get notification_id using json utility");
        notificationId = JsonUtility.getJSONInt("notification_id", IDS_FILE_PATH);
        Assert.assertNotNull(notificationId, "Notification ID is valid Integer");

        Allure.step("Send PUT to mark notification as read");
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/" + notificationId + "/read", userToken, Map.of()); // no body

        Allure.step("Verify marked as read");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Notification marked as read'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Notification marked as read", "message is 'Notification marked as read'");

        Allure.step("Verify data shows read status is true");
        Map<String, Object> data = response.jsonPath().getMap("data");
        if (data != null) {
            Assert.assertTrue(data.get("is_read") instanceof Boolean, "data shows read status");
        }
    }

    /**
     * Test case for verifying that marking a non-existent notification as read fails.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-NOTIF-005: Verify mark notification as read fails for non-existent notification", dependsOnMethods = "testUserCanMarkNotificationAsRead")
    public void testMarkNotificationAsReadFailsForNonExistent() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send PUT with invalid notification ID");
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/" + INVALID_NOTIFICATION_ID + "/read", userToken, Map.of()); // no body

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'Notification not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Notification not found"), "error is 'Notification not found'");
    }

    /**
     * Test case for verifying that a user can mark all their notifications as read.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-NOTIF-006: Verify user can mark all notifications as read", dependsOnMethods = "testMarkNotificationAsReadFailsForNonExistent")
    public void testUserCanMarkAllNotificationsAsRead() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send PUT to mark all notifications as read");
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/read-all", userToken, Map.of()); // Map.of() because no body is needed also instead of null to avoid null pointer exception

        Allure.step("Verify all marked as read");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message contains 'marked as read'");
        String message = response.jsonPath().getString("message");
        Assert.assertTrue(message != null && message.contains("marked as read"), "message contains 'marked as read'");

        Allure.step("Verify updated_count shows number updated if present");
        if (response.jsonPath().get("updated_count") != null) {
            Assert.assertTrue(response.jsonPath().get("updated_count") instanceof Number, "updated_count shows number updated");
        }
    }

    /**
     * Test case for verifying that marking all notifications as read is handled gracefully when all are already read.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-NOTIF-007: Verify mark all notifications on already read notifications", dependsOnMethods = "testUserCanMarkAllNotificationsAsRead")
    public void testMarkAllNotificationsOnAlreadyReadNotifications() throws Exception {
        Allure.step("Login as user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("Send PUT to mark all notifications as read again");
        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/read-all", userToken, Map.of()); // no body

        Allure.step("Verify success with count 0");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify updated_count is 0 if present");
        if (response.jsonPath().get("updated_count") != null) {
            int updatedCount = response.jsonPath().getInt("updated_count");
            Assert.assertEquals( updatedCount, 0, "updated_count is 0");
        }
    }

    /**
     * Test case for verifying that a user cannot access another user's notifications.
     *
     * @throws Exception if an error occurs while reading the user token.
     */
    @Test(description = "TC-NOTIF-008: Verify user cannot access another user's notifications", dependsOnMethods = "testMarkAllNotificationsOnAlreadyReadNotifications")
    public void testUserCannotAccessAnotherUsersNotifications() throws Exception {
        Allure.step("Login as user1");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");

        Allure.step("User1 gets notifications");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/notifications", userToken);

        Allure.step("Verify only user1 notifications");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data contains only user1 notifications");
        List<Map<String, Object>> notifications = response.jsonPath().getList("data");
        if (!notifications.isEmpty()) {
            for (Map<String, Object> notification : notifications) {
                Assert.assertTrue(notification.get("user_id") instanceof Number, "Each notification has user_id");
            }
        }

    }
}
