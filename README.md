# StudentBook

[![CI Status](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp/graph/badge.svg?token=8GQY2T0JDI)](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp)

StudentBook is a desktop contact book being developed for independent private tutors who prefer typing. Its planned first version keeps student names and one guardian contact per student in a local, single-user application.

![StudentBook interface](docs/images/Ui.png)

StudentBook is currently in development. The current application provides the inherited AddressBook features; the StudentBook commands and guardian fields below are planned requirements.

The proposed first version will let tutors:

* Add a student and list all students.
* Attach one guardian name and phone number to each student.
* Delete a student together with that student's guardian contact.
* Save records locally and reload them on the next launch.

Lesson scheduling, academic progress, payments and messaging are outside the proposed first version.

See the [product website](https://ay2627s1-cs2103t-f13-4.github.io/tp/), [User Guide](docs/UserGuide.md) and [Developer Guide](docs/DeveloperGuide.md). The User Guide currently describes the inherited application.

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org). It uses Java and JavaFX, with Gradle for building and testing.
