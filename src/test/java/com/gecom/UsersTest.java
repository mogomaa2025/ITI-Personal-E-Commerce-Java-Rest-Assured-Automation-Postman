package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import com.gecom.utils.Logger;
import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.gecom.utils.Const.*;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "UsersTest")
@Severity(SeverityLevel.CRITICAL)
public class UsersTest {


    @Test(description = "TC-USER-001: Verify admin can list all users")
    public void testAdminCanListAllUsers() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send GET request");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users", adminToken);

        Allure.step("Verify user list");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        // array like [{}, {}, ...] so use hashmap to represent each user object


       Allure.step("Verify count matches data length");

       //        List<Map<String, Object>> usersArray = response.jsonPath().getList("data"); // we should get data list first
//        int count = response.jsonPath().getInt("count");
//        Assert.assertEquals(count, usersArray.size(), "count matches data length"); //not size-1 because size is count of elements

        int count = response.jsonPath().getInt("count");
        int dataSize = response.jsonPath().getList("data").size();
        Assert.assertEquals(count, dataSize, "count matches data length");


        Allure.step("Verify each user has required fields");
        List<Map<String, Object>> usersArray = response.jsonPath().getList("data");
        Assert.assertTrue(usersArray != null && !usersArray.isEmpty(), "data is array");
        Map<String, Object> firstUser = usersArray.get(0); // first user for field checks
        Assert.assertTrue(firstUser.get("id") instanceof Integer, "Each user has id");
        Assert.assertTrue(firstUser.get("email") instanceof String, "Each user has email");
        Assert.assertTrue(firstUser.get("name") instanceof String, "Each user has name");
        Assert.assertTrue(firstUser.get("is_admin") instanceof Boolean, "Each user has is_admin");
        Assert.assertNotNull(firstUser.get("created_at"), "Each user has created_at");




        Allure.step("Save last user ID for other tests");
//        userId = (Integer) usersArray.get(usersArray.size() - 1).get("id"); // last one = size when talk about array index start with 0 so size()-1
//        JsonUtility.saveToken("user_id", userId.toString(), IDS_FILE_PATH);
        userId = (Integer) usersArray.get(usersArray.size() - 1).get("id"); //casting to Integer because get returns Object
        JsonUtility.saveValue("user_id", userId, IDS_FILE_PATH);
    }

    @Test(description = "TC-USER-002: Verify non-admin cannot list users", dependsOnMethods = "testAdminCanListAllUsers")
    public void testNonAdminCannotListUsers() throws Exception {
        Allure.step("Login as regular user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);

        Allure.step("Send GET request");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users", userToken);

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates admin required");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.toLowerCase().contains("admin"), "error indicates admin required");
    }

    @Test(description = "TC-USER-003: Verify list users fails without authentication", dependsOnMethods = "testNonAdminCannotListUsers")
    public void testListUsersFailsWithoutAuth() {
        Allure.step("Send GET without token");
        Response response = ApiUtils.getRequest(BASE_URL + "/users");

        Allure.step("Verify auth error");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates authentication required");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.toLowerCase().contains("missing")), "error indicates authentication required");
    }

    @Test(description = "TC-USER-004: Verify admin can get user by ID", dependsOnMethods = "testListUsersFailsWithoutAuth") // depend to get user_id
    public void testAdminCanGetUserById() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send GET with valid user ID");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/" + userId, adminToken);

        Allure.step("Verify user details");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

