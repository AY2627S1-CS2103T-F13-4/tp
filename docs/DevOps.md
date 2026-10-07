---
layout: page
title: DevOps guide
---

## Build and check

Run these commands from the repository root with JDK 25. On Windows, use `gradlew.bat` instead of `./gradlew`.

| Command | Result |
|---|---|
| `./gradlew run` | Build and run the app |
| `./gradlew shadowJar` | Build `build/libs/addressbook.jar` with its dependencies |
| `./gradlew runShadow` | Build and run that JAR |
| `./gradlew test` | Run all tests |
| `./gradlew clean test` | Delete previous build output, then test |
| `./gradlew check coverage` | Run tests and style checks; generate coverage reports |
| `./gradlew checkstyleMain checkstyleTest` | Check production and test code style |
| `./gradlew clean` | Delete build output |

See the [Gradle tutorial](https://se-education.org/guides/tutorials/gradle.html) for details.

## Continuous integration

GitHub Actions runs the workflows in `.github/workflows` on pushes and pull requests. Java CI checks the build on Linux, macOS and Windows.

JaCoCo measures test coverage. The Linux job uploads its report to Codecov. Check coverage separately from the build result; a passing build alone does not show which behavior was tested. Fork owners can follow the [Codecov setup guide](https://se-education.org/guides/tutorials/codecov.html).

### Repository checks

On macOS or Linux, run:

```text
./.github/run-checks.sh
```

These scripts check repository files, including line endings. They print warnings and errors to the terminal.

To add a check, create an executable `.github/check-*` script. `run-checks.sh` discovers it automatically. Print findings as `SEVERITY:FILENAME:LINE: MESSAGE`, where severity is `ERROR` or `WARN` and the filename is relative to the current directory. Exit with a non-zero code if errors occur.

## Make a release

1. Update the version in `src/main/java/seedu/address/MainApp.java`.
2. Run the checks, then build with `./gradlew shadowJar`.
3. Tag the release commit, for example `v1.2`.
4. [Create a GitHub release](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository) and attach `build/libs/addressbook.jar`.
