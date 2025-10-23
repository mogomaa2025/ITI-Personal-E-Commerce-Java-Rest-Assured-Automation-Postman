package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import com.gecom.utils.RemoveAllureResult;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import static com.gecom.utils.Const.*;
import static com.gecom.utils.RemoveAllureResult.deleteFolder;

public class AuthenticationTest {





    Faker faker = new Faker();

    @Test(groups = "Authentication" )
    public void testRegisterRandomUser() {
        userEmail = faker.internet().emailAddress();
        userPassword = faker.internet().password(8, 16, true, true, true);
        String username = faker.name().username();

        Map<String, String> body = new HashMap<>();
        body.put("email", userEmail);
        body.put("password", userPassword);
        body.put("username", username);

        Response response = ApiUtils.postRequest(BASE_URL + "/register", body);
        Assert.assertEquals(response.getStatusCode(), 201, "Registration should succeed");
        Assert.assertTrue(response.asString().contains("success"));
    }

    @Test(groups = "Authentication",  dependsOnMethods = "testRegisterRandomUser" )
    public void testLoginUser() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", userEmail);
        body.put("password", userPassword);

        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);
        Assert.assertEquals(response.getStatusCode(), 200, "Login should succeed");
        userToken = response.jsonPath().getString("token");
        Assert.assertNotNull(userToken, "Bearer token should be returned");

        // Save token to token.json via JsonUtility
        JsonUtility.saveToken("user", userToken, TOKEN_FILE_PATH);
    }

    @Test(groups = "Authentication",  dependsOnMethods = "testLoginUser" )
    public void testLoginAdmin() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("email", ""+adminEmail+"");
        body.put("password", ""+adminPass+"");

        Response response = ApiUtils.postRequest(BASE_URL + "/login", body);
        Assert.assertEquals(response.getStatusCode(), 200, "Admin login should succeed");

        adminToken = response.jsonPath().getString("token");
        Assert.assertNotNull(adminToken, "Admin Bearer token should be returned");

        // Save token to token.json via JsonUtility
        JsonUtility.saveToken("admin", adminToken, TOKEN_FILE_PATH);
    }
}
