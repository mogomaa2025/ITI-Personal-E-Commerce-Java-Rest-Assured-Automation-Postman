package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import static com.gecom.utils.Const.*;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "AuthenticationTest")
@Severity(SeverityLevel.CRITICAL)
public class AuthenticationTest {

    Faker faker = new Faker();


    @Test(description = "TC-AUTH-001: Verify user registration with valid data")
    public void testRegisterUserValidRequest() {
        Allure.step("Generate random email");
        userEmail = "test_" + faker.random().hex(8) + "@gmail.com";
        userPassword = "Test@123";
        String username = "Test User";

        Allure.step("Send POST with user data");
        Map<String, Object> body = new HashMap<>();
        body.put("email", userEmail);
        body.put("password", userPassword);
        body.put("phone", "+010"+faker.number().digits(8)); // new validation
        body.put("address", faker.address().fullAddress()); // new validation
        body.put("name", username);
        body.put("is_admin", false);

        Response response = ApiUtils.postRequest(BASE_URL + "/register", body);

        Allure.step("Verify status 201");
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Allure.step("Validate response");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message equals 'User registered successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "User registered successfully", "message should be 'User registered successfully'");

        Allure.step("Verify data.id is number");
        Assert.assertTrue(response.jsonPath().getInt("data.id") > 0, "data.id is valid number");

        Allure.step("Verify data.email matches input");
        Assert.assertEquals(response.jsonPath().getString("data.email"), userEmail, "data.email matches input");

        Allure.step("Verify data.name matches input");
        Assert.assertEquals(response.jsonPath().getString("data.name"), username, "data.name matches input");

        Allure.step("Verify data.is_admin is false");
        Assert.assertFalse(response.jsonPath().getBoolean("data.is_admin"), "data.is_admin is false"); //assert false

        Allure.step("Verify created_at is timestamp");
        String createdAt = response.jsonPath().getString("data.created_at");
        // !createdAt.isEmpty() means not "" and null mean has value
        //Assert.assertNotNull(createdAt, "created_at should not be null");
        //   Assert.assertFalse(createdAt.isEmpty(), "created_at should not be empty");
        Assert.assertTrue(createdAt != null && !createdAt.isEmpty(), "created_at is timestamp");

        //as we know for security purpose password should not be returned or exposed
        Allure.step("Verify no password in response");
        Assert.assertNull(response.jsonPath().get("data.password"), "No password in response");
    }

    @Test(description = "TC-AUTH-002: Verify registration fails with duplicate email", dependsOnMethods = "testRegisterUserValidRequest") // we use dependence because we test last email used
    public void testRegisterUserDuplicateEmail() {
        Allure.step("Use existing email");
        Map<String, Object> body = new HashMap<>();
        body.put("email", userEmail);
        body.put("password", "Test@123");
        body.put("name", "Test");
        body.put("phone", "+010"+faker.number().digits(8)); // new validation
        body.put("address", faker.address().fullAddress()); // new validation
        body.put("is_admin", false);

        Allure.step("Send POST request");
        Response response = ApiUtils.postRequest(BASE_URL + "/register", body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 409, "Status code is 409 Conflict");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates duplicate email");
        String errory = response.jsonPath().getString("error");
        Assert.assertTrue(errory != null && (errory.contains("already") || errory.contains("exists") || errory.contains("duplicate")), "error indicates duplicate email");
    }

    @Test(description = "TC-AUTH-003: Verify registration fails with missing required fields")
    public void testRegisterUserMissingRequiredFields() {
        Allure.step("Send POST with incomplete data");
        Map<String, Object> body = new HashMap<>();
        body.put("email", "test@test.com");
        // Missing password field

        Response response = ApiUtils.postRequest(BASE_URL + "/register", body);

        Allure.step("Verify validation error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates missing fields");
        Assert.assertEquals(response.jsonPath().getString("error"), "Email and password are required", "message should be 'Email and password are required'");

    }

    @Test(description = "TC-AUTH-004: Verify user login with valid credentials", dependsOnMethods = "testRegisterUserValidRequest") // use TC1 random register data
    public void testLoginUserValidCredentials() throws Exception {

        Allure.step("Send POST with valid credentials");
        Map<String, Object> body = new HashMap<>();
        body.put("email", userEmail);
        body.put("password", userPassword);

        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

        Allure.step("Verify status 200");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Validate tokens");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Login successful'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Login successful", "message is 'Login successful'");

        Allure.step("Verify token present and not empty");
        userToken = response.jsonPath().getString("token");
        Assert.assertTrue(userToken != null && !userToken.isEmpty(), "token present and not empty");

        Allure.step("Verify refresh_token present");
        refreshToken = response.jsonPath().getString("refresh_token");
        Assert.assertTrue(refreshToken != null && !refreshToken.isEmpty(), "Both tokens are JWT format");

