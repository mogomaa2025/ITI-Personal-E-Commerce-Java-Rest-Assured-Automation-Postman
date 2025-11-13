package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

import static com.gecom.utils.Const.*;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "NotificationsTest")
public class NotificationsTest {

    @Test
    public void testListNotifications() throws Exception {
        Allure.step("Starting testListNotifications...");

        Response response = ApiUtils.getRequest(BASE_URL + "/notifications?user_id=2");
        Allure.step("List notifications status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        notificationId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("notification_id", String.valueOf(notificationId), IDS_FILE_PATH);
        Allure.step("notification_id saved: " + notificationId);

        Allure.step("testListNotifications finished successfully.");
    }

    @Test(dependsOnMethods = "testListNotifications")
    public void testMarkNotificationRead() throws Exception {
        Allure.step("Starting testMarkNotificationRead...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        String notificationId = JsonUtility.getToken("notification_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Assert.assertNotNull(notificationId, "Notification ID not found");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/" + notificationId + "/read", userToken, Map.of());
        Allure.step("Mark notification read status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertEquals(response.jsonPath().getString("message"), "Notification marked as read");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testMarkNotificationRead finished successfully.");
    }

    @Test(dependsOnMethods = "testMarkNotificationRead")
    public void testMarkAllNotificationsRead() throws Exception {
        Allure.step("Starting testMarkAllNotificationsRead...");
        String userToken = JsonUtility.getToken("user", TOKEN_FILE_PATH);
        String notificationId = JsonUtility.getToken("notification_id", IDS_FILE_PATH);
        Assert.assertNotNull(userToken, "User token not found");
        Assert.assertNotNull(notificationId, "Notification ID not found");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/notifications/read-all?user_id=" + notificationId, userToken, Map.of());
        Allure.step("Mark all notifications read status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testMarkAllNotificationsRead finished successfully.");
    }
}
