---
layout: page
title: User Guide
---

**Implementation status:** StudentBook provides the contact-management commands described below, optional schooling levels, and one optional recurring weekly lesson slot per person. You can add, change, clear and view lesson slots. Commands for subjects, guardian contacts and hourly rates are not yet available. See [Student model and scope](StudentModel.md) for the planned scope.

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   The inherited AB3 GUI should appear in a few seconds with sample contacts. The image below is the planned StudentBook mock-up, not a screenshot of the current implementation.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [l/SCHOOLING_LEVEL] [i/LESSON_SLOT] [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

* `SCHOOLING_LEVEL` is optional free text such as `Primary 5` or `Sec 3` (1–30 characters; starts with a letter or digit, contains at least one letter, and otherwise allows letters, digits, spaces, apostrophes, hyphens, periods and parentheses). Extra spaces are collapsed and case is preserved.
* `LESSON_SLOT` is optional and records one lesson that repeats every week. Omit `i/` when the lesson time is not yet known. An empty `i/` is invalid when adding a person.
* Write the slot as `i/DAY START END`, e.g. `i/Wed 1.00pm 5.00pm`.
  * `DAY` is the three letter prefix of the day of the week, e.g. `Tue`, `Thu`
  * `START` and `END` are the start and end times of the lesson, respectively. Each should be written in format `HH.MM[am/pm]`, e.g. `12.00pm`, `3.30am`
* For English input, use the abbreviated day: `Mon`, `Tue`, `Wed`, `Thu`, `Fri`, `Sat` or `Sun`. Write each time in 12-hour form with a period, two minute digits and an `am` or `pm` suffix, such as `9.05am` or `1.30pm`. Do not put a space before the suffix. Use hours 1–12; noon is `12.00pm` and midnight is `12.00am`. Do not include seconds.

Examples:

* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Amy Tan p/91234567 e/amy@example.com a/Bedok Ave 1 l/Primary 5`
* `add n/Chris Lim p/92345678 e/chris@example.com a/Tampines Ave 4 l/Secondary 2 i/Wed 1.00pm 5.00pm`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

When a person has a lesson slot, their card shows a line such as `Lesson: Wednesday 1.00pm-5.00pm`. No lesson line is shown when the slot is absent. To enter or edit the slot, use the abbreviated day and separate start/end times described under [Adding a person](#adding-a-person-add); the card's display text is not the command input format.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [l/SCHOOLING_LEVEL] [i/LESSON_SLOT] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.
* To set or change the schooling level, enter `l/SCHOOLING_LEVEL`. To remove it, enter `l/` with nothing after it. Editing other fields leaves the schooling level unchanged.
* To set or change the lesson slot, enter `i/DAY START END` using the same [format and rules as `add`](#adding-a-person-add). A new slot replaces the existing slot. Include `i/` at most once per command.
* To remove the lesson slot, enter `i/` with nothing after it. Omitting `i/` leaves the existing slot unchanged, including when editing other fields. Changing or clearing the slot preserves the other fields unless you also specify changes to them.

Examples:

*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.
*  `edit 1 l/Secondary 1` Sets the schooling level of the 1st person to `Secondary 1`.
*  `edit 1 l/` Clears the schooling level of the 1st person.
*  `edit 1 i/Fri 9.00am 10.30am` Sets or replaces the 1st person's weekly lesson with Friday, 9 am to 10:30 am.
*  `edit 1 i/` Clears the 1st person's lesson slot and removes its line from the card.
*  `edit 1 p/91234567` Changes the 1st person's phone number and preserves their lesson slot.

### Locating persons by name or schooling level: `find`

Finds persons whose names contain any of the given keywords, or whose schooling level contains the given text.

Format: `find KEYWORD [MORE_KEYWORDS]` or `find l/TEXT`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search by keywords considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

* `find l/TEXT` lists persons whose schooling level contains `TEXT` (case-insensitive, partial matches allowed; for example, `l/prim` matches `Primary 5`). Persons with no schooling level are never listed. It cannot be combined with name keywords.

Examples:
* `find l/Primary` returns everyone whose schooling level contains `Primary`
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [l/SCHOOLING_LEVEL] [i/LESSON_SLOT] [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 l/Primary 5 i/Wed 1.00pm 5.00pm t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [l/SCHOOLING_LEVEL] [i/LESSON_SLOT] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`, `edit 2 l/`, `edit 2 i/Fri 9.00am 10.30am`, `edit 2 i/`
**Find** | `find KEYWORD [MORE_KEYWORDS]` or `find l/TEXT`<br> e.g., `find James Jake`, `find l/Primary`
**List** | `list`
**Help** | `help`

`LESSON_SLOT` means `DAY START END`.
