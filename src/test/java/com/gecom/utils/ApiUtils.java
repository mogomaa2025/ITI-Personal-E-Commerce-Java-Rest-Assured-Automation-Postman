package com.gecom.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiUtils {

    private static final AllureRestAssured ALLURE_FILTER = new AllureRestAssured();

    public static Response getRequest(String endpoint) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }


    

    public static Response getRequestWithAuth(String endpoint, String token) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }




    public static Response getRequestWithQuery(String endpoint, Map<String, String> query) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .queryParams(query)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }



    public static Response getRequestWithAuthQuery(String endpoint, Map<String, String> query, String token) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .queryParams(query)
                .header("Authorization", "Bearer " + token)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }



    public static Response postRequest(String endpoint, Object payload) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .contentType(ContentType.JSON)
                .body(payload)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }

    public static Response postRequestWithAuth(String endpoint, String token, Object payload) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(payload)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();

        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }

    public static Response putRequestWithAuth(String endpoint, String token, Object payload) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(payload)             .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }

    public static Response deleteRequestWithAuth(String endpoint, String token) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .header("Authorization", "Bearer " + token)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }

    public static Response deleteRequest(String endpoint) {
        Response response = given()
                .filter(ALLURE_FILTER)
                .contentType(ContentType.JSON)
                .log().body() // this log everything body
                .log().parameters() // this log query parameters
                .log().headers() // this log headers with tokens
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();

        System.out.println("------------------------------------------------");
        response.prettyPrint();
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("------------------------------------------------");
//        System.out.println("Headers: " + response.getHeaders());
        System.out.println("------------------------------------------------");
        return response;
    }
}


/*
no pretty print

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
 */