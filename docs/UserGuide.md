---
layout: page
title: User Guide
---
# StudentBook
StudentBook is a desktop contact book for private tutors. Type commands to manage student records.

This development version supports contact details, schooling levels, guardian contacts and lessons. Each student can have any number of lessons, and each lesson has a subject, a weekly time slot and an hourly rate. See the [Developer Guide](DeveloperGuide.md#product-scope) for the plan and open decisions.

## Table of Contents
* [Quick Start](#quick-start)
* [Main Window](#main-window)
* [Features](#features)
  * [Reading Command Formats](#reading-command-formats)

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

The command box sits at the top of the window, under the StudentBook name, since you use the app by typing. The app opens on **This week**, a calendar of every student's lessons. `list` and `find` show the **Students** table instead, and `week` brings the calendar back.

* **This week:** The calendar shows Monday to Sunday of the current week, with today's date circled and a red line at the current time. It always shows 9.00am to 8.00pm, and stretches to fit any lesson outside those hours. Each lesson shows the student, the time, and then its number, level and subject, such as `#3.1 · Sec 3 · A-Math`. The number means student `3`, lesson `1`, so `lesson-edit 3 1` changes that lesson. Lessons are coloured by school stage, as shown in the key at the top right. Lessons at the same time sit side by side, and that day widens to fit them. To see a lesson's rate, mode and location, type `view` with its student's number (e.g. `view 3` for `#3.1`), or point at the lesson. The calendar opens scrolled to the current time.
* **Tabs:** The row under the command box has **This week**, which runs `week`, and **Students**, which runs `list`; each tab shows the command it runs. After them, each schooling level in use runs `find level/LEVEL` (point at one to see the command). **All commands** at the right opens help. The levels are hidden in windows narrower than 900px, and any that do not fit are left out. In windows shorter than 560px, the StudentBook name and the data file path at the bottom are hidden to leave room for your students.
* **Header:** The page header shows how many students there are. **+ Add student** puts the `add` format in the command box for you to fill in.
* **Student list:** The list is a table with one row per student and the columns **#**, **Name**, **Subjects**, **Next lesson**, **Rate** and **Guardian**. **Next lesson** is the lesson that comes next in the week, with `+2 more` when the student has others; **Rate** is that lesson's rate. Use the number in the **#** column as `INDEX`. In narrow windows the Subjects and Guardian columns are hidden.
* **Student details:** On the **Students** table, a student's details open under their row: every lesson with its number, subject, day and time, rate, mode and location, then the phone, email, address, guardian and tags. Details open when a search leaves one student (e.g. `find hafiz`), when you press **Alt+Down** or **Alt+Up** to step through students, or when you click a row. Press **Esc** in an empty command box to close them. Use a lesson's number in the details as `LESSON_INDEX`.
* **Colours:** The initials badge and the level dots under the command box are amber for primary (`P5`, `Primary 5`), blue for secondary (`Sec 3`, `Secondary 4 Express`) and violet for JC (`JC1`). Levels written another way, such as `Poly Y1`, stay grey. In a student's details, a lesson's mode shows in green for `Physical` and teal for `Virtual`. Tags are grey.
* **Count:** The count beside the title shows how many students there are. After `find`, it shows how many match, such as `Showing 1 of 8`.
* **Empty list:** When the list is empty, the app suggests the next step. With no students, it shows an example `add` command you can type as shown. When a search matches no one, it suggests `list`. The This week screen does the same: with no lessons it shows an example `lesson-add` command, and when a search hides every lesson it suggests `list`.
* **Result:** The row under the command box shows the result of your last command. After a one-line success, it also shows your last few commands; click one to put it back in the command box, or press Up to step through all your earlier commands. A green tick marks a success. A red `!` on a pink background marks an error, such as an invalid index or format. When you get a command's format wrong, the row shows what the command does, its **Format** and an **Example**, so you can fix the command without opening help. Press **Tab** to put the Example in the command box, then edit it; you can also click a Format or Example line to copy it. Long messages take up to 40% of the window, so they can be read without scrolling.
* **Command box:** It is focused when the app starts and stays focused, so you can use the whole app from the keyboard: clicking elsewhere and closing the help window leave the cursor in the box, which is outlined in blue. Press Enter to run a command. After a successful command, the box clears. After an error, your text stays, shown in red, so you can fix it.

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

Format: `add name/NAME phone/PHONE_NUMBER email/EMAIL address/ADDRESS [tag/TAG]... [level/SCHOOLING_LEVEL]`

Examples:
* `add name/Alex Tan phone/91234567 email/alex@example.com address/123 Clementi Road tag/weekday level/Primary 5`
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

Format: `edit INDEX [name/NAME] [phone/PHONE_NUMBER] [email/EMAIL] [address/ADDRESS] [tag/TAG]... [level/SCHOOLING_LEVEL]`

* Edits the student at the specified [`INDEX`](#reading-command-formats).
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Empty parameters for optional fields (e.g. `tag/`) will clear the field

Examples:
* `edit 1 phone/91234567 email/alex.tan@example.com` changes the first displayed student's phone and email.
* `edit 2 name/Mei Ling tag/` changes the second displayed student's name and clears their tags.
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

Find students by level, lesson subject and/or lesson day.

Format: `find [level/TEXT] [subject/TEXT] [day/DAY]`

* The search is case-insensitive.
* At least one of the optional fields must be provided.
* Partial matches are allowed.
* Students without a level are excluded from the level filter
* When both subject and day are specified, the same lesson must match both criteria
* Day may be specified with three-letter or full names

Examples:
* `find level/prim` matches `Primary 5`.
* `find day/Mon` matches students with a lesson on Monday.
* `find level/Secondary subject/math day/wednesday` matches secondary students with a Math lesson on Wednesday.

![Name search results](images/findAlexDavidResult.png)

### Manage a student's lessons

Each student can have zero or more lessons. A lesson has a subject, a weekly time slot and an hourly rate, which are always present, and an optional mode (physical or virtual) and location. A student cannot have two identical lessons, but lessons with overlapping time slots are allowed. Lesson commands use the student's positive `STUDENT_INDEX` in the currently displayed list, including search results. They preserve all other student fields.

* **Create:** `lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]`
  Subject, slot and rate are required; mode and location are optional. The new lesson is added to the end of the student's lessons.
* **Read:** The **This week** calendar shows every lesson of the listed students (after a `find`, only the matching lessons) with its `#STUDENT_INDEX.LESSON_INDEX` number. In the **Students** table, the row shows the student's next lesson. Open the student's details (e.g. `find` their name, or **Alt+Down** on the table) to see every lesson: its number, subject, day and time (e.g. `Tue 4.00pm-5.30pm`) and rate (e.g. `SGD 60/h`), with the mode (`Physical` or `Virtual`) and location underneath when recorded. Use the lesson number as `LESSON_INDEX`.
* **Update:** `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]`
  Supply at least one field. Omitted fields stay unchanged. To remove a recorded mode or location, give its prefix with nothing after it: `mode/` clears the mode and `location/` clears the location (see the examples below).
* **Delete:** `lesson-delete STUDENT_INDEX LESSON_INDEX`
  Removes only that lesson. Later lessons move up by one position.

Field rules:

* **Subject** (`subject/`): 1 to 40 characters after space cleanup. Start with a letter or digit and include at least one letter. Use English letters, digits, spaces and these symbols: `'`, `-`, `.`, `(`, `)`. Leading and trailing spaces are removed and repeated spaces become one. Subjects are compared ignoring letter case, so `Math` and `math` are the same subject.
* **Lesson slot** (`slot/`): `DAY START END` on a single day, such as `Wed 1.00pm 5.00pm`. Use a three-letter day (`Mon` to `Sun`), and times like `1.00pm` or `10.30am` with minute precision. Days and `am`/`pm` are not case-sensitive, so `wed 1.00PM 5.00PM` also works. The end must be later than the start on the same day; overnight lessons are not supported. The details show a short day, for example `Wed 1.00pm-5.00pm`.
* **Hourly rate** (`rate/`): an amount in SGD per hour. It must be non-negative and have at most two decimal places; `45`, `45.5` and `45.50` are valid. Currency symbols, negative amounts and scientific notation are invalid.
* **Mode** (`mode/`, optional): `physical` or `virtual`, not case-sensitive. The lesson shows `Physical` or `Virtual`.
* **Location** (`location/`, optional): 1 to 100 characters, such as an address, room or meeting link. Repeated spaces become one, and locations are compared ignoring letter case. It must not contain a word of letters followed by `/` (such as `rate/`), which is read as a prefix; `Blk 123/4` and `https://zoom.us/j/1` are fine.
* **Clearing mode and location:** only `lesson-edit` can clear them, by leaving the value empty, for example `lesson-edit 1 2 location/` or `lesson-edit 1 2 mode/ location/`. `lesson-add` rejects an empty `mode/` or `location/`, and `lesson-delete` accepts neither.

Errors:

* Adding a lesson, or editing one so that it becomes identical to another of the same student's lessons, is rejected as a duplicate. Mode and location are part of a lesson's identity, so the same subject, slot and rate at a different location or mode is not a duplicate.
* A student index or lesson index that does not exist is rejected.
* Empty values (except an empty `mode/` or `location/` in `lesson-edit`, which clears the field), repeated prefixes, unknown prefixes and invalid values, such as a mode other than `physical` or `virtual`, are rejected. Any error leaves the record unchanged.

For example, after `list`:

1. `lesson-add 1 subject/Math slot/Wed 1.00pm 5.00pm rate/45.50`
2. `lesson-add 1 subject/English slot/Mon 4.00pm 5.00pm rate/40 mode/virtual location/https://zoom.us/j/123` adds an online lesson with a meeting link.
3. `lesson-edit 1 2 rate/50` changes the English lesson's rate.
4. `lesson-edit 1 2 mode/physical location/Clementi Library` changes it to an in-person lesson at a new place.
5. `lesson-edit 1 2 location/` removes the location but keeps the mode. `lesson-edit 1 2 mode/ location/` removes both.
6. `lesson-delete 1 1` removes the Math lesson. English is now lesson 1.

Lesson commands keep an active search filter. Lessons are saved automatically.

### Manage a student's guardian contact

Each student can have zero or one guardian, with a name and phone number. Guardian commands use the student's
positive `INDEX` in the currently displayed list, including search results. They preserve all other student fields.

* **Create:** `guardian-add INDEX name/NAME phone/PHONE`
  Both fields are required. If a guardian already exists, use `guardian-edit` instead.
* **Read:** The Guardian column shows the guardian's name, or a grey `Not set` when none exists.
  The student's details show the guardian's phone too.
* **Update:** `guardian-edit INDEX [name/NAME] [phone/PHONE]`
  Supply at least one field. Omitted fields stay unchanged. Empty values and repeated prefixes are rejected.
* **Delete:** `guardian-delete INDEX`
  Removes only the guardian. Editing or deleting a missing guardian reports an error.

Names must contain only alphanumeric characters and spaces and must not be blank. Phone numbers must contain
at least three digits and no other characters, following the existing contact validation rules. Leading and trailing
spaces are trimmed. Guardians may share the same details across different students.

For example, after `list`:

1. `guardian-add 1 name/Janet Tan phone/91234567`
2. `guardian-edit 1 phone/87654321`
3. `guardian-delete 1`

Guardian commands keep an active search filter. Errors leave existing records unchanged. Guardian details are
saved automatically. Older data files without a guardian still load with no guardian recorded.

### Delete a student: `delete`

Format: `delete INDEX`

Deletes the student at that position in the current list, including their attached details. There is no confirmation or undo.

* `list` then `delete 2` deletes the second student in the full list.
* `find Alex` then `delete 1` deletes the first search result.

### Delete all students: `clear`

Format: `clear`

Deletes every student record. There is no confirmation or undo.

### View help: `help`

Format: `help`

Opens a window with a link to this guide and the location of your data file. Press **Enter** to copy the link, and **Esc** to close the window and return to the command box. **F1** opens it too.

![Help window](images/helpMessage.png)

### Exit: `exit`

Format: `exit`

Closes the app.

## Saving and transferring records

The app saves automatically after each successfully executed command. Schooling levels, guardian contacts and lessons are saved with the contact details. Each student's lessons are stored as a `lessons` array, where every lesson has a `subject`, a `weeklyLessonSlot` and an `hourlyRate`, plus an optional `mode` (`PHYSICAL` or `VIRTUAL`) and `location`. Files without these two fields still load.

Older files that stored a student's `subject`, `subjects`, `weeklyLessonSlot` or `hourlyRate` directly on the student still load, but those values are ignored and are lost the next time the app saves. Re-enter them with `lesson-add` or copy them into a `lessons` array first.

If saving fails, the app reports an error. The change may still appear on screen without being saved to disk.

Records are stored in `data/studentbook.json` under the folder where you start the app.

To transfer records, close the app on both computers. Copy the data file to the corresponding location on the new computer, then start the app there.

### Edit the data file manually

Close the app and back up the JSON file before editing it. Keep the existing file structure and valid field values.

If the file is invalid, the app starts with an empty list. The invalid file remains until a successful command saves over it. Restore your backup before running more commands.

## Known issues

* **Window opens off-screen:** Close the app and delete `preferences.json`, then restart it. This resets the saved window position.
* **Help window stays minimized:** Restore the existing help window manually. Running `help` again does not restore it.

### Lesson clash warnings

Adding or editing a lesson warns when it overlaps another lesson on the same weekday. The change is still applied
and saved normally. Warnings identify each conflicting student by name and their lesson number, subject and time.
Checks include all students, even those hidden by a filter, and other lessons of the same student.
Warnings show the number of conflicting lessons and list each on a separate line.
Clashes appear in an amber feedback panel with an exclamation icon and a bold
"Lesson saved - scheduling clash detected" heading. The warning stays until the next command result.
Every successful lesson edit
checks for clashes, including a rate-only or subject-only edit; an existing clash will therefore warn again.
Back-to-back lessons do not clash. Deleting a lesson does not produce a warning.
Lesson numbers in warnings belong to the named student's lesson list; find that student before editing their lesson.

## Command summary

| Task | Format |
|---|---|
| Add | `add name/NAME phone/PHONE_NUMBER email/EMAIL address/ADDRESS [tag/TAG]... [level/SCHOOLING_LEVEL]` |
| Edit | `edit INDEX [name/NAME] [phone/PHONE_NUMBER] [email/EMAIL] [address/ADDRESS] [tag/TAG]... [level/SCHOOLING_LEVEL]` |
| Clear schooling level | `edit INDEX level/` |
| Find | `find KEYWORD [MORE_KEYWORDS]` or `find [level/TEXT] [subject/TEXT] [day/DAY]` |
| List | `list` |
| This week's lessons | `week` |
| A student's details | `view INDEX` |
| Add lesson | `lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE [mode/MODE] [location/LOCATION]` |
| Edit lesson | `lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION]` |
| Delete lesson | `lesson-delete STUDENT_INDEX LESSON_INDEX` |
| Add guardian | `guardian-add INDEX name/NAME phone/PHONE` |
| Edit guardian | `guardian-edit INDEX [name/NAME] [phone/PHONE]` |
| Delete guardian | `guardian-delete INDEX` |
| Delete | `delete INDEX` |
| Delete all | `clear` |
| Help | `help` |
| Exit | `exit` |
