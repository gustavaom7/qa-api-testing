# Contributing Guide

This is a portfolio project demonstrating API testing best practices. Contributions are welcome!

## Setup

1. Fork and clone the repository
2. Install Java 11+
3. Run `./gradlew build` to verify setup

## Making Changes

### Adding Tests

1. Create test class in `src/test/java/com/gustavaom7/tests/`
2. Follow naming convention: `*Tests` or `*Test`
3. Use Allure annotations for reporting:
   ```java
   @Epic("Feature Area")
   @Feature("Specific Feature")
   @DisplayName("Human-readable test name")
   @Severity(SeverityLevel.HIGH)
   ```

### Adding Response Models

1. Create POJO in `src/test/java/com/gustavaom7/models/`
2. Use Gson annotations for JSON mapping:
   ```java
   @SerializedName("json_field_name")
   private String javaFieldName;
   ```

### Adding JSON Schemas

1. Create schema in `src/test/resources/schemas/`
2. Use JSON Schema Draft 7
3. Reference in contract tests with `matchesJsonSchemaInClasspath("schemas/name.json")`

## Testing Locally

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests ClassName

# Generate Allure report
./gradlew allureReport allureServe
```

## Code Style

- **Naming**: `camelCase` for methods/variables, `PascalCase` for classes
- **Assertions**: Use AssertJ (fluent) over Hamcrest
- **Comments**: Only for non-obvious logic; code should be self-documenting
- **Line length**: Max 120 characters

## Pull Request Process

1. Branch from `main`
2. Make your changes
3. Ensure tests pass: `./gradlew test`
4. Push and open PR with description
5. PR is merged once CI passes

## Commit Messages

Follow conventional commits:
- `feat: add new test for X endpoint`
- `fix: correct assertion in Y test`
- `docs: update testing strategy`
- `refactor: consolidate helper methods`

## Questions?

Open an issue or contact [@gustavaom7](https://github.com/gustavaom7).

---

Thank you for contributing!
