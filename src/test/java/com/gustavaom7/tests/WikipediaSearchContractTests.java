package com.gustavaom7.tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Epic("Wikipedia API")
@Feature("Contract Testing")
@DisplayName("Wikipedia Search - Contract Tests")
class WikipediaSearchContractTests {

    private static final String WIKIPEDIA_API_BASE = "https://en.wikipedia.org/w/api.php";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = WIKIPEDIA_API_BASE;
    }

    @Test
    @DisplayName("Search response should match contract schema")
    @Description("Validates that API response conforms to expected JSON schema")
    @Severity(SeverityLevel.BLOCKER)
    void searchResponseShouldMatchContract() {
        given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "API")
                .when()
                .get()
                .then()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/wikipedia-search-response.json"));
    }

    @Test
    @DisplayName("All search results should have required fields")
    @Description("Contract test: Verify each search result contains mandatory fields")
    @Severity(SeverityLevel.CRITICAL)
    void searchResultsShouldContainRequiredFields() {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "Technology")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verify required root fields
        response.then().assertThat()
                .body("batchcomplete", org.hamcrest.Matchers.notNullValue())
                .body("query", org.hamcrest.Matchers.notNullValue())
                .body("query.search", org.hamcrest.Matchers.notNullValue());

        // Verify each result has required fields
        response.then().assertThat()
                .body("query.search[0].title", org.hamcrest.Matchers.notNullValue())
                .body("query.search[0].pageid", org.hamcrest.Matchers.notNullValue())
                .body("query.search[0].snippet", org.hamcrest.Matchers.notNullValue())
                .body("query.search[0].ns", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    @DisplayName("Response should have correct content type")
    @Severity(SeverityLevel.NORMAL)
    void responseShouldHaveCorrectContentType() {
        given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "Science")
                .when()
                .get()
                .then()
                .statusCode(200)
                .contentType(org.hamcrest.Matchers.containsString("application/json"));
    }

    @Test
    @DisplayName("Numeric fields should be numbers, not strings")
    @Description("Verify type consistency in contract")
    @Severity(SeverityLevel.CRITICAL)
    void numericFieldsShouldHaveCorrectType() {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "Education")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verify pageid is a number
        response.then().assertThat()
                .body("query.search[0].pageid", org.hamcrest.Matchers.isA(Number.class))
                .body("query.search[0].size", org.hamcrest.Matchers.isA(Number.class))
                .body("query.search[0].wordcount", org.hamcrest.Matchers.isA(Number.class));
    }
}
