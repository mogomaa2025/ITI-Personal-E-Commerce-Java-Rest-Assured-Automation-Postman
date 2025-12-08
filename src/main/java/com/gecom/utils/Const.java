package com.gecom.utils;

public class Const {

    // ============ API Configuration ============
    // public static final String BASE_URL =
    // "https://itigraduation.pythonanywhere.com/api"; // production server
    public static final String BASE_URL = "http://127.0.0.1:5000/api"; // local server
    public static final String TOKEN_FILE_PATH = "src/test/resources/token.json";
    public static final String REFRESH_TOKEN_FILE_PATH = "src/test/resources/token.json";
    public static final String IDS_FILE_PATH = "src/test/resources/ids.json";

    // ============ Test Data ============
    // Product Pagination TC-PROD-002
    public static final Integer PRODUCT_PAGINATION_PAGE = 2;
    public static final Integer PRODUCT_PAGINATION_PER_PAGE = 5;
    // Filter Products by Category TC-PROD-003
    public static final String FILTER_CATEGORY = "Electronics";
    public static final String SEARCH_QUERY = "mouse"; // TC-PROD-015
    // Filter Products by Price Range TC-PROD-004
    public static final Integer MIN_PRICE = 10;
    public static final Integer MAX_PRICE = 100;
    // Filter Products by MULTIPLE_CRITERIA TC-PROD-005
    public static final String MULTIPLE_CRITERIA_SEARCH = "laptop";
    public static final String MULTIPLE_CRITERIA_CATEGORY = "Electronics";
    public static final Integer MULTIPLE_CRITERIA_MIN_PRICE = 50;
    public static final Integer MULTIPLE_CRITERIA_MAX_PRICE = 500;
    // Thresholds
    public static final Integer THRESHOLD = 10; // TC-PROD-019
    public static final Integer CUSTOM_THRESHOLD = 5; // TC-PROD-020
    // Bluk update TC-PROD-023
    public static final Integer BULK_UPDATE_PRODUCT_ID1 = 1;
    public static final Double BULK_UPDATE_PRICE1 = 899.99;
    public static final Integer BULK_UPDATE_STOCK1 = 75;
    public static final Integer BULK_UPDATE_PRODUCT_ID2 = 2;
    public static final Double BULK_UPDATE_PRICE2 = 24.99;
    public static final Integer BULK_UPDATE_STOCK2 = 200;
    // Like Product TC-PROD-026
    public static final Integer LIKE_PRODUCT_ID = 1;
    public static final Integer PRODUCT_ID_LIKES_TO_COUNT = 1;
    // Inventory
    public static final Integer INVENTORY_PRODUCT_ID = 4;
    public static final Integer INVENTORY_STOCK = 40;
    public static final String TEST_LIST_HELP_ARTICLES_CATEGORY = "Orders";
    public static final String TEST_LIST_HELP_ARTICLES_SEARCH = "How to track my order";
    // Help Center Test Data TC-HELP-001 to TC-HELP-011
    public static final String HELP_ARTICLE_QUESTION = "How to track my order2?";
    public static final String HELP_ARTICLE_ANSWER = "Use the orders page to check status2.";
    public static final String HELP_ARTICLE_CATEGORY = "Orders2";
    public static final String HELP_ARTICLE_UPDATED_QUESTION = "How to track my order?";
    public static final String HELP_ARTICLE_UPDATED_ANSWER = "Visit the orders page and use the tracking link.";
    public static final String HELP_ARTICLE_UPDATED_CATEGORY = "Orders";
    public static final Integer INVALID_HELP_ARTICLE_ID = 99999;
    // Search Test Data TC-SEARCH-001 to TC-SEARCH-004
    public static final String SEARCH_CATEGORY = "Electronics";
    public static final String SEARCH_MIN_PRICE = "1";
    public static final String SEARCH_MAX_PRICE = "500";
    public static final String SEARCH_MIN_RATING = "4";
    public static final String SEARCH_SORT_BY = "price";
    public static final String SEARCH_SORT_ORDER = "asc";
    public static final String SEARCH_COMBINED_CATEGORY = "Electronics";
    public static final String SEARCH_COMBINED_MIN_PRICE = "100";
    public static final String SEARCH_COMBINED_MAX_PRICE = "1000";
    public static final String SEARCH_COMBINED_SORT_BY = "price";
    public static final String SEARCH_COMBINED_SORT_ORDER = "asc";
    public static final Integer SEARCH_RECOMMENDATION_PRODUCT_ID = 1;
    public static final Integer SEARCH_INVALID_PRODUCT_ID = 99999;
    // testAdminCanCreateProduct body
    public static final String PRODUCT_NAME = "new product1";
    public static final String DESCRIPTION = "new product description1";
    public static final Double PRICE = 29.99;
    public static final String CATEGORY = "Electronics";
    public static final Integer STOCK = 20;
    public static final String IMAGE_URL = "https://ci.suez.edu.eg/wp-content/uploads/2022/08/iti-logo.png";
    // product update TC-PROD-009
    public static final String UPDATED_DESCRIPTION = "Updated Desc";
    public static final Double UPDATED_PRICE = 34.99;
    public static final Integer UPDATED_STOCK = 75;

