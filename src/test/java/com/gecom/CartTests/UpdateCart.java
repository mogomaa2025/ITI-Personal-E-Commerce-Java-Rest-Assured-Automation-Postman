package com.gecom.CartTests;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;

import static com.gecom.utils.Base.*;

import java.util.HashMap;
import java.util.Map;

@Listeners({ com.gecom.utils.TestListener.class, AllureTestNg.class })
@Test(groups = "CartTest")
@Severity(SeverityLevel.CRITICAL)
public class UpdateCart {



    @BeforeMethod(onlyForGroups = "NeedItemsInCarts-user", alwaysRun = true)
    public void Precondition() throws Exception {
        userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
        Assert.assertNotNull(userToken, "User token is valid String");

        ApiUtils.deleteRequestWithAuth(BASE_URL + "/cart", userToken); // pre-condition for clean before add

        Map<String, Object> body = new HashMap<>();
        body.put("product_id", CART_PRODUCT_ID);
        body.put("quantity", CART_QUANTITY);
        Response response = ApiUtils.postRequestWithAuth(BASE_URL + "/cart/items", userToken, body);
        Assert.assertEquals(response.getStatusCode(), 201, "Status code is 201");

        Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");
        Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");
    }


        @Test(description = "TC-CART-005: Verify user can update cart item quantity", groups = { "Valid-Cart-Test",
                        "valid", "NeedItemsInCart" })
        public void testUserCanUpdateCartItemQuantity() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                cartItemId = (Integer) JsonUtility.getValue("cart_item_id", IDS_FILE_PATH);
                Assert.assertNotNull(cartItemId, "Cart item ID is valid Integer");

                Map<String, Object> body = new HashMap<>();
                body.put("quantity", CART_UPDATE_QUANTITY);
                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/cart/items/" + cartItemId, userToken,
                                body);
                Assert.assertEquals(response.getStatusCode(), 200, "Status code is 200");
                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertTrue(response.jsonPath().getBoolean("success"), "success is true");

                Assert.assertEquals(response.jsonPath().getString("message"), "Cart item updated successfully",
                                "message is 'Cart item updated successfully'");
                Assert.assertEquals(response.jsonPath().getInt("data.quantity"), CART_UPDATE_QUANTITY,
                                "data.quantity is " + CART_UPDATE_QUANTITY);

                Assert.assertNotNull(response.jsonPath().get("data.updated_at"), "updated_at is recent");
        }

        @Test(description = "TC-CART-006: Verify update cart item fails for non-existent item", groups = {
                        "Invalid-Cart-Test", "invalid" }, dependsOnMethods = "testUserCanUpdateCartItemQuantity")
        public void testUpdateCartItemFailsForNonExistentItem() throws Exception {
                userToken = (String) JsonUtility.getValue("user", TOKEN_FILE_PATH);
                Assert.assertNotNull(userToken, "User token is valid String");

                Map<String, Object> body = new HashMap<>();
                body.put("quantity", 2);

                Response response = ApiUtils.putRequestWithAuth(BASE_URL + "/cart/items/" + INVALID_CART_ITEM_ID,
                                userToken,
                                body);

                Assert.assertEquals(response.getStatusCode(), 404, "Status code is 404");

                Assert.assertNotNull(response.jsonPath(), "Response is valid JSON");

                Assert.assertFalse(response.jsonPath().getBoolean("success"), "success is false");

                String error = response.jsonPath().getString("error");
                Assert.assertTrue(error != null && error.contains("Cart item not found"),
                                "error is 'Cart item not found'");
        }

}
