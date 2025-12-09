package com.gecom.AuthenticationTests;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import com.github.javafaker.Faker;
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
@Test(groups = "AuthenticationTest")
@Severity(SeverityLevel.CRITICAL)
public class Login {

        Faker faker = new Faker();

        @Test(description = "TC-AUTH-004: Verify user login with valid credentials", groups = {
                        "Valid-Authentication-Test", "valid" , "token"  })
        public void testLoginUserValidCredentials() throws Exception {

                Map<String, Object> body = new HashMap<>();
                body.put("email", GetValidEmail());
                body.put("password", GetValidPassword());

                Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Login successful",
                                "message is 'Login successful'");

                Assert.assertTrue(userToken != null && !userToken.isEmpty(), "token present and not empty");
                SetUserToken(response);

                Assert.assertTrue(response.jsonPath().getInt("user.id") > 0, "user object has id");
                Assert.assertNotNull(response.jsonPath().getString("user.email"), "user object has email");
                Assert.assertNotNull(response.jsonPath().getString("user.name"), "user object has name");
                Assert.assertNotNull(response.jsonPath().get("user.is_admin"), "user object has is_admin");
//                JsonUtility.saveValue("user", userToken, TOKEN_FILE_PATH);

        }

        @Test(description = "TC-AUTH-005: Verify login fails with invalid password", groups = {
                        "Invalid-Authentication-Test", "invalid" })
        public void testLoginUserInvalidPassword() {
                Map<String, Object> body = new HashMap<>();
                body.put("email", "test@test.com");
                body.put("password", "WrongPass");

                Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

                Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(
                                error != null && (error.toLowerCase().contains("invalid")
                                                || error.toLowerCase().contains("credentials")),
                                "error indicates invalid credentials");
                Assert.assertNull(response.jsonPath().get("token"), "No token returned");
        }

        @Test(description = "TC-AUTH-006: Verify login fails with non-existent user", groups = {
                        "Invalid-Authentication-Test", "invalid" })
        public void testLoginUserNonExistent() {
                Map<String, Object> body = new HashMap<>();
                body.put("email", "notexist@test.com");
                body.put("password", "Test@123");

                Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

                Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && (error.toLowerCase().contains("invalid")
                                || error.toLowerCase().contains("credentials")),
                                "error indicates invalid credentials");
                Assert.assertNull(response.jsonPath().get("token"), "No token returned");
        }

        @Test(description = "TC-AUTH-007: Verify admin login with valid credentials", groups = {
                        "Valid-Authentication-Test", "valid", "token"  })
        public void testAdminLoginValidCredentials() throws Exception {
                Map<String, Object> body = new HashMap<>();
                body.put("email", ADMIN_EMAIL);
                body.put("password", ADMIN_PASSWORD);

                Response response = ApiUtils.postRequest(BASE_URL + "/login", body);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertTrue(response.jsonPath().getBoolean("user.is_admin"), "user.is_admin is true");
                SetAdminToken(response);
                SetRefreshToken(response);
                String adminRefreshToken = response.jsonPath().getString("refresh_token");
                Assert.assertTrue(adminToken != null && !adminToken.isEmpty(), "Both tokens present and valid JWT");
                Assert.assertTrue(adminRefreshToken != null && !adminRefreshToken.isEmpty(),
                                "Both tokens present and valid JWT");

        }

}
