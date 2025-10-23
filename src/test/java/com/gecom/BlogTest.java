package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gecom.utils.Const.*;

@Listeners({AllureTestNg.class})
public class BlogTest {

    @Test(groups = "BlogTest")
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

    @Test(groups = "BlogTest",  dependsOnMethods = "testListBlogPosts")
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



    @AfterSuite(alwaysRun = true)
    public static  void runAllureReport() throws IOException, InterruptedException {

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "generateReport.bat");
            pb.inheritIO(); // Print output directly to console

            Process process = pb.start();
            process.waitFor();

            System.out.println("Batch file completed!");

    }



    }



