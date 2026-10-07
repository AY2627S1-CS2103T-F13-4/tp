# StudentBook

[![CI Status](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp/graph/badge.svg?token=8GQY2T0JDI)](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp)

StudentBook helps private tutors keep student contacts and tuition details in one place. It runs on your computer and uses typed commands.

This development branch supports student contacts and subjects. You can add, view, edit and clear a subject. The app saves it automatically. Commands for the other tuition fields are planned.

The first version aims to let tutors:

* Record a student's name, phone, email, address and tags, with an optional subject and schooling level.
* Add one guardian's name and phone number to a student.
* Delete a student and their attached details.
* Save records locally and reopen them later.

Weekly lesson slots and hourly rates are planned extensions. Group classes, progress notes, fee calculations, payments and messaging are deferred. See [Student model and scope](docs/StudentModel.md) for the agreed design and feature owners.

Start with the [User Guide](docs/UserGuide.md) to use the app or the [Developer Guide](docs/DeveloperGuide.md) to contribute. The [project website](https://ay2627s1-cs2103t-f13-4.github.io/tp/) publishes the merged documentation.

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org). It uses Java and JavaFX, with Gradle for building and testing.
