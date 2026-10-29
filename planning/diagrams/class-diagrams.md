# Class diagrams

Class diagrams show the structure of an OO solution, not its behaviour. The course uses them for detailed design inside a component (the DG's Logic, Model and Storage diagrams) and, in a lighter form, for domain models.

Verify every diagram by looking at it: render, open the PNG, check notation, layout and match with the code, then commit the PNG with the `.puml`. See the [README steps](README.md#look-at-every-diagram-you-make).

## Notation

Class box ([W8.1a](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/classDiagramsBasic/))

- Three compartments: name, attributes, operations. Attributes always sit above operations.
- Attribute format `visibility name: type = default-value`. Operation format `visibility name(parameter-list): return-type`.
- All attributes go in one compartment; all operations in one compartment.
- Either compartment may be omitted or left empty. Members irrelevant to the diagram's purpose may be left out.
- Visibility: `+` public, `-` private, `#` protected, `~` package private. No symbol means unspecified.
- Class-level (static) members are underlined.
- Generic classes show the type parameter in a small dashed box on the top-right corner of the class box.

Associations ([W8.1a](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/classDiagramsBasic/))

- Solid line between the two classes.
- Navigability is an arrowhead on that line. The arrowhead is open (lined), not a filled triangle. `Logic --> Minefield` means a Logic object holds a reference to its Minefield. Arrowheads at both ends mean bidirectional, meaning both objects point to each other for the same link. Two separate one-way associations in opposite directions are not a bidirectional association.
- Multiplicity sits at the end of the line next to the class it counts: `0..1`, `1`, `*`, `n..m` (`1..*`, `2..5`). No multiplicity means unspecified.
- Role names also sit at the end of the line next to the class playing the role. Roles usually match the field name in code (`Man` has a field `wife`, so the role at the `Woman` end is `wife`). They are optional, but useful for telling apart several associations between the same two classes.
- An association label sits mid-line and describes the meaning. A small arrowhead next to it shows which way to read it (`Admin uses ▶ Student`).
- An association may instead be written as an attribute: `name: type [multiplicity] = default`, for example `squares: Square[100]`. Show each association as a line or an attribute, never both. The course prefers the line because it is easier to spot. ([W8.2c](https://nus-cs2103-ay2627-s1.github.io/website/book/uml/classDiagrams/associationsAsAttributes/what/))

Other relationships ([W8.2d](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/classDiagramsIntermediate/))

- Inheritance: solid line with a triangle at the parent. Filled or hollow does not matter, though hollow is typical.
- Interface: class box with `<<interface>>` above the name. Implementing it is drawn like inheritance but with a dashed line.
- Abstract class or method: `{abstract}` (preferred) or italics.
- Enumeration: `<<enumeration>>` above the name, values listed in the compartment below.
- Composition: filled diamond at the whole. Means the part cannot exist without the whole and there are no cycles. Deleting parts along with the whole is not enough on its own; the part must be an integral part of the whole at the concept level.
- Aggregation: hollow diamond. The course quotes Fowler's advice to leave it out because it adds confusion, and its exercise answer marks it as the symbol not to use.
- Dependency: dashed arrow from the class that depends to the class depended on. Use it only for a dependency the diagram doesn't already show. An association or inheritance already implies one. Typical cases are using another class's static member, or receiving an object as a parameter without keeping it.
- Generics with inheritance: `Goo extends Foo<String>` is drawn as a separate `Foo` box bound to `String`, with a dashed arrow to `Foo<T>`, optionally labelled `<<bind>>` and `<T -> String>`.
- Association class: a normal class joined by a dashed line to the middle of an association. It holds data about the link itself, like a `Marriage` date between `Man` and `Woman`. A property of only one end is not an association class: the course answer rejects `Mileage` on the `Driver` to `Car` link. ([W8.2e](https://nus-cs2103-ay2627-s1.github.io/website/book/oop/associations/associationClasses/))

Notes and constraints ([W8.2a, W8.2b optional](https://nus-cs2103-ay2627-s1.github.io/website/book/uml/notes/notes/))

- A note is a box with a folded corner, attached by a dashed line or left free-standing.
- A constraint goes inside a note in curly braces, written in natural language or OCL.

Stereotypes ([W10.1](https://nus-cs2103-ay2627-s1.github.io/website/book/designPatterns/singleton/what/))

- `<<role/purpose>>` above a class name optionally marks the role it plays, such as `<<Singleton>>` or `<<Facade>>`.

## Conceptual class diagrams (OODM)

A conceptual class diagram (CCD), also called an OO domain model, models the problem domain. Its notation is the class diagram notation minus methods and navigability. It has no solution-only classes such as `DatabaseConnection` or `Parser`. A diagram with navigability arrows is a class diagram, not a CCD; that is a course exercise answer. Association labels with reading arrows are common in CCDs. ([W9.1a](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/objectOrientedDomainModels/))

## Do

- Draw from the intended design, then check it against the code.
- Use multiplicity `1` for a compulsory field even if Java lets it be null.
- Keep one component per diagram and treat others as black boxes.
- Put role names where the course puts them: at the target end. `Student --> "0..1 guardian" Guardian` puts both the multiplicity and the role next to `Guardian`.

## Don't

- Don't put attributes and operations in separate compartments per member, or operations above attributes. Both are marked incorrect in the W8.1a exercise.
- Don't show an association both as a line and as an attribute.
- Don't add a dependency arrow on top of an association or inheritance.
- Don't use aggregation.
- Don't read missing multiplicity or visibility as a default.

## PlantUML pitfalls

These are places where PlantUML or our `style.puml` disagree with the course. Each one was checked by rendering.

- Visibility renders as coloured icons by default. Add `skinparam classAttributeIconSize 0` so `+ - # ~` show as text.
- `style.puml` has `hide members`. Show the members you need with `show X members` or `show members`.
- `style.puml` has `hide circle`, which also hides PlantUML's I/E/A markers. So `interface X` looks like a plain class and `enum X` looks like a class with a list. Always write the stereotype: `class "<<interface>>\nModel" as Model` (the repo's pattern) and `enum Mode <<enumeration>>`.
- Abstract: write `class "{abstract}\nItem" as Item` to get the preferred keyword. A `{abstract}` modifier on a member turns it italic instead of printing the keyword; italics is allowed, but it is easy to miss.
- `{static}` on a member underlines it. That matches the course.
- Navigability `-->` gives an open arrowhead, which matches the course. A plain `--` line means navigability not shown, not bidirectional. Use `<-->` for bidirectional.
- Inheritance `--|>`; interface realisation `..|>`; composition `*-->` (diamond at the whole, as in `ModelClassDiagram.puml`); dependency `..>`.
- Association label with reading direction: `Tutor "1" -- "*" Student : teaches >`.
- Association class: `(Tutor, Student) .. Enrolment`.
- A label starting with `*` is read as a list bullet. Escape it: `"~* items"`, as `ModelClassDiagram.puml` does.
- Generic classes: with the house white class font, PlantUML's corner box for `<T>` renders white on white. `LessonClassDiagram.puml` uses `skinparam genericDisplay old`, which prints `UniqueList<T>` inline instead. That is a readable deviation from the course's corner-box notation; use it knowingly, and check the PNG.

## Example

Renders with `docs/diagrams/style.puml`. It illustrates notation only; check real class names against the code.

```plantuml
@startuml
!include style.puml
skinparam arrowThickness 1.1
skinparam arrowColor MODEL_COLOR
skinparam classBackgroundColor MODEL_COLOR
skinparam classAttributeIconSize 0
skinparam classAttributeFontColor #FFFFFF

class "<<interface>>\nBillable" as Billable {
  +bill()
}
class "{abstract}\nItem" as Item {
  +print() {abstract}
}
class StockItem {
  -name: String
  +print()
  +bill()
}
class Review
enum Rating <<enumeration>> {
  GOOD
  OK
  POOR
}
class Inventory {
  {static} -count: int
  +getItemCount(): int
  +generateBill(b: Billable)
  +add(s: Item)
}
show Billable members
show Item members
show StockItem members
show Rating members
show Inventory members

Item ..|> Billable
StockItem -up-|> Item
StockItem *--> "1" Review
Review --> "1 rating" Rating
Inventory --> "~* items" Item
Inventory ..> Billable
@enduml
```

`Inventory ..> Billable` is a dependency because `generateBill` only receives a `Billable`. `Inventory` already has an association to `Item`, so it gets no dependency arrow to `Item`.

Association label, association class, role and note:

```plantuml
@startuml
!include style.puml
skinparam arrowThickness 1.1
skinparam arrowColor MODEL_COLOR
skinparam classBackgroundColor MODEL_COLOR

class Tutor
class Student
class Enrolment
class Guardian

Tutor "1" -- "*" Student : teaches >
(Tutor, Student) .. Enrolment
Student --> "0..1 guardian" Guardian
note right of Guardian : Shown only when set.
@enduml
```

## Sources

- W8.1a Class diagrams, basic: https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/classDiagramsBasic/
- W8.2a Notes: https://nus-cs2103-ay2627-s1.github.io/website/book/uml/notes/notes/
- W8.2b Constraints (optional): https://nus-cs2103-ay2627-s1.github.io/website/book/uml/notes/constraints/
- W8.2c Associations as attributes: https://nus-cs2103-ay2627-s1.github.io/website/book/uml/classDiagrams/associationsAsAttributes/what/
- W8.2d Class diagrams, intermediate: https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/classDiagramsIntermediate/
- Inheritance and generics: https://nus-cs2103-ay2627-s1.github.io/website/book/uml/classDiagrams/classInheritance/what/
- W8.2e Association classes: https://nus-cs2103-ay2627-s1.github.io/website/book/oop/associations/associationClasses/
- W8 guidance on intended design versus code: https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/topics.html
- W9.1a Conceptual class diagrams: https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/objectOrientedDomainModels/
- W10.1 Stereotypes (Singleton): https://nus-cs2103-ay2627-s1.github.io/website/book/designPatterns/singleton/what/
