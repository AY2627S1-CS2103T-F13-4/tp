# Architecture diagrams

An architecture diagram shows the major parts of a system and how they relate. Unlike UML, it has no standard notation, so the diagram must explain its own symbols. The DG's `ArchitectureDiagram.puml` is the main one.

Verify every diagram by looking at it: render, open the PNG, check notation, layout and match with the code, then commit the PNG with the `.puml`. See the [README steps](README.md#look-at-every-diagram-you-make).

## What the course says

Reading ([W6.2](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/topics.html))

- A component is a major part with one coherent responsibility. A single class like `Invoice` is too small.
- A dependency arrow means the tail needs the head. It shows reliance, not necessarily a method call; the legend says whether an arrow is a call, a message or a network request.
- Keep views apart. A logical view shows parts and dependencies; a deployment view shows where parts run. A diagram that blurs "four components" with "four computers" misleads.
- Readers ask four questions in order: what view is this, what are the parts, what is each responsible for, and what do the arrows mean.
- The course's caption for the AB3 architecture diagram reads dashed arrows as dependencies and solid arrows as associations.

Drawing ([W9.4a](https://nus-cs2103-ay2627-s1.github.io/website/book/architecture/architectureDiagrams/drawing/))

- Name each component by its responsibility, not its current implementation: `Storage`, not `JsonFileHandler`.
- Show only what is architecturally relevant. More than a handful of components means the diagram has slipped to a lower level.
- Use familiar symbols, such as a drum for a database, and explain them in a legend. If there are two kinds of arrow, make them look different and label both.
- Don't put double-headed arrows everywhere. The course's exercise answer says they hide the most important fact, the direction of each dependency. Keep double heads for genuinely two-way dependencies.

AI note (W6.2): agents can draft architecture diagrams quickly but may choose the wrong view, add too much detail, or mistake the current code structure for the intended architecture. A person still checks the view, detail level and notation.

## Do

- Add a legend, or a sentence under the diagram, saying what each arrow and line style means.
- Keep the same component colours as the rest of the DG (`UI_COLOR`, `LOGIC_COLOR`, `MODEL_COLOR`, `STORAGE_COLOR`).
- Use class diagrams one level down to show a component's insides.

## Don't

- Don't use one arrow style for two meanings.
- Don't draw classes on the architecture diagram.

## In this repo

- `ArchitectureDiagram.puml` draws components as `Class` boxes with `style.puml`, mixing solid coloured arrows and dashed arrows, and has no legend. The DG text below it lists responsibilities but doesn't say what the arrow styles mean. Per W9.4a that is a gap. Flagged here for the DG owner; not changed by this guide.
- PlantUML's `legend ... endlegend` adds a legend box.

## Example

```plantuml
@startuml
!include style.puml

Package " "<<Rectangle>>{
    Class UI UI_COLOR
    Class Logic LOGIC_COLOR
    Class Model MODEL_COLOR
    Class Storage STORAGE_COLOR
}

UI .right.> Logic
UI .down.> Model
Logic .down.> Model
Logic .right.> Storage
Storage .down.> Model

legend bottom
  dashed arrow = "depends on"
  (tail needs head)
endlegend
@enduml
```

## Sources

- W6.2 Architecture introduction and reading (Week 6 topics page): https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/topics.html
- W6.2d Different diagrams of one system: https://nus-cs2103-ay2627-s1.github.io/website/book/architecture/architectureDiagrams/views/
- W6.2e Reading: https://nus-cs2103-ay2627-s1.github.io/website/book/architecture/architectureDiagrams/reading/
- W9.4a Drawing: https://nus-cs2103-ay2627-s1.github.io/website/book/architecture/architectureDiagrams/drawing/
