---
layout: page
title: User Guide
---

StudentBook is a desktop contact book for private tutors. Type commands to manage student records.

This development version supports contact details and one optional subject per student. Schooling level, guardian, lesson-slot and hourly-rate commands are planned MVP work. See the [Developer Guide](DeveloperGuide.md#product-scope) for the plan and open decisions.

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

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [s/SUBJECT]`

Name, phone, email and address are required. Tags and subject are optional. You can add any number of tags, but only one subject.

```text
add n/Alex Tan p/91234567 e/alex@example.com a/123 Clementi Road t/weekday s/Math
add n/Mei Lim p/92345678 e/mei@example.com a/45 Dover Road
```

Records with exactly the same name are duplicates. Name matching is case-sensitive: `Alex Tan` and `alex tan` are treated as different names. Subject does not change this rule.

### Record or clear a subject

| Task | Command |
|---|---|
| Set or replace a subject | `edit 1 s/Math` |
| Clear a subject | `edit 1 s/` |
| Include a subject when adding a student | Add `s/Math` to the `add` command |

A missing subject appears as `Subject: Not recorded`. Leave out `s/` to keep the existing subject when editing. Changing a subject keeps every other field unchanged.

Subject rules:

* Use 1 to 40 characters after the space cleanup below. Start with a letter or digit and include at least one letter.
* Use English letters `A-Z` or `a-z`, digits `0-9`, spaces and these symbols: `'`, `-`, `.`, `(`, `)`.
* Leading and trailing spaces or tabs are removed. Repeated spaces or tabs become one space. For example, three spaces between `Combined` and `Science` become one.
* Uppercase and lowercase letters stay as entered.
* An empty `s/` is valid for clearing with `edit`, but invalid with `add`.
* Repeating `s/` in one command is invalid. Invalid input leaves the record unchanged.

### List all students: `list`

Format: `list`

Shows every student and clears any active search filter.

### Edit a student: `edit`

Format: `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [s/SUBJECT]`

Include at least one field to change. Fields you leave out stay unchanged.

* Tags replace the entire existing set. Use an empty `t/` to clear all tags.
* An empty `s/` clears the subject.

Examples:

* `edit 1 p/91234567 e/alex.tan@example.com` changes the first displayed student's phone and email.
* `edit 2 n/Mei Ling t/` changes the second displayed student's name and clears their tags.

### Find students by name: `find`

Format: `find KEYWORD [MORE_KEYWORDS]`

Searches names for whole words, ignoring letter case. A student appears if any keyword matches; keyword order does not matter.

* `find John` matches `John Doe`, but not `Johnny`.
* `find alex david` matches names containing `Alex` or `David`.
* Subject, phone and other fields are not searched.

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

Opens a window with a help-page link. That link currently points to the original AddressBook guide; use this guide for subject commands.

![Help window](images/helpMessage.png)

### Exit: `exit`

Format: `exit`

Closes the app.

## Saving and transferring records

The app saves automatically after each successfully executed command. Subjects are saved with the contact details; older files without subjects still load.

If saving fails, the app reports an error. The change may still appear on screen without being saved to disk.

By default, records are in `data/addressbook.json` under the folder where you start the app. This path can be changed in the app's configuration.

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
| Add | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [s/SUBJECT]` |
| Edit | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [s/SUBJECT]` |
| Clear subject | `edit INDEX s/` |
| Find | `find KEYWORD [MORE_KEYWORDS]` |
| List | `list` |
| Delete | `delete INDEX` |
| Delete all | `clear` |
| Help | `help` |
| Exit | `exit` |
