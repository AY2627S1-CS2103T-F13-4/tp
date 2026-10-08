# StudentBook

[![CI Status](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp/graph/badge.svg?token=8GQY2T0JDI)](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp)

StudentBook helps private tutors keep student contacts and tuition details in one place. It runs on your computer and uses typed commands.

This development branch supports student contacts, subjects, schooling levels, weekly lesson slots and hourly rates. You can add, view, edit and clear a subject, level, lesson slot or hourly rate, and find students by level. The app saves changes automatically. Guardian contact commands are planned.

The MVP will let tutors:

* Add, list, edit and delete student records.
* Record each student's subjects, schooling level, guardian contact, weekly lesson slot and hourly rate.
* Save records locally and reopen them later.

All five tuition features are part of the MVP. Payment processing and sending messages are out of scope. Other brainstormed ideas are backlog candidates, not promised additions.

![Planned StudentBook interface](docs/images/Ui.png)

This mock-up shows the planned MVP. The User Guide describes the commands available now. See the [Developer Guide](docs/DeveloperGuide.md#product-scope) for feature owners and decisions still to confirm.

Start with the [User Guide](docs/UserGuide.md) to use the app or the [Developer Guide](docs/DeveloperGuide.md) to contribute. The [project website](https://ay2627s1-cs2103t-f13-4.github.io/tp/) publishes the merged documentation.

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org). It uses Java and JavaFX, with Gradle for building and testing.
