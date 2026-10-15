# StudentBook

[![CI Status](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp/graph/badge.svg?token=8GQY2T0JDI)](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp)

StudentBook helps private tutors keep student contacts and tuition details in one place. It runs on your computer and uses typed commands.

StudentBook supports student contacts, optional schooling levels and guardian contacts. Each student can have multiple lessons; each lesson records a subject, weekly time slot and hourly rate, with optional mode and location. Use `lesson-add`, `lesson-edit` and `lesson-delete` to manage lessons, and `guardian-add`, `guardian-edit` and `guardian-delete` to manage a student's guardian contact. `find` can search by name or filter by schooling level, lesson subject and day; the lesson filters also apply to the weekly calendar. Changes are saved automatically.

Tutors can:

* Add, list, edit and delete student records.
* Record an optional schooling level and guardian contact, plus multiple lessons with subjects, weekly time slots and hourly rates.
* Save records locally and reopen them later.

All five tuition features are part of the MVP. Payment processing and sending messages are out of scope. Other brainstormed ideas are backlog candidates, not promised additions.

![StudentBook interface](docs/images/Ui.png)

This screenshot shows sample students. The User Guide describes the available commands. See the [Developer Guide](docs/DeveloperGuide.md#product-scope) for feature owners and decisions still to confirm.

Start with the [User Guide](docs/UserGuide.md) to use the app or the [Developer Guide](docs/DeveloperGuide.md) to contribute. The [project website](https://ay2627s1-cs2103t-f13-4.github.io/tp/) publishes the merged documentation.

For course milestones, weekly tasks, and release requirements, see the [TP planning guides](planning/README.md).

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org). It uses Java and JavaFX, with Gradle for building and testing.
