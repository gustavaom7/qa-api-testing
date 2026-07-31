# Quick Start Guide

Get up and running with the QA API Testing suite in 5 minutes.

## Prerequisites

- **Java 11+** (check with `java -version`)
- **Git**
- **5 MB disk space**

## Installation & First Run

### 1. Clone the repository
```bash
git clone https://github.com/gustavaom7/qa-api-testing.git
cd qa-api-testing
```

### 2. Make gradlew executable (Linux/Mac)
```bash
chmod +x gradlew
```

### 3. Run tests
```bash
# Option A: Using Gradle wrapper (recommended)
./gradlew test

# Option B: Using system Gradle (if installed)
gradle test

# Option C: Using Maven (if you prefer)
# See build.gradle for dependencies
```

**Expected output:**
```
BUILD SUCCESSFUL
> Task :test
15 tests passed
```

### 4. View results

#### Generate Allure Report (recommended)
```bash
./gradlew allureReport allureServe
```
Opens Allure dashboard in browser (http://localhost:4040)

#### View test summary
```bash
cat build/test-results/test/TEST-*.xml
```

#### Check logs
```bash
cat build/logs/api-tests.log
```

## Test Structure

```
15 Total Tests
├── Smoke Tests (5)
│   ├── Search for "Java" → returns results
│   ├── Multiple terms → all return results
│   ├── Empty search → returns empty
│   ├── Timestamps → valid format
│   └── Response time → < 3 seconds
├── Contract Tests (5)
│   ├── Schema validation
│   ├── Required fields
│   ├── Type checking
│   ├── Content-type
│   └── Numeric types
└── Security Tests (5)
    ├── SQL injection handling
    ├── XSS payload handling
    ├── Template injection
    ├── Large input (buffer overflow)
    └── Special characters
```

## Common Tasks

### Run specific test type
```bash
./gradlew test --tests "*SmokeTests"
./gradlew test --tests "*ContractTests"
./gradlew test --tests "*SecurityTests"
```

### Run single test
```bash
./gradlew test --tests "WikipediaSearchSmokeTests.searchForJavaShouldReturnValidResults"
```

### Debug mode (verbose output)
```bash
./gradlew test -i --tests WikipediaSearchSmokeTests
```

### Clean build
```bash
./gradlew clean test
```

### Generate only Allure report (no tests)
```bash
./gradlew allureReport
cat build/allure-report/index.html
```

## Troubleshooting

### "gradlew not found" or permission denied
```bash
chmod +x gradlew
./gradlew test
```

### "Java not found"
```bash
# Install Java 11+
# macOS: brew install openjdk@17
# Ubuntu: sudo apt-get install default-jdk
# Windows: Download from java.com

# Verify
java -version  # Should show 11+
```

### Tests timeout (>15 seconds)
```bash
# Check Wikipedia API is accessible
curl https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=Test

# Run tests sequentially (slower but more stable)
./gradlew test --max-parallel 1 -i
```

### Allure report won't open
```bash
# Generate report
./gradlew allureReport

# Manually open
open build/allure-report/index.html  # macOS
xdg-open build/allure-report/index.html  # Linux
start build\allure-report\index.html  # Windows
```

## Next Steps

1. **Read the docs**
   - `README.md` — Full project overview
   - `docs/TESTING_STRATEGY.md` — Testing approach
   - `docs/API_GUIDE.md` — API details and scenarios

2. **Explore the code**
   - Look at test classes: `src/test/java/com/gustavaom7/tests/`
   - Check models: `src/test/java/com/gustavaom7/models/`
   - Review helpers: `src/test/java/com/gustavaom7/helpers/`

3. **Customize for your API**
   - Change `WIKIPEDIA_API_BASE` to your API endpoint
   - Create response models matching your API
   - Add/remove test scenarios

4. **Set up CI/CD**
   - Push to GitHub
   - Add `SLACK_WEBHOOK_URL` secret
   - GitHub Actions runs automatically (daily + PR)

## Tips

- ✅ Tests are **independent** — run any subset, any order
- ✅ No authentication required (Wikipedia API is public)
- ✅ Tests run on **real API** (not mocked)
- ✅ Fast execution (~30 seconds for all 15 tests)
- ✅ Detailed Allure reports included

## Need Help?

- Check `docs/API_GUIDE.md` for API details
- See `CONTRIBUTING.md` for code style
- Open issue: [@gustavaom7](https://github.com/gustavaom7)

---

**Happy Testing! 🚀**
