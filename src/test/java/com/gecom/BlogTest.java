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
@Test(groups = "BlogTest")
public class BlogTest {


    @Test
    public void testListBlogPosts() throws Exception {
        Allure.step("Starting testListBlogPosts...");

        Response response = ApiUtils.getRequest(BASE_URL + "/blog/posts?status=published");
        Allure.step("List blog posts status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));


         blogPostId = JsonUtility.getLastUserId(response);
        JsonUtility.saveToken("blog_post_id", String.valueOf(blogPostId), IDS_FILE_PATH);
        Allure.step("blog_post_id saved: " + blogPostId);

        Allure.step("testListBlogPosts finished successfully.");
    }

    @Test(dependsOnMethods = "testListBlogPosts")
    public void testGetBlogPost() throws Exception {
        Allure.step("Starting testGetBlogPost...");
        String blogPostId = JsonUtility.getToken("blog_post_id", IDS_FILE_PATH);
        Assert.assertNotNull(blogPostId, "Blog post ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/blog/posts/" + blogPostId);
        Allure.step("Get blog post status code: " + response.getStatusCode());
        System.out.println("testGetBlogPost: " + response.asString());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Allure.step("testGetBlogPost finished successfully.");
    }




}
