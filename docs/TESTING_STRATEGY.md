# API Testing Strategy

## Overview

This document outlines the testing strategy for the QA API Automation suite, covering approach, trade-offs, and decision rationale.

## Test Pyramid

```
        △
       / \
      /   \  E2E / Smoke Tests (5 tests)
     /-----\
    /       \  Contract Tests (5 tests)
   /         \
  /-----------\
 /             \ Security Tests (5 tests)
/______________\
```

**Philosophy**: Emphasis on **contract** and **security** testing, with smoke tests as sanity checks. This balances coverage against execution time and maintenance burden.

## Test Categories & Rationale

### 1. Smoke Tests (Functional Verification)

**Purpose**: Verify basic API functionality and response structure.

**Coverage:**
- Search with common queries ("Java", "Python", etc.)
- Response contains expected fields
- Batch completion flag is set
- Timestamps are valid

**Rationale:**
- Quick feedback on API availability
- Catch obvious breaking changes
- Base layer for other test types

**Execution Time**: ~5-10 seconds (5 tests)

### 2. Contract Tests (Structural Validation)

**Purpose**: Ensure API response adheres to expected schema and contains all required fields.

**Coverage:**
- JSON Schema validation via `json-schema-validator`
- Type consistency (numbers are numbers, not strings)
- Required fields presence across all results
- Content-Type correctness

**Rationale:**
- Detects breaking changes to response structure
- Type mismatches often indicate data serialization bugs
- Schema-driven testing scales to complex APIs
- Catches issues earlier than integration tests

**Execution Time**: ~5-10 seconds (5 tests)

### 3. Security Tests (Input Handling)

**Purpose**: Verify API handles malicious input safely without crashing or exposing internals.

**Coverage:**
- SQL injection attempts: `'; DROP TABLE users; --`
- XSS payloads: `<script>alert('XSS')</script>`
- Template injection: `{{7*7}}`, `${7*7}`
- Java reflection: `java.lang.Runtime`
- Large input (buffer overflow): 1KB-5KB strings
- Special characters: `<>`, `&&`, `||`, etc.
- URL-encoded payloads

**Rationale:**
- Wikipedia API is public-facing; defense against abuse is critical
- Tests verify safe error handling (200 response with no crash)
- Doesn't assume API is "behind a firewall" (good practice)
- Validates escaping/encoding is done correctly

**Execution Time**: ~10-15 seconds (5 tests)

**Important Note**: These tests assume **external API protection** (e.g., WAF, rate limiting). They do NOT test authentication or authorization (out of scope).

## What's NOT Tested (Trade-offs)

### Performance Testing
- **Why excluded**: Wikipedia API is public and stable; internal APIs need load testing.
- **How to add**: Use tools like `k6` or `JMeter` in a separate suite.

### End-to-End Workflows
- **Why excluded**: API tests focus on individual endpoints, not multi-step flows.
- **How to add**: Create integration tests that chain endpoint calls.

### Authentication & Authorization
- **Why excluded**: Wikipedia Search API is public (no auth). Auth testing requires credentials.
- **How to add**: Add a separate test suite for authenticated endpoints.

### Data-Driven Tests at Scale
- **Why excluded**: Test data is static (common search terms); large datasets belong in performance tests.
- **How to add**: Use `@ParameterizedTest` with CSV/JSON data providers.

## Test Data Strategy

### Search Queries (Smoke & Contract Tests)
- **Fixed set**: "Java", "Python", "API", "Testing", "Technology"
- **Rationale**: Wikipedia has stable, consistent results for these terms
- **Risk**: If Wikipedia removes articles, tests fail (acceptable for this demo)

### Injection Payloads (Security Tests)
- **Fixed set**: SQL, XSS, template injection, Java reflection
- **Rationale**: Testing for known patterns, not generating random payloads
- **Coverage**: Covers OWASP Top 10 input validation gaps

### Input Size (Security Tests)
- **100, 1000, 5000 characters**: Tests buffer overflow risk
- **Rationale**: Catches issues at boundary conditions

## Flakiness & Resilience

### Expected Flakiness
- **Wikipedia API timeout**: ~1-2% (network latency)
- **Seasonal result changes**: ~0.5% (article content updates)

### Mitigation
- **3-second timeout**: Allows slow networks without hanging
- **Parameterized tests**: Repeat queries catch transient failures
- **Allure retry logic**: Can re-run failed tests automatically

### Monitoring
- Allure Report tracks retry rate
- GitHub Actions logs capture failures
- Slack notifications alert on consistent failures

## Continuous Integration

### Schedule
- **Daily**: 9 AM UTC (off-peak for Wikipedia servers)
- **On push/PR**: Immediate feedback for developers

### Artifacts
- **Allure Report**: HTML report with detailed results
- **Test Results**: JUnit XML for integration with other tools

### Notifications
- **Slack**: Pass/fail alerts in team channel
- **GitHub**: Check runs on PRs

## Scaling to Production APIs

To adapt this suite for your own API:

1. **Update Base URL**: Change `WIKIPEDIA_API_BASE` to your API endpoint
2. **Create Response Models**: Define POJOs with `@SerializedName` annotations
3. **Add JSON Schema**: Create `.json` schema files in `src/test/resources/schemas/`
4. **Adjust Test Data**: Replace public queries with your endpoint's realistic use cases
5. **Add Authentication**: Use RestAssured's `.auth()` for API keys or OAuth
6. **Configure Timeouts**: Adjust `.withTimeout()` based on your SLAs

### Example for Custom API
```java
// Base configuration
private static final String API_BASE = "https://api.example.com/v2";
private static final String API_KEY = System.getenv("API_KEY");

// Build request with auth
RequestSpecification spec = new RequestSpecBuilder()
    .setBaseUri(API_BASE)
    .addHeader("Authorization", "Bearer " + API_KEY)
    .build();

// Use in tests
given(spec)
    .queryParam("search", "example")
    .get("/search")
    .then()
    .statusCode(200);
```

## Future Enhancements

### Short-term (Weeks 1-2)
- [ ] Add performance baselines (p50, p95 latency)
- [ ] Implement retry logic for transient failures
- [ ] Add database seeding/cleanup for integration tests

### Medium-term (Weeks 3-6)
- [ ] GraphQL API testing (if applicable)
- [ ] Contract testing with Pact (bi-directional)
- [ ] Load testing with k6

### Long-term (Weeks 7+)
- [ ] Chaos testing (inject faults into API)
- [ ] Mutation testing (verify tests catch bugs)
- [ ] Multi-environment matrix (staging, production)

---

**Last Updated**: 2026-07-30  
**Author**: Gustavo Mesquita  
**Version**: 1.0
