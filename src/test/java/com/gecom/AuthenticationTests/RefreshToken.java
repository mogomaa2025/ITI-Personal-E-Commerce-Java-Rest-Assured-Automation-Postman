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
public class RefreshToken {

        Faker faker = new Faker();

        @Test(description = "TC-AUTH-008: Verify token refresh with valid refresh token", groups = {
                        "Valid-Authentication-Test", "valid" })
        public void testRefreshTokenValid() throws Exception {
                refreshToken = (String) JsonUtility.getValue("refreshToken", REFRESH_TOKEN_FILE_PATH);
                Assert.assertNotNull(refreshToken, "refreshToken token not found");

                Map<String, Object> body = new HashMap<>();
                body.put("refresh_token", refreshToken);

                Response response = ApiUtils.postRequest(BASE_URL + "/refresh", body);

                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "Token refreshed successfully",
                                "message is 'Token refreshed successfully'");
                String newToken = response.jsonPath().getString("token");
                Assert.assertTrue(newToken != null && !newToken.isEmpty(), "New token is valid JWT");
        }

        @Test(description = "TC-AUTH-009: Verify refresh fails with invalid token", groups = {
                        "Invalid-Authentication-Test", "invalid" })
        public void testRefreshTokenInvalid() {
                Map<String, Object> body = new HashMap<>();
                body.put("refresh_token", "invalid_token");

                Response response = ApiUtils.postRequest(BASE_URL + "/refresh", body);
                Assert.assertEquals(response.getStatusCode(), 401, "Status code is 401");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && (error.toLowerCase().contains("invalid")
                                || error.toLowerCase().contains("expired") || error.toLowerCase().contains("token")),
                                "error indicates invalid token");
        }

        @Test(description = "TC-AUTH-010: Verify refresh fails without refresh token", groups = {
                        "Invalid-Authentication-Test", "invalid" })
        public void testRefreshTokenMissing() {
                Map<String, Object> body = new HashMap<>();
                // no body

                Response response = ApiUtils.postRequest(BASE_URL + "/refresh", body);
                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && (error.toLowerCase().contains("required")),
                                "error indicates missing token");
        }
}
