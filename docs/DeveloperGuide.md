---
layout: page
title: Developer Guide
---

StudentBook builds on AddressBook Level 3, or AB3. The architecture below describes the inherited application. The shared tuition model, subject workflow and schooling-level workflow are implemented on this branch; other tuition commands are planned.

Start with [setup](SettingUp.md), then the [shared tuition model](#shared-tuition-model) and [product scope](#product-scope).

* Table of Contents
{:toc}

## Acknowledgements

* Based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3) from the [SE-EDU initiative](https://se-education.org).

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

The diagram shows the original contact model. The [shared tuition model](#shared-tuition-model) section describes the added fields.

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

A student has zero or more subjects in an immutable `Set<Subject>`. The other tuition fields use `Optional`; absence means "not recorded". The agreed schooling-level requirement is pending Min Wenn's implementation. Rates remain optional.

| Field | Java type | Rules |
|---|---|---|
| Subjects | `Set<Subject>` | Zero or more labels, each 1 to 40 ASCII characters after the space cleanup below |
| Schooling level | `Optional<SchoolingLevel>` | One text label, 1 to 30 ASCII characters after the space cleanup below |
| Guardian | `Optional<GuardianContact>` | Both `Name` and `Phone`, using AB3 validation |
| Weekly lesson | `Optional<WeeklyLessonSlot>` | `DayOfWeek`, `LocalTime start` and `LocalTime end`; whole minutes, with end later on the same day |
| Hourly rate | `Optional<HourlyRate>` | SGD/hour as a non-negative `BigDecimal`, with at most two decimal places; zero is a recorded value |

Subject and level use text labels, not enums. For both:

* Remove leading and trailing spaces or tabs. Replace repeated spaces or tabs with one space. Keep uppercase and lowercase letters as entered.
* Start with an ASCII letter or digit and include at least one letter.
* Allow letters, digits, spaces, apostrophes, hyphens, periods and parentheses.
* Do not map synonyms or check whether a subject matches a level.

Subject equality ignores case. The set preserves input order and the first spelling of a repeated label.

Siblings can have the same guardian details, but each student keeps a separate copy. There is no tutor/student role field or generic `class[]` field.

#### Constructors and editing

`Person` retains its five-argument constructor, which leaves tuition fields absent.

The full constructor order is:

```text
name, phone, email, address, tags,
subjects, schoolingLevel, guardianContact, weeklyLessonSlot, hourlyRate
```

The subject set and its entries must be non-null. `getSubjects()` returns an immutable set. Every `Optional` argument must be non-null.

The shared foundation provides the value types, model fields, JSON support, test builders and field preservation during edits. Feature owners add commands, display and tests separately.

* Every edit keeps fields it does not change.
* Full equality and hashing include all tuition fields.
* Duplicate detection still compares exact, case-sensitive names.
* Existing sample records may leave tuition fields absent.

#### JSON storage

Storage uses the configured file path, default `data/addressbook.json`, and the top-level `persons` array. Existing properties and record order are preserved.

The added JSON properties are:

| Property | JSON value |
|---|---|
| `subjects` | Array of text strings; empty means no subjects |
| `schoolingLevel` | Text string |
| `guardianContact` | Object with `name` and `phone` |
| `weeklyLessonSlot` | Object with uppercase English `day`, such as `MONDAY`, and `start`/`end` in `HH:mm` |
| `hourlyRate` | Decimal string, such as `"45.50"` |

Missing or null `subjects` loads as an empty set. Older files with a single `subject` string still load and save back as `subjects`. Supplying both properties with non-null values is rejected to avoid losing data. Other missing or null tuition properties load as `Optional.empty()`. Supplied values must have the correct JSON type and pass validation. Valid old files must still load. Keep the existing name and duplicate rules, and never silently discard data.

Tests cover old-file loading, save/reload and preservation of unrelated fields. A failed save reports an error but does not undo the in-memory change. A failed load does not block commands. These inherited failure behaviors differ from the earlier specification; see [decisions to confirm](#decisions-to-confirm).

### Subject workflow

| Step | Code and behavior |
|---|---|
| Parse | `AddCommandParser` and `EditCommandParser` accept repeated `s/` prefixes. `ParserUtil.parseSubjects` validates every label and removes case-insensitive duplicates. |
| Edit | `EditPersonDescriptor` distinguishes omission, replacement of the whole set, and clearing with one empty `s/`. Other fields stay unchanged. |
| Display | `PersonCard` shows all subjects or `Subjects: Not recorded`. |
| Save | `JsonAdaptedPerson` writes a string array and accepts older single-subject files. |

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

**Value proposition**: StudentBook keeps student contacts, tuition details, weekly lesson slots and hourly rates in one place, so private tutors can find and update them quickly.

The MVP includes student records and all five tuition features below. Keep the existing AB3 contact fields and commands. Payment processing and sending messages are out of scope. Group classes, progress notes, follow-ups and other brainstormed ideas remain backlog candidates, not delivery commitments.

| Owner | MVP feature | Story IDs |
|---|---|---|
| Min Wenn | Schooling level | 5; level updates in 12 |
| Pranav | Subject | 6; subject updates in 12 |
| Jian Yi | Weekly lesson slot | 25, 28, 29, 48 |
| Dylan | Hourly rate | 53, 54 |
| Mervin | Guardian contact | 7, 8, 22, 23 |

The shared types and storage are implemented. This branch supports add, display, edit and clear for subjects and schooling levels, plus finding students by level. Other owners add their command and display flows in separate PRs. Week 8 needs a small working increment per person; the full MVP is the v1.3 target.

### Agreed field rules

Subjects are optional and may contain multiple labels. Schooling level is mandatory by agreement, with enforcement left to Min Wenn. Hourly rate remains optional for now. Grouping a subject, rate and timeslot into a lesson is an idea for next week, not part of this change.

### Decisions to confirm

The earlier Google specification and the current code differ on the rules below. Keeping AB3 fields was agreed, but it did not settle every validation or failure rule. These are open product decisions, not changes to implement automatically.

| Decision | Earlier specification | Current implementation |
|---|---|---|
| Contact fields | Name required on creation | Name, phone, email and address required; confirm whether all four contact fields are needed |
| Duplicate names | Normalized, case-insensitive comparison | Exact, case-sensitive name comparison |
| Names and phones | Punctuation in names; optional `+` and 7–15 digits in phones | AB3 alphanumeric names; phones with at least 3 digits and no `+` |
| Save/load failures | Roll back failed saves; block commands after a failed load | In-memory changes can remain after a failed save; commands remain available after a failed load |

The pre-chat Git and Google documents also differed on registration and guardian attachment. The current plan adds the guardian separately. Confirm the remaining rules with the team before treating the earlier draft as superseded.


### User stories

Priorities from the planning Sheet are retained: `***` = high, `**` = medium, `*` = low. Priority does not determine whether a story is in the MVP or already implemented.

The MVP covers stories 1–9, subject/level updates in 12, guardian name/phone updates in 22 and removal in 23, slots in 25/28/29/48, and rates in 53/54. The table also preserves existing AB3 capabilities and other ideas for future reference. Unselected ideas are backlog candidates, not commitments, including school in story 12 and guardian email in story 22.

| ID | Priority | As a... | I want to... | So that I can... |
|---|---|---|---|---|
| 1 | `***` | private tutor | add a student record with a name | begin tracking each student |
| 2 | `***` | private tutor | view the list of current students | see who I currently teach |
| 3 | `***` | private tutor | view a student's subjects and schooling level | prepare for the right subject and level |
| 4 | `***` | private tutor | delete a student record | remove students who leave or recreate incorrect records |
| 5 | `***` | private tutor | record a student's schooling level | choose material at the right level |
| 6 | `***` | private tutor | record the subjects I teach a student | remember what I teach each student |
| 7 | `***` | private tutor | record one guardian's name and contact details for a student | reach the right adult about lessons |
| 8 | `***` | private tutor | view the guardian name and contact details linked to a student | contact a guardian without searching elsewhere |
| 9 | `***` | private tutor | keep my student details and guardian contacts when I close and reopen the app | continue my work without re-entering information |
| 10 | `**` | private tutor | edit a student's name | correct typos without recreating the record |
| 11 | `**` | private tutor | record a student's school | remember which school the student attends |
| 12 | `**` | private tutor | update a student's school, schooling level or subjects | keep records current as circumstances change |
| 13 | `**` | private tutor | search for a student by a partial name | retrieve a record quickly |
| 14 | `**` | private tutor | sort students alphabetically | scan a long student list faster |
| 15 | `**` | private tutor | filter students by school | find students from the same school |
| 16 | `**` | private tutor | filter students by schooling level | prepare level-appropriate materials |
| 17 | `**` | private tutor | filter students by subject | prepare for a subject-specific class |
| 18 | `**` | private tutor | mark a student as inactive | keep former students without cluttering the current list |
| 19 | `**` | private tutor | view inactive students separately | refer to past records when needed |
| 20 | `**` | private tutor | add another guardian to a student | keep alternative contacts for the same student |
| 21 | `**` | private tutor | record each guardian's relationship to the student | know who I am contacting |
| 22 | `**` | private tutor | edit a guardian's contact details | keep changed phone numbers and emails current |
| 23 | `**` | private tutor | remove a guardian from a student | discard contacts that are no longer relevant |
| 24 | `**` | private tutor | search by guardian name or contact | find the right student when a guardian contacts me |
| 25 | `**` | private tutor | add a recurring weekly lesson slot to a student | remember when the student is taught |
| 26 | `**` | private tutor | add more than one weekly lesson slot to a student | track students with several lessons each week |
| 27 | `**` | private tutor | view lessons scheduled on a chosen day | prepare for that day's students |
| 28 | `**` | private tutor | edit a lesson slot | keep the agreed schedule current |
| 29 | `**` | private tutor | remove a lesson slot | clear a cancelled arrangement |
| 30 | `**` | private tutor | record a lesson's location or delivery mode | know where and how the lesson will happen |
| 31 | `**` | private tutor | assign students to a shared group class | manage one lesson arrangement for its attendees |
| 32 | `**` | private tutor | view every student in a group class | prepare the correct class list |
| 33 | `**` | private tutor | add a dated progress note to a student | remember what happened in recent lessons |
| 34 | `**` | private tutor | view a student's dated progress notes in chronological order | recall learning needs before the next lesson |
| 35 | `**` | private tutor | record a follow-up item for a guardian | remember what I need to communicate |
| 36 | `**` | private tutor | list outstanding guardian follow-up items | avoid missing promised updates |
| 37 | `**` | private tutor | record a student's payment status | know which payments need follow-up without processing them |
| 38 | `**` | private tutor | record a guardian's preferred contact method | contact each guardian appropriately |
| 39 | `**` | private tutor exploring the app | find out which student-management tasks the app supports | decide whether it suits my work |
| 40 | `**` | private tutor learning the app | look up command usage and examples | complete a task when I do not remember what to type |
| 41 | `**` | private tutor | combine subject, schooling-level and lesson-day filters | find students matching the criteria for my preparation |
| 42 | `**` | private tutor | see the next scheduled lesson and its attending students | know which lesson to prepare for next |
| 43 | `**` | private tutor | edit a guardian follow-up item | keep the recorded request accurate when arrangements change |
| 44 | `**` | private tutor | mark a guardian follow-up item as complete | remove finished work from my outstanding follow-ups |
| 45 | `**` | private tutor learning the app | see what needs correcting when an entry is rejected | fix the entry without guessing why it failed |
| 46 | `**` | tutor who communicates directly with a student | record the student's own phone number or email address | keep their contact details with their record |
| 47 | `**` | tutor who needs to contact a student directly | view the student's contact details | reach them without searching elsewhere |
| 48 | `**` | tutor checking a student’s lesson arrangements | view that student's weekly lesson times | confirm when I teach them |
| 49 | `**` | tutor planning my week | view all my lessons arranged by day and time | see which students I will teach each day |
| 50 | `*` | private tutor | identify students who share a guardian contact | coordinate communication without contacting the same guardian separately for each student |
| 51 | `*` | private tutor | receive a warning when separate lesson arrangements overlap | avoid accepting conflicting arrangements |
| 52 | `*` | private tutor | export all records to a human-readable file | inspect and back up my data outside the app |
| 53 | `**` | private tutor | record and view the hourly rate agreed for a student | check the agreed rate before discussing fees |
| 54 | `**` | private tutor | change or remove a student's recorded hourly rate | keep the agreed rate current |

### Use cases

These use cases describe the intended behavior. The [User Guide](UserGuide.md) lists the commands available now.

The actor is a private tutor and the system is StudentBook. The app is already open unless stated otherwise. The registration and failure paths below follow the current implementation; [decisions to confirm](#decisions-to-confirm) lists differences from the earlier draft.

**MSS** means main success scenario. UC01, UC02, UC03, UC04 and UC06 cover MVP goals. UC05 is a backlog candidate.

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

#### UC04: Change a recurring lesson slot (planned MVP feature)

**Related user stories:** 2, 28, 48.

**Preconditions:** The student has an existing recurring lesson slot.

**MSS**

1. Tutor requests to list students.
2. StudentBook displays students and their weekly lesson times.
3. Tutor requests to change a student's slot, supplying the displayed index and new day and times.
4. StudentBook displays the updated lesson arrangement.

Use case ends.

**Extensions**

* 2a. The student list is empty.
  * Use case ends.
* 3a. The student index is invalid, or the new day or times are invalid.
  * 3a1. StudentBook explains the error without changing the arrangement.
  * 3a2. Tutor corrects and resubmits the request.
  * Use case resumes at step 3.

#### UC05: Complete a guardian follow-up (backlog candidate)

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
5. **Saved data.** After a successful save and normal shutdown, restore every contact and tuition field on restart, including implemented tuition fields. Users should not have to re-enter data.
6. **Offline use.** Manage and save records on the tutor's computer without internet access or an online account.
7. **Data protection.** Keep student and guardian contact details out of external services and diagnostic logs. The tutor's operating-system account and file permissions control access to local files.
8. **File errors.** Report file failures clearly. The intended recovery behavior still needs confirmation; see [decisions to confirm](#decisions-to-confirm). Valid AB3 files must load with new fields absent.

### Glossary

* **StudentBook**: The desktop app for managing student records.
* **Independent private tutor**: A tutor who manages students and lessons without administrative staff.
* **Student record**: One student's contact details, subjects, schooling level, guardian contact, weekly lesson slot and hourly rate. Notes remain a backlog candidate.
* **Guardian contact**: The name and phone number of the adult responsible for a student. The first version allows one optional guardian per student. A recorded guardian needs both fields.
* **Schooling level**: The student's stage of primary or secondary education, such as Primary 5 or Secondary 3.
* **Subject**: An academic subject taught by the tutor to a student, such as Mathematics or English.
* **Recurring lesson slot**: A student's weekly lesson day, start time and end time. The current model stores at most one slot per student.
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

1. Add a unique test student with subject `Combined Science`, using three spaces between the words. The card should show one space.
2. Change it with `edit INDEX s/Math`, then edit the phone without `s/`. The subject should stay `Math`.
3. Restart the app. Both edits should remain.
4. Run `edit INDEX s/` and restart again. The card should show `Subjects: Not recorded`.
5. Add or edit with `s/Math s/Science s/math`. The card should show `Math, Science`, including after restart.
6. Try a blank subject on `add`, `edit INDEX s/Math s/`, and an invalid label such as `s/123`. Each should reject the whole command without changing any record.

### Missing or invalid data files

Close the app and back up the test data before each case.

* **Missing file:** Move `data/addressbook.json` out of the test folder, then restart. Sample records should appear.
* **Invalid file:** Replace the test file with invalid JSON, then restart. An empty list should appear and the log should record the load failure. Do not run commands before restoring the backup; a successful command saves over the file.
* **Old valid file:** Load AB3 data without tuition properties. The contact details should remain and subjects should show as not recorded.
