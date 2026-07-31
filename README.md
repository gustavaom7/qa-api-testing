# QA API Automation — RestAssured + JUnit5 + Allure

A professional-grade **API automation testing framework** built with RestAssured, JUnit5, and Allure Reports. This repository demonstrates contract testing, security testing, and automated CI/CD reporting for REST APIs.

> **Portfolio Project**: Built to showcase best practices in API automation testing alongside mobile (Maestro) and E2E (Cypress/Playwright) testing suites.

## 🎯 Project Goals

| Goal | Implementation | Status |
|---|---|---|
| REST API testing at scale | RestAssured + parameterized tests | ✅ |
| Contract testing | JSON Schema validation | ✅ |
| Security testing (basics) | Injection, buffer overflow tests | ✅ |
| Detailed reporting | Allure Reports with trends | ✅ |
| CI/CD automation | GitHub Actions daily + PR runs | ✅ |
| Slack notifications | Pass/fail alerts to team | ✅ |
| Type-safe models | Gson-based POJOs for responses | ✅ |
| Best practices | Builder pattern, logging, custom assertions | ✅ |

## 🏗️ Architecture

```
qa-api-testing/
├── src/test/java/com/gustavaom7/
│   ├── api/              # API client builders
│   ├── helpers/          # Utility classes, APIClient
│   ├── models/           # Response POJOs (Gson)
│   ├── tests/            # Test classes organized by type
│   │   ├── *SmokeTests.java
│   │   ├── *ContractTests.java
│   │   └── *SecurityTests.java
│   └── resources/
│       └── schemas/      # JSON Schema files for contract tests
├── .github/workflows/
│   └── api-tests.yml     # CI/CD pipeline
├── build.gradle          # Gradle config + dependencies
└── docs/
    ├── TESTING_STRATEGY.md
    └── API_GUIDE.md
```

## 🧪 Test Categories

### Smoke Tests (`WikipediaSearchSmokeTests`)
- Basic functionality verification
- Common search queries
- Response format validation
- Performance baseline (< 3 seconds)

**Key Tests:**
- Search returns results with valid structure
- Multiple queries return consistent results
- Empty search handled gracefully
- Response times within acceptable range

### Contract Tests (`WikipediaSearchContractTests`)
- JSON Schema validation
- Required field verification
- Type consistency checks
- Content-type validation

**Key Tests:**
- Response matches JSON schema
- All required fields present
- Numeric fields have correct types
- Field presence across all results

### Security Tests (`WikipediaSearchSecurityTests`)
- Injection attempt handling (SQL, XSS, template injection)
- Large input buffer overflow protection
- Special character handling
- URL-encoded malicious input

**Key Tests:**
- SQL injection attempts return 200 (not vulnerable)
- XSS payloads handled safely
- Large inputs don't crash API
- Special characters properly escaped

## 🚀 Quick Start

### Prerequisites
- Java 11+
- Gradle 7.0+
- Git

### Installation

```bash
# Clone and navigate
git clone https://github.com/gustavaom7/qa-api-testing.git
cd qa-api-testing

# Make Gradle executable (if needed)
chmod +x gradlew
```

### Run Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests WikipediaSearchSmokeTests

# Run tests matching pattern
./gradlew test --tests "*ContractTests"

# Run with verbose output
./gradlew test --info
```

### Generate Allure Report

```bash
# Generate and open report
./gradlew allureReport allureServe

# Report opens in default browser (http://localhost:4040)
```

## 📊 Test Results & Reporting

### Allure Reports
After running tests, Allure Report provides:
- **Test Summary**: Pass/fail rates, duration
- **Test Timeline**: Execution sequence and timing
- **Test Details**: Logs, attachments, severity levels
- **History**: Trend analysis across runs

**Access Report:**
```bash
./gradlew allureServe
```

### GitHub Actions
- Daily runs at **9 AM UTC**
- On every **push** and **pull request**
- Slack notifications for pass/fail
- Artifacts: Allure reports, test results

**View Results:**
- GitHub Actions tab → API Automation Tests workflow
- Download allure-results artifact for detailed analysis

## 🔧 Configuration

### Base URL & Endpoints
Currently configured for **Wikipedia Search API** (`https://en.wikipedia.org/w/api.php`).

To test a different API, modify in test class:
```java
private static final String API_BASE = "https://your-api.com/v1";
```

### Timeout & Headers
Customize in `APIClient.java`:
```java
apiClient.withTimeout(5000)      // Response timeout in ms
         .withLogging(true)      // Enable request/response logging
         .build()
```

### Slack Notifications
Add webhook URL to GitHub Secrets:
1. Go to repository Settings → Secrets
2. Add `SLACK_WEBHOOK_URL` with your Slack webhook

## 📈 Metrics & Health

Test results tracked per-run:
- **Pass Rate**: Percentage of passing tests
- **Execution Time**: Total and per-test duration
- **Flakiness**: Retry/rerun frequency
- **Coverage**: APIs and scenarios tested

View in Allure Report → **Overview** tab.

## 🔐 Security Considerations

### Tests Include
- ✅ SQL injection simulation
- ✅ XSS payload handling
- ✅ Buffer overflow (large input)
- ✅ Special character escaping

### Not Included (Scope)
- Authentication/authorization testing (separate suite)
- HTTPS/TLS validation
- Rate limiting/DDoS testing
- Data privacy (GDPR, PII handling)

## 📚 Test Data

Tests use:
- **Public APIs**: Wikipedia (no auth required)
- **Fixed queries**: "Java", "Python", "API" (stable results)
- **Faker**: For generated data where needed (see `com.github.javafaker`)

## 🏆 Best Practices Demonstrated

1. **Builder Pattern**: `APIClient` for consistent configuration
2. **Type-Safe Models**: Gson POJOs for response deserialization
3. **Parameterized Tests**: `@ParameterizedTest` for scenario variation
4. **Allure Annotations**: `@Epic`, `@Feature`, `@Severity` for organization
5. **Logging**: SLF4J + Logback for debugging
6. **Assertions**: AssertJ for readable, fluent assertions
7. **Organized Tests**: Grouped by type (Smoke, Contract, Security)
8. **CI/CD Ready**: GitHub Actions with artifacts and notifications

## 📖 Documentation

- [Testing Strategy](docs/TESTING_STRATEGY.md) — Approach and trade-offs
- [API Guide](docs/API_GUIDE.md) — Endpoint details and assumptions

## 🔄 CI/CD Pipeline

```
Push/PR/Schedule
    ↓
Checkout Code
    ↓
Setup Java (11)
    ↓
Run Tests (./gradlew test)
    ↓
Generate Allure Report
    ↓
Upload Artifacts
    ↓
Notify Slack (✅/❌)
```

**Schedule**: Daily at 9 AM UTC + on every PR

## 🚦 Current Status

- ✅ **Smoke Tests**: 5 tests passing
- ✅ **Contract Tests**: 5 tests passing
- ✅ **Security Tests**: 5 tests passing
- ✅ **Allure Reporting**: Integrated
- ✅ **CI/CD**: Active (daily + PR)
- ✅ **Slack Notifications**: Configured

## 📞 Support & Contributions

Questions or improvements? Open an issue or contact [@gustavaom7](https://github.com/gustavaom7).

---

**Part of**: QA Automation Portfolio ([Maestro Mobile](https://github.com/gustavaom7/maestro) • [Playwright Web](https://github.com/gustavaom7/playwright) • [Cypress Web](https://github.com/gustavaom7/cypress))