//        Allure.step("Verify data has all user fields");
//        Assert.assertTrue(response.jsonPath().getInt("data.id") > 0, "data has id");
//        Assert.assertNotNull(response.jsonPath().getString("data.email"), "data has email");
//        Assert.assertNotNull(response.jsonPath().getString("data.name"), "data has name");
//        Assert.assertNotNull(response.jsonPath().get("data.is_admin"), "data has is_admin");
//        Assert.assertNotNull(response.jsonPath().getString("data.created_at"), "data has created_at");

        // this way we also verify the fields are not null and have correct types
        Allure.step("Verify data has the right data types");
        Assert.assertTrue(response.jsonPath().get("data.id") instanceof Integer, "id should be INTEGER");
        Assert.assertTrue(response.jsonPath().get("data.email") instanceof String, "email should be STRING");
        Assert.assertTrue(response.jsonPath().get("data.name") instanceof String, "name should be STRING");
        Assert.assertTrue(response.jsonPath().get("data.is_admin") instanceof Boolean, "is_admin should be BOOLEAN");
        Assert.assertTrue(response.jsonPath().get("data.created_at") instanceof String, "created_at should be STRING");



        Allure.step("Verify returned user ID matches requested");
        Assert.assertNotNull(userId);
        Assert.assertEquals(response.jsonPath().getInt("data.id"), userId, "Returned user ID matches requested");
    }

    @Test(description = "TC-USER-005: Verify get user by ID fails for non-existent user", dependsOnMethods = "testAdminCanGetUserById")
    public void testGetUserByIdFailsForNonExistent() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send GET with non-existent ID");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/999999", adminToken);

        Allure.step("Verify 404 error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'User not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("not found"), "error is 'User not found'");
    }

    @Test(description = "TC-USER-006: Verify admin can update user information", dependsOnMethods = "testGetUserByIdFailsForNonExistent") // dependent to use id from previous test response
    public void testAdminCanUpdateUser() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send PUT with updates");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Updated Name");
        body.put("phone", "555-0101");
        body.put("address", "123 Updated St");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/users/" + userId, adminToken, body);

        Allure.step("Verify user updated");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'User updated successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "User updated successfully", "message is 'User updated successfully'");

        Allure.step("Verify data reflects updated values");
        Assert.assertEquals(response.jsonPath().getString("data.name"), "Updated Name", "data reflects updated values");
        Assert.assertEquals(response.jsonPath().getString("data.phone"), "555-0101", "data reflects updated values");

        Allure.step("Verify updated_at timestamp present");
        Assert.assertNotNull(response.jsonPath().getString("data.updated_at"), "updated_at timestamp present");

    }

    @Test(description = "TC-USER-007: Verify update user fails for non-existent user", dependsOnMethods = "testAdminCanUpdateUser")
    public void testUpdateUserFailsForNonExistent() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send PUT with non-existent ID");
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Test");

        Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/users/999999", adminToken, body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'User not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("not found"), "error is 'User not found'");
    }

    @Test(description = "TC-USER-008: Verify admin can delete user", dependsOnMethods = "testUpdateUserFailsForNonExistent") // dependent to use id from previous test response
    public void testAdminCanDeleteUser() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send DELETE");
        userId = JsonUtility.getJSONInt("user_id", IDS_FILE_PATH);
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/users/" + userId, adminToken);

        Allure.step("Verify deleted");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'User deleted successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "User deleted successfully", "message is 'User deleted successfully'");

        Allure.step("Verify data has id and email");
        Assert.assertTrue(response.jsonPath().getInt("data.id") > 0, "data has id");
        Assert.assertNotNull(response.jsonPath().getString("data.email"), "data has email");

        Logger.step("**Verify user no longer exists after deletion**");
        Allure.step("Confirm GET returns 404");
        Response getResponse = ApiUtils.getRequestWithAuth(BASE_URL + "/users/" + userId, adminToken);
        Assert.assertEquals(getResponse.getStatusCode(), 404, "Subsequent GET returns 404");
    }

    @Test(description = "TC-USER-009: Verify user cannot delete user", dependsOnMethods = "testAdminCanDeleteUser")
    public void testUserCannotDeleteUser() throws Exception {
        Allure.step("Login as regular user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);

        Allure.step("Send DELETE");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/users/10", userToken); // already existing user id=10

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify error indicates admin required");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.equals("Admin privileges required"), "error indicates admin required");
    }

    @Test(description = "TC-USER-010: Verify delete users fails without authentication", dependsOnMethods = "testUserCannotDeleteUser")
    public void testDeleteUserFailsWithoutAuth() {
        Allure.step("Send DELETE without token");
        Response response = ApiUtils.deleteRequest(BASE_URL + "/users/10"); // already existing user id = 10

        Allure.step("Verify auth error");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates authentication required");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.equals("Token is missing")), "error indicates authentication required");
    }

    @Test(description = "TC-USER-011: Verify delete user fails for non-existent user", dependsOnMethods = "testDeleteUserFailsWithoutAuth")
    public void testDeleteUserFailsForNonExistent() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send DELETE with non-existent ID");
        Response response = ApiUtils.deleteRequestWithAuth(BASE_URL + "/users/999999", adminToken); // non existing user id

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error is 'User not found'");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.equals("User not found"), "error is 'User not found'"); // using true here due to using null
    }

    @Test(description = "TC-USER-012: Verify admin can view user activity", dependsOnMethods = "testDeleteUserFailsForNonExistent")
    public void testAdminCanViewUserActivity() throws Exception {
        Allure.step("Login as admin");
        adminToken = JsonUtility.getJSONString("admin", TOKEN_FILE_PATH);

        Allure.step("Send GET for user activity");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/10/activity", adminToken); // already existing user id=10

        Allure.step("Verify activity data");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data has required fields");
        Assert.assertNotNull(response.jsonPath().getList("data.orders"), "data has orders array");
        Assert.assertNotNull(response.jsonPath().getList("data.reviews"), "data has reviews array");
        Assert.assertTrue(response.jsonPath().get("data.user_id") instanceof Integer, "");
        Assert.assertTrue(response.jsonPath().get("data.total_orders") instanceof Integer, "");
        Assert.assertTrue(response.jsonPath().get("data.total_reviews") instanceof Integer, "");
      //  Assert.assertTrue(response.jsonPath().get("data.total_spent") instanceof Double, ""); // Double and BigDecimal are not working, only number work but it's not specific enough
        Assert.assertTrue(response.jsonPath().getDouble("data.total_spent") >= 0, "total_spent is number"); //  "total_spent": 989.8699999999999
        Assert.assertTrue(response.jsonPath().get("data.cart_items") instanceof Integer, "");


    }

    @Test(description = "TC-USER-013: Verify user cannot view user activity", dependsOnMethods = "testAdminCanViewUserActivity")
    public void testUserCannotViewUserActivity() throws Exception {
        Allure.step("Login as regular user");
        userToken = JsonUtility.getJSONString("user", TOKEN_FILE_PATH);

        Allure.step("Send GET for user activity");
        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/users/10/activity", userToken); // already existing user id=10

        Allure.step("Verify access denied");
        Assert.assertEquals(response.getStatusCode(), 403, "Status code is 403");

        Allure.step("Verify error indicates admin required");
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Admin privileges required", "error indicates admin required");
    }

    @Test(description = "TC-USER-014: Verify view user activity fails without authentication", dependsOnMethods = "testUserCannotViewUserActivity")
    public void testViewUserActivityFailsWithoutAuth() {
        Allure.step("Send GET without token");
        Response response = ApiUtils.getRequest(BASE_URL + "/users/10/activity"); // already existing user id = 10

        Allure.step("Verify auth error");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates authentication required");
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Token is missing", "error indicates authentication required");
    }
}
