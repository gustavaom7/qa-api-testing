# API Testing Guide

## Tested Endpoints

### Wikipedia Search API

**Base URL**: `https://en.wikipedia.org/w/api.php`

**Endpoint**: GET `/w/api.php`

**Parameters**:
| Parameter | Type | Example | Notes |
|---|---|---|---|
| `action` | string | `query` | Required: API action |
| `format` | string | `json` | Required: Response format |
| `list` | string | `search` | Required: List type |
| `srsearch` | string | `Java` | Required: Search query |

**Example Request**:
```bash
curl "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=Java"
```

**Response Structure**:
```json
{
  "batchcomplete": true,
  "query": {
    "search": [
      {
        "ns": 0,
        "title": "Java (programming language)",
        "pageid": 15094,
        "size": 85000,
        "wordcount": 12000,
        "snippet": "...",
        "timestamp": "2026-07-30T12:00:00Z"
      }
    ]
  }
}
```

**Status Codes**:
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server-side error (rare)

**Rate Limiting**: Wikipedia allows ~50 requests/second. No authentication required.

## Test Scenarios

### Smoke Test Scenarios

1. **Search for common programming language**
   - Input: "Java"
   - Expected: At least one result, valid structure

2. **Search for multiple terms**
   - Input: "Python", "Database", "API", "Testing"
   - Expected: All return results (at least 1)

3. **Empty search**
   - Input: "" (empty string)
   - Expected: 200 status, empty results array

4. **Response time**
   - Expected: < 3000ms (3 seconds)

### Contract Test Scenarios

1. **Schema validation**
   - Verify response matches `schemas/wikipedia-search-response.json`

2. **Required fields**
   - Verify all results have: `title`, `pageid`, `snippet`, `ns`

3. **Type checking**
   - `pageid`, `size`, `wordcount` must be integers
   - `title`, `snippet` must be strings
   - `batchcomplete` must be boolean

### Security Test Scenarios

1. **SQL Injection**
   - Input: `'; DROP TABLE users; --`
   - Expected: 200 response, no error exposure

2. **XSS Payload**
   - Input: `<script>alert('XSS')</script>`
   - Expected: 200 response, escaped in output

3. **Template Injection**
   - Input: `{{7*7}}`, `${7*7}`
   - Expected: 200 response, treated as literal strings

4. **Large Input**
   - Input: 5000 character string
   - Expected: No timeout, no crash

5. **Special Characters**
   - Input: `<>`, `&&`, `||`, `$$`
   - Expected: 200 response, properly handled

## Running Individual Tests

### Run all tests
```bash
./gradlew test
```

### Run smoke tests only
```bash
./gradlew test --tests WikipediaSearchSmokeTests
```

### Run a specific test method
```bash
./gradlew test --tests WikipediaSearchSmokeTests.searchForJavaShouldReturnValidResults
```

### Run with debug output
```bash
./gradlew test --tests WikipediaSearchSmokeTests -i
```

### Run parameterized test with specific parameter
```bash
./gradlew test --tests "*multipleSearchTermsShouldReturnResults*"
```

## Test Assertions

### Smoke Tests Use:
- AssertJ fluent assertions for readability
- Hamcrest matchers for response validation
- Custom models (Gson POJOs) for type-safe assertions

**Example**:
```java
SearchResponse response = ...;
assertThat(response.getSearchResults())
    .isNotEmpty()
    .hasSizeGreaterThan(0);
```

### Contract Tests Use:
- JSON Schema validator
- Response body path matchers

**Example**:
```java
.body("query.search[0].title", notNullValue())
.body("query.search[0].pageid", isA(Number.class))
```

### Security Tests Use:
- String content verification
- Status code validation

**Example**:
```java
assertThat(response.getBody().asString())
    .doesNotContain("Exception")
    .doesNotContain("SQLException");
```

## Extending the Tests

### Add a New Test Class

1. Create file in `src/test/java/com/gustavaom7/tests/`
2. Inherit base setup pattern:
```java
@Epic("Wikipedia API")
@Feature("New Feature")
class NewFeatureTests {
    private static APIClient apiClient;

    @BeforeAll
    static void setUp() {
        apiClient = new APIClient(WIKIPEDIA_API_BASE);
        RestAssured.requestSpecification = apiClient.build();
    }
}
```

3. Add test methods with `@Test` and Allure annotations:
```java
@Test
@DisplayName("Descriptive test name")
@Description("What this test verifies")
@Severity(SeverityLevel.HIGH)
void testName() {
    // Arrange, Act, Assert
}
```

### Add New Response Model

1. Create class in `src/test/java/com/gustavaom7/models/`
2. Use Gson annotations:
```java
public class MyResponse {
    @SerializedName("field_name")
    private String fieldName;

    public String getFieldName() {
        return fieldName;
    }
}
```

3. Use in tests:
```java
MyResponse response = given()
    .when()
    .get()
    .then()
    .extract()
    .as(MyResponse.class);
```

### Add New JSON Schema

1. Create file in `src/test/resources/schemas/`
2. Define schema (JSON Schema Draft 7):
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["field1", "field2"],
  "properties": {
    "field1": { "type": "string" },
    "field2": { "type": "number" }
  }
}
```

3. Use in contract tests:
```java
.body(matchesJsonSchemaInClasspath("schemas/my-schema.json"))
```

## Troubleshooting

### Tests Timeout
**Symptom**: Gradle test task hangs or times out (>15s per test)

**Causes**:
- Network connectivity issue
- Wikipedia API temporarily unavailable
- System load/resource contention

**Solution**:
```bash
# Run with explicit timeout
./gradlew test --max-parallel 1 -i

# Check Wikipedia API status
curl https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=Test
```

### Assertion Failures
**Symptom**: Test fails with "AssertionError: expected X but got Y"

**Debug**:
1. Check test logs: `build/test-results/test/TEST-*.xml`
2. Run with verbose output: `./gradlew test -i`
3. Check Allure Report for actual vs expected values

### Schema Validation Failures
**Symptom**: Contract test fails: "JSON schema validation failed"

**Debug**:
1. Print actual response: Add `System.out.println(response.prettyPrint())`
2. Validate schema online: https://www.jsonschemavalidator.net/
3. Update schema if API changed

## Best Practices

### Writing Tests
- ✅ One assertion per test (or grouped related assertions)
- ✅ Descriptive test names that explain what's being tested
- ✅ Use `@DisplayName` for human-readable output
- ✅ Add `@Description` for context in Allure Report
- ✅ Use appropriate `@Severity` level

### Organizing Tests
- ✅ Group by type (Smoke, Contract, Security)
- ✅ Use `@Feature` for logical grouping
- ✅ Keep test data close to test logic
- ✅ Avoid test interdependencies (each test standalone)

### CI/CD
- ✅ Run tests on every PR (fail if tests don't pass)
- ✅ Keep tests fast (< 1 minute for full suite)
- ✅ Monitor flakiness in Allure Report
- ✅ Archive reports for post-mortem analysis

---

**Last Updated**: 2026-07-30  
**Status**: Ready for use
