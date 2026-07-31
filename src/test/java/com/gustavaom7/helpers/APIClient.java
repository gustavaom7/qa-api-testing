package com.gustavaom7.helpers;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * APIClient builder class for consistent API request configuration
 * Handles common headers, base URL, and logging across all tests
 */
public class APIClient {
    private static final Logger logger = LoggerFactory.getLogger(APIClient.class);

    private String baseURL;
    private long responseTimeout = 5000;
    private boolean enableLogging = true;

    public APIClient(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Build RequestSpecification with configured settings
     * @return RequestSpecification ready to use with RestAssured
     */
    public RequestSpecification build() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseURL)
                .setContentType("application/json")
                .addHeader("User-Agent", "QA-Automation-Suite/1.0");

        if (enableLogging) {
            builder.addFilter(new RequestLoggingFilter())
                    .addFilter(new ResponseLoggingFilter());
        }

        logger.info("API Client configured: baseURL={}, timeout={}ms", baseURL, responseTimeout);
        return builder.build();
    }

    /**
     * Set response timeout in milliseconds
     */
    public APIClient withTimeout(long timeoutMs) {
        this.responseTimeout = timeoutMs;
        return this;
    }

    /**
     * Enable/disable request-response logging
     */
    public APIClient withLogging(boolean enabled) {
        this.enableLogging = enabled;
        return this;
    }

    public String getBaseURL() {
        return baseURL;
    }
}
