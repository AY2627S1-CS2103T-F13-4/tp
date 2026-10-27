# Object diagrams

An object diagram is a snapshot of objects and their links at one moment. It is an instance of a class diagram. In the DG it suits state snapshots, such as the undo/redo history diagrams `UndoRedoState0.puml` to `UndoRedoState5.puml`.

Verify every diagram by looking at it: render, open the PNG, check notation, layout and match with the code, then commit the PNG with the `.puml`. See the [README steps](README.md#look-at-every-diagram-you-make).

## Notation

From [W8.1b](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/objectDiagrams/) and [W8.1c](https://nus-cs2103-ay2627-s1.github.io/website/book/uml/miscellaneous/objectVsClassDiagrams/):

- Object box header `objectName:ClassName`, underlined. `:ClassName` for an unnamed object.
- Attributes as `attribute = value`, for example `size = 3`. No visibility, no type.
- Leave out class-level attributes; they belong to the class.
- The attributes compartment may be omitted. The operations compartment is always omitted.
- A link is a solid line. Navigability arrowheads are allowed.
- No multiplicities: each line links exactly one object to one other.
- No inheritance lines, abstract classes, interfaces or dependencies.
- Association labels or roles only when needed to tell links apart. The course example: if a `Student` links to two `Degree` objects through `major` and `minor` associations, a link without a role is ambiguous.
- For a subclass instance, show one object, typed as the child or the parent class. Never draw both a `Person` object and an `Employee` object for the same person. Using the parent type is fine if the subclass doesn't matter to the reader.
- Several object diagrams can come from one class diagram.

## Do

- Check that every link is allowed by the class diagram's multiplicities.
- Name objects when the text refers to them (`ab0:AddressBook`).

## Don't

- Don't show operations, multiplicities, types or visibility.
- Don't leave object names un-underlined. That is the main visual difference from a class box, and from a sequence diagram participant.

## PlantUML pitfalls

- PlantUML does not underline object names. Write them as `object "<u>alice:Student</u>" as alice`. `UndoRedoState1.puml` does the same with `class State1 as "<u>ab0:AddressBook</u>"`.
- `style.puml` has `hide members`, which also hides object fields. Add `show alice fields` for each object whose values you want to show. Checked by rendering: without it the fields disappear.
- `style.puml` makes class text white. For objects, set `ObjectFontColor`, `ObjectBorderColor` and `ObjectAttributeFontColor` explicitly, as below, or the undo/redo diagrams' class-based pattern with `ClassFontColor #000000`.

## Example

```plantuml
@startuml
!include style.puml
skinparam ObjectFontColor #000000
skinparam ObjectBorderColor #000000
skinparam ObjectBackgroundColor #FFFFAA
skinparam ObjectAttributeFontColor #000000

object "<u>alice:Student</u>" as alice {
  name = "Alice Tan"
}
object "<u>:Lesson</u>" as mathsLesson {
  hourlyRate = 40
}
object "<u>:Lesson</u>" as physicsLesson
object "<u>maths:Subject</u>" as maths
object "<u>physics:Subject</u>" as physics

show alice fields
show mathsLesson fields

alice -- mathsLesson
alice -- physicsLesson
mathsLesson -- maths
physicsLesson -- physics
@enduml
```

## Sources

- W8.1b Object diagrams: https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/objectDiagrams/
- W8.1c Object versus class diagrams: https://nus-cs2103-ay2627-s1.github.io/website/book/uml/miscellaneous/objectVsClassDiagrams/
- Week 8 topics page: https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/topics.html
