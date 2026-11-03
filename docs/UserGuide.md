---
layout: page
title: User Guide
---
StudentBook helps private tutors manage student and guardian contacts, recurring lessons, progress notes and follow-ups using typed commands. Records stay on your computer and changes are saved automatically.

## Table of contents

* [Quick start](#quick-start)
* [Main window](#main-window)
* [Features](#features)
  * [Student tracking](#student-tracking)
    * [Add a student: `add`](#add-a-student-add)
    * [Edit a student: `edit`](#edit-a-student-edit)
    * [List all active students: `list`](#list-all-active-students-list)
    * [Sort students by rate: `sort`](#sort-students-by-rate-sort)
    * [View a student's details: `view`](#view-a-students-details-view)
    * [Find students by name: `find`](#find-students-by-name-find)
    * [Find students by filter criteria: `find`](#find-students-by-filter-criteria-find)
    * [Delete a student: `delete`](#delete-a-student-delete)
    * [Delete all students: `clear`](#delete-all-students-clear)
  * [Archiving students](#archiving-students)
    * [Archive or restore a student: `archive`, `unarchive`](#archive-or-restore-a-student-archive-unarchive)
    * [List archived students: `list-archive`](#list-archived-students-list-archive)
  * [Lesson tracking](#lesson-tracking)
    * [Add a lesson: `lesson-add`](#add-a-lesson-lesson-add)
    * [Edit a lesson: `lesson-edit`](#edit-a-lesson-lesson-edit)
    * [Delete a lesson: `lesson-delete`](#delete-a-lesson-lesson-delete)
    * [Show weekly lessons: `week`](#show-weekly-lessons-week)
  * [Guardian tracking](#guardian-tracking)
    * [Add a guardian: `guardian-add`](#add-a-guardian-guardian-add)
    * [Edit a guardian: `guardian-edit`](#edit-a-guardian-guardian-edit)
    * [Delete a guardian: `guardian-delete`](#delete-a-guardian-guardian-delete)
  * [Notes](#notes)
    * [Add a progress note: `note-add`](#add-a-progress-note-note-add)
    * [Edit a progress note: `note-edit`](#edit-a-progress-note-note-edit)
    * [Delete a progress note: `note-delete`](#delete-a-progress-note-note-delete)
  * [Follow-ups](#follow-ups)
    * [Add a guardian follow-up: `followup-add`](#add-a-guardian-follow-up-followup-add)
    * [Edit a guardian follow-up: `followup-edit`](#edit-a-guardian-follow-up-followup-edit)
    * [Complete a guardian follow-up: `followup-done`](#complete-a-guardian-follow-up-followup-done)
    * [List students with outstanding follow-ups: `followups`](#list-students-with-outstanding-follow-ups-followups)
  * [Miscellaneous](#miscellaneous)
    * [View help: `help`](#view-help-help)
    * [Exit: `exit`](#exit-exit)
* [Saving and transferring records](#saving-and-transferring-records)
* [Known issues](#known-issues)
* [Command Input Formats](#command-input-formats)
  * [Reading Command Formats](#reading-command-formats)
  * [Field Specifications](#field-specifications)
  * [Command Syntax](#command-syntax)

## Quick start

1. Install Java **25**. Mac users can follow the [Java installation guide](https://se-education.org/guides/tutorials/javaInstallationMac.html). Check your version in a terminal:
   ```shell
   java -version
   ```

1. Download `studentbook.jar` from the [StudentBook releases](https://github.com/AY2627S1-CS2103T-F13-4/tp/releases).
1. Put the JAR in an empty folder where you have permission to create files. Open a terminal in that folder and run:
   ```shell
   java -jar "studentbook.jar"
   ```
   Use the actual filename if it differs. If double-clicking the JAR does nothing, start it this way to see the error.

1. The first launch opens with sample data. Type `list` and press Enter.
1. Add a student:

   ```text
   add name/Alex Tan phone/91234567 email/alex@example.com address/123 Clementi Road
   ```

   Alex appears in **Other** because he has no schooling level. Replace `STUDENT_INDEX` below with his displayed number:

   ```text
   lesson-add STUDENT_INDEX subject/Math slot/Wed 1.00pm 5.00pm rate/45.50
   ```

   Type `exit` to close the app.

## Main window

[![StudentBook main window](images/Ui.png)](images/Ui.png)

* **This week** shows your active students' lessons on a calendar (`week`). **Students** shows your students (`list`), and **Archives** shows archived students (`list-archive`). `find` searches whichever of these two lists is open.
* A lesson marked `#3.1` is student `3`'s lesson `1`. Edit it with `lesson-edit 3 1 ...`.
* To expand a student's full details below their row, click it or type `view STUDENT_INDEX`. In narrow windows, the Subjects and Guardian columns are hidden; the expanded details still show them.
* Usage errors show the command format and an example below the box. Press **Tab** to copy an available example into the box.

| Key | Action |
|---|---|
| Up / Down | Previous commands |
| Esc | Clear the box; if already empty, close student details |
| Alt+Up / Alt+Down | Scroll the calendar, or move between students |
| Alt+Left / Alt+Right | Scroll the calendar sideways when the command box is empty |
| Page Up / Page Down | Scroll the calendar or student list |
| Ctrl+L (Cmd+L on macOS) | Move the cursor to the command box |
| F1 | Help |

## Features

See [Command Input Formats](#command-input-formats) for command syntax and field rules.

### Student tracking

#### Add a student: `add`

Adds a student.

Format: `add name/NAME phone/PHONE email/EMAIL address/ADDRESS [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]`

Examples:
* `add name/Alex Tan phone/91234567 email/alex@example.com address/123 Clementi Road tag/weekday sch/Clementi Primary School level/Primary 5`
* `add name/Mei Lim phone/92345678 email/mei@example.com address/45 Dover Road`

<div markdown="block" class="alert alert-primary">

:bulb: **Tips:**<br>
* A new student has no lessons or guardian. Add them with [`lesson-add`](#add-a-lesson-lesson-add) and [`guardian-add`](#add-a-guardian-guardian-add) respectively.
* Students cannot share names with one another (case-sensitive). If two students share identical names, consider using their full names or adding an identifier in the name field, e.g. `Alex Tan Wei Ming (Senior)`

</div>

#### Edit a student: `edit`

Changes a student's details.

Format: `edit STUDENT_INDEX [name/NAME] [phone/PHONE] [email/EMAIL] [address/ADDRESS] [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]`

* Edits the student at the specified [`STUDENT_INDEX`](#reading-command-formats).
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Empty parameters for optional fields (e.g. `tag/`, refer to [adding students](#add-a-student-add)) will clear the field


Examples:
* `edit 1 phone/91234567 email/alex.tan@example.com` changes the first displayed student's phone and email.
* `edit 2 name/Mei Ling tag/` changes the second displayed student's name and clears their tags.
* `edit 1 sch/Clementi Primary School` changes the first displayed student's school.
* `edit 1 level/Primary 6` changes the first displayed student's schooling level.

<div markdown="block" class="alert alert-primary">

:bulb: **Tips:**<br>
* To edit a student's lessons or guardians, use [`lesson-edit`](#edit-a-lesson-lesson-edit) and [`guardian-edit`](#edit-a-guardian-guardian-edit) respectively.
</div>

#### List all active students: `list`

Shows all active students in StudentBook.

Format: `list`

* This command clears active searches, filters and sorts.
* Ordering is done based on the following criteria:
  1. Schooling stage (Primary/Secondary/JC/Other, in that order)
  1. Schooling level (in order of increasing seniority, then unrecognised levels)
  1. Name (alphabetical)

#### Sort students by rate: `sort`

Orders students by hourly rate within each group.

Format: `sort rate asc` or `sort rate desc`

Ordering is changed to the following criteria:
1. Schooling stage
2. Hourly rate
3. Schooling level
4. Name (alphabetical)

* `asc`/`desc` sort the rates in ascending/descending order respectively
* Students with several lessons are ranked by their lowest rate in `asc` and highest rate in `desc`
* Students with no lessons always come last

#### View a student's details: `view`

Displays a student's full details.

Format: `view STUDENT_INDEX`

#### Find students by name: `find`

Finds students whose names contain any of the given words.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive.
* Only full words match.

Examples:
* `find lim` finds `Chloe Lim` and `Ethan Lim`.
* `find isaac ryan` finds `Isaac Ng` and `Ryan Goh Jun Jie`.

#### Find students by filter criteria: `find`

Finds students by name, guardian, school, level, lesson subject or lesson day.

Format: `find [name/TEXT] [guardian/TEXT] [sch/TEXT] [level/TEXT] [subject/TEXT] [day/DAY]`

* The search filter will persist until `list`, `list-archive` or `find` commands overwrite it
* Prefix arguments match substrings and are case-insensitive.
* At least one of the optional fields must be provided.
* Only entries that have partial/complete matches to all supplied arguments are listed.
* For lesson-related criteria, the same lesson must match all given criteria
* `guardian/` searches the guardian's name or phone.
* `level/` searches equivalent stages, e.g. `prim` matches `P5`, `Secondary` matches `Sec 3`

Examples:
* `find level/prim` matches students of level `Primary 5` and `P5`.
* `find day/Mon` matches students with a lesson on Monday.
* `find level/Secondary subject/math day/wednesday` matches secondary students with a Math lesson on Wednesday.

#### Delete a student: `delete`

Deletes a student from StudentBook.

Format: `delete STUDENT_INDEX`

#### Delete all students: `clear`

Delete all student records from StudentBook or the archive, depending on the active tab.

Format: `clear`

Format: `delete STUDENT_INDEX`

* In **Students** or **This Week** tabs, `clear` deletes **all active and archived students**.
* In **Archives** (after `list-archive`), it deletes **only archived students**, keeping active students.

<div markdown="span" class="alert alert-warning">

:warning: `clear` cannot be undone. Copy `data/studentbook.json` first if you might want the records back.

</div>

### Archiving students

#### Archive or restore a student: `archive`, `unarchive`

Archive or unarchive a student in StudentBook.

Format: `archive STUDENT_INDEX`, `unarchive STUDENT_INDEX`

* Archived students do not appear in the active `list` or weekly calendar
* To restore students, use `list-archive` to view the student in the archive, then `unarchive` the student using the corresponding index number

#### List archived students: `list-archive`

Shows all archived students in StudentBook.

Format: `list-archive`

### Lesson Tracking

#### Add a lesson: `lesson-add`

Add a lesson for a given student.

Format: `lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]`

* A student cannot have two identical lessons.
* A lesson that overlaps another lesson is still saved, with a [clash warning](#troubleshooting). Lessons are checked against all your active students, including any hidden by a search; an archived student's lessons are checked against other archived students.

Examples:
* `lesson-add 3 subject/English slot/Mon 10.00am 12.00pm rate/40`
* `lesson-add 1 subject/Math slot/Fri 2.00pm 4.00pm rate/40 mode/physical location/Home`
* `lesson-add 1 subject/Math slot/Sun 2.00pm 4.00pm rate/40 mode/virtual location/Zoom`

<div markdown="span" class="alert alert-warning">

:warning: If two lessons overlap on the same day and time, it is saved with a warning. Use `lesson-edit` to resolve a clash.

</div>

#### Edit a lesson: `lesson-edit`

Edit a lesson for a given student.

Format: `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]`

* Targets the student at the specified [`STUDENT_INDEX`](#reading-command-formats).
* Edits the lesson at the specified [`LESSON_INDEX`](#reading-command-formats).
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Empty parameters for optional fields (e.g. `mode/`, refer to [adding lessons](#add-a-lesson-lesson-add)) will clear the field

Examples:
* `lesson-edit 1 2 subject/Mathematics`
* `lesson-edit 2 1 mode/physical location/Home`
* `lesson-edit 3 1 mode/ location/`

#### Delete a lesson: `lesson-delete`

Deletes a lesson for a given student

Format: `lesson-delete STUDENT_INDEX LESSON_INDEX`

#### Show weekly lessons: `week`

Display the weekly lesson schedule.

Format: `week`

* Hover over a lesson for its full details and actual times.
* Short lessons use compact cards
* Lessons shorter than 15 minutes are drawn as 15-minute blocks for readability

### Guardian Tracking

#### Add a guardian: `guardian-add`

Add a guardian for a given student.

Format: `guardian-add STUDENT_INDEX name/NAME phone/PHONE`

* Each student has at most one guardian. To change one, use [`guardian-edit`](#edit-a-guardian-guardian-edit).
* Names and phones follow the same rules as in [`add`](#add-a-student-add).

Examples:
* `guardian-add 8 name/Jane Doe phone/81234567` adds a guardian to `Sarah Wong`, the only sample student without one.

#### Edit a guardian: `guardian-edit`

Edit a guardian for a given student.

Format: `guardian-edit STUDENT_INDEX [name/NAME] [phone/PHONE]`

* Edits the guardian of the student at the specified [`STUDENT_INDEX`](#reading-command-formats).
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.

Examples:
* `guardian-edit 1 name/Jane Doe`
* `guardian-edit 1 phone/81234567`

#### Delete a guardian: `guardian-delete`

Removes a student's guardian.

Format: `guardian-delete STUDENT_INDEX`

Format: `guardian-delete STUDENT_INDEX`

### Notes

#### Add a progress note: `note-add`

Adds a dated progress note to a student in StudentBook.

Format: `note-add STUDENT_INDEX date/YYYY-MM-DD text/TEXT`

* Put `date/` before `text/`; everything after `text/`, including words resembling prefixes, is note text.
* Date should be in `YYYY-MM-DD` format, such as `2026-10-15`. It cannot be earlier than `2000-01-01` or later than today.
* Duplicate notes will not be added

Examples:
* `note-add 1 date/2026-10-15 text/Finished the fractions worksheet; revise decimals next`
* `note-add 1 date/2026-10-08 text/Struggled with word problems` then goes before the first note and becomes note 1.

#### Edit a progress note: `note-edit`

Changes a note's date, text or both.

Format: `note-edit STUDENT_INDEX NOTE_INDEX [date/YYYY-MM-DD] [text/TEXT]`

* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Dates, text and field ordering follow the same rules as [`note-add`](#add-a-progress-note-note-add).


Examples:
* `note-edit 4 2 text/Chain rule much better; start kinematics next`
* `note-edit 4 2 date/2026-10-15`

#### Delete a progress note: `note-delete`

Deletes one of a student's notes.

Format: `note-delete STUDENT_INDEX NOTE_INDEX`

* The result shows the deleted note's text. Later notes move up by one.

Examples:
* `note-delete 4 1`

### Follow-ups

#### Add a guardian follow-up: `followup-add`

Adds something you still need to do for a student's guardian, such as sending a progress report.

Format: `followup-add STUDENT_INDEX text/TEXT`

* Duplicate follow-ups (case-insensitive) are not allowed.
* A student does not need a guardian to have follow-ups, and `guardian-delete` keeps them.

Examples:
* `followup-add 1 text/Send the term 4 progress report`

#### Edit a guardian follow-up: `followup-edit`

Replaces the text of a follow-up.

Format: `followup-edit STUDENT_INDEX FOLLOW_UP_INDEX text/TEXT`

* `FOLLOW_UP_INDEX` is the follow-up's number in the student's details.
* `TEXT` follows the same rules as in [`followup-add`](#add-a-guardian-follow-up-followup-add).

Examples:
* `followup-edit 2 1 text/Confirm the make-up lesson on Saturday`

#### Complete a guardian follow-up: `followup-done`

Marks a follow-up as done, which removes it.

Format: `followup-done STUDENT_INDEX FOLLOW_UP_INDEX`

* The result shows the removed follow-up's text. Later follow-ups move up by one.

Examples:
* `followup-done 2 1`

#### List students with outstanding follow-ups: `followups`

Shows only active students with at least one follow-up.

Format: `followups`

Shows only active students who have at least one outstanding follow-up. Archived students are left out and keep their follow-ups. Type `list` to see every student again.

<div markdown="block" class="alert alert-primary">

:bulb: **Tips:**<br>
* After `followups`, a student whose last follow-up you complete leaves the list, so the students after them move up a number. Check the number before the next command.

</div>

### Miscellaneous

#### View help: `help`

Opens this guide in your web browser.

* Alternate hotkey: **F1**
* You can also click **All commands** or choose **Help → Help** from the menu.
* The location of your data file is shown in the app's footer.

#### Exit: `exit`

Closes the app.

## Saving and transferring records

StudentBook saves every change to `data/studentbook.json` in the folder you start it from. The app's footer shows where the file is.

To move to another computer, close StudentBook, then copy the file into the new app folder as `data/studentbook.json`. Back up any file already there first.

<div markdown="span" class="alert alert-warning">

:warning: Back up `data/studentbook.json` before editing it by hand. If StudentBook cannot read it, it starts with no students. See [Troubleshooting](#troubleshooting) to get your records back.

</div>

## Troubleshooting

Click a problem to see the fix. Bold text is the message StudentBook shows.

<script>window.addEventListener('beforeprint', () => document.querySelectorAll('details').forEach(d => { d.open = true; }));</script>

<details class="trouble" markdown="1">
<summary><strong>The student index provided is invalid</strong> (or lesson, note or follow-up index)</summary>

Use the number shown on screen; lesson, note and follow-up numbers are in the student's details. Numbers change after searches and sorts.

</details>
<details class="trouble" markdown="1">
<summary>A student is missing from <strong>Students</strong></summary>

A search is on (`find`, `followups` or a level filter). Type `list`. Still missing? Type `list-archive`, since they may be archived.

</details>
<details class="trouble" markdown="1">
<summary><strong>An active student already has this name</strong></summary>

Type `list` to find them. If this is a different student, add something to the name, e.g. `Alex Tan Sec 2`.

</details>
<details class="trouble" markdown="1">
<summary><strong>An archived student already has this name</strong></summary>

Type `list-archive` and `unarchive` them, or add something to the new name.

</details>
<details class="trouble" markdown="1">
<summary><strong>This student already has a guardian</strong></summary>

Change the guardian with `guardian-edit`, or run `guardian-delete` and then `guardian-add`.

</details>
<details class="trouble" markdown="1">
<summary><strong>This student has no guardian</strong></summary>

Use `guardian-add` first.

</details>
<details class="trouble" markdown="1">
<summary><strong>Lesson saved - scheduling clash detected</strong>, or a <strong>Warning: Lesson ... overlaps</strong> line after <code>unarchive</code></summary>

The change is saved anyway. The warning lists the lessons it overlaps. Move one with `lesson-edit STUDENT_INDEX LESSON_INDEX slot/...` or remove it with `lesson-delete`. A lesson that starts when another ends does not clash.

</details>
<details class="trouble" markdown="1">
<summary><strong>Could not save data</strong></summary>

Nothing changed. Check that you can write to the `data` folder, then run the command again.

</details>
<details class="trouble" markdown="1">
<summary><strong>Could not read</strong> the data file, and StudentBook started with no students</summary>

Close StudentBook before making any change, since the next change saves over `studentbook.json`. Your old records are in `studentbook-backup-<date>-<time>.json` beside it. Fix that file, rename it to `studentbook.json` and restart. If no backup could be made, StudentBook closes without touching the file.

</details>
<details class="trouble" markdown="1">
<summary>The Subjects or Guardian column, or some level filters, are missing</summary>

The window is too narrow to show them. Widen the window, or use `view STUDENT_INDEX` to see a student's full details.

</details>
<details class="trouble" markdown="1">
<summary>The window opens off-screen</summary>

Close StudentBook, delete `preferences.json` in the app folder and restart. This resets the window position.

</details>
<details class="trouble" markdown="1">
<summary>The user guide does not open</summary>

Check your default browser and internet connection, or open the [user guide](https://ay2627s1-cs2103t-f13-4.github.io/tp/UserGuide.html) yourself.

</details>

## Command Input Formats

### Reading Command Formats

* `UPPER_CASE` words are user-defined parameters.
  E.g. `name/NAME` &rarr; `name/John Doe`.
* `[square brackets]` mark optional fields.
  E.g. `name/NAME [tag/TAG]` &rarr; `name/John Doe tag/friend` or `name/John Doe`.
* `…`​ means a field can be repeated.
  E.g. `[tag/TAG]…​` &rarr; ` ` or `tag/friend` or `tag/friend tag/family`.
* Parameters can be in any order.
  E.g. `name/NAME phone/PHONE` &rarr; `name/John Doe phone/87654321` or `phone/87654321 name/John Doe`
* `INDEX` fields e.g. `STUDENT_INDEX` refer to the number beside that specific field in the display
* Exception: in note commands, `text/` comes last. See [`note-add`](#add-a-progress-note-note-add).
* Commands with no parameters (`help`, `list`, `clear`, `exit`, etc.) ignore extra arguments.
  E.g. `help 123` &rarr; `help`

When copying commands from a PDF, check spaces around line breaks.

### Field Specifications

| Field | Input rules |
|---|---|
| `name/` | Starts with a letter/number; permits Unicode letters, accents, numbers, spaces, apostrophes, hyphens and periods. Slashes are allowed only in `s/o` and `d/o`. Extra spaces collapse; case is preserved. Student names must be unique (case-sensitive); use a full name or identifier, e.g. `Alex Tan 2`. Guardian names follow the same character rules. |
| `phone/` | 7–15 digits, optional leading `+`; spaces/hyphens may separate digits, e.g. `+65 9123 4567`. Applies to students and guardians. |
| `day/` in `find` | Three-letter or full weekday name, e.g. `Mon` or `Monday`. |
| `subject/` | 1–40 characters after trimming/collapsing spaces and tabs. Starts with an ASCII letter/digit and includes a letter; only ASCII letters, digits, spaces and `'-.()&` are allowed. |
| `slot/` | `DAY START END`, e.g. `Wed 1.00pm 5.00pm`. Three-letter day; minute-precise times from `12.00am` to `11.59pm`; start before end on the same day. |
| `rate/` | Non-negative SGD amount per hour, at most two decimal places, e.g. `45.50`. |
| `mode/` | `physical` or `virtual`, ignoring case. |
| `location/` | 1–100 characters, e.g. an address or meeting link. |
| `date/` | Real `YYYY-MM-DD` date from `2000-01-01` through today, e.g. `2026-10-15`. |

### Command Syntax

| Task | Format |
|---|---|
| Add | `add name/NAME phone/PHONE email/EMAIL address/ADDRESS [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]` |
| Edit | `edit STUDENT_INDEX [name/NAME] [phone/PHONE] [email/EMAIL] [address/ADDRESS] [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]` |
| Clear school | `edit STUDENT_INDEX sch/` |
| Clear schooling level | `edit STUDENT_INDEX level/` |
| Find | `find KEYWORD [MORE_KEYWORDS]` or `find [name/TEXT] [guardian/TEXT] [sch/TEXT] [level/TEXT] [subject/TEXT] [day/DAY]` |
| List | `list` |
| Sort by rate | `sort rate asc` or `sort rate desc` |
| List archived students | `list-archive` |
| Archive | `archive STUDENT_INDEX` |
| Restore from archive | `unarchive STUDENT_INDEX` |
| This week's lessons | `week` |
| A student's details | `view STUDENT_INDEX` |
| Add lesson | `lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]` |
| Edit lesson | `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]` |
| Delete lesson | `lesson-delete STUDENT_INDEX LESSON_INDEX` |
| Add guardian | `guardian-add STUDENT_INDEX name/NAME phone/PHONE` |
| Edit guardian | `guardian-edit STUDENT_INDEX [name/NAME] [phone/PHONE]` |
| Delete guardian | `guardian-delete STUDENT_INDEX` |
| Add note | `note-add STUDENT_INDEX date/YYYY-MM-DD text/TEXT` |
| Edit note | `note-edit STUDENT_INDEX NOTE_INDEX [date/YYYY-MM-DD] [text/TEXT]` |
| Delete note | `note-delete STUDENT_INDEX NOTE_INDEX` |
| Add follow-up | `followup-add STUDENT_INDEX text/TEXT` |
| Edit follow-up | `followup-edit STUDENT_INDEX FOLLOW_UP_INDEX text/TEXT` |
| Complete follow-up | `followup-done STUDENT_INDEX FOLLOW_UP_INDEX` |
| List outstanding follow-ups | `followups` |
| Delete | `delete STUDENT_INDEX` |
| Delete all | `clear` |
| Help | `help` |
| Exit | `exit` |
