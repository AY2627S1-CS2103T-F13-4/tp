---
layout: page
title: User Guide
---

StudentBook is a desktop contact book for private tutors. Type commands to manage student records.

This development version supports contact details, subject and schooling level. Guardian, lesson-slot and hourly-rate commands are planned MVP work. See the [Developer Guide](DeveloperGuide.md#product-scope) for the plan and open decisions.

* Table of Contents
{:toc}

## Quick start

1. Install Java `25`. Mac users should follow the [course Java installation guide](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Build the development version from [Pranav's fork](https://github.com/pranavp311/tp). The subject feature is awaiting merge into the [team repository](https://github.com/AY2627S1-CS2103T-F13-4/tp).

   ```text
   git clone --branch student-subject https://github.com/pranavp311/tp.git studentbook
   cd studentbook
   ./gradlew shadowJar
   ```

   On Windows, use `gradlew.bat shadowJar` for the last command. The JAR is `build/libs/addressbook.jar`.

1. Copy the JAR into the folder where you want to keep your records. Open a terminal in that folder and run:

   ```text
   java -jar addressbook.jar
   ```

   On first launch, the app shows sample contacts. Some windows and messages still use the original AddressBook name.

1. Type `list` in the command box and press Enter. Then try:

   ```text
   add n/Alex Tan p/91234567 e/alex@example.com a/123 Clementi Road s/Math
   ```

   This adds Alex with Math as the subject. Type `exit` to close the app.

## Features

### Reading command formats

* Replace uppercase words with your values. For example, `n/NAME` becomes `n/Alex Tan`.
* Square brackets mark optional fields. Do not type the brackets.
* `...` means a field can be repeated. For example, `[t/TAG]...` allows zero or more tags.
* Fields may appear in any order. Prefixes such as `n/` and `s/` are case-sensitive.
* `INDEX` is the number beside a student in the current list. It must be 1 or greater.
* `help`, `list`, `clear` and `exit` ignore extra arguments.
* When copying commands from a PDF, check that spaces around line breaks are preserved.

### Add a student: `add`

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [s/SUBJECT]... [l/SCHOOLING_LEVEL]`

Name, phone, email and address are required. Tags, subjects and schooling level are optional in this build. Repeat `s/` for multiple subjects. Only one level is supported.

The agreed next change is to require a schooling level. Min Wenn owns that change; it is not enforced in this build.

```text
add n/Alex Tan p/91234567 e/alex@example.com a/123 Clementi Road t/weekday s/Math s/Science l/Primary 5
add n/Mei Lim p/92345678 e/mei@example.com a/45 Dover Road
```

Records with exactly the same name are duplicates. Name matching is case-sensitive: `Alex Tan` and `alex tan` are treated as different names. Subject and schooling level do not change this rule.

### Record or clear subjects

| Task | Command |
|---|---|
| Replace all subjects | `edit 1 s/Math s/Science` |
| Keep only Math | `edit 1 s/Math` |
| Clear all subjects | `edit 1 s/` |
| Include subjects when adding a student | Add `s/Math s/Science` to the `add` command |

No subjects appears as `Subjects: Not recorded`. Leave out `s/` to keep the existing subjects when editing. Supplying subjects replaces the entire list and keeps every other field unchanged.

Subject rules:

* Use 1 to 40 characters per subject after the space cleanup below. Start with a letter or digit and include at least one letter.
* Use English letters `A-Z` or `a-z`, digits `0-9`, spaces and these symbols: `'`, `-`, `.`, `(`, `)`.
* Leading and trailing spaces or tabs are removed. Repeated spaces or tabs become one space. For example, three spaces between `Combined` and `Science` become one.
* Repeated labels are kept once, ignoring case. The first spelling stays as entered; `s/Math s/math` records `Math` once.
* An empty `s/` is valid for clearing with `edit`, but invalid with `add`.
* A blank `s/` cannot be mixed with other subjects. Invalid input rejects the whole command and leaves the record unchanged.

### Record or clear a schooling level

| Task | Command |
|---|---|
| Set or replace a level | `edit 1 l/Secondary 1` |
| Clear a level | `edit 1 l/` |
| Include a level when adding a student | Add `l/Primary 5` to the `add` command |

The card shows `Level: Primary 5` when a level is recorded. If none is recorded, the level line is hidden. Leave out `l/` to keep the existing level when editing. Changing a level keeps every other field unchanged.

Levels use the same character and spacing rules as subjects, with a limit of 30 characters. An empty `l/` clears the level with `edit`, but is invalid with `add`. Repeating `l/` in one command is invalid. Invalid input leaves the record unchanged.

### List all students: `list`

Format: `list`

Shows every student and clears any active search filter.

### Edit a student: `edit`

Format: `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [s/SUBJECT]... [l/SCHOOLING_LEVEL]`

Include at least one field to change. Fields you leave out stay unchanged.

* Tags replace the entire existing set. Use an empty `t/` to clear all tags.
* Subjects replace the entire existing list. An empty `s/` clears all subjects.
* An empty `l/` clears the schooling level.

Examples:

* `edit 1 p/91234567 e/alex.tan@example.com` changes the first displayed student's phone and email.
* `edit 2 n/Mei Ling t/` changes the second displayed student's name and clears their tags.
* `edit 1 s/Math l/Primary 6` changes both the subject and schooling level.

### Find students by name or schooling level: `find`

Format: `find KEYWORD [MORE_KEYWORDS]` or `find l/TEXT`

Without `l/`, searches names for whole words, ignoring letter case. A student appears if any keyword matches; keyword order does not matter.

* `find John` matches `John Doe`, but not `Johnny`.
* `find alex david` matches names containing `Alex` or `David`.

Use `find l/TEXT` to search schooling levels, ignoring letter case. Partial matches are allowed: `find l/prim` matches `Primary 5`. Students without a level are excluded. Supply one non-empty `l/` value; it cannot be combined with name keywords.

Subject, phone and other fields are not searched.

![Name search results](images/findAlexDavidResult.png)

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

Opens a window with a help-page link. That link currently points to the original AddressBook guide; use this guide for subject and schooling-level commands.

![Help window](images/helpMessage.png)

### Exit: `exit`

Format: `exit`

Closes the app.

## Saving and transferring records

The app saves automatically after each successfully executed command. Subjects and schooling levels are saved with the contact details. Older files with one `subject`, or no subjects, still load. New saves store a `subjects` array.

If saving fails, the app reports an error. The change may still appear on screen without being saved to disk.

Records are stored in `data/addressbook.json` under the folder where you start the app.

To transfer records, close the app on both computers. Copy the data file to the corresponding location on the new computer, then start the app there.

### Edit the data file manually

Close the app and back up the JSON file before editing it. Keep the existing file structure and valid field values.

If the file is invalid, the app starts with an empty list. The invalid file remains until a successful command saves over it. Restore your backup before running more commands.

## Known issues

* **Window opens off-screen:** Close the app and delete `preferences.json`, then restart it. This resets the saved window position.
* **Help window stays minimized:** Restore the existing help window manually. Running `help` again does not restore it.

## Command summary

| Task | Format |
|---|---|
| Add | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [s/SUBJECT]... [l/SCHOOLING_LEVEL]` |
| Edit | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [s/SUBJECT]... [l/SCHOOLING_LEVEL]` |
| Clear subjects | `edit INDEX s/` |
| Clear schooling level | `edit INDEX l/` |
| Find | `find KEYWORD [MORE_KEYWORDS]` or `find l/TEXT` |
| List | `list` |
| Delete | `delete INDEX` |
| Delete all | `clear` |
| Help | `help` |
| Exit | `exit` |
