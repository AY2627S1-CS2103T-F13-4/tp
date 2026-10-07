---
layout: page
title: Setting up and getting started
---

## Set up the project

1. Fork the repository and clone your fork.
2. Install JDK `25`.
3. In IntelliJ IDEA, [select JDK 25](https://se-education.org/guides/tutorials/intellijJdk.html) and [import the project as a Gradle project](https://se-education.org/guides/tutorials/intellijImportGradleProject.html).
4. Run `seedu.address.Main` and try a few commands.
5. [Run the tests](Testing.md).

You can also build and run from a terminal:

```text
./gradlew run
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Before writing code

* Configure IntelliJ with the [project code style](https://se-education.org/guides/tutorials/intellijCodeStyle.html). The [Checkstyle plugin](https://se-education.org/guides/tutorials/checkstyle.html) can flag issues while you type.
* Read the [architecture](DeveloperGuide.md#architecture) and [shared student model](StudentModel.md).
* GitHub Actions runs checks on pushes and pull requests. Its configuration is in `.github/workflows`.

These tutorials introduce the inherited AB3 code:

* [Trace a command](https://se-education.org/guides/tutorials/ab3TracingCode.html)
* [Add a command](https://se-education.org/guides/tutorials/ab3AddRemark.html)
* [Remove a field](https://se-education.org/guides/tutorials/ab3RemovingFields.html)
