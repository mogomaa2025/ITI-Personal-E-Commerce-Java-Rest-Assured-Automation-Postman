package com.gecom;

import com.gecom.utils.ApiUtils;
import com.gecom.utils.JsonUtility;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.gecom.utils.Const.*;

@Listeners({AllureTestNg.class})
public class StatsAnalyticsTest {

    @Test(groups = "StatsAnalyticsTest")
    public void testGetAdminStats() throws Exception {
        Allure.step("Starting testGetAdminStats...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/stats", adminToken);
        Allure.step("Admin stats API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Map<String, Object> data = response.jsonPath().getMap("data");
        System.out.println("testGetAdminStats: " + data); //log
        Assert.assertNotNull(data.get("total_orders"));
        Assert.assertNotNull(data.get("total_products"));
        Assert.assertNotNull(data.get("total_revenue"));
        Assert.assertNotNull(data.get("total_reviews"));
        Assert.assertNotNull(data.get("total_users"));

        Allure.step("testGetAdminStats finished successfully.");
    }

    @Test(groups = "StatsAnalyticsTest",  dependsOnMethods = "testGetAdminStats")
    public void testGetDashboardAnalytics() throws Exception {
        Allure.step("Starting testGetDashboardAnalytics...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        Response response = ApiUtils.getRequestWithAuth(BASE_URL + "/analytics/dashboard", adminToken);
        Allure.step("Dashboard analytics API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        //check monthly_revenue is double and not null
        Double monthlyRevenue = response.jsonPath().getDouble("data.monthly_revenue");
        System.out.println("testGetDashboardAnalytics, Monthly Revenue: " + monthlyRevenue); //log
        Assert.assertNotNull(monthlyRevenue, "Monthly revenue should not be null");

        Allure.step("testGetDashboardAnalytics finished successfully.");
    }

    @Test(groups = "StatsAnalyticsTest",  dependsOnMethods = "testGetDashboardAnalytics")
    public void testGetSalesReport() throws Exception {
        Allure.step("Starting testGetSalesReport...");
        String adminToken = JsonUtility.getToken("admin", TOKEN_FILE_PATH);
        Assert.assertNotNull(adminToken, "Admin token not found");

        String endpoint = BASE_URL + "/analytics/reports/sales?start_date="+SalesReportStartDate+"&end_date="+SalesReportEndDate+"";
        Response response = ApiUtils.getRequestWithAuth(endpoint, adminToken);
        Allure.step("Sales report API response status code: " + response.getStatusCode());

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        System.out.println("testGetSalesReport: " + response.asString()); //log full

        //check total_sales is double and not null
        Double total_sales = response.jsonPath().getDouble("data.summary.total_sales"); //level 3 tree using https://jsonformatter.org/
        System.out.println("testGetSalesReport, total_sales: " + total_sales); //log
        Assert.assertNotNull(total_sales, "total_sales should not be null");




        //check count of orders = total of orders
        Double total_orders = response.jsonPath().getDouble("data.summary.total_orders"); //level 3 tree using https://jsonformatter.org/

        //loop through id by id and count
        List<Map<String, Object>> orders = response.jsonPath().getList("data.orders");
        double counter = 0.0;

        if (orders != null) {
            for (Map<String, Object> order : orders) {
                try {
                    Double amount = Double.valueOf(order.get("id").toString());
                    counter += 1;
//                    System.out.println("Order amount: " + amount);
                } catch (Exception e) {
                    System.out.println("Failed to process order amount: " + e.getMessage());
                }
            }
        }

        System.out.println("Total total count of all orders: " + counter);
        Assert.assertEquals(counter, total_orders, "counter should be equal to total_orders");


        Allure.step("testGetSalesReport finished successfully.");



        // check total_sales = summ of daily_sales

     /*  individual sales

      Double sales1 = response.jsonPath().getDouble("data.orders[1].total_amount");
        System.out.println(sales1);
        Double sales2 = response.jsonPath().getDouble("data.orders[2].total_amount");
       System.out.println(sales2);
*/


//        List<Map<String, Object>> orders = response.jsonPath().getList("data.orders");
//        double totalSum = 0.0;
//
//        if (orders != null) {
//            for (Map<String, Object> order : orders) {
//                try {
//                    Double amount = Double.valueOf(order.get("total_amount").toString());
//                    totalSum += amount;
////                    System.out.println("Order amount: " + amount);
//                } catch (Exception e) {
//                    System.out.println("Failed to process order amount: " + e.getMessage());
//                }
//            }
//        }
//
//        System.out.println("Total sum of all orders: " + totalSum);







    }
}