        Allure.step("Verify user object has id/email/name/is_admin");
        Assert.assertTrue(response.jsonPath().getInt("user.id") > 0, "user object has id");
        Assert.assertNotNull(response.jsonPath().getString("user.email"), "user object has email");
        Assert.assertNotNull(response.jsonPath().getString("user.name"), "user object has name");
        Assert.assertNotNull(response.jsonPath().get("user.is_admin"), "user object has is_admin");

        Allure.step("Save tokens");
        JsonUtility.saveValue("user", userToken, TOKEN_FILE_PATH);
    }

    @Test(description = "TC-AUTH-005: Verify login fails with invalid password")
    public void testLoginUserInvalidPassword() {
        Allure.step("Send POST with wrong password");
        Map<String, Object> body = new HashMap<>();
        body.put("email", "test@test.com");
        body.put("password", "WrongPass");

        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates invalid credentials");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.toLowerCase().contains("invalid") || error.toLowerCase().contains("credentials")), "error indicates invalid credentials");

        Allure.step("Verify no token returned");
        Assert.assertNull(response.jsonPath().get("token"), "No token returned");
    }

    @Test(description = "TC-AUTH-006: Verify login fails with non-existent user")
    public void testLoginUserNonExistent() {
        Allure.step("Send POST with non-existent email");
        Map<String, Object> body = new HashMap<>();
        body.put("email", "notexist@test.com");
        body.put("password", "Test@123");

        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates invalid credentials");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.toLowerCase().contains("invalid") || error.toLowerCase().contains("credentials")), "error indicates invalid credentials");

        Allure.step("Verify no token returned");
        Assert.assertNull(response.jsonPath().get("token"), "No token returned");
    }

    @Test(description = "TC-AUTH-007: Verify admin login with valid credentials")
    public void testAdminLoginValidCredentials() throws Exception {
        Allure.step("Send POST with admin credentials");
        Map<String, Object> body = new HashMap<>();
        body.put("email", ADMIN_EMAIL);
        body.put("password", ADMIN_PASSWORD);

        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

        Allure.step("Verify status 200");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify user.is_admin is true");
        Assert.assertTrue(response.jsonPath().getBoolean("user.is_admin"), "user.is_admin is true");

        Allure.step("Verify both tokens present and valid JWT");
        adminToken = response.jsonPath().getString("token");
        refreshToken = response.jsonPath().getString("refresh_token");
        String adminRefreshToken = response.jsonPath().getString("refresh_token");
        Assert.assertTrue(adminToken != null && !adminToken.isEmpty(), "Both tokens present and valid JWT");
        Assert.assertTrue(adminRefreshToken != null && !adminRefreshToken.isEmpty(), "Both tokens present and valid JWT");

        Allure.step("Save token for admin operations");
        JsonUtility.saveValue("admin", adminToken, TOKEN_FILE_PATH);
        // not mandatory as we use public static variable in const we can use it directly instead of json file
        // it's just for my graduation iti project
        JsonUtility.saveValue("refreshToken", refreshToken, REFRESH_TOKEN_FILE_PATH);
    }

    @Test(description = "TC-AUTH-008: Verify token refresh with valid refresh token",dependsOnMethods = "testAdminLoginValidCredentials")
    public void testRefreshTokenValid() throws Exception {
        refreshToken = JsonUtility.getJSONString("refreshToken", REFRESH_TOKEN_FILE_PATH);
        Assert.assertNotNull(refreshToken, "refreshToken token not found");

        Allure.step("Use valid refresh token");
        Map<String, Object> body = new HashMap<>();
        body.put("refresh_token", refreshToken);

        Allure.step("Send POST to refresh");
        Response response = ApiUtils.postRequest(BASE_URL + "/refresh", body);

        Allure.step("Verify new token returned");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify message is 'Token refreshed successfully'");
        Assert.assertEquals(response.jsonPath().getString("message"), "Token refreshed successfully", "message is 'Token refreshed successfully'");

        Allure.step("Verify new token present");
        String newToken = response.jsonPath().getString("token");
        Assert.assertTrue(newToken != null && !newToken.isEmpty(), "New token is valid JWT");
    }

    @Test(description = "TC-AUTH-009: Verify refresh fails with invalid token")
    public void testRefreshTokenInvalid() {
        Allure.step("Send POST with invalid token");
        Map<String, Object> body = new HashMap<>();
        body.put("refresh_token", "invalid_token");

        Response response = ApiUtils.postRequest(BASE_URL + "/refresh", body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates invalid token");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.toLowerCase().contains("invalid") || error.toLowerCase().contains("expired") || error.toLowerCase().contains("token")), "error indicates invalid token");
    }

    @Test(description = "TC-AUTH-010: Verify refresh fails without refresh token")
    public void testRefreshTokenMissing() {
        Allure.step("Send POST without refresh token");
        Map<String, Object> body = new HashMap<>();
        // no body

        Response response = ApiUtils.postRequest(BASE_URL + "/refresh", body);

        Allure.step("Verify error");
        Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates missing token");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && (error.toLowerCase().contains("required")), "error indicates missing token");
    }
}
