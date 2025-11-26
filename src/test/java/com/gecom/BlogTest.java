package com.gecom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;

import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

import static com.gecom.utils.Const.*;

/**
 * This class contains test cases for the blog functionalities,
 * including retrieving all posts, handling empty results, and fetching specific posts.
 */
@Listeners({com.gecom.utils.TestListener.class, AllureTestNg.class})
@Test(groups = "BlogTest")
@Severity(SeverityLevel.CRITICAL)
public class BlogTest {

    /**
     * Test case for verifying that all blog posts can be retrieved successfully.
     *
     * @throws Exception if an error occurs while saving the blog post ID.
     */
    @Test(description = "TC-BLOG-001: Verify get all blog posts")
    public void testGetAllBlogPosts() throws Exception {
        Allure.step("Send GET to blog posts");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", "published");
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/blog/posts", queryParams);

        Allure.step("Verify status code is 200");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is array");
        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        Allure.step("Verify count matches array length");
        List<Map<String, Object>> posts = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, posts.size(), "count matches array length");

        Allure.step("Verify each post has id/title/content/author/published_date");
        if (!posts.isEmpty()) {
            Map<String, Object> firstPost = posts.get(0);
            Assert.assertTrue(firstPost.get("id") instanceof Integer, "Each post has id");
            Assert.assertTrue(firstPost.get("title") instanceof String, "Each post has title");
            Assert.assertTrue(firstPost.get("content") instanceof String, "Each post has content");
            Assert.assertTrue(firstPost.get("author") instanceof String, "Each post has author");
            Assert.assertTrue(firstPost.get("created_at") instanceof String, "Each post has published_date");
            
            Allure.step("Save last blog post ID");
            Map<String, Object> lastPost = posts.get(posts.size() - 1);
            blogPostId = (Integer) lastPost.get("id");
            JsonUtility.saveValue("blog_post_id", blogPostId, IDS_FILE_PATH);
        }
    }

    /**
     * Test case for verifying that an empty array is returned when no blog posts are found.
     *
     * @throws Exception if an error occurs.
     */
    @Test(description = "TC-BLOG-002: Verify empty blog returns empty array", dependsOnMethods = "testGetAllBlogPosts")
    public void testEmptyBlogReturnsEmptyArray() throws Exception {
        Allure.step("Send GET to non-existent blog endpoint");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", "non-exist");
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/blog/posts",queryParams);

        Allure.step("Verify status code is 200");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data is empty array");
        List<Map<String, Object>> data = response.jsonPath().getList("data");
        Assert.assertNotNull(data, "data is array");
        Assert.assertEquals(data.size(), 0, "data is empty array");

        Allure.step("Verify count is 0");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, 0, "count is 0");
    }

    /**
     * Test case for verifying that a specific blog post can be retrieved successfully.
     *
     * @throws Exception if an error occurs while reading the blog post ID.
     */
    @Test(description = "TC-BLOG-003: Verify get specific blog post", dependsOnMethods = "testEmptyBlogReturnsEmptyArray")
    public void testGetSpecificBlogPost() throws Exception {
        Allure.step("Get blog post ID from previous test");
        blogPostId = JsonUtility.getJSONInt("blog_post_id", IDS_FILE_PATH);
        Assert.assertNotNull(blogPostId, "Blog post ID not found");

        Allure.step("Send GET for specific post");
        Response response = ApiUtils.getRequest(BASE_URL + "/blog/posts/" + blogPostId);

        Allure.step("Verify status code is 200");
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is true");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Allure.step("Verify data has complete post info");
        Map<String, Object> post = response.jsonPath().getMap("data");
        Assert.assertNotNull(post, "data has complete post info");
        Assert.assertTrue(post.get("id") instanceof Integer, "Post has id");
        Assert.assertTrue(post.get("title") instanceof String, "Post has title");
        Assert.assertTrue(post.get("content") instanceof String, "Post has content");
        Assert.assertTrue(post.get("author") instanceof String, "Post has author");
        Assert.assertTrue(post.get("created_at") instanceof String, "Post has created_at");
    }

    /**
     * Test case for verifying that retrieving a blog post with a non-existent ID fails.
     *
     * @throws Exception if an error occurs.
     */
    @Test(description = "TC-BLOG-004: Verify get blog post fails for non-existent ID", dependsOnMethods = "testGetSpecificBlogPost")
    public void testGetBlogPostFailsForNonExistentID() throws Exception {
        Allure.step("Send GET with invalid post ID");
        Response response = ApiUtils.getRequest(BASE_URL + "/blog/posts/99999");

        Allure.step("Verify status code is 404");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

        Allure.step("Verify response is valid JSON");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Allure.step("Verify success is false");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

        Allure.step("Verify error indicates post not found");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Post not found"), "error indicates post not found");
    }
}