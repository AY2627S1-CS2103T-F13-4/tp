---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

--------------------------------------------------------------------------------------------------------------------

## Generating test data

The `seed` Gradle task writes a StudentBook data file filled with realistic Singapore tuition students: local names, HDB addresses, primary to JC levels and adult learners, schools that fit each level, guardians, siblings who share a guardian and address, weekly lessons with typical subjects, slots, rates, locations and modes, and a few progress notes, guardian follow-ups and archived students. It builds students from the real model classes and saves them through `JsonAddressBookStorage`, so every generated file is one the app can load. The generator is `seedu.address.testutil.StudentBookSeeder`.

Run it with no arguments to write 40 students to `data/studentbook.json`:

```text
./gradlew seed
```

To change the defaults, pass options through `--args`, e.g. `./gradlew seed --args="--students 500 --force"`:

```text
./gradlew seed --args="[--students N] [--seed S] [--out FILE] [--force] [--edge-cases]"
```

Option | Meaning | Default
-------|---------|--------
`--students N` | Number of random students, at most 1376 (the number of distinct names the generator can make). Try 500 for stress testing. | `40`
`--seed S` | Random seed. The same seed always gives the same file. | `2103`
`--out FILE` | File to write, relative to the repository root. | `data/studentbook.json`
`--force` | Replace `FILE` if it already exists. Without it, the task refuses and leaves the file unchanged, so it never wipes your own data by accident. |
`--edge-cases` | Also add fixed students for boundary testing (see below). The random students are unchanged. |

The task prints the number of students, lessons and overlapping lesson pairs it wrote. Since the default output is the file the app loads, `./gradlew run` then opens the generated data.

`--edge-cases` adds these fixed students after the random ones:

* `Alex Tan` and `alex tan`, whose names differ only in letter case.
* A student whose name, address, tags, school, level, subject, location, progress note and follow-up are at or near their length limits.
* Back-to-back lessons that touch but do not overlap (Monday and Friday).
* A partial overlap, a lesson contained in another, and two lessons in exactly the same slot (Wednesday and Thursday).
* A lesson whose location is a full Zoom link.
* A student with no lessons, and a student with no lessons, guardian, level or school.
* An archived student, who is hidden from the default list.

<div markdown="span" class="alert alert-info">:information_source: **Note:** The app's built-in sample data, shown when no data file exists, is separate. It comes from `SampleDataUtil`: eight unarchived students covering every level group, with a school for each school student, physical and virtual lessons, two siblings sharing a guardian and one pair of overlapping lessons.
</div>
