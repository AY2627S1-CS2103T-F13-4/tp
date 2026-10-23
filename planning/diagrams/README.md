# Drawing diagrams the CS2103T way

These guides record the UML and architecture notation taught in CS2103T AY2026/27 Semester 1, so diagrams in the StudentBook Developer Guide (DG) match what the course expects. Course pages were read on 23 October 2026. Where the course differs from standard UML or from PlantUML defaults, the guides say so.

What the DG needs, and how much UML each member should contribute, is covered in [Deliverables: UML and diagram guidance](../04-deliverables.md#uml-and-diagram-guidance). These guides cover notation only.

## Look at every diagram you make

Reading the `.puml` source is not verification. For every new or changed diagram:

1. Render it with the command in [Rendering in this repo](#rendering-in-this-repo).
2. Open the PNG and look at it. Agents can view images with their Read tool.
3. Check that:
   1. it rendered at all, with no PlantUML error image (a grey box of error text);
   2. every notation element matches the rules in the guide for that diagram type: arrowheads, line styles, multiplicities, activation bars, frames, guards;
   3. the layout is readable: no overlapping labels, no crossing lines that hide meaning, text the same size as the other DG diagrams, nothing cut off;
   4. it matches the current code: class names, method names, fields and call order.
4. If anything is off, fix the source, render again and look again.
5. Commit the regenerated PNG in `docs/images/` together with the `.puml`, and check that the DG references the PNG. Prefer `<img src="images/X.png" width="..." />`, since the width keeps text size consistent with neighbouring diagrams.

## Pick the diagram type

| You want to explain | Use | Guide |
|---|---|---|
| Which classes exist and how they relate | Class diagram | [class-diagrams.md](class-diagrams.md) |
| The problem domain, before any design | Conceptual class diagram (OODM) | [class-diagrams.md](class-diagrams.md#conceptual-class-diagrams-oodm) |
| A snapshot of objects at one moment, such as undo/redo state | Object diagram | [object-diagrams.md](object-diagrams.md) |
| How objects interact to carry out one command or scenario | Sequence diagram | [sequence-diagrams.md](sequence-diagrams.md) |
| A workflow or algorithm with branches, loops or parallel steps | Activity diagram | [activity-diagrams.md](activity-diagrams.md) |
| The major components and their dependencies | Architecture diagram | [architecture-diagrams.md](architecture-diagrams.md) |
| Which actors use which features | Use case diagram | [use-case-diagrams.md](use-case-diagrams.md) |

The course also introduces communication, state machine, deployment, component, package, composite structure, timing and interaction overview diagrams, but only as an optional Week 11 overview (W11.8) with one example each and no notation rules. Avoid them in the DG unless a teammate agrees there is a real need; the course gives us nothing to check them against.

## General rules from the course

- Draw diagrams to explain something the text needs. The course calls a DG with one section per diagram type, filled with diagrams nobody asked for, a failure to understand documentation. Put extra diagrams in an appendix. ([W9.8b](https://nus-cs2103-ay2627-s1.github.io/website/book/documentation/guidelines/aimForComprehensibility/what/))
- Describe top-down: architecture first, then each component, then details. ([W9.8c](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/topics.html))
- Show the intended design, not a literal translation of the code. A field that can be null in code may still be multiplicity `1` if the design intends it to always be set. ([W8 guidance](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/topics.html))
- Omit what doesn't serve the diagram's purpose: compartments, members, parameters (`foo(...)`), activation bars, return arrows. Omission must not create ambiguity. ([W6.1a minimal notation](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingBehaviors/sequenceDiagramsBasic/))
- Don't trust IDE- or AI-generated diagrams. The course warns that an agent may pick the wrong view, include too much detail, or mistake current code structure for intended architecture. [04-deliverables](../04-deliverables.md#uml-and-diagram-guidance) records the same warning for IDE output.
- No visibility shown means "unspecified", not public. No multiplicity shown means "unspecified", not 1. UML has no defaults for either. ([W8.1a](https://nus-cs2103-ay2627-s1.github.io/website/book/modeling/modelingStructures/classDiagramsBasic/))
- Use the course's notation even when PlantUML offers something fancier. The exam has hand-drawn UML, so the course is strict about notation.

## Rendering in this repo

Sources live in `docs/diagrams/*.puml`. Rendered PNGs live in `docs/images/`. Render one diagram:

```sh
cd docs/diagrams && java -jar <path-to-plantuml.jar> -tpng -o ../images X.puml
```

The DG also links the [se-education PlantUML tutorial](https://se-education.org/guides/tutorials/plantUml.html).

House style, taken from `docs/diagrams/style.puml` and the existing diagrams:

- Start every class, object, sequence and architecture diagram with `!include style.puml`. Activity diagrams are the exception: `CommitActivityDiagram.puml` uses `skin rose` with `ActivityFontSize 15` and `ArrowFontSize 12`.
- Colour by component: `UI_COLOR` (green), `LOGIC_COLOR` (blue), `MODEL_COLOR` (red), `STORAGE_COLOR` (gold), each with lighter `_T1`/`_T2` and darker `_T3`/`_T4` shades. Class diagrams set `skinparam classBackgroundColor` and `arrowColor` to their component colour. Sequence diagrams group participants in `box Logic LOGIC_COLOR_T1 ... end box` and colour each participant with the darker shade.
- `style.puml` sets `hide members`, `hide circle`, `hide footbox`, white class text and bold arrow labels. Sequence diagrams override with `skinparam ArrowFontStyle plain`.
- Layout is steered with direction hints (`-up->`, `-right->`) and hidden links (`A -[hidden]right- B`). `ModelClassDiagram.puml` uses an invisible white class `I` as an anchor.
- Sequence diagrams use a hidden arrow before `destroy` so the X lands below the last activation bar (see `DeleteSequenceDiagram.puml`).
- `style.puml` defines `STORAGE_COLOR_T2` twice and has no `STORAGE_COLOR_T4`. That looks like a typo for `_T4`. Avoid relying on either until someone fixes it.

## Course idiosyncrasies checklist

Run this before committing any diagram. Every item links to the guide with the source.

Verification

- [ ] Rendered, opened and looked at the PNG; no error image ([above](#look-at-every-diagram-you-make)).
- [ ] Notation matches the guide, layout is readable, text size matches other DG diagrams, nothing is cut off.
- [ ] Names, fields and call order match the current code.
- [ ] Regenerated PNG is committed in `docs/images/` and referenced from the DG.

Class and object diagrams

- [ ] Visibility shows as `+ - # ~` text, not PlantUML icons (`skinparam classAttributeIconSize 0`). ([class](class-diagrams.md#plantuml-pitfalls))
- [ ] Interfaces carry `<<interface>>`, enumerations `<<enumeration>>`. With `hide circle` PlantUML shows no other marker. ([class](class-diagrams.md#plantuml-pitfalls))
- [ ] Abstract classes show `{abstract}` (preferred) or italics.
- [ ] Each association appears as a line or as an attribute, never both.
- [ ] No dependency arrow where an association or inheritance already links the two classes.
- [ ] No aggregation diamond. The course recommends against it.
- [ ] Role names sit at the far end of the line, next to the class playing the role; a label in the middle of the line is an association label, read in the direction of its arrowhead.
- [ ] Object names are underlined with `<u>...</u>` (PlantUML does not do it), show `name = value` without types, and have no methods or multiplicities. ([object](object-diagrams.md))

Sequence diagrams

- [ ] Calls are solid with filled heads (`->`); returns are dashed (`-->`). Never `->>`, which is the asynchronous arrowhead. ([sequence](sequence-diagrams.md))
- [ ] Participant names are `:Class` or `name:Class`, not underlined.
- [ ] Activation bars start when the call arrives, end when it returns, and are never broken in between.
- [ ] Frame keywords are only `loop`, `alt`, `opt`, `par`, `ref` and `sd` (so no `while` or `if` frames). Write guards without brackets; PlantUML adds them.
- [ ] Chained calls are unrolled into separate arrows; static calls go to a `<<class>>` participant.

Activity diagrams

- [ ] Guards are on the flows, in square brackets, with exactly one true at each branch. ([activity](activity-diagrams.md))
- [ ] Every fork ends in one join; no double-headed arrows.

Architecture diagrams

- [ ] Components named by responsibility, a handful of them, with a legend for every arrow and symbol type. ([architecture](architecture-diagrams.md))

## How these guides were researched

Pages read on 23 October 2026, with every collapsible panel, nested panel, exercise and answer expanded in a headless browser:

- Week topic pages [6](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/topics.html), [7](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week7/topics.html), [8](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/topics.html), [9](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/topics.html), [10](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week10/topics.html), [11](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/topics.html), [12](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/topics.html) and [13](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week13/topics.html).
- The textbook pages those topics include, under `https://nus-cs2103-ay2627-s1.github.io/website/book/`.

None of these pages was marked tentative when read. Weeks 12 and 13 say "No topics assigned to this week." Weeks 10 and 11 add no notation rules beyond stereotypes such as `<<Singleton>>` (W10.1) and the optional W11.8 overview. Some exercises have no published answer (for example "Find notation errors in Sequence Diagram" in W6.1a). Where a guide reads an answer off a course image, it says so.

If the course site changes, recheck the linked pages before relying on these rules.
