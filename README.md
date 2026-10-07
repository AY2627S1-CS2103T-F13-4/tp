# StudentBook

[![CI Status](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-F13-4/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp/graph/badge.svg?token=8GQY2T0JDI)](https://codecov.io/gh/AY2627S1-CS2103T-F13-4/tp)

StudentBook is a desktop contact book being developed for independent private tutors who prefer typing. It retains student names, phone numbers, email addresses, addresses and tags, and adds tuition details in a local, single-user application.

![StudentBook interface](docs/images/Ui.png)

StudentBook is currently in development. This branch retains the inherited AddressBook commands and adds subject entry, display, editing, clearing and persistence. The other tuition fields have shared model/storage support; their command flows remain planned.

The proposed first version will let tutors:

* Keep existing contact fields and add optional subject and schooling level details.
* Optionally attach one guardian name and phone number after creating a student.
* Delete a student together with that student's guardian contact.
* Save records locally and reload them on the next launch.

Weekly lesson slots and hourly rates are selected extension workstreams outside the core MVP. Shared group classes, academic progress, fee calculation, payment processing and messaging are deferred. See the [shared student model and scope](docs/StudentModel.md) for field types and integration rules.

See the [product website](https://ay2627s1-cs2103t-f13-4.github.io/tp/), [User Guide](docs/UserGuide.md) and [Developer Guide](docs/DeveloperGuide.md). The User Guide describes commands supported by this development version.

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org). It uses Java and JavaFX, with Gradle for building and testing.
