---
layout: page
title: Developer Guide
---
The architecture and implementation sections describe inherited AB3 behaviour unless marked as planned. See [Student model and scope](StudentModel.md) for the planned core fields, extension types and integration contract.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**
_{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_
* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
* Pranav Pappu used [OpenAI Codex](https://openai.com/codex/) to review and standardise the StudentBook scope, proposed model and related documentation, including the shared Google Doc and user-story Sheet. Codex also implemented the shared tuition model, JSON adapters, edit preservation, subject commands/display and regression tests, followed by independent adversarial review.
--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

Independent private tutors who:
* manage multiple primary- or secondary-school students without administrative support
* keep track of student details, lesson arrangements, and guardian contacts themselves
* regularly retrieve and update this information before and after lessons
* can type quickly and prefer typing over mouse-driven input

**Value proposition**: StudentBook aims to give independent private tutors one place to keep and
quickly retrieve student details, guardian contacts, lesson arrangements, and follow-up notes,
reducing the administrative work involved in managing their students.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| ID | Priority | As a …​                                    | I want to …​                     | So that I can…​                                                        |
|----|----------| ------------------------------------------ | ------------------------------ | ---------------------------------------------------------------------- |
| 1 | `***` | private tutor | add a student record with a name | I can begin tracking each student |
| 2 | `***` | private tutor | view the list of current students | I can see who I currently teach |
| 3 | `***` | private tutor | view a student's subject and schooling level | I can prepare with the relevant academic context |
| 4 | `***` | private tutor | delete a student record | I can remove leavers and correct unusable entries by re-adding them |
| 5 | `***` | private tutor | record a student's schooling level | I can choose material at the right level |
| 6 | `***` | private tutor | record the subject I teach a student | I can remember what I teach each student |
| 7 | `***` | private tutor | record one guardian's name and contact details for a student | I can reach the right adult about lessons |
| 8 | `***` | private tutor | view the guardian name and contact details linked to a student | I can contact a guardian without searching elsewhere |
| 9 | `***` | private tutor | keep my student details and guardian contacts when I close and reopen the app | I can continue my work without re-entering information |
| 10 | `**` | private tutor | edit a student's name | I can correct typos without recreating the record |
| 11 | `**` | private tutor | record a student's school | I can remember the student's school context |
| 12 | `**` | private tutor | update a student's school, schooling level or subjects | I can keep records current as circumstances change |
| 13 | `**` | private tutor | search for a student by a partial name | I can retrieve a record quickly |
| 22 | `**` | private tutor | edit a guardian's contact details | I can keep changed phone numbers and emails current |
| 25 | `**` | private tutor | add a recurring weekly lesson slot to a student | I can remember when the student is taught |
| 28 | `**` | private tutor | edit a lesson slot | I can reflect an agreed schedule change |
| 29 | `**` | private tutor | remove a lesson slot | I can clear a cancelled arrangement |
| 33 | `**` | private tutor | add a dated progress note to a student | I can remember what happened in recent lessons |
| 34 | `**` | private tutor | view a student's dated progress notes in chronological order | I can recall learning needs before the next lesson |
| 35 | `**` | private tutor | record a follow-up item for a guardian | I can remember what I need to communicate |
| 36 | `**` | private tutor | list outstanding guardian follow-up items | I can avoid missing promised updates |
| 40 | `**` | private tutor learning the app | look up command usage and examples | I can complete a task when I do not remember what to type |
| 43 | `**` | private tutor | edit a guardian follow-up item | I can keep the recorded request accurate when arrangements change |
| 44 | `**` | private tutor | mark a guardian follow-up item as complete | I can remove finished work from my outstanding follow-ups |
| 45 | `**` | private tutor learning the app | see what needs correcting when an entry is rejected | I can fix the entry without guessing why it failed |
| 46 | `**` | tutor who communicates directly with a student | record the student’s own phone number or email address | keep their contact details with their record |
| 47 | `**` | tutor who needs to contact a student directly | view the student’s contact details | reach them without searching elsewhere |
| 48 | `**` | tutor checking a student’s lesson arrangements | view that student’s weekly lesson times | confirm when I teach them |
| 49 | `**` | tutor planning my week | view all my lessons arranged by day and time | see which students I will teach each day |
| 53 | `**` | private tutor | record and view an hourly rate | I can keep the agreed SGD rate with the student |
| 54 | `**` | private tutor | change or remove an hourly rate | I can keep fee arrangements current |
| 52 | `*` | private tutor | export all records to a human-readable file | I can inspect and back up my data outside the app |



### Subject implementation

`AddCommandParser` and `EditCommandParser` recognize optional `s/`. `ParserUtil.parseSubject` validates the normalized value. `EditPersonDescriptor` distinguishes an omitted subject from an explicit clear, so editing another field retains the subject. Student cards show its value or `Not recorded`. `JsonAdaptedPerson` stores it as a string or null and treats missing legacy properties as absent.

`SubjectWorkflowTest` exercises add, edit, clear, filtered indexes, invalid input, duplicate identity and save/reload. `TuitionStorageTest` covers preservation of all shared fields. Other tuition command flows remain planned.

### Use cases

For all use cases below, the **System** is **StudentBook** and the **Actor** is an independent private tutor.
The tutor has launched the application unless stated otherwise. All existing AB3 contact fields and commands remain. New tuition fields are optional; their absence does not prevent registration. **MSS** means main success scenario.
These use cases describe planned requirements, rather than features already implemented in the inherited application.
UC01, UC02, UC03 and UC06 cover separate goals in the proposed first version. UC04 and UC05 illustrate later features.
Command syntax will be specified in the User Guide when the corresponding features are implemented.

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
  * 2a1. StudentBook reports the save failure. The new student may remain in memory, but persistence is not confirmed.
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
  * 2a1. StudentBook reports the save failure. The contact may remain in memory, but persistence is not confirmed.
  * Use case ends.

### Non-Functional Requirements

The following are acceptance targets for StudentBook; they do not claim that the current application has been verified against them.

1. **Compatibility:** The application should run on Windows, macOS, and Linux with Java `25`, subject to the bundled JavaFX runtime's platform and architecture support.
2. **Capacity and response time:** With up to 1,000 student records, each with one guardian contact, adding, listing, and deleting a record should update the displayed result within two seconds on a supported computer with at least 4 GB of RAM and local storage. Measure this from command submission to the displayed result, excluding application startup.
3. **Keyboard usability:** After launching the application, a tutor should be able to add, list, and delete student records using the keyboard without requiring mouse interaction.
4. **Error feedback:** Rejected commands should display a readable explanation of the incorrect input and how to correct it. Invalid input should leave existing records unchanged.
5. **Persistence:** Following a successful save and normal shutdown, reopening the application should restore all existing contact fields, subjects, schooling levels, guardian contacts and every implemented extension without manual re-entry.
6. **Offline operation:** Managing and saving student records should work without an internet connection. Records should be stored locally for a single tutor rather than requiring an online account.
7. **Data protection:** Student and guardian contact details should not be sent to external services or included in diagnostic logs. Access to local data files relies on the tutor's operating-system account and file permissions.
8. **Storage failure reporting:** Report save failures clearly. The inherited application may retain an in-memory change after a failed save. Transactional rollback and a blocked startup recovery UI are future improvements, outside the shared-foundation increment. Valid AB3 files must load with new fields absent.

### Glossary

* **StudentBook**: The local desktop application being developed to help independent private tutors manage student information.
* **Independent private tutor**: A tutor who manages their own students and lesson administration without dedicated administrative support.
* **Student record**: An entry representing a student taught by the tutor. Retains name, phone, email, address and tags, with optional subject, schooling level and guardian contact. Lesson slots and rates are selected extensions; notes are deferred.
* **Guardian contact**: The name and contact details of the adult responsible for a student. The core MVP stores zero or one contact per student, with both name and phone required when a contact is present.
* **Schooling level**: The student's stage of primary or secondary education, such as Primary 5 or Secondary 3.
* **Subject**: An academic subject taught by the tutor to a student, such as Mathematics or English.
* **Recurring lesson slot**: A weekly lesson arrangement specifying a day, start time and end time for a student. The first extension supports zero or one slot per student.
* **Hourly rate**: An optional agreed SGD amount per hour, stored using `BigDecimal`. It is a record, not payment processing or a computed lesson fee.
* **Progress note**: A dated note recording a student's learning progress or observations from a lesson.
* **Guardian follow-up item**: A recorded action the tutor needs to carry out for a guardian, such as providing an update. An outstanding item has not yet been marked complete.
* **Displayed index**: The position identifying a student in the currently displayed list. It can change when the list changes and is not a permanent student identifier.
* **Local storage**: Data files on the tutor's computer that retain records between application sessions.
* **Private contact detail**: A student or guardian's phone number, email address, or other contact information intended for the tutor's use rather than public sharing.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
