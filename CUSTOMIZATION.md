# Customizing for Your API

This guide shows how to adapt the qa-api-testing framework to test your own API.

## Step 1: Choose Your API

Identify a public API to test. Good candidates:
- **REST APIs** with JSON responses (required)
- **No authentication** (optional — can add Bearer tokens, API keys)
- **Public endpoints** (safe to test repeatedly)

### Example APIs
- GitHub REST API: `https://api.github.com`
- OpenWeather: `https://api.openweathermap.org`
- PokéAPI: `https://pokeapi.co/api/v2`
- JSONPlaceholder: `https://jsonplaceholder.typicode.com`

## Step 2: Update Base URL

Edit test classes and change the base URL:

```java
// Before (Wikipedia)
private static final String API_BASE = "https://en.wikipedia.org/w/api.php";

// After (e.g., GitHub)
private static final String API_BASE = "https://api.github.com";
```

Apply to all test classes:
- `WikipediaSearchSmokeTests.java` → `GitHubSearchSmokeTests.java`
- `WikipediaSearchContractTests.java` → `GitHubSearchContractTests.java`
- `WikipediaSearchSecurityTests.java` → `GitHubSearchSecurityTests.java`

## Step 3: Create Response Models

Create POJOs matching your API response structure.

### Example: GitHub Search Response

```java
package com.gustavaom7.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GitHubSearchResponse {
    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("incomplete_results")
    private boolean incompleteResults;

    @SerializedName("items")
    private List<GitHubItem> items;

    public static class GitHubItem {
        @SerializedName("id")
        private long id;

        @SerializedName("name")
        private String name;

        @SerializedName("full_name")
        private String fullName;

        @SerializedName("url")
        private String url;

        // Getters
        public long getId() { return id; }
        public String getName() { return name; }
        public String getFullName() { return fullName; }
        public String getUrl() { return url; }
    }

    // Getters for root fields
    public int getTotalCount() { return totalCount; }
    public boolean isIncompleteResults() { return incompleteResults; }
    public List<GitHubItem> getItems() { return items; }
}
```

### Tips for Creating Models
1. Browse API response in browser or Postman
2. Identify JSON field names
3. Map to Java types:
   - JSON strings → Java `String`
   - JSON numbers → Java `int`, `long`, `double`
   - JSON booleans → Java `boolean`
   - JSON objects → Java classes
   - JSON arrays → Java `List<T>`
4. Use `@SerializedName` for JSON field names

## Step 4: Update JSON Schema

Create a JSON Schema matching your API response.

### Example: GitHub Search Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "GitHub Search Response",
  "type": "object",
  "required": ["total_count", "incomplete_results", "items"],
  "properties": {
    "total_count": {
      "type": "integer",
      "minimum": 0
    },
    "incomplete_results": {
      "type": "boolean"
    },
    "items": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["id", "name", "full_name", "url"],
        "properties": {
          "id": { "type": "integer" },
          "name": { "type": "string" },
          "full_name": { "type": "string" },
          "url": { "type": "string" },
          "description": { "type": ["string", "null"] }
        }
      }
    }
  }
}
```

Save as: `src/test/resources/schemas/github-search-response.json`

### JSON Schema Tools
- Validator: https://www.jsonschemavalidator.net/
- Generator: https://www.jsonschema.net/

## Step 5: Rewrite Tests

Replace Wikipedia-specific tests with your API scenarios.

### Smoke Test Example (GitHub)

```java
@Test
@DisplayName("Search for popular repos should return results")
void searchForPopularReposShouldReturnResults() {
    Response response = given()
            .queryParam("q", "stars:>10000")
            .when()
            .get("/search/repositories")
            .then()
            .statusCode(200)
            .extract()
            .response();

    GitHubSearchResponse searchResponse = response.as(GitHubSearchResponse.class);

    assertThat(searchResponse).isNotNull();
    assertThat(searchResponse.getItems())
            .isNotEmpty()
            .hasSizeGreaterThan(0);

    GitHubSearchResponse.GitHubItem firstRepo = searchResponse.getItems().get(0);
    assertThat(firstRepo.getName()).isNotNull();
    assertThat(firstRepo.getUrl()).isNotNull();
}
```

### Contract Test Example (GitHub)

```java
@Test
@DisplayName("Search response should match contract schema")
void searchResponseShouldMatchContract() {
    given()
            .queryParam("q", "language:java")
            .when()
            .get("/search/repositories")
            .then()
            .statusCode(200)
            .assertThat()
            .body(matchesJsonSchemaInClasspath("schemas/github-search-response.json"));
}
```

### Security Test Example (GitHub)

```java
@ParameterizedTest(name = "Injection: {0}")
@ValueSource(strings = {"'; DROP TABLE repos; --", "<script>alert('XSS')</script>"})
void searchShouldHandleInjections(String maliciousInput) {
    Response response = given()
            .queryParam("q", maliciousInput)
            .when()
            .get("/search/repositories")
            .then()
            .extract()
            .response();

    assertThat(response.getStatusCode()).isIn(200, 422); // GitHub returns 422 for invalid queries
    assertThat(response.getBody().asString())
            .doesNotContain("Exception")
            .doesNotContain("stack trace");
}
```

## Step 6: Handle Authentication (if needed)

For authenticated APIs, add headers/tokens to APIClient.

### Example: GitHub with Personal Token

```java
private static final String API_TOKEN = System.getenv("GITHUB_TOKEN");

