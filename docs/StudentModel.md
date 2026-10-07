---
layout: page
title: Student model and scope
---

# Student model and scope

This page defines the shared fields, feature owners and rules for combining their work. The [User Guide](UserGuide.md) lists commands that work now.

## Scope

StudentBook is for one private tutor. Keep the existing AddressBook Level 3, or AB3, name, phone, email, address and tags, with their current validation and commands. The tutor uses the app; they are not a separate record.

Add five optional, immutable values to `Person`. Any of them can be absent, even on new students. Absence means "not recorded", not an empty string or a made-up value.

| Field | Java type | Rules |
|---|---|---|
| Subject | `Optional<Subject>` | One text label, 1 to 40 ASCII characters after the space cleanup below |
| Schooling level | `Optional<SchoolingLevel>` | One text label, 1 to 30 ASCII characters after the space cleanup below |
| Guardian | `Optional<GuardianContact>` | Both `Name` and `Phone`, using AB3 validation |
| Weekly lesson | `Optional<WeeklyLessonSlot>` | `DayOfWeek`, `LocalTime start` and `LocalTime end`; whole minutes, with end later on the same day |
| Hourly rate | `Optional<HourlyRate>` | SGD/hour as a non-negative `BigDecimal`, with at most two decimal places; zero is a recorded value |

Subject and level use text labels, not enums. For both:

* Remove leading and trailing spaces or tabs. Replace repeated spaces or tabs with one space. Keep uppercase and lowercase letters as entered.
* Start with an ASCII letter or digit and include at least one letter.
* Allow letters, digits, spaces, apostrophes, hyphens, periods and parentheses.
* Do not map synonyms or check whether a subject matches a level.

Siblings can have the same guardian details, but each student keeps a separate copy. There is no tutor/student role field or generic `class[]` field.

## Integration rules

Keep the class name `Person` and its existing five-argument constructor. That constructor leaves all tuition fields absent.

The full constructor order is:

```text
name, phone, email, address, tags,
subject, schoolingLevel, guardianContact, weeklyLessonSlot, hourlyRate
```

Every `Optional` argument must be non-null.

The shared foundation provides the value types, model fields, JSON support, test builders and field preservation during edits. Feature owners add commands, display and tests separately.

* Every edit keeps fields it does not change.
* Full equality and hashing include all tuition fields.
* Duplicate detection still compares exact, case-sensitive names.
* Existing sample records may leave tuition fields absent.

## Storage

Keep the configured file path, default `data/addressbook.json`, and the top-level `persons` array. Preserve existing properties and record order.

Add these JSON properties:

| Property | JSON value |
|---|---|
| `subject` | Text string |
| `schoolingLevel` | Text string |
| `guardianContact` | Object with `name` and `phone` |
| `weeklyLessonSlot` | Object with uppercase English `day`, such as `MONDAY`, and `start`/`end` in `HH:mm` |
| `hourlyRate` | Decimal string, such as `"45.50"` |

Missing or null tuition properties load as `Optional.empty()`. Supplied values must have the correct JSON type and pass validation. Valid old files must still load. Keep the existing name and duplicate rules, and never silently discard data.

Check that old files load, all fields survive save/reload, and editing one field preserves the others. A failed save reports an error but does not undo the change in memory. A failed load does not block further commands. Back up files before manual edits.

## Feature owners

| Owner | Feature | Story IDs |
|---|---|---|
| Min Wenn | Schooling level | 5, 12 |
| Pranav | Subject | 6, 12 |
| Jian Yi | Weekly lesson slot | 25, 28, 29, 48 |
| Dylan | Hourly rate | 53, 54 |
| Mervin | Guardian contact | 7, 8, 22 |

This follows the agreed A to E allocation, checked against earlier GitHub assignments. Assign each feature a v1.2 issue. Each member needs a merged PR with working feature code for Week 8. Start small; full create, read, update and delete support can follow.

## Current and planned features

The first version covers stories 1 to 9 in `copy_sheet1`: student records, subject/level, one guardian and saved data. A required feature can still have an optional field. Weekly slots and rates are extensions.

The subject branch supports `add ... s/SUBJECT`, card display and save/reload. Use `edit INDEX s/SUBJECT` to change a subject and `edit INDEX s/` to clear it. Other fields stay unchanged.

Guardian attachment is a separate planned command: `guardian INDEX n/NAME p/PHONE`. Level, slot and rate syntax will be defined in their feature PRs. Existing edit, search and help commands remain available.

## Deferred work

Multiple subjects or weekly slots, overnight lessons, group classes, conflict checks, fee calculations, payments, progress notes and follow-ups are future candidates.

If group classes are added, store one shared schedule with student references. A displayed list index can change, so it must not become a permanent student ID.

Keep the active Google Doc, `copy_sheet1`, README and Developer Guide aligned with this page. Original brainstorming and the exported Week 6 PDF are historical. The Week 6 submission is non-binding; the Week 8 FAQ does not require resubmission.
