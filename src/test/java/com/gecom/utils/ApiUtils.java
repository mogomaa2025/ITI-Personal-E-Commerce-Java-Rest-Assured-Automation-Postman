package com.gecom.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Utility class for simplifying REST Assured API requests.
 * This class provides methods for making GET, POST, PUT, and DELETE requests with and without authentication.
 */
public class ApiUtils {

    private static final AllureRestAssured ALLURE_FILTER = new AllureRestAssured();

    /**
     * Sends a GET request to the specified endpoint.
     *
     * @param endpoint The API endpoint to send the GET request to.
     * @return The response from the API.
     */
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


    /**
     * Sends a GET request to the specified endpoint with an authentication token.
     *
     * @param endpoint The API endpoint to send the GET request to.
     * @param token    The authentication token.
     * @return The response from the API.
     */
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




    /**
     * Sends a GET request to the specified endpoint with query parameters.
     *
     * @param endpoint The API endpoint to send the GET request to.
     * @param query    A map of query parameters to include in the request.
     * @return The response from the API.
     */
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



    /**
     * Sends a GET request to the specified endpoint with query parameters and an authentication token.
     *
     * @param endpoint The API endpoint to send the GET request to.
     * @param query    A map of query parameters to include in the request.
     * @param token    The authentication token.
     * @return The response from the API.
     */
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



    /**
     * Sends a POST request to the specified endpoint with a payload.
     *
     * @param endpoint The API endpoint to send the POST request to.
     * @param payload  The payload to include in the request body.
     * @return The response from the API.
     */
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

    /**
     * Sends a POST request to the specified endpoint with a payload and an authentication token.
     *
     * @param endpoint The API endpoint to send the POST request to.
     * @param token    The authentication token.
     * @param payload  The payload to include in the request body.
     * @return The response from the API.
     */
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

    /**
     * Sends a PUT request to the specified endpoint with a payload and an authentication token.
     *
     * @param endpoint The API endpoint to send the PUT request to.
     * @param token    The authentication token.
     * @param payload  The payload to include in the request body.
     * @return The response from the API.
     */
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

    /**
     * Sends a DELETE request to the specified endpoint with an authentication token.
     *
     * @param endpoint The API endpoint to send the DELETE request to.
     * @param token    The authentication token.
     * @return The response from the API.
     */
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

    /**
     * Sends a DELETE request to the specified endpoint.
     *
     * @param endpoint The API endpoint to send the DELETE request to.
     * @return The response from the API.
     */
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