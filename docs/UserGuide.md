---
layout: page
title: User Guide
---
# StudentBook
StudentBook is a desktop contact book for private tutors. Type commands to manage student records.

This development version supports contact details, schools, schooling levels, guardian contacts and lessons. Each student can have any number of lessons, and each lesson has a subject, a weekly time slot and an hourly rate. See the [Developer Guide](DeveloperGuide.md#product-scope) for the plan and open decisions.

## Table of Contents
{:toc}

## Quick Start
1. Install Java `25`. Mac users should follow the [course Java installation guide](https://se-education.org/guides/tutorials/javaInstallationMac.html).
1. Build the development version from the [team repository](https://github.com/AY2627S1-CS2103T-F13-4/tp).
   ```shell
   git clone https://github.com/AY2627S1-CS2103T-F13-4/tp.git studentbook
   cd studentbook
   ./gradlew shadowJar
   ```
   
   On Windows, use `gradlew.bat shadowJar` for the last command. The JAR is `build/libs/studentbook.jar`.

1. Copy the JAR into the folder where you want to keep your records. Open a terminal in that folder and run:
   ```shell
   java -jar studentbook.jar
   ```

1. Type `list` in the command box and press Enter. Then try:

   ```text
   add name/Alex Tan phone/91234567 email/alex@example.com address/123 Clementi Road
   lesson-add 1 subject/Math slot/Wed 1.00pm 5.00pm rate/45.50
   ```

   The first command adds Alex, who appears at the bottom of the list. Replace `1` in the second command with Alex's number in the list. It gives that student a Math lesson on Wednesdays from 1pm to 5pm at SGD 45.50 per hour. Type `exit` to close the app.

## Main Window

![StudentBook main window](images/Ui.png)

You type every command in the **command box** at the top. The app has two screens:

| Screen | Shows | Open it with |
|---|---|---|
| **This week** (opens first) | A calendar of every student's lessons | `week`, or the **This week** tab |
| **Students** | A table of students | `list` or `find`, or the **Students** tab |

### This week

* Shows Monday to Sunday of the current week. Today's date is circled, and a red line marks the current time.
* Shows 9.00am to 8.00pm, and stretches to fit lessons outside those hours. It opens scrolled to the current time.
* Each lesson shows the student, the time, then its number, level and subject, e.g. `#3.1 · Sec 3 · A-Math`.
* Lessons are coloured by school stage (key at the top right). Lessons at the same time sit side by side, and that day widens to fit them.

To change a lesson you see on the calendar:

1. Read its number, e.g. `#3.1` means student `3`, lesson `1`.
1. Type `lesson-edit 3 1` followed by the fields to change.

To see a lesson's rate, mode and location, point at it, or type `view` with its student's number (e.g. `view 3`).

### Students

* One row per student, with the columns **#**, **Name**, **Subjects**, **Next lesson**, **Rate** and **Guardian**.
* **Next lesson** is the student's next lesson in the week, with `+2 more` if they have others. **Rate** is that lesson's rate.
* Use the number in the **#** column as `INDEX`.
* The count beside the title shows how many students there are, or how many match after `find`, e.g. `Showing 1 of 8`.

**Student details** open under a student's row and show their school and contact details, guardian, follow-ups, lessons, tags and notes. Each lesson shows its number, subject, day and time and rate, plus its mode and location if set. Use a lesson's number as `LESSON_INDEX`.

| To | Do this |
|---|---|
| Open a student's details | Click their row, type `view INDEX`, or press **Alt+Down** / **Alt+Up** to step through students. A search that leaves one student (e.g. `find hafiz`) opens them too. |
| Close the details | Press **Esc** in an empty command box |

### Tabs and header

* **This week** and **Students** tabs run `week` and `list`; each tab shows its command.
* After them, one tab per schooling level in use runs `find level/LEVEL` (point at it to see the command).
* **All commands** at the right opens help.
* **+ Add student** in the header puts the `add` format in the command box for you to fill in.

### Results and errors

The row under the command box shows the result of your last command.

* **Success:** a green tick. After a one-line success, it also shows your last few commands; click one to reuse it, or press **Up** to step through your earlier commands from this session.
* **Error:** a red `!` on pink. Your command stays in the box, in red, so you can fix it.
* **Empty list or calendar:** the app suggests the next step, such as an example `add` or `lesson-add` command, or `list` when a search matches nothing.

To fix a command with the wrong format:

1. Read what the command does, its **Format** and an **Example**, shown under the error.
1. Press **Tab** to put the Example in the command box (or click a Format or Example line to copy it).
1. Edit it and press **Enter**.

Long messages take up to 40% of the window before they scroll.

### Colours

| Colour | Means |
|---|---|
| Amber | Primary level, e.g. `P5`, `Primary 5` |
| Blue | Secondary level, e.g. `Sec 3`, `Secondary 4 Express` |
| Violet | JC level, e.g. `JC1` |
| Grey | Any other level (e.g. `Poly Y1`), and tags |
| Green / teal | Lesson mode `Physical` / `Virtual`, in a student's details |

Level colours appear on the initials badge and the level tabs.

### Small windows

* Narrower than 900px: the level tabs, and the **Subjects** and **Guardian** columns, are hidden. Level tabs that do not fit are left out.
* Shorter than 560px: the StudentBook name and the data file path at the bottom are hidden, to leave room for your students.

### Keyboard shortcuts

The command box is focused when the app starts and stays focused (outlined in blue), so you can use the whole app from the keyboard. Clicking elsewhere or closing help leaves the cursor in the box. After a successful command, the box clears.

| Key | Action |
|---|---|
| Enter | Run the command |
| Up / Down | Show earlier commands from this session. Down past the newest brings back what you were typing. |
| Esc | Clear the command box. In an empty box, close a student's details. Also closes the help window. |
| Alt+Down / Alt+Up | On **This week**, scroll the calendar. On **Students**, open the next or previous student's details. (On a Mac, Alt is the Option key.) |
| Tab | When the result shows an Example (e.g. after a format error), put that Example in the command box |
| Page Up / Page Down | Also scroll, if your keyboard has these keys (on a Mac laptop: Fn+Up / Fn+Down) |
| F1 | Open help, as the `help` command does (on a Mac laptop you may need Fn+F1) |

Every shortcut is optional: each one has a command that does the same, such as `week`, `list`, `view INDEX` and `help`.

## Features

### Reading Command Formats
<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* `UPPER_CASE` words are user-defined parameters.<br>
  E.g. `name/NAME` &rarr; `name/John Doe`.

* `[square brackets]` mark optional fields.<br>
  E.g. `name/NAME [tag/TAG]` &rarr; `name/John Doe tag/friend` or `name/John Doe`.

* `…`​ means a field can be repeated.<br>
  E.g. `[t/TAG]…​` &rarr; ` ` or `t/friend` or `t/friend t/family`. 

* Parameters can be in any order.<br>
  E.g. `name/NAME p/PHONE_NUMBER` &rarr; `name/John Doe phone/87654321` or `phone/87654321 name/John Doe`

* `INDEX` is the number beside a student in the current list display.

* Commands with no parameters (`help`, `list`, `clear`, `exit`, etc.) ignore extra arguments.
  E.g. `help 123` &rarr; `help`
</div>

<div markdown="span" class="alert alert-warning">

**:warning: When copying commands from the PDF version of this document, spaces around line breaks may be omitted.**

</div>

### Add a student: `add`

Adds a student to StudentBook

Format: `add name/NAME phone/PHONE_NUMBER email/EMAIL address/ADDRESS [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]`

Examples:
* `add name/Alex Tan phone/91234567 email/alex@example.com address/123 Clementi Road tag/weekday sch/Clementi Primary School level/Primary 5`
* `add name/Mei Lim phone/92345678 email/mei@example.com address/45 Dover Road`

<div markdown="block" class="alert alert-primary">

:bulb: **Tips:**<br>
* A new student has no lessons or guardians. Add them with [`lesson-add`](#manage-a-students-lessons) and [`guardian-add`](#manage-a-students-guardian-contact) respectively.
* The added student cannot share a name with an existing one (case-sensitive).

</div>


### List all students: `list`

Shows a list of all students in StudentBook, grouped by schooling level.

Format: `list`

### Edit a student: `edit`

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
* To edit a student's lessons or guardians, use [`lesson-edit`](#manage-a-students-lessons) and [`guardian-edit`](#manage-a-students-guardian-contact) respectively.

</div>

### Show weekly lessons: `week`

Displays all lessons for students in the current list display.

Format: `week`

### View a student's details: `view`

Displays the student at `INDEX` and their details.

Format: `view INDEX`

### Find students by name: `find`

Find students whose names contain any of the given keywords

Format: `find KEYWORD [MORE_KEYWORDS]`
* The search is case-insensitive.
* Only full words match.

Examples:
* `find John` matches `John Doe`, but not `Johnny`.
* `find alex david` matches names containing `Alex` or `David`.

### Find students by filter criteria: `find`

Find students by name, guardian, school, level, lesson subject and/or lesson day.

Format: `find [name/TEXT] [guardian/TEXT] [sch/TEXT] [level/TEXT] [subject/TEXT] [day/DAY]`

* The search is case-insensitive.
* At least one of the optional fields must be provided.
* Partial matches are allowed.
* Students without a level are excluded from the level filter
* When multiple lesson-related criteria are specified, the same lesson must match all criteria
* Day may be specified with three-letter or full names

Examples:
* `find level/prim` matches `Primary 5`.
* `find day/Mon` matches students with a lesson on Monday.
* `find level/Secondary subject/math day/wednesday` matches secondary students with a Math lesson on Wednesday.

The result of `find subject/math day/wed`: students with a Math lesson on Wednesday.

![Result of find subject/math day/wed](images/findResult.png)

### Delete a student: `delete`

Deletes a student from StudentBook.

Format: `delete INDEX`

### Delete all students: `clear`

Deletes all students from StudentBook.

Format: `clear`

### View help: `help`

Displays link to this guide and location of data file.

Format: `help`

* Alternate hotkey: **F1**
* Press **Enter** to copy the link
* Press **Esc** to close the window and return to the command box

![Help window](images/helpMessage.png)

### Exit: `exit`

Closes the app.

Format: `exit`

### Add a lesson: `lesson-add`

Add a lesson for a given student.

Format: `lesson-add INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]`

Examples:
* `lesson-add 3 subject/English slot/Mon 10.00am 12.00pm rate/40`
* `lesson-add 1 subject/Math slot/Fri 4.00pm 6.00pm rate/40 mode/physical location/Home`
* `lesson-add 1 subject/Math slot/Fri 4.00pm 6.00pm rate/40 mode/virtual location/Zoom`


### Edit a lesson: `lesson-edit`

Edit a lesson for a given student.

Format: `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]`

* Targets the student at the specified [`STUDENT_INDEX`](#reading-command-formats).
* Edits the lesson at the specified [`LESSON_INDEX`](#reading-command-formats)
* Existing values will be updated to the input values
* Empty parameters for optional fields (e.g. `mode/`) will clear the field

Examples:
* `lesson-edit 1 3 subject/Mathematics`
* `lesson-edit 2 1 mode/physical location/Home`
* `lesson-edit 3 1 mode/ location/`

### Delete a lesson: `lesson-delete`

Deletes a lesson for a given student

Format: `lesson-delete STUDENT_INDEX LESSON_INDEX`

Field rules:
* **Subject** (`subject/`)
  * 1-40 characters
  * Start with a letter/digit, include at least 1 letter
* **Lesson Slot** (`slot/`)
  * Format: `DAY START END`
  * `DAY`: Three-letter day, e.g. `Mon`, `Sun`
  * `START`/`END`: Minute-precise time, e.g. `10.30am`, `1.00pm`
  * `START` must be before `END`
* **Hourly Rate** (`rate/`)
  * Numeric value in SGD per hour
  * Non-negative, to nearest cent, e.g. `45.50`
* **Mode** (`mode/`)
  * `physical` or `virtual` only (case insensitive)
* **Location** (`location/`)
  * 1-100 characters

### Add a guardian: `guardian-add`

Add a guardian for a given student.

Format: `guardian-add INDEX name/NAME phone/PHONE`

Examples:
* `guardian-add 1 name/Jane Doe phone/81234567`

### Edit a guardian: `guardian-edit`

Edit a guardian for a given student.

Format: `guardian-edit INDEX [name/NAME] [phone/PHONE]`

* Edits the guardian of the student at the specified [`INDEX`](#reading-command-formats).
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.

Examples:
* `edit 1 name/Jane Doe`
* `edit 1 name/81234567`

### Delete a guardian: `guardian-delete`

Delete a guardian for a given student.

Format: `guardian-delete INDEX`

## Saving and transferring records

StudentBook saves automatically after every successful command, to `data/studentbook.json` in the folder where you start the app. Everything you enter is saved, including lessons, guardians, notes, follow-ups and archived students.

<div markdown="span" class="alert alert-warning">
:warning: If saving fails, the app shows an error. The change may still appear on screen but is not saved.
</div>

To move your records to another computer:

1. Close StudentBook on both computers.
1. On the new computer, go to the folder where you start StudentBook and create a `data` folder there if it does not exist.
1. Copy `data/studentbook.json` into that `data` folder.
1. Start StudentBook there.

### Edit the data file manually

1. Close StudentBook.
1. Back up `data/studentbook.json`.
1. Edit the file, keeping its structure and valid values.
1. Start StudentBook and check your records.

<div markdown="block" class="alert alert-warning">

:warning: **If the file is invalid, the app starts with an empty list.** The invalid file stays until a command saves over it, so restore your backup before running any command.

</div>

What the file holds:

* Each student has an optional `school`, and a `lessons` array. Each lesson has a `subject`, a `weeklyLessonSlot` and an `hourlyRate`, plus an optional `mode` (`PHYSICAL` or `VIRTUAL`) and `location`.
* Files without `school`, `mode` or `location` still load.
* Older files that store `subject`, `subjects`, `weeklyLessonSlot` or `hourlyRate` directly on a student still load, but those values are ignored and lost on the next save. Re-enter them with `lesson-add`, or move them into a `lessons` array first.

## Known issues

* **Window opens off-screen:** Close the app and delete `preferences.json`, then restart it. This resets the saved window position.
* **Help window stays minimized:** Restore the existing help window manually. Running `help` again does not restore it.

### Lesson clash warnings

When you add or edit a lesson that overlaps another lesson on the same day, StudentBook **still saves it** and shows an amber warning with an exclamation icon, headed "Lesson saved - scheduling clash detected". The warning says how many lessons clash, then lists each on its own line: the student's name, their lesson number, subject and time.

* All students are checked, including those hidden by a search, and the student's own other lessons.
* Back-to-back lessons (one ends when the next starts) do not clash.
* Every lesson edit re-checks, so even a rate-only edit warns again about an existing clash.
* Deleting a lesson never warns.
* The warning stays until your next command.

To fix a clash:

1. Note the student's name and lesson number in the warning. The lesson number belongs to that student's own list.
1. `find` that student, then use their `#` number.
1. Move one of the lessons with `lesson-edit STUDENT_INDEX LESSON_INDEX slot/...`.

## Command summary

| Task | Format |
|---|---|
| Add | `add name/NAME phone/PHONE_NUMBER email/EMAIL address/ADDRESS [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]` |
| Edit | `edit INDEX [name/NAME] [phone/PHONE_NUMBER] [email/EMAIL] [address/ADDRESS] [tag/TAG]... [sch/SCHOOL] [level/SCHOOLING_LEVEL]` |
| Clear school | `edit INDEX sch/` |
| Clear schooling level | `edit INDEX level/` |
| Find | `find KEYWORD [MORE_KEYWORDS]` or `find [name/TEXT] [guardian/TEXT] [sch/TEXT] [level/TEXT] [subject/TEXT] [day/DAY]` |
| List | `list` |
| This week's lessons | `week` |
| A student's details | `view INDEX` |
| Add lesson | `lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]` |
| Edit lesson | `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]` |
| Delete lesson | `lesson-delete STUDENT_INDEX LESSON_INDEX` |
| Add guardian | `guardian-add INDEX name/NAME phone/PHONE` |
| Edit guardian | `guardian-edit INDEX [name/NAME] [phone/PHONE]` |
| Delete guardian | `guardian-delete INDEX` |
| Add note | `note-add STUDENT_INDEX date/DATE text/TEXT` |
| Delete note | `note-delete STUDENT_INDEX NOTE_INDEX` |
| Add follow-up | `followup-add STUDENT_INDEX text/TEXT` |
| Edit follow-up | `followup-edit STUDENT_INDEX FOLLOW_UP_INDEX text/TEXT` |
| Complete follow-up | `followup-done STUDENT_INDEX FOLLOW_UP_INDEX` |
| List outstanding follow-ups | `followups` |
| Delete | `delete INDEX` |
| Delete all | `clear` |
| Help | `help` |
| Exit | `exit` |
