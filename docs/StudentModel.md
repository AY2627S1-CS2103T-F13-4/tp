---
layout: page
title: Student model and scope
---

# Student model and scope

This is the shared implementation contract for StudentBook. The existing application supplies AB3 commands and contact fields. New tuition commands are available only when documented as implemented in the User Guide.

## Scope and fields

One private tutor uses the app to manage students. Retain AB3 name, phone, email, address and tags, including their existing validation and command behavior. The tutor is the user, not another record. There is no tutor/student role field or generic `class[]` field.

Add these immutable value objects to `Person`. All five are optional, including on newly created records, so existing commands and saved records remain valid. Absence means not recorded; it is never a fake value or an empty string.

| Field | Model type | Value rules |
|---|---|---|
| `subject` | `Optional<Subject>` | One text label, 1–40 ASCII characters after trimming and collapsing spaces/tabs |
| `schoolingLevel` | `Optional<SchoolingLevel>` | One text label, 1–30 ASCII characters after trimming and collapsing spaces/tabs |
| `guardianContact` | `Optional<GuardianContact>` | Both a `Name` and a `Phone`, using existing AB3 validation |
| `weeklyLessonSlot` | `Optional<WeeklyLessonSlot>` | `DayOfWeek`, `LocalTime start`, `LocalTime end`; minute precision and end after start on the same day |
| `hourlyRate` | `Optional<HourlyRate>` | SGD per hour, non-negative `BigDecimal`, at most two decimal places; zero differs from absence |

Subject and level start with an ASCII letter or digit, contain at least one letter, and otherwise allow letters, digits, spaces, apostrophes, hyphens, periods and parentheses. Preserve case; use text labels rather than fixed enums. No synonym mapping or subject-level compatibility check is required. Matching guardian details on siblings are independent values.

## Shared implementation contract

Keep the Java name `Person`. Retain the existing five-argument constructor as a convenience that leaves new fields absent. The full constructor order is `name, phone, email, address, tags, subject, schoolingLevel, guardianContact, weeklyLessonSlot, hourlyRate`. Every `Optional` argument must be non-null.

The shared foundation adds the five types, model fields, JSON adapters, builder support and edit-copy preservation. It does not implement teammates' command flows. Existing sample records may leave the new fields absent. Every edit preserves fields it does not change. Full equality and hashing include the new fields; duplicate detection retains AB3's exact, case-sensitive name comparison.

Keep the configured AB3 storage path, default `data/addressbook.json`, and the `persons` JSON envelope. Retain existing properties and add `subject`, `schoolingLevel`, `guardianContact`, `weeklyLessonSlot` and `hourlyRate`. Missing or null new properties load as `Optional.empty()`; existing valid files and name distinctions remain valid. Invalid supplied new values fail validation. Guardian JSON contains `name` and `phone`; slot JSON contains an uppercase English `day` such as `MONDAY`, and `start`/`end` as `HH:mm`; rate is a decimal string such as `45.50`. Do not rename or silently discard existing data.

Verify all fields through save/reload, loading an old file, and editing an existing field on a fully populated record. Preserve the inherited save-failure behavior for this increment: report the failure; the in-memory edit may remain. Do not promise transactional rollback or a blocked startup UI that has not been implemented. Back up files before manual editing.

## Feature ownership and delivery

| Owner | Feature | Story IDs |
|---|---|---|
| Min Wenn | Schooling level | 5, 12 |
| Pranav | Subject | 6, 12 |
| Jian Yi | Weekly lesson slot | 25, 28, 29, 48 |
| Dylan | Hourly rate | 53, 54 |
| Mervin | Guardian contact | 7, 8, 22 |

Ownership follows the team's reused A–E allocation, checked against the earlier GitHub assignments. Each owner delivers commands, display and tests for their feature on the shared foundation. Track work in assigned v1.2 issues. Each member needs a merged functional-code PR for Week 8; full CRUD is not required in that first increment.

The core MVP stories remain #1–9 in the current story sheet. High priority means the capability is required, not that every student must have a value for every field. Slots and rates are selected extensions. Existing edit/search/help commands remain available. The subject feature adds optional `s/SUBJECT` to `add`, displays it, and saves/reloads it. `edit INDEX s/SUBJECT` updates it; empty `s/` clears it. These commands are implemented on the subject branch and preserve every other field.

Guardian attachment remains a separate planned command, `guardian INDEX n/NAME p/PHONE`. Schooling-level, slot and rate command details belong to their feature PRs. Do not advertise unimplemented commands as available.

## Deferred work and documentation

Multiple subjects, multiple weekly slots, overnight lessons, shared group classes, conflict detection, fee calculations, payments, progress notes and follow-ups are future candidates. A future shared class should have student references and one schedule; displayed list indexes must not become persistent identifiers.

Keep the active Google Doc sections, the story sheet's `copy_sheet1`, README and DG aligned with this contract. The User Guide describes executable behavior. Original brainstorming and the previously exported Week 6 PDF remain historical. The Week 6 submission is non-binding; the Week 8 FAQ does not require resubmission.
