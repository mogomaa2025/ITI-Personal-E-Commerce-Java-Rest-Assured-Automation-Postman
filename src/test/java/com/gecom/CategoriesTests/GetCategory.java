package com.gecom.CategoriesTests;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import static com.gecom.utils.Const.*;
import java.util.List;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "CategoriesTest")
@Severity(SeverityLevel.CRITICAL)
public class GetCategory {

    @Test(description = "TC-CAT-003: Verify user can view all categories", groups = { "Valid-Categories-Test",
            "valid" })
    public void testUserCanViewAllCategories() throws Exception {
        Response response = ApiUtils.getRequest(BASE_URL + "/categories");

        Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

        Assert.assertNotNull(response.jsonPath().getList("data"), "data is array");

        int count = response.jsonPath().getInt("count");
        int dataSize = response.jsonPath().getList("data").size();
        Assert.assertEquals(count, dataSize, "count matches data length");

        Assert.assertTrue(dataSize > 0, "At least one CATEGORY exists");

        List<Map<String, Object>> categories = response.jsonPath().getList("data");
        for (Map<String, Object> category : categories) {
            Assert.assertTrue(category.get("id") instanceof Integer, "Each category has id");
            Assert.assertTrue(category.get("name") instanceof String, "Each category has name");
            Assert.assertTrue(category.get("description") instanceof String, "Each category has description");
            Assert.assertTrue(category.get("created_at") instanceof String, "Each category has created_at");
        }

        categoryId = (Integer) categories.get(dataSize - 1).get("id");
        JsonUtility.saveValue("category_id", categoryId, IDS_FILE_PATH);

        List<String> categoryNames = response.jsonPath().getList("data.name");
        List<String> uniqueCategoryNames = categoryNames.stream().distinct()
                .collect(java.util.stream.Collectors.toList());
        JsonUtility.saveValue("category_names", uniqueCategoryNames, IDS_FILE_PATH);
    }

}