    // account credentials
    public static final String ADMIN_EMAIL = "admin@test.com";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String USER_EMAIL = "user@test.com";
    public static final String USER_PASSWORD = "user123";

    // Allure Results Directory not the the report directory
    public static final String ALLURE_RESULTS_DIR = "allure-results";

    // Cart Test Data TC-CART-001 to TC-CART-012
    public static final Integer CART_PRODUCT_ID = 4;
    public static final Integer CART_QUANTITY = 2;
    public static final Integer CART_UPDATE_QUANTITY = 3;
    public static final Integer INVALID_QUANTITY = 0;
    public static final Integer INVALID_PRODUCT_ID = 99999;
    public static final Integer INVALID_CART_ITEM_ID = 99999;

    // Orders Test Data TC-ORDER-001 to TC-ORDER-023
    public static final String ORDER_SHIPPING_ADDRESS = "123 Test Street";
    public static final String ORDER_UPDATED_SHIPPING_ADDRESS = "456 alex iti, City, State 1234";
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_PROCESSING = "processing";
    public static final String ORDER_STATUS_SHIPPED = "shipped";
    public static final String ORDER_STATUS_DELIVERED = "delivered";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    public static final String INVALID_ORDER_STATUS = "invalid_status";
    public static final Integer INVALID_ORDER_ID = 99999;
    public static final String EXPORT_ORDERS_START_DATE = "2024-01-01";
    public static final String EXPORT_ORDERS_END_DATE = "2025-12-31";

    // Reviews Test Data TC-REV-001 to TC-REV-008
    public static final Integer REVIEW_RATING = 4;
    public static final Integer REVIEW_RATING_INVALID = 6;
    public static final String REVIEW_COMMENT = "Excellent product!";
    public static final String REVIEW_COMMENT_ANOTHER = "Another review";
    public static final Integer REVIEW_PRODUCT_ID_FOR_REVIEW = 1;

    // Stats & Analytics Test Data TC-STATS-001 to TC-STATS-006
    public static final String SALES_REPORT_START_DATE = "2024-01-01";
    public static final String SALES_REPORT_END_DATE = "2026-12-31";

    // Contact Test Data TC-CONT-001 to TC-CONT-007
    public static final String CONTACT_NAME = "Test User";
    public static final String CONTACT_EMAIL = "test@example.com";
    public static final String CONTACT_SUBJECT = "Test Subject";
    public static final String CONTACT_MESSAGE = "Test message content";
    public static final String CONTACT_INCOMPLETE_NAME = "Test";
    public static final String CONTACT_RESPONSE = "Thank you for contacting us. We will help you...";
    public static final String CONTACT_STATUS_PENDING = "pending";
    public static final Integer INVALID_CONTACT_MESSAGE_ID = 9999;

    // Wishlist Test Data TC-WISH-001 to TC-WISH-007
    public static final Integer INVALID_WISHLIST_ITEM_ID = 99999;
    public static final Integer INVALID_WISHLIST_PRODUCT_ID = 999999;

    // Coupons Test Data TC-COUP-001 to TC-COUP-009
    public static final String COUPON_DESCRIPTION = "10% off first order";
    public static final String COUPON_DISCOUNT_TYPE = "percentage";
    public static final Integer COUPON_DISCOUNT_VALUE = 10;
    public static final Integer COUPON_MIN_ORDER_AMOUNT = 50;
    public static final Integer COUPON_MAX_DISCOUNT = 30;
    public static final Integer COUPON_USAGE_LIMIT = 100;
    public static final Double COUPON_VALIDATE_ORDER_AMOUNT = 120.0;
    public static final String COUPON_INVALID_CODE = "INVALID";
    public static final String COUPON_EXPIRED_CODE = "EXPIRED";

    // Notifications Test Data TC-NOTIF-001 to TC-NOTIF-008
    public static final Integer NOTIFICATION_TEST_CREATE_COUNT = 10;
    public static final Integer INVALID_NOTIFICATION_ID = 99999;

    // ============ Dynamic Variables ============
    // not final const
    public static String adminToken;
    public static String refreshToken;
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
    public static Integer categoryId;
    public static Integer contactMessageId;
    public static Integer helpId;
    public static Integer likeId;
    public static Integer someLikeId;
    public static Integer cancelOrderId;
    public static Integer nonPendingOrderId;
    public static Integer reviewId;
    public static Integer reviewProductId;
    public static Integer ProductIDWithoutReview;
    public static Integer recommendationProductId;

}
