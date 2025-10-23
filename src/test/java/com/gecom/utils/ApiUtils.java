package com.gecom.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiUtils {

    private static final AllureRestAssured ALLURE_FILTER = new AllureRestAssured();

    public static Response getRequest(String endpoint) {
        return given()
                .filter(ALLURE_FILTER)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }


    

    public static Response getRequestWithAuth(String endpoint, String token) {
        return given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response postRequest(String endpoint, Object payload) {
        return given()
                .filter(ALLURE_FILTER)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response postRequestWithAuth(String endpoint, String token, Object payload) {
        return given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response putRequestWithAuth(String endpoint, String token, Object payload) {
        return given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response deleteRequestWithAuth(String endpoint, String token) {
        return given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }
}
