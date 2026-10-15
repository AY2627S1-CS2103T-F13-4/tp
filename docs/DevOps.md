---
layout: page
title: DevOps guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Build automation

This project uses Gradle for **build automation and dependency management**. **We recommend reading [this Gradle tutorial from se-edu/guides](https://se-education.org/guides/tutorials/gradle.html).**


The following commands perform common Gradle tasks.


* **`clean`**: Deletes the files created during the previous build tasks (e.g. files in the `build` folder).<br>
  For example: `./gradlew clean`

* **`shadowJar`**: Uses the Shadow plugin to create the fat JAR file `build/libs/studentbook.jar`.<br>
  For example: `./gradlew shadowJar`

* **`run`**: Builds and runs the application.<br>
  **`runShadow`**: Builds the application as a fat JAR, then runs it.

* **`checkstyleMain`**: Runs the code style check for the main code base.<br>
  **`checkstyleTest`**: Runs the code style check for the test code base.

* **`test`**
  * `./gradlew test`: Runs all tests.
  * `./gradlew clean test`: Cleans the project before running all tests

--------------------------------------------------------------------------------------------------------------------

## Continuous integration (CI)

This project uses GitHub Actions for CI. The necessary workflow configuration files are in `.github/workflows`. No further setup is required.

### Code coverage

As part of CI, Gradle generates JaCoCo coverage reports from the tests, and CI uploads the coverage data to Codecov. Codecov then provides information about test coverage.

The GitHub Actions build and the Codecov coverage status are separate checks. A successful Gradle build does not mean Codecov's coverage target passed. If Codecov reports a failure, inspect its report to distinguish a coverage drop from an upload or service error. Review the uncovered code and add useful tests where appropriate; a coverage-target failure does not by itself mean the build or tests failed.

To enable Codecov for forks of this project, follow the steps given in [this se-edu guide](https://se-education.org/guides/tutorials/codecov.html).

### Repository-wide checks

In addition to Gradle checks, CI runs repository-wide checks. Unlike Gradle checks, which cover files used in the build, these checks cover every repository file and enforce rules that are hard to apply on development machines, such as line-ending requirements.

These checks are implemented as POSIX shell scripts, and thus can only be run on POSIX-compliant operating systems such as macOS and Linux. To run all checks locally on these operating systems, execute the following in the repository root directory:

`./.github/run-checks.sh`

Any warnings or errors will be printed out to the console.

**If adding new checks:**

* Checks are implemented as executable `check-*` scripts within the `.github` directory. The `run-checks.sh` script will automatically pick up and run files named as such. That is, you can add more such files if you need and the CI will do the rest.

* Check scripts should print out errors in the format `SEVERITY:FILENAME:LINE: MESSAGE`
  * SEVERITY is either ERROR or WARN.
  * FILENAME is the path to the file relative to the current directory.
  * LINE is the line of the file where the error occurred and MESSAGE is the message explaining the error.

* Check scripts must exit with a non-zero exit code if any errors occur.

--------------------------------------------------------------------------------------------------------------------

## Making a release

Here are the steps to create a new release from the tested team `master` branch.

1. Update the version number in this repository's [`MainApp.java`](https://github.com/AY2627S1-CS2103T-F13-4/tp/blob/master/src/main/java/seedu/address/MainApp.java) to match the planned release, e.g. `V1.3` for tag `v1.3`. Merge that change before building the release.
1. Check the merged commit with `./gradlew check` and generate its fat JAR with `./gradlew shadowJar` (Windows: `gradlew.bat`). The output is `build/libs/studentbook.jar`.
1. Copy the JAR to a clean, writable folder outside the repository. Run `java -jar studentbook.jar`, try the main student, lesson, guardian and search workflows, close the app, then reopen it and check that changes persist in `data/studentbook.json`.
1. Capture screenshots or a recording of the released app showing its main features. Write release notes that describe the changes since the previous release (for v1.3, since AB3) and include that visual evidence.
1. Tag the tested merged commit with the release version, e.g. `v1.3`. [Create a GitHub release](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository) from that tag, paste the release notes and upload the tested `studentbook.jar`.
