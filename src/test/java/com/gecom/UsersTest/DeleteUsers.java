package com.gecom.UsersTest;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.gecom.utils.Const.*;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "UsersTest")
@Severity(SeverityLevel.CRITICAL)
public class DeleteUsers {

    @Test(description = "TC-USER-008: Verify admin can delete user", groups = { "Valid-Users-Test", "valid" })
    public void testAdminCanDeleteUser() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        userId = (Integer) JsonUtility.getValue("user_id", IDS_FILE_PATH);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/users/" + userId, adminToken);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertEquals(response.jsonPath().getString("message"), "User deleted successfully",
                "message is 'User deleted successfully'");
        Assert.assertTrue(response.jsonPath().getInt("data.id") > 0, "data has id");
        Assert.assertNotNull(response.jsonPath().getString("data.email"), "data has email");

        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/users/" + userId, adminToken);
        Assert.assertEquals(getResponse.getStatusCode(), 404, "Subsequent GET returns 404");
    }

    @Test(description = "TC-USER-009: Verify user cannot delete user", groups = { "Invalid-Users-Test", "invalid" })
    public void testUserCannotDeleteUser() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/users/10", userToken);
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.equals("Admin privileges required"), "error indicates admin required");
    }

    @Test(description = "TC-USER-010: Verify delete users fails without authentication", groups = {
            "Invalid-Users-Test", "invalid" })
    public void testDeleteUserFailsWithoutAuth() {
        Response response = ApiUtils.deleteRequest(BASE_URL + "/users/10"); // already existing user id = 10
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.equals("Token is missing")),
                "error indicates authentication required");
    }

    @Test(description = "TC-USER-011: Verify delete user fails for non-existent user", groups = {
            "Invalid-Users-Test", "invalid" })
    public void testDeleteUserFailsForNonExistent() throws Exception {
        adminToken = (String) JsonUtility.getValue("admin", TOKEN_FILE_PATH);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/users/999999", adminToken); // non existing user
                                                                                                    // id
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.equals("User not found"), "error is 'User not found'");
    }

}
