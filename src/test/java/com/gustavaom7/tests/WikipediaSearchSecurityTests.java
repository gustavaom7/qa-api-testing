package com.gustavaom7.tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Wikipedia API")
@Feature("Security Testing")
@DisplayName("Wikipedia Search - Security Tests")
class WikipediaSearchSecurityTests {

    private static final String WIKIPEDIA_API_BASE = "https://en.wikipedia.org/w/api.php";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = WIKIPEDIA_API_BASE;
    }

    @ParameterizedTest(name = "Injection test: {0}")
    @ValueSource(strings = {
            "'; DROP TABLE users; --",
            "1' OR '1'='1",
            "<script>alert('XSS')</script>",
            "{{7*7}}",
            "${7*7}",
            "java.lang.Runtime"
    })
    @DisplayName("API should safely handle injection attempts in search")
    @Description("Verify API escapes/handles malicious input without errors")
    @Severity(SeverityLevel.CRITICAL)
    void searchShouldHandleInjectionAttempts(String maliciousInput) {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", maliciousInput)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Should not throw 500 error
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Should not expose stack traces
        assertThat(response.getBody().asString())
                .doesNotContain("Exception")
                .doesNotContain("SQLException")
                .doesNotContain("stack trace");
    }

    @ParameterizedTest(name = "Large input: {0} chars")
    @ValueSource(ints = {100, 1000, 5000})
    @DisplayName("API should handle large input without buffer overflow")
    @Severity(SeverityLevel.NORMAL)
    void searchShouldHandleLargeInput(int inputLength) {
        String largeInput = "a".repeat(inputLength);

        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", largeInput)
                .when()
                .get()
                .then()
                .extract()
                .response();

        // Should not crash or timeout
        assertThat(response.getStatusCode())
                .as("API should handle large input without crashing")
                .isIn(200, 400);
    }

    @ParameterizedTest(name = "Special chars: {0}")
    @ValueSource(strings = {"<>", "&&", "||", "%%", "$$", "@@", "##"})
    @DisplayName("API should handle special characters safely")
    @Severity(SeverityLevel.NORMAL)
    void searchShouldHandleSpecialCharacters(String specialChars) {
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", specialChars)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.getBody().asString()).contains("batchcomplete");
    }

    @ParameterizedTest(name = "Encoding: {0}")
    @ValueSource(strings = {
            "%27", // Single quote
            "%22", // Double quote
            "%3C", // <
            "%3E"  // >
    })
    @DisplayName("URL-encoded malicious input should be handled safely")
    @Severity(SeverityLevel.NORMAL)
    void searchShouldHandleEncodedMaliciousInput(String encodedInput) {
        // RestAssured automatically handles encoding, but let's verify it's safe
        Response response = given()
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", encodedInput)
                .when()
                .get()
                .then()
                .extract()
                .response();

        assertThat(response.getStatusCode()).isIn(200, 400);
        assertThat(response.getBody().asString())
                .doesNotContain("Exception")
                .doesNotContain("error");
    }
}