@BeforeAll
static void setUp() {
    RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(API_BASE)
            .addHeader("Authorization", "Bearer " + API_TOKEN)
            .addHeader("Accept", "application/vnd.github.v3+json")
            .build();

    RestAssured.requestSpecification = spec;
}
```

### Common Auth Patterns

**API Key in header:**
```java
.addHeader("X-API-Key", apiKey)
```

**Bearer token:**
```java
.addHeader("Authorization", "Bearer " + token)
```

**Basic auth:**
```java
.auth().basic(username, password)
```

**API Key in query:**
```java
.queryParam("api_key", apiKey)
```

## Step 7: Update CI/CD Configuration

Edit `.github/workflows/api-tests.yml`:
- Change test class names
- Add authentication secrets if needed
- Adjust timeout if API is slower

### Example: GitHub API with auth
```yaml
- name: Run API tests
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  run: |
    ./gradlew test --info
```

## Step 8: Add to Your Portfolio

Push to GitHub and link from your main portfolio:

```bash
git init
git add .
git commit -m "feat: add API testing suite for [YourAPI]"
git remote add origin https://github.com/YOUR_USERNAME/qa-api-testing.git
git push -u origin main
```

Link in main `README.md`:
```markdown
- [API Testing Suite](https://github.com/gustavaom7/api-testing-restassured) — RestAssured, Contract Testing, Security Tests
```

## Testing Your Customization

### 1. Verify API is accessible
```bash
curl "https://api.github.com/search/repositories?q=stars:>10000"
```

### 2. Run tests
```bash
./gradlew test --tests "*SearchTests"
```

### 3. Check results
```bash
./gradlew allureServe
```

### 4. Debug failures
```bash
./gradlew test -i --tests GitHubSearchSmokeTests
```

## Common Pitfalls

| Problem | Solution |
|---|---|
| `404 Not Found` | Check base URL and endpoint path |
| `401 Unauthorized` | Add authentication headers/tokens |
| `403 Forbidden` | Check API key/token is valid |
| `422 Unprocessable Entity` | Validate query parameters |
| `500 Internal Server Error` | API may be down; retry later |
| JSON parsing fails | Model fields don't match JSON (check case sensitivity) |
| Schema validation fails | Schema doesn't match actual response (use validator tool) |

## Next Steps

1. ✅ Create response models
2. ✅ Write smoke tests
3. ✅ Add contract tests (optional but recommended)
4. ✅ Add security tests (optional)
5. ✅ Set up CI/CD
6. ✅ Commit to GitHub
7. ✅ Link from portfolio

---

**Questions?** See `docs/API_GUIDE.md` or open an issue.
