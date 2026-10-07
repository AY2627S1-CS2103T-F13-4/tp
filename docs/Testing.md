---
layout: page
title: Testing guide
---

## Run tests

Use JDK 25. From the repository root:

```text
./gradlew test
```

Use `./gradlew clean test` for a clean run, or `./gradlew check coverage` to include style checks and coverage. On Windows, replace `./gradlew` with `gradlew.bat`.

In IntelliJ, right-click `src/test/java` and choose **Run 'All Tests'**. To run fewer tests, right-click a package, class or test method instead.

See the [Gradle tutorial](https://se-education.org/guides/tutorials/gradle.html) for more options.

## Test coverage

| Test type | Purpose | Example |
|---|---|---|
| Unit | Check one class or method | `StringUtilTest` |
| Integration | Check how components work together | `StorageManagerTest` |
| Hybrid | Check individual behavior and interactions | `LogicManagerTest` |

For tuition changes, check `TuitionValuesTest`, `TuitionStorageTest` and `SubjectWorkflowTest`. Cover valid and invalid input, unchanged fields, old-file loading, and save/reload.

Use the [manual checks](DeveloperGuide.md#appendix-instructions-for-manual-testing) to test the app through its interface.
