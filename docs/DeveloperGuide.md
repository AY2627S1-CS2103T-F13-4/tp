---
layout: page
title: Developer Guide
---

StudentBook builds on AddressBook Level 3, or AB3. The architecture below describes the inherited application. The shared tuition model and subject workflow are implemented on this branch; other tuition commands are planned.

Start with [setup](SettingUp.md) and [Student model and scope](StudentModel.md).

* Table of Contents
{:toc}

## Acknowledgements

* Based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) from the [SE-EDU initiative](https://se-education.org).
* Pranav Pappu used [OpenAI Codex](https://openai.com/codex/) to review and edit the repository docs, Google Doc and story Sheet. Codex also implemented the shared tuition model, JSON support, field preservation, subject commands/display and regression tests. Separate reviewers checked the changes.

## Getting started

Follow the [setup guide](SettingUp.md), then run the [tests](Testing.md).

## Design

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

`Main` and `MainApp` start the app, connect its components and clean up on shutdown.

| Component | Responsibility |
|---|---|
| [UI](#ui-component) | Display records and accept commands |
| [Logic](#logic-component) | Parse and execute commands |
| [Model](#model-component) | Hold records and settings in memory |
| [Storage](#storage-component) | Read and write JSON files |
| [Commons](#common-classes) | Share utilities across components |

Each main component exposes an interface and implements it in a manager class. For example, callers use `Logic`; `LogicManager` implements it.

<img src="images/ComponentManagers.png" width="300" />

This sequence shows a `delete 1` command passing through the components:

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Diagram sources are in `docs/diagrams`. See the [PlantUML tutorial](https://se-education.org/guides/tutorials/plantUml.html) to edit them.

### UI component

![UI structure](images/UiClassDiagram.png)

`MainWindow` contains `CommandBox`, `ResultDisplay`, `PersonListPanel` and `StatusBarFooter`. These classes extend `UiPart` and use JavaFX layouts in `src/main/resources/view`.

The UI sends commands to `Logic` and observes the filtered student list. It displays `Person` objects and refreshes when the list changes.

### Logic component

<img src="images/LogicClassDiagram.png" width="550" />

1. `LogicManager` passes command text to `AddressBookParser`.
2. The parser selects a command parser, such as `DeleteCommandParser`.
3. That parser creates a `Command`, which `LogicManager` executes against the model.
4. After execution, `LogicManager` saves the data and returns a `CommandResult`. A save failure raises a command error even if the model has already changed.

![Delete command sequence](images/DeleteSequenceDiagram.png)

Command parsers implement the `Parser` interface and use the helpers below:

<img src="images/ParserClasses.png" width="600" />

The `DeleteCommandParser` lifeline should end at the X marker; PlantUML draws it longer.

### Model component

<img src="images/ModelClassDiagram.png" width="450" />

`ModelManager` holds the address book, user preferences and the current filtered list.

* `UniquePersonList` stores the records.
* The UI observes an unmodifiable `ObservableList<Person>` of matching records.
* Preferences are exposed through `ReadOnlyUserPrefs`.
* The model does not depend on UI, Logic or Storage.

The diagram shows the original contact model. [Student model and scope](StudentModel.md) defines the added tuition fields.

An inherited alternative stores unique tags centrally and lets records reference them:

<img src="images/BetterModelClassDiagram.png" width="450" />

This alternative is not the current implementation.

### Storage component

<img src="images/StorageClassDiagram.png" width="550" />

`StorageManager` delegates record files to `JsonAddressBookStorage` and preferences to `JsonUserPrefsStorage`. Both use JSON and convert stored values to model objects.

### Common classes

Shared helpers live in `seedu.address.commons`.

## Implementation

### Shared tuition model

`Person` keeps all AB3 contact fields and adds five optional tuition values. Existing constructors and JSON files remain valid. See [Student model and scope](StudentModel.md) for types, validation, equality and storage rules.

### Subject workflow

| Step | Code and behavior |
|---|---|
| Parse | `AddCommandParser` and `EditCommandParser` accept `s/`. `ParserUtil.parseSubject` normalizes and validates it. |
| Edit | `EditPersonDescriptor` distinguishes omitted, supplied and cleared subjects. Other fields stay unchanged. |
| Display | `PersonCard` shows the subject or `Subject: Not recorded`. |
| Save | `JsonAdaptedPerson` stores a string or null. Missing old-file properties mean no subject. |

`SubjectWorkflowTest` covers add, edit, clear, filtered indexes, invalid input, duplicates and save/reload. `TuitionStorageTest` checks all shared fields.

### Inherited undo/redo proposal

Undo and redo are not implemented or part of the selected tuition work. The inherited AB3 proposal uses `VersionedAddressBook` to store snapshots and a pointer to the current snapshot.

* `commit()` adds a snapshot after a successful change and removes any redo history.
* `undo()` moves to the previous snapshot; `redo()` moves to the next.
* Failed commands and read-only commands do not add snapshots. Moving past either end reports an error.
* Full snapshots are simpler to implement but use more memory. Command-specific inverse operations use less memory but require correct undo logic for each command.

The inherited diagrams remain in `docs/diagrams` and `docs/images` for reference. Data archiving is also unimplemented.

## Documentation, logging, testing, dev-ops

* [Documentation](Documentation.md)
* [Testing](Testing.md)
* [Logging](Logging.md)
* [Builds, CI and releases](DevOps.md)

## Appendix: Requirements

### Product scope

**Target user profile**:

Independent private tutors who:
* manage multiple primary- or secondary-school students without administrative support
* keep track of student details, lesson arrangements, and guardian contacts themselves
* regularly retrieve and update this information before and after lessons
* can type quickly and prefer typing over mouse-driven input

**Value proposition**: StudentBook keeps student details and guardian contacts in one place, so tutors can find and update them quickly. Lesson arrangements and follow-up notes are planned extensions.


### User stories

Priorities: `***` = required, `**` = optional, `*` = low priority. These describe product goals, not implementation status.

| ID | Priority | As a... | I want to... | So that I can... |
|----|----------| ------------------------------------------ | ------------------------------ | ---------------------------------------------------------------------- |
| 1 | `***` | private tutor | add a student record with a name | begin tracking each student |
| 2 | `***` | private tutor | view the list of current students | see who I currently teach |
| 3 | `***` | private tutor | view a student's subject and schooling level | prepare for the right subject and level |
| 4 | `***` | private tutor | delete a student record | remove students who leave or recreate incorrect records |
| 5 | `***` | private tutor | record a student's schooling level | choose material at the right level |
| 6 | `***` | private tutor | record the subject I teach a student | remember what I teach each student |
| 7 | `***` | private tutor | record one guardian's name and contact details for a student | reach the right adult about lessons |
| 8 | `***` | private tutor | view the guardian name and contact details linked to a student | contact a guardian without searching elsewhere |
| 9 | `***` | private tutor | keep my student details and guardian contacts when I close and reopen the app | continue my work without re-entering information |
| 10 | `**` | private tutor | edit a student's name | correct typos without recreating the record |
| 11 | `**` | private tutor | record a student's school | remember which school the student attends |
| 12 | `**` | private tutor | update a student's school, schooling level or subjects | keep records current as circumstances change |
| 13 | `**` | private tutor | search for a student by a partial name | retrieve a record quickly |
| 22 | `**` | private tutor | edit a guardian's contact details | keep changed phone numbers and emails current |
| 25 | `**` | private tutor | add a recurring weekly lesson slot to a student | remember when the student is taught |
| 28 | `**` | private tutor | edit a lesson slot | keep the agreed schedule current |
| 29 | `**` | private tutor | remove a lesson slot | clear a cancelled arrangement |
| 33 | `**` | private tutor | add a dated progress note to a student | remember what happened in recent lessons |
| 34 | `**` | private tutor | view a student's dated progress notes in chronological order | recall learning needs before the next lesson |
| 35 | `**` | private tutor | record a follow-up item for a guardian | remember what I need to communicate |
| 36 | `**` | private tutor | list outstanding guardian follow-up items | avoid missing promised updates |
| 40 | `**` | private tutor learning the app | look up command usage and examples | complete a task when I do not remember what to type |
| 43 | `**` | private tutor | edit a guardian follow-up item | keep the recorded request accurate when arrangements change |
| 44 | `**` | private tutor | mark a guardian follow-up item as complete | remove finished work from my outstanding follow-ups |
| 45 | `**` | private tutor learning the app | see what needs correcting when an entry is rejected | fix the entry without guessing why it failed |
| 46 | `**` | tutor who communicates directly with a student | record the student’s own phone number or email address | keep their contact details with their record |
| 47 | `**` | tutor who needs to contact a student directly | view the student’s contact details | reach them without searching elsewhere |
| 48 | `**` | tutor checking a student’s lesson arrangements | view that student’s weekly lesson times | confirm when I teach them |
| 49 | `**` | tutor planning my week | view all my lessons arranged by day and time | see which students I will teach each day |
| 53 | `**` | private tutor | record and view the hourly rate agreed for a student | check the agreed rate before discussing fees |
| 54 | `**` | private tutor | change or remove a student's recorded hourly rate | keep the agreed rate current |
| 52 | `*` | private tutor | export all records to a human-readable file | inspect and back up my data outside the app |



### Use cases

These use cases describe the intended behavior. The [User Guide](UserGuide.md) lists the commands available now.

The actor is a private tutor and the system is StudentBook. The app is already open unless stated otherwise. Tuition fields are optional; registration still requires the existing contact fields.

**MSS** means main success scenario. UC01, UC02, UC03 and UC06 cover first-version goals. UC04 and UC05 describe later features.

#### UC01: Register a student

**Related user stories:** 1, 5, 6, 9.

**MSS**

1. Tutor requests to add a student, supplying name, phone, email and address, with optional tags and supported tuition details.
2. StudentBook confirms the addition and displays the new student.

Use case ends.

**Extensions**

* 1a. A required detail is missing or a supplied value is invalid.
  * 1a1. StudentBook reports the error without adding a record.
  * 1a2. Tutor corrects and resubmits the request.
  * Use case resumes at step 1.
* 1b. A student with the exact same name already exists.
  * 1b1. StudentBook reports the duplicate without adding a record.
  * Use case ends.
* 2a. StudentBook cannot save the change.
  * 2a1. StudentBook reports the save failure. The student may still appear on screen without being saved.
  * Use case ends.

#### UC02: Retrieve a guardian's contact details

**Related user stories:** 2, 8.

**MSS**

1. Tutor requests to list students.
2. StudentBook displays student records with their linked guardian names and contact details.
3. Tutor reads the intended student's guardian contact details.

Use case ends. Contacting the guardian takes place outside StudentBook.

**Extensions**

* 2a. No student records exist.
  * 2a1. StudentBook displays an empty list.
  * Use case ends.

* 2b. The intended student has no guardian contact.
  * 2b1. StudentBook indicates that no guardian is recorded.
  * Use case ends.

#### UC03: Remove a student who has stopped attending lessons

**Related user stories:** 2, 4.

**MSS**

1. Tutor requests to list students.
2. StudentBook displays the student list with an index for each record.
3. Tutor requests to delete the intended student using the displayed index.
4. StudentBook removes that student's record and linked guardian contact, and displays a success message and the updated list.

Use case ends.

**Extensions**

* 2a. The student list is empty.
  * Use case ends.
* 3a. The supplied index is invalid for the displayed list.
  * 3a1. StudentBook displays an error without deleting any record.
  * Use case resumes at step 2.

#### UC04: Change a recurring lesson slot (planned beyond the first version)

**Related user stories:** 13, 28, 48.

**Preconditions:** The student has an existing recurring lesson slot.

**MSS**

1. Tutor searches for the student by a partial name.
2. StudentBook displays matching students and their weekly lesson times.
3. Tutor identifies the student and requests to change a specific lesson slot, supplying the new day and time.
4. StudentBook displays the updated lesson arrangement.

Use case ends.

**Extensions**

* 2a. No students match the search.
  * 2a1. StudentBook displays an empty result list.
  * 2a2. Tutor submits a revised search.
  * Use case resumes at step 2.
* 3a. The student or lesson slot reference is invalid, or the new day or time is invalid.
  * 3a1. StudentBook explains the error without changing the arrangement.
  * 3a2. Tutor corrects and resubmits the request.
  * Use case resumes at step 3.

#### UC05: Complete a guardian follow-up (planned beyond the first version)

**Related user stories:** 36, 44.

**Preconditions:** At least one outstanding guardian follow-up item exists.

**MSS**

1. Tutor requests to list outstanding guardian follow-up items.
2. StudentBook displays the items with their associated students and guardians.
3. Tutor carries out the selected follow-up outside StudentBook.
4. Tutor requests to mark the selected item as complete.
5. StudentBook records its completion and removes it from the outstanding list.

Use case ends.

**Extensions**

* 4a. The supplied item reference is invalid or the item is already complete.
  * 4a1. StudentBook explains the error without changing any item.
  * Use case resumes at step 1.

#### UC06: Attach a guardian contact

**Related user stories:** 7, 9.

**Preconditions:** The student exists and is visible in the displayed list.

**MSS**

1. Tutor requests to attach a guardian, supplying the student's displayed index and the guardian's name and phone.
2. StudentBook displays the linked guardian and confirms the addition.

Use case ends.

**Extensions**

* 1a. The index or guardian details are invalid, or a guardian is already attached.
  * 1a1. StudentBook reports the error without changing the record.
  * 1a2. Tutor corrects and resubmits the request.
  * Use case resumes at step 1.
* 2a. StudentBook cannot save the change.
  * 2a1. StudentBook reports the save failure. The contact may still appear on screen without being saved.
  * Use case ends.

### Non-functional requirements

These are acceptance targets. They have not all been verified in the current app.

1. **Compatibility.** Run on Windows, macOS and Linux with Java `25`, on systems supported by the bundled JavaFX runtime.
2. **Capacity and speed.** Support 1,000 students, each with one guardian. Adding, listing or deleting a record should show its result within two seconds. Test on a supported computer with at least 4 GB of RAM and local storage. Measure from command submission to the displayed result, excluding startup.
3. **Keyboard use.** After launch, let tutors add, list and delete students without a mouse.
4. **Error messages.** Explain what is wrong and how to fix it. Invalid input should leave existing records unchanged.
5. **Saved data.** After a successful save and normal shutdown, restore every contact and tuition field on restart, including implemented extensions. Users should not have to re-enter data.
6. **Offline use.** Manage and save records on the tutor's computer without internet access or an online account.
7. **Data protection.** Keep student and guardian contact details out of external services and diagnostic logs. The tutor's operating-system account and file permissions control access to local files.
8. **File errors.** Report save failures clearly. A failed save may leave the change in memory. Undoing that change and blocking commands after a failed load are future improvements. Valid AB3 files must load with new fields absent.

### Glossary

* **StudentBook**: The desktop app for managing student records.
* **Independent private tutor**: A tutor who manages students and lessons without administrative staff.
* **Student record**: One student's name, phone, email, address and tags, with optional subject, schooling level and guardian contact. Lesson slots and rates are planned extensions; notes are future work.
* **Guardian contact**: The name and phone number of the adult responsible for a student. The first version allows one optional guardian per student. A recorded guardian needs both fields.
* **Schooling level**: The student's stage of primary or secondary education, such as Primary 5 or Secondary 3.
* **Subject**: An academic subject taught by the tutor to a student, such as Mathematics or English.
* **Recurring lesson slot**: A student's weekly lesson day, start time and end time. The first extension allows one optional slot per student.
* **Hourly rate**: The agreed SGD amount per hour, stored as `BigDecimal`. It does not calculate fees or process payments.
* **Progress note**: A dated note recording a student's learning progress or observations from a lesson.
* **Guardian follow-up item**: A recorded action the tutor needs to carry out for a guardian, such as providing an update. An outstanding item has not yet been marked complete.
* **Displayed index**: The number beside a student in the current list. It can change when the list changes, so it is not a permanent student ID.
* **Local storage**: Files on the tutor's computer that keep records between launches.
* **Private contact detail**: A student or guardian's phone number, email address, or other contact information intended for the tutor's use rather than public sharing.

## Appendix: Instructions for manual testing

Use an empty test folder and sample data. These checks are a starting point; also try variations and boundary values.

### Launch and shutdown

1. Build the JAR using the [Quick start](UserGuide.md#quick-start), copy it into the test folder and run `java -jar addressbook.jar` there.
2. Check that the window opens with sample contacts.
3. Resize and move the window, then close and reopen the app. Check that it keeps the new size and position.

### Student deletion

1. Run `list` with several records present, then `delete 1`. The first record should disappear and the result should describe it.
2. Try `delete 0`, `delete`, `delete x` and an index larger than the list. Each should report an error and leave all records unchanged.
3. Run `find Alex`, then delete a valid result index. Check that it deletes that search result, not the record at the same position in the full list.

### Subject and saved data

1. Add a unique test student with `s/Combined   Science`. The card should show `Combined Science`.
2. Change it with `edit INDEX s/Math`, then edit the phone without `s/`. The subject should stay `Math`.
3. Restart the app. Both edits should remain.
4. Run `edit INDEX s/` and restart again. The card should show `Subject: Not recorded`.
5. Try a blank subject on `add`, a repeated `s/` prefix and an invalid subject such as `123`. Each should report an error without changing any record.

### Missing or invalid data files

Close the app and back up the test data before each case.

* **Missing file:** Move `data/addressbook.json` out of the test folder, then restart. Sample records should appear.
* **Invalid file:** Replace the test file with invalid JSON, then restart. An empty list should appear and the log should record the load failure. Do not run commands before restoring the backup; a successful command saves over the file.
* **Old valid file:** Load AB3 data without tuition properties. The contact details should remain and subjects should show as not recorded.
