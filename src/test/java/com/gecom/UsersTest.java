package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.restassured.response.Response;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import static com.gecom.utils.Const.*;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "UsersTest")
public class UsersTest {




    @Test
    public void testListUsersAdmin() throws Exception {
        Allure.step("Starting testListUsersAdmin...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testListUsersAdmin");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users", adminToken);
        Allure.step("List users API response status code: " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200, "Should succeed for admin");

        // Extract the first user ID from the response and save it
        userId = JsonUtility.getLastUserId(response);
        Allure.step("Retrieved user ID: " + userId);
        JsonUtility.saveToken("user_id", userId.toString(), IDS_FILE_PATH);

        Allure.step("testListUsersAdmin finished successfully.");
    }

    @Test(dependsOnMethods = "testListUsersAdmin")
    public void testGetUserAdmin() throws Exception {
        Allure.step("Starting testGetUserAdmin...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");
        String userIdStr = JsonUtility.getToken("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userIdStr, "User ID not found for testGetUserAdmin");
        Allure.step("Getting user with ID: " + userIdStr);

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/" + userIdStr, adminToken);
        Allure.step("Get user API response status code: " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200, "Should succeed for single user");

        // Verify the correct user is returned
        Assert.assertTrue(response.asString().contains(userIdStr), "Correct user returned");

        Allure.step("testGetUserAdmin finished successfully.");
    }

    @Test(dependsOnMethods = "testGetUserAdmin")
    public void testPutUpdateUserAdmin() throws Exception {
        Allure.step("Starting testPutUpdateUserAdmin...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");
        String userIdStr = JsonUtility.getToken("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userIdStr, "User ID not found for testPutUpdateUserAdmin");
        Allure.step("Updating user with ID: " + userIdStr);

        //body
        Map<String, Object> body = new HashMap<>();
        body.put("name", "todelete2");
        body.put("phone", "555-0101");
        body.put("address", "123 Updated Street");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/users/" + userIdStr, adminToken, body);
        Allure.step("Update user API response status code: " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200, "Should succeed for update");

        // Verify the response message
        Assert.assertEquals(response.jsonPath().getString("message"), "User updated successfully", "Update success message only");

        Allure.step("testPutUpdateUserAdmin finished successfully.");
    }

    @Test(dependsOnMethods = "testPutUpdateUserAdmin")
    public void testDeleteUserAdmin() throws Exception {
        Allure.step("Starting testDeleteUserAdmin...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");
        String userIdStr = JsonUtility.getToken("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userIdStr, "User ID not found for testDeleteUserAdmin");
        Allure.step("Deleting user with ID: " + userIdStr);

        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/users/" + userIdStr, adminToken);
        Allure.step("Delete user API response status code: " + response.getStatusCode());
        Assert.assertTrue(response.getStatusCode() == 200);

        Allure.step("testDeleteUserAdmin finished successfully.");
    }

    @Test(dependsOnMethods = "testDeleteUserAdmin")
    public void testGetUserActivityAdmin() throws Exception {
        Allure.step("Starting testGetUserActivityAdmin...");
        adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        String userIdStr = JsonUtility.getToken("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userIdStr, "User ID not found for testGetUserActivityAdmin");
        Assert.assertNotNull(adminToken, "Admin token not found for testDeleteCategory");
        Allure.step("Getting activity for user with ID: " + userIdStr);

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/" + userIdStr + "/activity", adminToken);
        Allure.step("Get user activity API response status code: " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200, "Should succeed for user activity");

        Allure.step("testGetUserActivityAdmin finished successfully.");
    }



}
