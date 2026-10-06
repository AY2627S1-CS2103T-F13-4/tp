---
layout: page
title: Student model and scope
---

# Student model and scope

This document specifies the planned StudentBook model. The current application still uses inherited AB3 fields and commands. The model below is a design contract for incremental implementation, not a claim that these features are available.

## Core MVP

One private tutor uses the app to manage student records. Each student has a name, one subject, one schooling level and zero or one guardian contact. There is no tutor/student role field and no generic `class[]` field.

| Field | Type and meaning |
|---|---|
| `name` | `Name`, the student's display name |
| `subject` | `Subject`, an immutable value object containing validated text |
| `schoolingLevel` | `SchoolingLevel`, an immutable value object containing validated text |
| `guardianContact` | Zero or one `GuardianContact` containing both name and phone |

Subject and level are text labels, not fixed enums. Guardian contact is optional at creation and is added separately. A contact must have both a name and a phone; siblings may have matching independent contacts. The tutor is the user, not another record in the student list.

Core stories use the current User Stories sheet's `copy_sheet1` IDs: #1 add, #2 list, #3 view academic details, #4 delete, #5 level, #6 subject, #7 add guardian, #8 view guardian and #9 save/reload.

The intended creation command is `add n/NAME s/SUBJECT l/LEVEL`; guardian attachment is `guardian INDEX n/NAME p/PHONE`. The core MVP permits correction by deleting and re-adding. Editing is an extension, not a prerequisite for completing the core MVP. These commands are planned; consult the User Guide for the current executable commands.

## Extension workstreams

The proposed five workstreams are schooling level, subject, weekly lesson slot, hourly rate and guardian contact. Workstream ownership is by feature, including its command, storage, display and tests. This allocation does not require all five complete CRUD implementations in v1.2. Names must be confirmed against the team's earlier allocation; list position is not a story ID.

| Extension | Initial design | Deferred |
|---|---|---|
| Weekly lesson slot, #25, #28-29, #48 | Zero or one `WeeklyLessonSlot` per student; `DayOfWeek`, `LocalTime start`, `LocalTime end`; 24-hour `HH:mm`, with end after start on the same day | Multiple slots, overnight lessons, shared classes and conflict detection |
| Hourly rate, #53-54 | Optional `HourlyRate`, SGD per hour, represented with `BigDecimal`; non-negative and at most two decimal places | Fee calculation, currency conversion and payment processing |

An absent rate means not recorded and differs from a zero rate. An absent slot means not scheduled. Fee calculation needs a separately documented duration and rounding rule before implementation.

Shared group classes remain a future candidate. If implemented, use one `TuitionClass` with student references and one shared schedule. Do not copy a mutable class object into every student, and do not use changing display indexes as persistent references. Several attendees of the same class must not be reported as overlapping separate lessons.

## Shared implementation contract

Keep the Java class name `Person` during this increment if that avoids unrelated renaming. Use core constructor order `name, subject, schoolingLevel, guardianContact`. Append `weeklyLessonSlot, hourlyRate` only when integrating those extensions. Use `Optional<GuardianContact>`, `Optional<WeeklyLessonSlot>` and `Optional<HourlyRate>` in the model API, and JSON `null` for absence. Use `Optional.empty()`, never empty strings or fake objects. Do not pre-build a large framework for future fields.

Integrate the core model in a small compiling PR that updates affected construction sites together: `Person`, `AddCommandParser`, `EditCommand.createEditedPerson`, `JsonAdaptedPerson`, `SampleDataUtil` and `PersonBuilder`. Update equality, hashing, formatting and UI bindings where affected. Feature owners then build from that merged base and keep their own PRs small.

Every edit must preserve fields it does not change. Identity/duplicate detection is separate from full-value equality. Each implemented field must survive JSON save/reload, including optional values. Verify editing one feature after adding another and reloading a record with all implemented fields.

Do not invent subject or level values to load old AB3 records. Preserve incompatible files and show an explicit migration or unsupported-format message. A migration policy belongs in the PR that changes the file schema. Never silently discard old records.

## Documentation boundaries

The Google Doc's Week 6 MVP draft contains detailed intended command rules. The current story sheet supplies IDs and priorities. This page supplies the shared model and extension boundaries. Keep all three aligned when a design changes. README and DG use cases must describe the same core MVP. The User Guide continues to describe executable behaviour and labels planned changes separately.

Historical brainstorming and the previously exported Week 6 PDF are not current specifications. The submitted Week 6 specification is non-binding under the Week 8 FAQ; no resubmission is required.
