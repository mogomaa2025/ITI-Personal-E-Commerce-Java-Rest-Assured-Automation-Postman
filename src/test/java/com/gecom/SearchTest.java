package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.gecom.utils.Const.*;

@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "SearchTest")
public class SearchTest {

    @Test(groups = "testAdvancedSearch")
    public void testAdvancedSearch() {
        Allure.step("Starting testAdvancedSearch...");

        String endpoint = BASE_URL + "/search/advanced?q="+searchQuery+"&category="+searchCategory+"&min_price="+searchMinPrice+"&max_price="+searchMaxPrice+"&min_rating="+searchMinRating+"&sort_by="+searchSortBy+"&sort_order="+searchSortOrder+" ";
        Response response = ApiUtils.getRequest(endpoint);
        Allure.step("Advanced search status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        String responseBody = response.asString().toLowerCase();
        System.out.println("Advanced Search Response Body: " + responseBody);
        Assert.assertTrue(responseBody.contains(" " + searchQuery + " "));

        Allure.step("testAdvancedSearch finished successfully.");
    }

    @Test(dependsOnMethods = "testAdvancedSearch")
    public void testProductRecommendations() {
        Allure.step("Starting testProductRecommendations...");

        Response response = ApiUtils.getRequest(BASE_URL + "/recommendations/1");
        Allure.step("Product recommendations status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 201 Created");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testProductRecommendations finished successfully.");
    }

    @Test(dependsOnMethods = "testProductRecommendations")
    public void testUserRecommendations() throws Exception {
        Allure.step("Starting testUserRecommendations...");
        String userId = JsonUtility.getToken("user_id", IDS_FILE_PATH);
        Assert.assertNotNull(userId, "User ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/recommendations/user/" + userId);
        Allure.step("User recommendations status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testUserRecommendations finished successfully.");
    }
}
