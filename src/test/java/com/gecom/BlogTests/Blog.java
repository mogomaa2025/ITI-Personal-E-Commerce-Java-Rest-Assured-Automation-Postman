package com.gecom.BlogTests;

import static com.gecom.utils.Base.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "BlogTest")
@Severity(SeverityLevel.MINOR)
public class Blog {

    @Test(description = "TC-BLOG-001: Verify get all blog posts", groups = { "Valid-Blog-Test", "valid" })
    public void testGetAllBlogPosts() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", "published");
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/blog/posts", queryParams);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");
        List<Map<String, Object>> posts = response.jsonPath().getList("data");
        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, posts.size(), "count matches array length");

        if (!posts.isEmpty()) {
            Map<String, Object> firstPost = posts.get(0);
            Assert.assertTrue(firstPost.get("id") instanceof Integer, "Each post has id");
            Assert.assertTrue(firstPost.get("title") instanceof String, "Each post has title");
            Assert.assertTrue(firstPost.get("content") instanceof String, "Each post has content");
            Assert.assertTrue(firstPost.get("author") instanceof String, "Each post has author");
            Assert.assertTrue(firstPost.get("created_at") instanceof String, "Each post has published_date");

            Map<String, Object> lastPost = posts.get(posts.size() - 1);
            blogPostId = (Integer) lastPost.get("id");
            JsonUtility.saveValue("blog_post_id", blogPostId, IDS_FILE_PATH);
        }
    }

    @Test(description = "TC-BLOG-002: Verify empty blog returns empty array", groups = { "Invalid-Blog-Test",
            "invalid" })
    public void testEmptyBlogReturnsEmptyArray() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", "non-exist");
        Response response = ApiUtils.getRequestWithQuery(BASE_URL + "/blog/posts", queryParams);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        List<Map<String, Object>> data = response.jsonPath().getList("data");
        Assert.assertNotNull(data, "data is array");
        Assert.assertEquals(data.size(), 0, "data is empty array");

        int count = response.jsonPath().getInt("count");
        Assert.assertEquals(count, 0, "count is 0");
    }

    @Test(description = "TC-BLOG-003: Verify get specific blog post", groups = { "Valid-Blog-Test", "valid" })
    public void testGetSpecificBlogPost() throws Exception {
        blogPostId = (Integer) JsonUtility.getValue("blog_post_id", IDS_FILE_PATH);
        Assert.assertNotNull(blogPostId, "Blog post ID not found");

        Response response = ApiUtils.getRequest(BASE_URL + "/blog/posts/" + blogPostId);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Map<String, Object> post = response.jsonPath().getMap("data");
        Assert.assertNotNull(post, "data has complete post info");
        Assert.assertTrue(post.get("id") instanceof Integer, "Post has id");
        Assert.assertTrue(post.get("title") instanceof String, "Post has title");
        Assert.assertTrue(post.get("content") instanceof String, "Post has content");
        Assert.assertTrue(post.get("author") instanceof String, "Post has author");
        Assert.assertTrue(post.get("created_at") instanceof String, "Post has created_at");
    }

    @Test(description = "TC-BLOG-004: Verify get blog post fails for non-existent ID", groups = { "Invalid-Blog-Test",
            "invalid" })
    public void testGetBlogPostFailsForNonExistentID() throws Exception {
        Response response = ApiUtils.getRequest(BASE_URL + "/blog/posts/99999");
        Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");
        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");
        String error = response.jsonPath().getString("error");
        Assert.assertTrue(error != null && error.contains("Post not found"), "error indicates post not found");
    }
}