---
layout: page
title: User Guide
---
# StudentBook
StudentBook helps private tutors manage student and guardian contacts, recurring lessons, progress notes and follow-ups using typed commands. Records stay on your computer and changes are saved automatically.

## Table of Contents

* [Quick Start](#quick-start)
* [Main Window](#main-window)
* [Features](#features)
  * [Reading Command Formats](#reading-command-formats)
  * [Student Tracking](#student-tracking)
    * [Add a student: `add`](#add-a-student-add)
    * [Edit a student: `edit`](#edit-a-student-edit)
    * [List all students: `list`](#list-all-students-list)
    * [Sort students by rate: `sort`](#sort-students-by-rate-sort)
    * [View a student's details: `view`](#view-a-students-details-view)
    * [Find students by name: `find`](#find-students-by-name-find)
    * [Find students by filter criteria: `find`](#find-students-by-filter-criteria-find)
    * [Delete a student: `delete`](#delete-a-student-delete)
    * [Delete all students: `clear`](#delete-all-students-clear)
  * [Archiving Students](#archiving-students)
    * [Archive or restore a student: `archive`, `unarchive`](#archive-or-restore-a-student-archive-unarchive)
    * [List archived students: `list-archive`](#list-archived-students-list-archive)
  * [Lesson Tracking](#lesson-tracking)
    * [Lesson fields and numbering](#lesson-fields-and-numbering)
    * [Add a lesson: `lesson-add`](#add-a-lesson-lesson-add)
    * [Edit a lesson: `lesson-edit`](#edit-a-lesson-lesson-edit)
    * [Delete a lesson: `lesson-delete`](#delete-a-lesson-lesson-delete)
    * [Show weekly lessons: `week`](#show-weekly-lessons-week)
    * [Lesson clash warnings](#lesson-clash-warnings)
  * [Guardian Tracking](#guardian-tracking)
    * [Add a guardian: `guardian-add`](#add-a-guardian-guardian-add)
    * [Edit a guardian: `guardian-edit`](#edit-a-guardian-guardian-edit)
    * [Delete a guardian: `guardian-delete`](#delete-a-guardian-guardian-delete)
  * [Progress Notes](#progress-notes)
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
* [Command summary](#command-summary)

## Quick Start

1. Install Java **25**. Mac users can follow the [Java installation guide](https://se-education.org/guides/tutorials/javaInstallationMac.html). Check your version in a terminal:
   ```shell
   java -version
   ```

1. Download `studentbook.jar` from the [StudentBook releases](https://github.com/AY2627S1-CS2103T-F13-4/tp/releases).
1. Put the JAR in an empty folder where you have permission to create files. Open a terminal in that folder and run:
   ```shell
   java -jar "studentbook.jar"
   ```
   Use the actual filename if it differs. Launch from the terminal so you can check errors if double-clicking does not work.

1. The first launch opens with sample data. Type `list` and press Enter.
1. Add a student:

   ```text
   add name/Alex Tan phone/91234567 email/alex@example.com address/123 Clementi Road
   ```

   Alex appears in **Other** because he has no schooling level. Replace `INDEX` below with his displayed number:

   ```text
   lesson-add INDEX subject/Math slot/Wed 1.00pm 5.00pm rate/45.50
   ```

   Type `exit` to close the app.

## Main Window

[![StudentBook main window](images/Ui.png)](images/Ui.png)

Type commands in the box at the top and press **Enter**. Select the screenshot to view it at full size.

* **This week** shows your active students' lessons on a calendar (`week`). **Students** shows your students (`list`), and **Archives** shows archived students (`list-archive`). `find` searches whichever of these two lists is open.
* A lesson marked `#3.1` is student `3`'s lesson `1`. Edit it with `lesson-edit 3 1 ...`.
* To expand a student's full details below their row, click it or type `view INDEX`. In narrow windows, the Subjects and Guardian columns are hidden; the expanded details still show them.
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

### Reading Command Formats

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* `UPPER_CASE` words are user-defined parameters.<br>
  E.g. `name/NAME` &rarr; `name/John Doe`.

* `[square brackets]` mark optional fields.<br>
  E.g. `name/NAME [tag/TAG]` &rarr; `name/John Doe tag/friend` or `name/John Doe`.

* `…`​ means a field can be repeated.<br>
  E.g. `[tag/TAG]…​` &rarr; ` ` or `tag/friend` or `tag/friend tag/family`.

* Parameters can be in any order.<br>
  E.g. `name/NAME phone/PHONE_NUMBER` &rarr; `name/John Doe phone/87654321` or `phone/87654321 name/John Doe`

* `INDEX` is the number beside a student in the current list display.

* Exception: in note commands, `text/` comes last. See [`note-add`](#add-a-progress-note-note-add).

* Commands with no parameters (`help`, `list`, `clear`, `exit`, etc.) ignore extra arguments.
  E.g. `help 123` &rarr; `help`
</div>

<div markdown="span" class="alert alert-warning">

**:warning: When copying commands from the PDF version of this document, spaces around line breaks may be omitted.**

</div>

### Student Tracking

#### Add a student: `add`

Adds a student to StudentBook

After adding, StudentBook switches to **Students**, clears the active search and opens the new student's details.

Format: `add name/NAME phone/PHONE_NUMBER email/EMAIL address/ADDRESS [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]`

Examples:
* `add name/Alex Tan phone/91234567 email/alex@example.com address/123 Clementi Road tag/weekday sch/Clementi Primary School level/Primary 5`
* `add name/Mei Lim phone/92345678 email/mei@example.com address/45 Dover Road`

<div markdown="block" class="alert alert-primary">

:bulb: **Tips:**<br>
* A new student has no lessons or guardians. Add them with [`lesson-add`](#add-a-lesson-lesson-add) and [`guardian-add`](#add-a-guardian-guardian-add) respectively.
* Phone numbers have 7 to 15 digits. You can add a `+` country code and separate digits with spaces or hyphens, e.g. `+65 9123 4567`, which is saved as `+6591234567`. Guardian phones follow the same rules.
* Each student needs a unique name. StudentBook rejects a name that is already used by an active or archived student.
* Extra spaces in a name are removed, so `Alex   Tan` is saved as `Alex Tan`.
* Capitalisation is kept as typed, so `Alex Tan` and `alex tan` count as different names. Avoid relying on this, as such students are easy to mix up.
* Names must start with a letter or number. They can contain Unicode letters and accents, numbers, spaces, apostrophes, hyphens and periods, e.g. `Mary-Ann O'Brien` or `Zoë Ng`. Slashes are supported in the relationship terms `s/o` and `d/o`, e.g. `Ravi s/o Kumar`. Guardian names follow the same rules.
* If two of your students share a name, add something that tells them apart, e.g. `Alex Tan Wei Ming`, `Alex Tan Sec 2` or `Alex Tan 2`.

</div>

#### Edit a student: `edit`

Edits an existing student in StudentBook

Format: `edit INDEX [name/NAME] [phone/PHONE_NUMBER] [email/EMAIL] [address/ADDRESS] [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]`

* Edits the student at the specified [`INDEX`](#reading-command-formats).
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Empty parameters for optional fields (e.g. `tag/`) will clear the field

Examples:
* `edit 1 phone/91234567 email/alex.tan@example.com` changes the first displayed student's phone and email.
* `edit 2 name/Mei Ling tag/` changes the second displayed student's name and clears their tags.
* `edit 1 sch/Clementi Primary School` changes the first displayed student's school.
* `edit 1 level/Primary 6` changes the first displayed student's schooling level.

<div markdown="block" class="alert alert-primary">

:bulb: **Tips:**<br>
* To edit a student's lessons or guardians, use [`lesson-edit`](#edit-a-lesson-lesson-edit) and [`guardian-edit`](#edit-a-guardian-guardian-edit) respectively.
* A new name must follow the same rules as in [`add`](#add-a-student-add): it cannot match another active or archived student's name.

</div>

#### List all students: `list`

Shows all active students in StudentBook, grouped by school stage (Primary, Secondary, JC, Other), and selects this list for searches and indexed commands.

Format: `list`

Within each group, students are ordered by level (P1 before P2), then alphabetically by name. For ordering, `P5` and `Primary 5` are equivalent. Levels StudentBook does not recognise come after the numbered ones, and students with no level come last.

This clears the active search, the calendar's lesson filter and any `sort`, so students return to this order.

#### Sort students by rate: `sort`

Orders students by hourly rate **within each group**. The Primary, Secondary, JC and Other groups stay where they are, and only the students inside each group are reordered. Numbers shown on screen still match the `INDEX` used by commands.

Format: `sort rate asc` or `sort rate desc`

* `asc` puts the lowest rate first and `desc` puts the highest rate first.
* A student with several lessons is ranked by their lowest rate for `asc` and their highest rate for `desc`.
* Students with no lessons always come last in their group.
* While a sort is on, the **Rate** column is headed **Min rate** or **Max rate** and shows the rate the sort uses, instead of the next lesson's rate.
* Students with the same rate are ordered by level, then name, as in `list`.
* The sort stays in place when you edit lessons or use `find`. Type `list` to go back to the usual order by level, then name.

Example: `sort rate desc`

#### View a student's details: `view`

Displays the student at `INDEX` and their details.

Format: `view INDEX`

#### Find students by name: `find`

Find students whose names contain any of the given keywords

Format: `find KEYWORD [MORE_KEYWORDS]`
* The search is case-insensitive.
* Only full words match.

Examples:
* `find John` matches `John Doe`, but not `Johnny`.
* `find alex david` matches names containing `Alex` or `David`.

#### Find students by filter criteria: `find`

Find students by name, guardian, school, level, lesson subject and/or lesson day.

Format: `find [name/TEXT] [guardian/TEXT] [sch/TEXT] [level/TEXT] [subject/TEXT] [day/DAY]`

* The search is case-insensitive.
* At least one of the optional fields must be provided.
* Text criteria match substrings. All supplied criteria must match; `guardian/` searches the guardian's name or phone.
* Students without a level are excluded from the level filter
* When multiple lesson-related criteria are specified, the same lesson must match all criteria
* Day may be specified with three-letter or full names

Searches persist through edits, deletions, archiving and restoration. Students enter or leave the results as their details change, and displayed indices are renumbered. The active and archived lists keep separate searches; `week` uses the active student and lesson filters. Adding a student clears active filters to show the new student. Use `list` or `list-archive` to reset that list's search, or `find` to replace it.

Examples:
* `find level/prim` matches `Primary 5`.
* `find day/Mon` matches students with a lesson on Monday.
* `find level/Secondary subject/math day/wednesday` matches secondary students with a Math lesson on Wednesday.

For `find subject/math day/wed`, `week` shows only matching Wednesday maths lessons; expanded student details still show all of each student's lessons.

#### Delete a student: `delete`

Deletes a student from StudentBook.

Format: `delete INDEX`

#### Delete all students: `clear`

Deletes records permanently, regardless of the current search filter. There is no undo.

Format: `clear`

* In **Students** or **This week**, `clear` deletes **all active and archived students**.
* In **Archives** (after `list-archive`), it deletes **only archived students**, keeping active students.

### Archiving Students

#### Archive or restore a student: `archive`, `unarchive`

`archive INDEX` moves an active student to the archived list, keeping their details, lessons, notes and follow-ups. Archived students do not appear in the active list or weekly calendar.

To restore a student, run `list-archive`, then `unarchive INDEX` using the number shown in that tab. The student moves back to the active list; the archived tab stays open. Run `list` to see the restored student.

#### List archived students: `list-archive`

Shows all archived students in a separate **Archives** tab with the same layout as **Students**. Searches and commands using `INDEX` now use this list. Type `list` to return to active students.

Format: `list-archive`

### Lesson Tracking

#### Lesson fields and numbering

Open a student's details with `view INDEX` to see their numbered lessons. `STUDENT_INDEX` is the student's displayed number; `LESSON_INDEX` is the lesson's number within those details. Deleting a lesson renumbers the lessons after it.

These field rules apply to `lesson-add` and `lesson-edit`:

| Field | Rules |
|---|---|
| `subject/` | 1–40 characters; starts with a letter/digit and contains at least one letter |
| `slot/` | `DAY START END`, e.g. `Wed 1.00pm 5.00pm`. Use a three-letter day and minute-precise times. Start must be before end on the same day; overnight lessons are unsupported |
| `rate/` | Non-negative SGD amount per hour with at most two decimal places, e.g. `45.50` |
| `mode/` | `physical` or `virtual`, ignoring case |
| `location/` | 1–100 characters, such as an address or meeting link |

#### Add a lesson: `lesson-add`

Add a lesson for a given student.

Format: `lesson-add INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]`

Examples:
* `lesson-add 3 subject/English slot/Mon 10.00am 12.00pm rate/40`
* `lesson-add 1 subject/Math slot/Fri 4.00pm 6.00pm rate/40 mode/physical location/Home`
* `lesson-add 1 subject/Math slot/Fri 4.00pm 6.00pm rate/40 mode/virtual location/Zoom`

#### Edit a lesson: `lesson-edit`

Edit a lesson for a given student.

Format: `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]`

* Targets the student at the specified [`STUDENT_INDEX`](#reading-command-formats).
* Edits the lesson at the specified [`LESSON_INDEX`](#lesson-fields-and-numbering).
* Provide at least one field; omitted fields keep their existing values.
* Empty `mode/` or `location/` clears that optional field.

Examples:
* `lesson-edit 1 3 subject/Mathematics`
* `lesson-edit 2 1 mode/physical location/Home`
* `lesson-edit 3 1 mode/ location/`

#### Delete a lesson: `lesson-delete`

Deletes a lesson for a given student

Format: `lesson-delete STUDENT_INDEX LESSON_INDEX`

#### Show weekly lessons: `week`

Displays lessons for active students, retaining the active list's search and lesson filters. Running `week` from the archived tab returns to active students.

Format: `week`

Hover over a lesson for its full details and actual times. Short lessons use compact cards; lessons shorter than 15 minutes are drawn as 15-minute blocks for readability. This does not change their saved times or clash checks.

#### Lesson clash warnings

If a valid lesson change or student restoration causes a clash, it is saved with a warning. Checks include students hidden by searches, within the same active or archived list. Lessons meeting exactly at an endpoint do not clash. Use `lesson-edit` to resolve a clash.

### Guardian Tracking

#### Add a guardian: `guardian-add`

Add a guardian for a given student.

Format: `guardian-add INDEX name/NAME phone/PHONE`

Examples:
* `guardian-add 1 name/Jane Doe phone/81234567`

#### Edit a guardian: `guardian-edit`

Edit a guardian for a given student.

Format: `guardian-edit INDEX [name/NAME] [phone/PHONE]`

* Edits the guardian of the student at the specified [`INDEX`](#reading-command-formats).
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.

Examples:
* `guardian-edit 1 name/Jane Doe` changes the first displayed student's guardian name.
* `guardian-edit 1 phone/81234567` changes the first displayed student's guardian phone.

#### Delete a guardian: `guardian-delete`

Delete a guardian for a given student.

Format: `guardian-delete INDEX`

### Progress Notes

#### Add a progress note: `note-add`

Adds a dated progress note to a student, such as what you covered in a lesson or what to revise next.

Format: `note-add STUDENT_INDEX date/DATE text/TEXT`

* Both `date/` and `text/` are required.
* Put `date/` before `text/`; everything after `text/`, including words resembling prefixes, is note text.
* `DATE` is a real date in `YYYY-MM-DD` format, such as `2026-10-15`. It cannot be earlier than `2000-01-01` or later than today.
* `TEXT` is 1 to 500 characters on one line. Spaces at the start and end are removed.
* Notes are listed oldest first with their number, date and text.
* Adding a note with the same date and text as an existing note of that student is rejected.

Examples:
* `note-add 1 date/2026-10-15 text/Finished the fractions worksheet; revise decimals next`
* `note-add 1 date/2026-10-08 text/Struggled with word problems` is placed before the first note and becomes note 1.

#### Edit a progress note: `note-edit`

Changes the date, text or both for an existing progress note.

Format: `note-edit STUDENT_INDEX NOTE_INDEX [date/DATE] [text/TEXT]`

* `NOTE_INDEX` is the note's number in the student's details before the edit.
* Provide at least one field. Omitted fields keep their existing values; fields cannot be cleared.
* Dates, text and field ordering follow the same rules as [`note-add`](#add-a-progress-note-note-add).
* An edit cannot create another note with the same date and text for that student.
* Changing a date can change note numbers. Check the updated details before your next command.

Examples:
* `note-edit 1 2 text/Finished fractions; revise decimals next` changes only the text.
* `note-edit 1 2 date/2026-10-08` corrects only the date.

#### Delete a progress note: `note-delete`

Deletes one progress note from a student.

Format: `note-delete STUDENT_INDEX NOTE_INDEX`

* `NOTE_INDEX` is the note's number in the student's details. Later notes move up by one.
* The message shows the deleted note's text, so you can check it was the right one.

Example: `note-delete 1 1`

### Follow-ups

#### Add a guardian follow-up: `followup-add`

Adds something you still need to do for a student's guardian, such as sending a progress report or confirming a make-up lesson.

Format: `followup-add STUDENT_INDEX text/TEXT`

* `TEXT` is 1 to 200 characters on one line. Spaces at the start and end are removed, and repeated spaces between words become one.
* The follow-up is added at the end of the student's list. Follow-ups appear in the student's details, under the guardian, with their numbers.
* A follow-up that reads the same as another one of that student (ignoring letter case) is rejected.
* A student does not need a guardian to have follow-ups, and `guardian-delete` keeps them.

Example: `followup-add 1 text/Send the term 4 progress report`

#### Edit a guardian follow-up: `followup-edit`

Replaces the text of a follow-up. Its number stays the same.

Format: `followup-edit STUDENT_INDEX FOLLOW_UP_INDEX text/TEXT`

* `FOLLOW_UP_INDEX` is the follow-up's number in the student's details.
* The same rules for `TEXT` as in `followup-add` apply, including rejecting duplicates.

Example: `followup-edit 1 2 text/Confirm the make-up lesson on Saturday`

#### Complete a guardian follow-up: `followup-done`

Marks a follow-up as done by removing it from the student's list.

Format: `followup-done STUDENT_INDEX FOLLOW_UP_INDEX`

* The message shows the removed follow-up's text. Later follow-ups move up by one.

Example: `followup-done 1 1`

#### List students with outstanding follow-ups: `followups`

Shows only active students who have at least one outstanding follow-up. Archived students are left out and keep their follow-ups. Type `list` to see every student again.

Format: `followups`

<div markdown="block" class="alert alert-primary">

:bulb: **Tips:**<br>
* After `followups`, a student whose last follow-up you complete leaves the list, so the students after them move up a number. Check the number before the next command.
* Notes and follow-ups are saved automatically. Older data files without them still load, with none recorded.

</div>

### Miscellaneous

#### View help: `help`

Opens this user guide in your default web browser. An internet connection is required to load the guide.

Format: `help`

* Alternate hotkey: **F1**
* You can also click **All commands** or choose **Help → Help** from the menu.
* The location of your data file is shown in the app's footer.

#### Exit: `exit`

Closes the app.

Format: `exit`

## Saving and transferring records

StudentBook saves record changes automatically to `data/studentbook.json`, in the folder you start it from.

If saving fails, your records stay unchanged. Fix the reported problem and retry the command.
Viewing and searching do not save.

To move to another computer, close StudentBook, then copy that file into the new app folder as `data/studentbook.json` before launching there. Back up any existing destination file first.

<div markdown="block" class="alert alert-warning">

:warning: Back up the file before editing it by hand. If StudentBook cannot load it, it makes a backup beside it and starts empty. The next record change replaces the original. To recover, close the app, fix the backup and restore it as `data/studentbook.json`. If a backup cannot be made, the app closes without changing your records.

</div>

## Known issues

* **Window opens off-screen:** Close the app and delete `preferences.json`, then restart it. This resets the saved window position.
* **User guide does not open:** Check your default browser and internet connection. You can open the [user guide](https://ay2627s1-cs2103t-f13-4.github.io/tp/UserGuide.html) manually.

## Command summary

| Task | Format |
|---|---|
| Add | `add name/NAME phone/PHONE_NUMBER email/EMAIL address/ADDRESS [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]` |
| Edit | `edit INDEX [name/NAME] [phone/PHONE_NUMBER] [email/EMAIL] [address/ADDRESS] [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]` |
| Clear school | `edit INDEX sch/` |
| Clear schooling level | `edit INDEX level/` |
| Find | `find KEYWORD [MORE_KEYWORDS]` or `find [name/TEXT] [guardian/TEXT] [sch/TEXT] [level/TEXT] [subject/TEXT] [day/DAY]` |
| List | `list` |
| Sort by rate | `sort rate asc` or `sort rate desc` |
| List archived students | `list-archive` |
| Archive | `archive INDEX` |
| Restore from archive | `unarchive INDEX` |
| This week's lessons | `week` |
| A student's details | `view INDEX` |
| Add lesson | `lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]` |
| Edit lesson | `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]` |
| Delete lesson | `lesson-delete STUDENT_INDEX LESSON_INDEX` |
| Add guardian | `guardian-add INDEX name/NAME phone/PHONE` |
| Edit guardian | `guardian-edit INDEX [name/NAME] [phone/PHONE]` |
| Delete guardian | `guardian-delete INDEX` |
| Add note | `note-add STUDENT_INDEX date/DATE text/TEXT` |
| Edit note | `note-edit STUDENT_INDEX NOTE_INDEX [date/DATE] [text/TEXT]` |
| Delete note | `note-delete STUDENT_INDEX NOTE_INDEX` |
| Add follow-up | `followup-add STUDENT_INDEX text/TEXT` |
| Edit follow-up | `followup-edit STUDENT_INDEX FOLLOW_UP_INDEX text/TEXT` |
| Complete follow-up | `followup-done STUDENT_INDEX FOLLOW_UP_INDEX` |
| List outstanding follow-ups | `followups` |
| Delete | `delete INDEX` |
| Delete all | `clear` |
| Help | `help` |
| Exit | `exit` |
