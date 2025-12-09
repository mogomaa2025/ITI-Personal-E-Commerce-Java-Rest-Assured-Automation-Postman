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
import java.util.List;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "UsersTest")
@Severity(SeverityLevel.CRITICAL)
public class GetUsers {

    @Test(description = "TC-USER-001: Verify admin can list all users", groups = { "Valid-Users-Test", "valid" })
    public void testAdminCanListAllUsers() throws Exception {
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users", adminToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        // array like [{}, {}, ...] so use hashmap to represent each user object

        // List<Map<String, Object>> usersArray = response.jsonPath().getList("data");
        // // we should get data list first
        // int count = response.jsonPath().getInt("count");
        // Assert.assertEquals(count, usersArray.size(), "count matches data length");
        // //not size-1 because size is count of elements

        int count = response.jsonPath().getInt("count");
        int dataSize = response.jsonPath().getList("data").size();
        Assert.assertEquals(count, dataSize, "count matches data length");

        List<Map<String, Object>> usersArray = response.jsonPath().getList("data");
        Assert.assertTrue(usersArray != null && !usersArray.isEmpty(), "data is array");
        Map<String, Object> firstUser = usersArray.get(0); // first user for field checks
        Assert.assertTrue(firstUser.get("id") instanceof Integer, "Each user has id");
        Assert.assertTrue(firstUser.get("email") instanceof String, "Each user has email");
        Assert.assertTrue(firstUser.get("name") instanceof String, "Each user has name");
        Assert.assertTrue(firstUser.get("is_admin") instanceof Boolean, "Each user has is_admin");
        Assert.assertNotNull(firstUser.get("created_at"), "Each user has created_at");

        userId = (Integer) usersArray.get(usersArray.size() - 1).get("id"); // casting to Integer because get returns
                                                                            // Object
        JsonUtility.saveValue("user_id", userId, IDS_FILE_PATH);
    }

    @Test(description = "TC-USER-002: Verify non-admin cannot list users", groups = { "Invalid-Users-Test",
            "invalid" }, dependsOnMethods = "testAdminCanListAllUsers")
    public void testNonAdminCannotListUsers() throws Exception {
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users", userToken);
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.toLowerCase().contains("admin"), "error indicates admin required");
    }

    @Test(description = "TC-USER-003: Verify list users fails without authentication", groups = {
            "Invalid-Users-Test", "invalid" }, dependsOnMethods = "testNonAdminCannotListUsers")
    public void testListUsersFailsWithoutAuth() {
        Response response = ApiUtils.getRequest(BASE_URL + "/users");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.toLowerCase().contains("missing")),
                "error indicates authentication required");
    }

    @Test(description = "TC-USER-004: Verify admin can get user by ID", groups = { "Valid-Users-Test",
            "valid" }, dependsOnMethods = "testListUsersFailsWithoutAuth")
    public void testAdminCanGetUserById() throws Exception {
        userId = GetUserID();
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/" + userId, adminToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        // Allure.step("Verify data has all user fields");
        // Assert.assertTrue(response.jsonPath().getInt("data.id") > 0, "data has id");
        // Assert.assertNotNull(response.jsonPath().getString("data.email"), "data has
        // email");
        // Assert.assertNotNull(response.jsonPath().getString("data.name"), "data has
        // name");
        // Assert.assertNotNull(response.jsonPath().get("data.is_admin"), "data has
        // is_admin");
        // Assert.assertNotNull(response.jsonPath().getString("data.created_at"), "data
        // has created_at");

        // this way we also verify the fields are not null and have correct types
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "id should be INTEGER");
        Assert.assertTrue(response.jsonPath().get("data.email") instanceof String, "email should be STRING");
        Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "name should be STRING");
        Assert.assertTrue(response.jsonPath().get("data.is_admin") instanceof Boolean, "is_admin should be BOOLEAN");
        Assert.assertTrue(response.jsonPath().get("data.created_at") instanceof String, "created_at should be STRING");
        Assert.assertNotNull(userId);
        Assert.assertEquals(response.jsonPath().getInt("data.id"), userId, "Returned user ID matches requested");
    }

    @Test(description = "TC-USER-005: Verify get user by ID fails for non-existent user", groups = {
            "Invalid-Users-Test", "invalid" }, dependsOnMethods = "testAdminCanGetUserById")
    public void testGetUserByIdFailsForNonExistent() throws Exception {
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/999999", adminToken);
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("not found"), "error is 'User not found'");
    }

    @Test(description = "TC-USER-012: Verify admin can view user activity", groups = { "Valid-Users-Test",
            "valid" }, dependsOnMethods = "testGetUserByIdFailsForNonExistent")
    public void testAdminCanViewUserActivity() throws Exception {
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/10/activity", adminToken);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
        Assert.assertNotNull(response.jsonPath().getList("data.orders"), "data has orders array");
        Assert.assertNotNull(response.jsonPath().getList("data.reviews"), "data has reviews array");
        Assert.assertTrue(response.jsonPath().get("data.user_id") instanceof Integer, "");
        Assert.assertTrue(response.jsonPath().get("data.total_orders") instanceof Integer, "");
        Assert.assertTrue(response.jsonPath().get("data.total_reviews") instanceof Integer, "");
        // Assert.assertTrue(response.jsonPath().get("data.total_spent") instanceof
        // Double, ""); // Double and BigDecimal are not working, only number work but
        // it's not specific enough
        Assert.assertTrue(response.jsonPath().getDouble("data.total_spent") >= 0, "total_spent is number"); // "total_spent":
                                                                                                            // 989.8699999999999
        Assert.assertTrue(response.jsonPath().get("data.cart_items") instanceof Integer, "");

    }

    @Test(description = "TC-USER-013: Verify user cannot view user activity", groups = { "Invalid-Users-Test",
            "invalid" }, dependsOnMethods = "testAdminCanViewUserActivity")
    public void testUserCannotViewUserActivity() throws Exception {
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/10/activity", userToken);
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Admin privileges required", "error indicates admin required");
    }

    @Test(description = "TC-USER-014: Verify view user activity fails without authentication", groups = {
            "Invalid-Users-Test", "invalid" }, dependsOnMethods = "testUserCannotViewUserActivity")
    public void testViewUserActivityFailsWithoutAuth() {
        Response response = ApiUtils.getRequest(BASE_URL + "/users/10/activity");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Token is missing", "error indicates authentication required");
    }
}
