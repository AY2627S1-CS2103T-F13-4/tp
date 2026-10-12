# Deliverables and release checks

Checked 12 October 2026. [Official TP deliverables](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html) is the primary source for this file, including its expanded FAQs and optional/retired sections. This guide describes deliverable quality; use [the weekly plan](02-weekly-plan.md) to determine when each item is due.

## What is required

| Deliverable | Location or form | Main acceptance condition |
|---|---|---|
| Executable | Runnable JAR distributed through the specified release/submission channel; the constraints permit one ZIP if one-JAR packaging is impossible | Usable by the chosen audience; within the 100 MB product limit; does not need undeclared setup or missing files |
| Source | Team GitHub repository | Corresponds to the released executable, documentation, and website |
| User Guide | Main content in `docs/UserGuide.md`, published as a web page | Covers current features accurately and matches the submitted JAR |
| Developer Guide | Main content in `docs/DeveloperGuide.md`, published as a web page | Explains the current product to future developers; contains accurate requirements, acknowledgements, and manual-testing instructions |
| Diagram sources | PlantUML `.puml` files in `docs/diagrams`, or editable source for an agreed alternative | Can be maintained alongside the code; diagrams use appropriate course notation |
| Product website | Team GitHub Pages | Product-specific home page, current `Ui.png`, AboutUs, and working UG/DG links |
| PPP | `docs/team/<github_username_in_lower_case>.md` | **Optional this semester**, unless staff requests it to resolve contribution uncertainty |
| Product demo | None required | **Removed from this semester's requirements** |

The website still contains older generic wording about every student submitting a PPP and detailed demo instructions. The explicit semester-specific optional/removed notices take precedence. Do not create a demo deadline from the retired panel.

## Executable and source consistency

The final product must be releasable: it may defer some ideas, but its delivered features must be useful to at least some target users. "It compiles" does not establish usability. The source repository, JAR, live UG, and live DG must describe the same version.

Keep the JAR reasonably small even below the 100 MB cap. Unnecessary assets, excessive image resolution, or heavy libraries without a proportional user benefit can be reported as bloat. The course specifically warns against adding JavaFX WebView merely to display the UG because of its packaging cost. [File-size constraint](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).

For v1.3, release notes explain the changes from AB3 and include screenshots or recordings demonstrating them; attach the actual JAR. For v1.4, explain changes since v1.3 and attach the new JAR. Check that the downloaded release asset starts from a clean writable folder with the required Java version and that persistence survives restart. These are practical verification steps, not evidence that a release has already been submitted. [Week 9](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w9.html), [Week 10](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w10.html).

Final submission filenames, tags, Canvas instructions, deadline, freeze timing, and any rules about changes after submission must be reread from [Week 12](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/index.html) and [its TP page](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w12.html) once confirmed. Those weekly pages were tentative at this check, so this reference deliberately does not freeze their draft mechanics.

## User Guide

Write for the target user doing real tasks. Cover every current feature with correct command syntax, examples, results, necessary prerequisites, and meaningful limitations. A discrepancy can be a documentation bug or a functionality/design bug depending on which behavior ought to change.

The guide need not catalogue every rare validation detail when the information is obvious or the application gives a sufficiently informative message. Avoid burying the main workflow under edge cases. Do not omit a significant limit that would mislead an ordinary user, such as what duplicate detection actually detects.

General deliverable guidance allows future features if clearly marked as coming soon. Do not make them sound available; recheck later release instructions before keeping such material in the final UG. Only describe future features that the team has agreed to include.

Use screenshots where they teach something; each one creates future maintenance work. Crop a screenshot to the relevant controls when the rest of the UI is irrelevant. Ensure examples and screenshots match the release. Hierarchical section and figure numbering is optional, but readers must still be able to follow references through titles or working hyperlinks.

## Developer Guide

The DG should give future developers enough orientation to enter the code: the main architecture, where important logic lives, the relationships and interactions that are difficult to infer, and the rationale behind significant design decisions. It should complement the code, rather than narrate every statement or duplicate every implementation detail. Choosing sufficient detail is itself part of the assessed documentation skill.

Use precise prose, examples, diagrams, and occasional small snippets or pseudocode. Explain important tradeoffs and alternatives. A snippet should earn its maintenance cost; long pasted implementations become stale quickly. Main graded content belongs in the single `docs/DeveloperGuide.md` file so the grading tools can find it.

