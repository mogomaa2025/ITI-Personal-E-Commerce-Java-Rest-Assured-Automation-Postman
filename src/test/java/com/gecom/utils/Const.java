package com.gecom.utils;

public class Const {
    // ============ API Configuration ============
//    public static final String BASE_URL = "https://itigraduation.pythonanywhere.com/api";
    public static final String BASE_URL = "http://127.0.0.1:5000/api";
    public static final String TOKEN_FILE_PATH = "src/test/resources/token.json";
    public static final String IDS_FILE_PATH = "src/test/resources/ids.json";

    public static String adminToken;
    public static String userToken;
    public static Integer userId;
    public static String userEmail;
    public static String userPassword;
    public static Integer orderID;
    public static Integer productId;
    public static Integer helpArticleId;
    public static Integer wishlistItemId;
    public static String CouponCode;
    public static Integer notificationId;
    public static Integer blogPostId;
    public static Integer cartItemId;


    public static String SalesReportStartDate = "2024-01-01";
    public static String SalesReportEndDate = "2026-12-31";
    public static String testListHelpArticlesCaategory = "Orders";
    public static String testListHelpArticlesSearch = "How to track my order";
    public static String searchQuery = "mouse";
    public static String searchCategory = "Electronics";
    public static String searchMinPrice = "1";
    public static String searchMaxPrice = "500";
    public static String searchMinRating = "4";
    public static String searchSortBy = "price";
    public static String searchSortOrder = "asc";


    public static String adminEmail = "admin@test.com";
    public static String adminPass = "admin123";

    // Allure Results Directory not the the report directory
    public static final String ALLURE_RESULTS_DIR = "allure-results";



}
