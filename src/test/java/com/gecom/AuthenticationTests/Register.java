package com.gecom.AuthenticationTests;

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

import static com.gecom.utils.Const.*;

import java.util.HashMap;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "AuthenticationTest")
@Severity(SeverityLevel.CRITICAL)
public class Register {

        Faker faker = new Faker();

        @Test(description = "TC-AUTH-001: Verify user registration with valid data", groups = {
                        "Valid-Authentication-Test", "valid" })
        public void testRegisterUserValidRequest() {
                Allure.step("Generate random email");
                userEmail = "test_" + faker.random().hex(8) + "@gmail.com";
                userPassword = "Test@123";
                String username = "Test User";

                Allure.step("Send POST with user data");
                Map<String, Object> body = new HashMap<>();
                body.put("email", userEmail);
                body.put("password", userPassword);
                body.put("phone", "+010" + faker.number().digits(8)); // new validation
                body.put("address", faker.address().fullAddress()); // new validation
                body.put("name", username);
                body.put("is_admin", false);

                Response response = ApiUtils.postRequest(BASE_URL + "/register", body);

                Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
                Assert.assertEquals(response.jsonPath().getString("message"), "User registered successfully",
                                "message should be 'User registered successfully'");
                Assert.assertTrue(response.jsonPath().getInt("data.id") > 0, "data.id is valid number");
                Assert.assertEquals(response.jsonPath().getString("data.email"), userEmail, "data.email matches input");
                Assert.assertEquals(response.jsonPath().getString("data.name"), username, "data.name matches input");
                Assert.assertFalse(response.jsonPath().getBoolean("data.is_admin"), "data.is_admin is false");
                String createdAt = response.jsonPath().getString("data.created_at");
                Assert.assertTrue(createdAt != null && !createdAt.isEmpty(), "created_at is timestamp");

                // as we know for security purpose password should not be returned or exposed
                Assert.assertNull(response.jsonPath().get("data.password"), "No password in response");
        }

        @Test(description = "TC-AUTH-002: Verify registration fails with duplicate email", groups = {
                        "Invalid-Authentication-Test", "invalid" })
        public void testRegisterUserDuplicateEmail() {
                Allure.step("Use existing email");
                Map<String, Object> body = new HashMap<>();
                body.put("email", userEmail);
                body.put("password", "Test@123");
                body.put("name", "Test");
                body.put("phone", "+010" + faker.number().digits(8)); // new validation
                body.put("address", faker.address().fullAddress()); // new validation
                body.put("is_admin", false);

                Response response = ApiUtils.postRequest(BASE_URL + "/register", body);

                Assert.assertEquals(response.getStatusCode(), 409, "Status code is 409 Conflict");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

                String errory = response.jsonPath().getString("error");
                Assert.assertTrue(errory != null && (errory.contains("already") || errory.contains("exists")
                                || errory.contains("duplicate")), "error indicates duplicate email");
        }

        @Test(description = "TC-AUTH-003: Verify registration fails with missing required fields", groups = {
                        "Invalid-Authentication-Test", "invalid" })
        public void testRegisterUserMissingRequiredFields() {
                Allure.step("Send POST with incomplete data");
                Map<String, Object> body = new HashMap<>();
                body.put("email", "test@test.com");
                // Missing password field

                Response response = ApiUtils.postRequest(BASE_URL + "/register", body);

                Assert.assertEquals(response.getStatusCode(), 400, "Status code is 400");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
                Assert.assertEquals(response.jsonPath().getString("error"), "Email and password are required",
                                "message should be 'Email and password are required'");

        }

}