Keep the Acknowledgements accurate about external ideas, code, libraries, prose, diagrams, and AI assistance. Give the origin, links, and extent of reuse or inspiration. See [reuse and AI rules](03-workflow-and-quality.md#reuse-attribution-and-ai).

Proposed future implementations are optional. The existing proposed-feature sections may be removed or retained, but retained designs must still fit the current product; outdated proposals can attract DG bugs. A proposal can provide useful technical-writing/UML experience when an implemented feature offers little diagram work. Distinguish a hypothetical design from the current system and from an agreed team commitment.

### Requirements and testing appendices

| Section | What to maintain |
|---|---|
| Product scope | Actual audience, need, and value proposition; no contradictory product promises |
| User stories | All current user needs, with near-future needs where useful; sensible priorities |
| Use cases | All nontrivial, distinct interaction patterns in current features; important extensions and failures; avoid repetitive trivial use cases |
| Nonfunctional requirements | Specific, realistic, relevant expectations that the product actually meets or clearly identifies as future goals |
| Glossary | Domain terms that help the intended reader |
| `Instructions for Manual Testing` | **Required.** A path through all changed user-testable features with important copyable inputs and expected results; unchanged AB3 features can be omitted |
| `Appendix: Effort` | Optional, about one page; explain difficulty, challenges, effort, and achievements relative to AB3 |

Requirements already implemented must describe their actual implementation, including use-case steps. Requirements deferred to future iterations can remain. The instruction to retain useful future requirements is distinct from permission to remove an optional detailed proposed implementation.

The manual-testing appendix complements the UG. It does not need to enumerate every input variation or reproduce the entire UG. Include enough setup and representative operations for a tester to chart a path, then leave room for exploratory variations. Wrong instructions are reportable bugs. Independently execute important examples against the release before claiming this appendix is correct.

Other development pages such as SettingUp and DevOps are not graded TP deliverables. Updating them can count as contribution but is not required merely to match every product change. Leaving them in place is normally fine; deleting them can create broken links in the graded DG.

### UML and diagram guidance

Everyone should contribute to DG UML work. Updating an existing diagram is sufficient when appropriate; a new diagram is not universally mandatory. Try multiple diagram types where they explain different aspects. If the implemented changes offer no UML opportunity, a clearly labelled future design can provide one. Week 10 recommends starting this work early to learn the tools and discover misunderstandings before the final documentation rush.

Use the notation taught in the course. If it cannot express a detail cleanly, use the closest appropriate notation with a note, explain through pseudocode/prose, or show an explicitly simplified model. Avoid unfamiliar notation merely because a tool offers it.

PlantUML is the default and a good fit for incremental version control. Another tool is allowed if its editable source is committed and maintainable. Use a consistent tool across the team unless a concrete limitation warrants an exception. IDE-generated diagrams are risky: they can use different notation, include excessive detail, or infer design relationships incorrectly, such as composition or multiplicity. Inspect the meaning rather than accepting generated output as authoritative.

Keep each diagram focused:

- Omit irrelevant utility classes, members, parameters, routine self-calls, exception details, and repetition. Use notes where omitting a detail could mislead.
- Use representative classes or placeholders for repeated structures. The supplied AB3 example uses one representative command class rather than drawing every command.
- Show one component's internals while treating surrounding components as black boxes when that serves the explanation.
- Split large models into subcomponents or separate diagrams. Sequence `ref` frames and activity-diagram rakes can help.
- Use architectural interactions when internal object detail would obscure the point. Show relationships visually with suitable arrows rather than burying them in attribute text.
- Put diagrams near their explanation, give them readable text, and size their text consistently with the surrounding document.

The example popups illustrate excessive member detail, too many classes, overcomplicated sequence flows, and mismatched diagram/text sizing. The underlying example links are reference illustrations, not mandatory diagrams to reproduce.

## Website and team page

Publish the documentation through the configured GitHub Pages setup. Replace inherited AB3 branding and home-page content with the actual product while retaining appropriate origin attribution. Verify live pages, not only Markdown in the repository; broken navigation, images, and HTML rendering can make otherwise correct content unusable.

`Ui.png` must show the current product. Use realistic, suitably licensed or fabricated demonstration data; show the main capabilities in a populated state. Avoid irrelevant blank space, distracting annotations, garbage values, and uncropped desktop backgrounds. Use adequate resolution. The example tabs contrast annotated, sparsely populated, and poorly cropped images with clean screenshots.

AboutUs photos should normally be recent individual images with a clearly visible face. A lower-resolution photo is allowed for security concerns, and a placeholder is allowed for privacy concerns. Member names may be full names, partial names, or GitHub usernames. Link PPPs when members opt to provide them; the generic PPP-link instruction does not reinstate the semester's waived PPP requirement.

## Optional PPP

Use a PPP when contribution evidence is useful or staff requests it to settle a dispute. Publish the Markdown page under `docs/team/<lowercase-github-username>.md`; no PDF submission is required under the current deliverables page. It is useful portfolio evidence as well as an assessment aid.

Include a brief product overview, a link to the individual's code-dashboard view, implemented enhancements, UG and DG sections/diagrams, team tasks, reviews and mentoring, and any useful contributions beyond the team. Support claims with evidence and explain depth or difficulty where the result alone hides it. Do not inflate effort or take credit for reused work.

The opening one or two product-description sentences may be shared by team members; the rest should be individually written. UG/DG extracts and diagram reproductions are optional. Follow the linked AB3 PPP example as a starting format rather than treating its sample contribution claims as facts about this project.

## What a release check should establish

Before calling a release ready, note the exact commit and JAR, verify the relevant tests/build and a clean launch, test important real workflows and persistence, check UG command examples and DG/manual-test accuracy, open the published website, and inspect release metadata/assets. Check source attribution and course constraints alongside runtime behavior. Record any remaining uncertainty plainly.

Record the checks and their results in the release issue or PR. Coordinate publication and submission with the assigned team member. This checklist alone does not establish that StudentBook is ready for release.
