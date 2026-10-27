# Sequence diagrams

A sequence diagram shows how entities interact in one scenario: components responding to a user action, or objects inside a component handling one method call. In the DG it is the main way to explain how a command runs.

Verify every diagram by looking at it: render, open the PNG, check notation, layout and match with the code, then commit the PNG with the `.puml`. See the [README steps](README.md#look-at-every-diagram-you-make).

## Notation

Basics ([W6.1a](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingBehaviors/sequenceDiagramsBasic/))

- Entities are actors (stick figures) or instances (boxes). Time runs downwards.
- A participant is `:TextUi` for an unnamed instance, or `name:Class` to tell two instances apart. Names are not underlined, unlike object diagrams.
- Lifeline: dashed vertical line while the instance exists.
- Method call: solid arrow with a filled arrowhead, labelled with the operation. Return: dashed arrow, optionally labelled with the returned value.
- Lined (open) arrowheads mean asynchronous calls. The course treats them as out of scope, so every call uses a filled head.
- Activation bar: the period the method runs. It starts when the call arrives and ends when the method returns.
- Loop: frame labelled `loop` with a `[condition]`.
- Object creation: the constructor arrow ends at the side of the new object's box, which sits at the height of the call. The constructor's activation bar hangs below the box.
- Minimal notation: activation bars and return arrows may be dropped if that loses nothing; informal labels such as `get minefield info` are allowed when exact calls don't matter; `foo(...)` hides parameters.

Intermediate ([W6.1b](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingBehaviors/sequenceDiagramsIntermediate/))

- Deletion: the lifeline ends in an X. In Java this marks where the object stops being referenced and can be garbage-collected.
- Self-invocation: an arrow from the object's bar back to itself, with a second bar stacked on the first. Callbacks to the caller (`Chapter#getText()` calling back `Book#getAuthor()`) also stack a bar on the caller's bar.
- Unroll chained calls first. `new Book().add(new Chapter())` becomes three arrows: create `b:Book`, create `c:Chapter`, then `add(c)`. `person.getAddress().printStreet()` becomes two calls on two participants.
- `alt` frame with `[guard]` partitions separated by dashed lines. At most one partition runs; none running is fine.
- `opt` frame with one `[guard]` partition.
- Static method: the call goes to the class itself, shown as a participant labelled `<<class>>` above the class name, such as `<<class>> Person`.
- An object created and not stored (`new Quote().print()`) can be shown deleted soon after its last call. (W6.1b exercise hint.)

Advanced ([W6.1c](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingBehaviors/sequenceDiagramsAdvanced/), revisited in [W8.3c](https://nus-cs2103-ay2627-s1.github.io/website/book/uml/sequenceDiagrams/referenceFrames/) and [W8.3d](https://nus-cs2103-ay2627-s1.github.io/website/book/uml/sequenceDiagrams/parallelPaths/))

- `par` frame: partitions run in parallel. In Java this implies multithreading, so StudentBook will rarely need it.
- `ref` frame: a named box spanning the participants involved, standing in for an interaction drawn as a separate diagram. The separate diagram is framed with `sd` and the same name.

## Common errors the course flags

- Activation bar too long: starting before the call arrives, or continuing after the return.
- Broken activation bar: a gap in the middle of one method's execution.
- The W6.1a exercise "Find notation errors" has no published answer. Reading the course image, it shows `while` used as a frame keyword instead of `loop`, a return drawn as a solid arrow, calls with no activation bars on the callee, and unlabelled calls. Treat this list as this guide's reading, not a course answer.

## Do

- Start with `[-> X : method(args)` for a call arriving from outside the diagram, and end with `[<-- X`.
- Match the DG text. If the text says the parser creates the command, the diagram shows that creation arrow.
- Use `ref` frames to split long flows, and omit routine self-calls and getters that don't help.

## Don't

- Don't use `->>`. It draws the lined arrowhead, which means asynchronous.
- Don't use any frame keyword other than `loop`, `alt`, `opt`, `par`, `ref` and `sd`.
- Don't draw a chained call as one arrow.
- Don't call a static method on an instance participant.

## PlantUML pitfalls

- `->` gives the filled head the course wants; `-->` gives the dashed return.
- Activation bars are manual. Put `activate X` right after the call arrow and `deactivate X` right after the return arrow. A late `activate` makes the bar start after the call; a `deactivate` followed by more calls breaks the bar.
- `create X` before the constructor arrow makes the arrow land on the box, matching the course.
- `destroy X` draws the X. Our diagrams add `X -[hidden]-> Caller` first so the X sits below the last bar (see `DeleteSequenceDiagram.puml`).
- PlantUML wraps frame guards in brackets itself. Write `alt index is valid`, `else else`, `loop each tag`. Writing `alt [index is valid]` gives double brackets.
- `ref over A, B : name` draws the `ref` box. In the referenced diagram, `mainframe **sd** name` draws the `sd` frame.
- `par` partitions are separated with `else`.
- Static calls: `participant "<<class>>\nParserUtil" as ParserUtil`, as in `NoteAddSequenceDiagram.puml`.
- Self-invocation renders as a self-arrow with a second bar starting just under it. That is PlantUML's version of the course's stacked bar and is acceptable.
- `hide footbox` in `style.puml` removes the participant boxes at the bottom. That matches the course's diagrams.

## Example

```plantuml
@startuml
!include style.puml
skinparam ArrowFontStyle plain

box Logic LOGIC_COLOR_T1
participant ":LogicManager" as LogicManager LOGIC_COLOR
participant ":XYZCommandParser" as Parser LOGIC_COLOR
participant "<<class>>\nParserUtil" as ParserUtil LOGIC_COLOR
participant "c:XYZCommand" as Command LOGIC_COLOR
end box

box Model MODEL_COLOR_T1
participant "m:Model" as Model MODEL_COLOR
end box

[-> LogicManager : execute("xyz 1")
activate LogicManager

create Parser
LogicManager -> Parser : XYZCommandParser()
activate Parser
Parser --> LogicManager
deactivate Parser

LogicManager -> Parser : parse("1")
activate Parser

Parser -> ParserUtil : parseIndex("1")
activate ParserUtil
ParserUtil --> Parser : index
deactivate ParserUtil

create Command
Parser -> Command : XYZCommand(index)
activate Command
Command --> Parser
deactivate Command

Parser --> LogicManager : c
deactivate Parser
'Hidden arrow to position the destroy marker below the end of the activation bar.
Parser -[hidden]-> LogicManager
destroy Parser

LogicManager -> Command : execute(m)
activate Command

Command -> Command : validate(...)
activate Command
Command --> Command
deactivate Command

alt index is valid
  Command -> Model : updateStudent(...)
  activate Model
  Model --> Command
  deactivate Model
else else
  Command -> Command : throwInvalidIndex()
  activate Command
  Command --> Command
  deactivate Command
end

loop each tag
  Command -> Model : addTag(t)
  activate Model
  Model --> Command
  deactivate Model
end

ref over Command, Model : save to storage

Command --> LogicManager : r
deactivate Command

[<-- LogicManager : r
deactivate LogicManager
@enduml
```

The referenced interaction, drawn separately with an `sd` frame and a `par` frame:

```plantuml
@startuml
!include style.puml
skinparam ArrowFontStyle plain
mainframe **sd** save to storage

participant "c:XYZCommand" as Command LOGIC_COLOR
participant "m:Model" as Model MODEL_COLOR

Command -> Model : getAddressBook()
activate Model
Model --> Command : ab
deactivate Model

par
  Command -> Model : refresh()
  activate Model
  Model --> Command
  deactivate Model
else
  Command -> Command : log()
  activate Command
  Command --> Command
  deactivate Command
end
@enduml
```

Both examples show notation only. `XYZCommand` and its calls are placeholders.

## Sources

- W6.1a Sequence diagrams, basic: https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingBehaviors/sequenceDiagramsBasic/
- W6.1b Sequence diagrams, intermediate: https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingBehaviors/sequenceDiagramsIntermediate/
- W6.1c Sequence diagrams, advanced: https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingBehaviors/sequenceDiagramsAdvanced/
- W8.3c Reference frames: https://nus-cs2103-ay2627-s1.github.io/website/book/uml/sequenceDiagrams/referenceFrames/
- W8.3d Parallel paths: https://nus-cs2103-ay2627-s1.github.io/website/book/uml/sequenceDiagrams/parallelPaths/
- Week 6 and Week 8 topic pages: https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/topics.html and https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/topics.html
