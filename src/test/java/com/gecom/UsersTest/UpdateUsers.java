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

import static com.gecom.utils.Base.*;

import java.util.HashMap;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "UsersTest")
@Severity(SeverityLevel.CRITICAL)
public class UpdateUsers {

    @Test(description = "TC-USER-006: Verify admin can update user information", groups = { "Valid-Users-Test",
            "valid" }) // response
    public void testAdminCanUpdateUser() throws Exception {
        userId = GetUserID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Updated Name");
        body.put("phone", "555-0101");
        body.put("address", "123 Updated St");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/users/" + userId, adminToken, body);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertEquals(response.jsonPath().getString("message"), "User updated successfully",
                "message is 'User updated successfully'");
        Assert.assertEquals(response.jsonPath().getString("data.name"), "Updated Name", "data reflects updated values");
        Assert.assertEquals(response.jsonPath().getString("data.phone"), "555-0101", "data reflects updated values");
        Assert.assertNotNull(response.jsonPath().getString("data.updated_at"), "updated_at timestamp present");

    }

    @Test(description = "TC-USER-007: Verify update user fails for non-existent user", groups = {
            "Invalid-Users-Test", "invalid" }, dependsOnMethods = "testAdminCanUpdateUser")
    public void testUpdateUserFailsForNonExistent() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Test");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/users/999999", adminToken, body);

        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("not found"), "error is 'User not found'");
    }

}
