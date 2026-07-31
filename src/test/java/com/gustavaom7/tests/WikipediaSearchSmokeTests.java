package com.gustavaom7.tests;

import com.google.gson.Gson;
import com.gustavaom7.helpers.APIClient;
import com.gustavaom7.models.SearchResponse;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

@Epic("Wikipedia API")
@Feature("Search Functionality")
@DisplayName("Wikipedia Search - Smoke Tests")
class WikipediaSearchSmokeTests {

    private static APIClient apiClient;
    private static final String WIKIPEDIA_API_BASE = "https://en.wikipedia.org/w/api.php";

    @BeforeAll
    static void setUp() {
        apiClient = new APIClient(WIKIPEDIA_API_BASE);
        RestAssured.requestSpecification = apiClient.withLogging(true).build();
    }

    @Test
    @DisplayName("Search for 'Java' should return results with valid structure")
    @Description("Verify that searching for a common term returns properly formatted results")
    @Severity(SeverityLevel.BLOCKER)
    void searchForJavaShouldReturnValidResults() {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "Java")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        SearchResponse searchResponse = response.as(SearchResponse.class);

        assertThat(searchResponse).isNotNull();
        assertThat(searchResponse.isBatchComplete()).isTrue();
        assertThat(searchResponse.getSearchResults())
                .isNotEmpty()
                .hasSizeGreaterThan(0);

        SearchResponse.SearchResult firstResult = searchResponse.getSearchResults().get(0);
        assertThat(firstResult.getTitle()).isNotNull();
        assertThat(firstResult.getSnippet()).isNotEmpty();
        assertThat(firstResult.getPageId()).isGreaterThan(0);
    }

    @ParameterizedTest(name = "Search: {0}")
    @ValueSource(strings = {"Python", "Database", "API", "Testing"})
    @DisplayName("Search for various terms should all return results")
    @Description("Verify API works consistently across different search queries")
    @Severity(SeverityLevel.CRITICAL)
    void multipleSearchTermsShouldReturnResults(String searchTerm) {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", searchTerm)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.getContentType()).contains("application/json");

        SearchResponse searchResponse = response.as(SearchResponse.class);
        assertThat(searchResponse.getSearchResults())
                .as("Results for search term: " + searchTerm)
                .isNotEmpty();
    }

    @Test
    @DisplayName("Search result has consistent timestamp format")
    @Severity(SeverityLevel.NORMAL)
    void searchResultsHaveValidTimestamps() {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "Computer")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        SearchResponse searchResponse = response.as(SearchResponse.class);
        SearchResponse.SearchResult firstResult = searchResponse.getSearchResults().get(0);

        // Verify timestamp follows ISO 8601 format
        assertThat(firstResult.getTimestamp())
                .as("Timestamp should follow ISO 8601 format")
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
    }

    @Test
    @DisplayName("Empty search should return empty results, not error")
    @Severity(SeverityLevel.NORMAL)
    void emptySearchShouldReturnEmptyResults() {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        SearchResponse searchResponse = response.as(SearchResponse.class);
        assertThat(searchResponse.getSearchResults()).isEmpty();
    }

    @Test
    @DisplayName("Response time should be under 3 seconds")
    @Severity(SeverityLevel.NORMAL)
    void responseTimeShouldBeAcceptable() {
        given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", "Wikipedia")
                .when()
                .get()
                .then()
                .time(org.hamcrest.Matchers.lessThan(3000L))
                .statusCode(200);
    }
}
