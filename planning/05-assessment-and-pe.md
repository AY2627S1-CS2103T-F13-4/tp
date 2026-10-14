# Assessment, practical exam, and bug triage

Checked 12 October 2026. Primary sources: [TP grading](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html), [Practical Exam, including its nested guidance and FAQs](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-pe.html), and [peer evaluations](https://nus-cs2103-ay2627-s1.github.io/website/admin/peerEvaluations.html).

**Recheck before the PE:** Weeks 11–13 are tentative. This file preserves the current standing assessment and PE procedures, not a confirmed future timetable. Before the PE, read its live page, the relevant weekly summary/admin page, and announcements. Confirm phase windows, repository availability, download/decryption instructions, support arrangements, and any revised script format. Do not submit or respond using these notes alone.

## How TP marks are allocated

TP is worth **50 marks: 45 individual and 5 team**. It is not bell-curved; CS2103T projects are assessed separately from CS2103 projects.

| Component | Marks | What matters |
|---|---:|---|
| Product design | 5 individual | Features form a coherent product suited to the target user; useful design choices and absence of significant feature flaws |
| Implementation | 15 individual | Code quality, plus sufficient personal implementation effort |
| Quality assurance | 10 individual | Own testing/escaped bugs: 7; finding bugs in the PE: 3 |
| Documentation | 10 individual | Quality of UG/DG and the individual's contribution, with documentation bugs considered |
| Project management | 10, comprising 5 team + 5 individual | Iterative process, timely work, Git/GitHub practice, and fair participation in shared work |

Code quality is inspected manually and automatically. Required conventions, sensible abstraction levels, little duplication, and appropriate error handling/defensive measures matter. Effort is estimated for the whole product by peer testers and tutors, then allocated to members using team evaluations, tutor judgment, and Git evidence. The published baseline says a team effort rating above 5 with a roughly equal share should satisfy the effort bar. More LoC does not by itself earn higher marks.

UG/DG quality is evaluated as a whole, then individual contribution is assessed and cross-checked. A polished team document does not establish that every member contributed. Keep honest evidence of authorship and review.

The contribution scale is relative to an equal share: for a four-person team, equal share is one quarter; "equal share + 10%" means 1.10 times that quarter, not an additional ten percentage points of the whole project. Implementation, UG, and DG contributions are rated separately. The product-design rubric distinguishes a clear narrow audience, useful value, efficient typing-oriented interaction, and features that fit together. UG/DG comparisons to AB3 use four choices: significantly lower, slightly lower, similar, or higher quality.

For project management, aim to complete at least **75% of tasks on time**, use meaningful issues/owners/milestones/releases, follow the prescribed workflows, and leave buffers. For the team-task aspect, do close to an equal share and have commits in **at least four of Weeks 7, 8, 9, 10, 11, and 12**. Avoid manufacturing trivial commits or misleading task histories to satisfy a counter.

## Peer evaluation and professionalism

Team evaluations cover implementation, UG/DG contributions, shared work, and professionalism. Be dependable, meet commitments, attend punctually, communicate progress and delays, and respond in a reasonable time (the guidance uses about 24 hours). Quality and care matter alongside effort. Technical competence, mentoring, and communication can also inform distinction/bonus judgments and tutor recruitment.

Peer judgments are inputs, not an automatic unverified transfer of marks. Staff cross-check contribution claims against the code and other evidence. Use confidential/anonymous feedback channels honestly when offered; do not assume a teammate's identity from a rating. The preliminary and midterm evaluations differ from the final evaluation; follow each week's actual instructions. If a contribution dispute requires evidence, the otherwise optional PPP can be requested. Each student must make their own honest contribution judgments.

## What PE assesses

PE measures testing, product evaluation, effort estimation, and the ability to report/respond to defects accurately. Tester credit and developer penalties are calculated separately; they are not a zero-sum exchange. Bugs in one's work affect different assessment components according to their nature. Developer testing considers **bug density**, not just raw bug count.

Tester marks are calibrated to how buggy the assigned product proves to be across its testers. If hardly any bugs are found collectively, more weight can be put on the tester's own developer-testing result. Full credit does not require discovering every defect and identifying the six objectively worst ones. Do honest focused testing and select the strongest reports you actually found.

Severity affects credit/penalty. Functionality, design, and documentation bugs affect different grade components; their testing credit is roughly comparable, so choose the correct type rather than trying to optimize labels. Penalties are divided equally among responsible assignees. Duplicate reports do not multiply the developer penalty, while different testers can receive credit for finding the same defect. A tester does not gain multiple credits for their own duplicates. Obvious defects can earn less credit. Incorrect rejection, severity reduction, or duplicate classification can hurt response-accuracy marks.

## Phase map

| Phase | Work | Planning guidance from the current PE page |
|---|---|---|
| 1: tester reports and evaluates | Test the assigned product and UG/DG, submit overall evaluations, select reports | About a 24-hour Friday-noon to Saturday-noon window; suggested active work: 60 minutes product, 30 docs, 15 evaluation, 15 selection |
| 2: developers respond | Assign responsibility, classify reports, explain changes/disputes, link duplicates correctly | Sunday–Tuesday after testing; aim to finish early enough to use the status report and buffer |
| 3: testers respond | Read developer responses and object only where justified | Wednesday–Friday after developer response; wait for the official start announcement |
| 4: tutor moderation | Staff resolve disputed classifications | Students normally have no further action |

These relative windows are orientation only. The PE page currently gives Phase 1 as **Fri 6 Nov, noon, to Sat 7 Nov, noon** (tentative; recheck nearer the date). Take all other dates from the live schedule. The longer windows are buffers, not an instruction to spend all available hours testing. Staff assistance is concentrated around the scheduled session.

## Phase 1: preparation and launch

- Accept the invitation to the course GitHub organization and verify the required Java **25** environment.
- Staff creates a private `pe-<your-GitHub-username>` repository shortly before the session. Use that issue tracker. Do not alter the supplied labels' names or colors. Extra convenience labels may be permitted, but do not give them the reserved `type.` or `severity.` prefixes.
- Obtain the encrypted assigned download in advance following the live instructions. The download, individually supplied password part, common password announcement, and nested ZIP contents have separate roles. Don't write your student ID or password into these shared notes.
- Extract the archive properly into a writable location. Read its README and the linked UG. Ignore packaging/padding files identified by the instructions. Do not launch a JAR from inside a ZIP.
- Check `java -version` and launch the actual extracted filename with `java -jar`, quoting paths when necessary. A clean folder avoids interference from another run's data. On Linux/Wayland, the course documents `GDK_BACKEND=x11 java -jar ...` for the specified GTK failure; use it only for that relevant environment/problem.
- If the assigned product cannot start after checking the documented setup and Java version, contact the named course support channel with the requested screenshot and GitHub username. The current page names Prof. Damith through Teams (`dcsdcr`), with email fallback; verify availability at the session. Do not choose an alternative product yourself. If staff assigns a replacement, close reports about the old product and continue only with the replacement.

PE is individual work. Automated scripts and AI are allowed, but each report must come from the student's **own testing**. Copying another person's discovered bug as one's own is academic dishonesty.

## Phase 1: scope, report format, and selection

Test the submitted JAR and the web-rendered UG/DG. The rest of the website, README, Markdown source, and code quality are not themselves PE documentation targets. Source inspection can help testing, but a code-quality concern needs an observable in-scope defect to become a PE report. Configuration/data files matter through supported behavior, including documented manual editing. Terminal output is lower-priority but can be a bug when misleading or alarming. Start with the manual-test appendix and expand into exploratory, system, and acceptance testing.

Each issue must be self-contained and report **one bug**. Include a specific title, setup/version/environment where relevant, reproducible steps, actual and expected results, and evidence of user impact. Put everything needed in the description: Phase 1 comments are not processed as part of the report. Do not use cross-issue links or sub-issues to bundle reports; IDs can change during transfer. Escape angle-bracket placeholders or put them in code formatting so GitHub does not hide them as HTML.

Use exactly one type and one severity label:

| Label | Meaning |
|---|---|
| `type.FunctionalityBug` | Implemented behavior does not work as it should |
| `type.FeatureFlaw` | A design choice or missing capability impairs the target user's task |
| `type.DocumentationBug` | Incorrect, missing, confusing, or badly presented UG/DG information |
| `severity.VeryLow` | Cosmetic/minor presentation issue without meaningful operational disruption |
| `severity.Low` | Minor issue or rare inconvenience; limited interference with normal work |
| `severity.Medium` | Meaningful occasional inconvenience, while the product can still be used |
| `severity.High` | Makes the product almost unusable for most intended users |

Use the live definitions, not this shorthand, for a borderline report. Explain both the chosen impact and why the next lower severity is insufficient. Prefer the lower severity when uncertain unless the evidence fits the higher one more closely. Missing severity defaults to Low; multiple severities lead to the lowest. Missing/multiple type labels can lead to an arbitrary type, so correct the metadata yourself.

Only reports/updates within the phase deadline count. Select **at most six** by leaving them open and closing the rest before the deadline; the highest-scoring **five** are graded. A report may be reopened before the cutoff. If more than six remain open, scripts prioritize severity, breaking ties by earlier issue number; excess reports are ignored. Select by impact, validity, and confidence, not bug type. Correct initial severity on an Accepted or NotInScope result can increase accuracy credit; the page gives an indicative 15–20%, not a guaranteed fixed formula.

Do not bundle numerous cosmetic defects under an extremely broad issue to evade the limit. If several distinct bugs are combined, developers may select one and discard the others. A valid defect remains valid even if the tester suggests the wrong fix or chooses the wrong type label.

## Phase 1: overall evaluation

Complete the assigned TEAMMATES evaluation; it is required and missing it can incur a penalty. Use the recovery-email facility if the link is missing. When the server is busy, wait several minutes instead of repeatedly reloading.

Evaluate product usefulness and cohesion for its stated user, especially efficient CLI use. Judge the UG's usefulness and the DG's value to future developers relative to the provided AB3 reference. The effort scale is 0–20, with implementing AB3 from scratch at 10 and a typical sufficient TP increment around 5. Exclude documentation, tests, and project-management work from this implementation-effort estimate, and do not adjust for team size yourself; the grading process handles that. Give sincere comments and concerns where requested rather than arbitrary ratings.

## Phase 2: developer responses

Received reports appear in the team's private course repository, `pe-<team-ID>`. Do not create replacement issues, transfer reports, or edit the original report title/body. Those actions can invalidate a response and leave the report treated as accepted. Closing/pinning reports for team organization is allowed in this phase; closed reports are still processed. Contact staff for a report assigned to the wrong product.

Assign each report to the member(s) responsible for that feature/section. No assignee, or assigning every member, counts as **unassigned**. No more than **50% of all received reports** may be unassigned. Exceeding that brings a **20% tax on each person's total bug penalty** under the current rules. All reports count toward that ratio regardless of response; duplicate reports inherit the original's assignees and still count individually. Responsibility for a report is separate from scheduling its fix; coordinate any code changes through the team workflow and applicable freeze rules.

Choose exactly one response label and ensure type/severity are correct even when disputing the report:

| Response | Use when |
|---|---|
| `response.Accepted` | It is a valid defect that should be addressed in the current product |
| `response.NotInScope` | It is a valid improvement, but justifiably lower-priority than the work delivered |
| `response.NotApplicable` | The alleged problem is not a valid defect; behavior is correct or the claim is mistaken |
| `response.CannotReproduce` | Reasonable repeated attempts with the available information cannot reproduce it |
| `response.IssueUnclear` | The report lacks enough understandable information to determine the issue |

Missing or multiple response labels default to Accepted. Do not confuse NotInScope with NotApplicable: the former acknowledges a valid potential improvement. Do not reject a real defect for a wrong label or incorrect proposed remedy; correct the label and explain which remedy is appropriate. Reporters cannot participate in an informal clarification conversation during this anonymous phase, so do not rely on asking questions in ordinary comments.

Put the developer explanation in **one comment starting with `# T`**, at most **500 words**. The latest matching comment is used. A comment is required when lowering severity, choosing a non-Accepted response, marking a duplicate, or changing type. Accepting entirely unchanged needs no justification. A type change needs the response comment but does not itself require a justification under the current instructions. Explain all disputed decisions clearly, using titles rather than unstable cross-repository issue numbers.

Unjustified rejection or downgrading is weak evidence if challenged. Moderators choose between supported positions; do not start with an exaggerated bargaining position in the hope of meeting halfway. Use a separate `# U` comment only for unprofessional conduct, not for an inaccurate or unpersuasive report.

### Duplicate handling

Use GitHub **sub-issues**: make duplicate reports children of one representative original report. Keep the structure flat; do not create chains of duplicates. The original's response/type/severity/assignees control the children. Each child's `# T` explanation must state why it is a duplicate. The tester receives both relevant explanations, so put any inherited severity reduction's rationale where it will be visible.

Reports are duplicates when they concern the same defect or a common underlying problem that cannot be fixed independently. Similar symptoms alone are insufficient. Two unrelated typos that can be corrected independently are not automatically duplicates. If the duplicate classification is overturned, the inherited classification can still matter under the stated process, so choose and justify it carefully.

Recommended team process: set an internal responsibility-assignment deadline, assign remaining reports by agreement, draft responses, and have teammates check them before the external deadline. Accurate fair responses matter more than reducing the accepted count.

## Phase 3: tester responses

Wait for the official announcement before acting; seeing script comments appear does not mean the transfer is finished. Responses are copied back to the tester's private repository. Read the report's listed aspects that may be challenged. The permitted objections are response, lowered severity, and duplicate status; **bug type is not an objection category**.

If you agree, no counter-response is needed. Otherwise, add a separate comment for each disputed aspect, **at most 500 words each**, starting with the exact applicable heading:

| Heading | Purpose |
|---|---|
| `# S` | Challenge the severity |
| `# R` | Challenge the developer response |
| `# D` | Challenge duplicate classification |
| `# U` | Report unprofessional wording/conduct |

For `# R`, explicitly say if the requested outcome is NotInScope; otherwise Accepted is assumed. Additional evidence may be provided, but it carries less weight than information included in the original report, especially if the developer could not reasonably infer it. Explain why the response is wrong instead of merely repeating the report.

Do not change the original title/body/labels, create/edit/delete repository labels, or close issues in this phase. Reopen an accidentally closed issue. Edit the existing objection before the deadline rather than creating multiple versions. No objection means agreement; optional agreement checkboxes do not replace a required objection.

To withdraw an objection, delete it or remove its special heading. Hiding it or checking an agreement box does **not** retract it. There is little value in disputing severity/duplicate status after agreeing to a response that earns no bug credit. NotInScope is different: severity still affects the value of the accepted improvement and the accuracy calculation.

The current rules offer an accuracy amnesty for agreeing with the developer's response, not for an incorrect original severity. An unsuccessful objection can reduce accuracy, particularly when better information was available. Correct final response/severity judgments can improve credit; published bonus percentages are indicative and should be reread before relying on them.

## Phase 4 and support

Tutors adjudicate disputes using the report, developer explanation, and tester objection. Students normally take no further action. Staff will not pre-decide whether a proposed report is a bug or assign its severity during the assessment; those are assessed judgments. General procedural questions can go through the designated channel with relevant context.

## Triage principles for design and implementation

Read the full [PE triage guidelines](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-pe.html) for a contested case. These points should also guide development before PE:

- **Inherited defects:** AB3 behavior is not automatically exempt. The team owns its delivered product.
- **Reproduction:** make a reasonable attempt to understand the issue even if steps are imperfect but screenshots/context reveal it. OS-specific defects are valid on supported systems; affected reach can influence severity. A transferred broken image may need staff assistance rather than instant rejection.
- **Scope:** NotInScope needs a credible priority/resource argument relative to work already done. A known limitation or coming-soon note is evidence, not immunity. Essential usability or an obviously better design achievable at similar effort is difficult to defer credibly. If equally persuasive subjective design arguments remain, the course's feature-design FAQ favors the developer.
- **Long/extreme inputs:** deliberate sabotage alone can fall outside scope, but ordinary mistakes must not crash the app, corrupt data, or make it unusable. Apply the same principle to numeric overflow. Reasonable length limits are allowed; limits must fit actual users. Clipping can be cosmetic or serious depending on information lost.
- **Validation:** accepting useful real-world inputs often matters more than enforcing a programmer's narrow format. Warn rather than block when flexibility is safe; handle harmful invalid input properly. A parser delimiter is not by itself a sufficient reason to exclude common legitimate names without considering impact.
- **Duplicates and search:** case/whitespace variants can represent the same entity. Warn about near matches where appropriate, allow sensible user choice, and explain detection limits. Case sensitivity should reflect the represented domain. Search should help the user recover likely results; the guideline's OR-versus-AND example is context-specific, not a command to rewrite every search.
- **Commands:** optimize frequent tasks for fast typing with memorable syntax; unnecessary case sensitivity, long keywords, or awkward characters can be feature flaws. Useful short aliases plus longer memorable forms can help.
- **Errors:** identify the actual problem and remedy where feasible. A generic message can be inadequate even when technically true. Distinguish invalid values from malformed input, or give an accurate combined message where further differentiation is justifiably lower-priority.
- **Manual data editing:** support at least AB3's promise that correctly edited files work. Stronger UG promises must be met. The human-editable storage constraint still applies.
- **Undocumented behavior:** hidden unreleased work may be outside the released scope if users are unlikely to encounter it. An exposed unfinished feature, or a useful non-obvious feature with missing instructions, can be a bug. Obvious intuitive behavior need not always have a lengthy UG entry.

## Triage principles for documentation

Broken links can be Low or Medium depending on their effect. Formatting, spacing, inconsistent alignment, or unreadable diagrams can be bugs even if a determined reader could work around them; minor presentation issues are usually VeryLow. An obvious typo, including text in the UI, is normally a VeryLow documentation bug, while an inconsequential grammatical preference may be NotInScope.

UML must use correct relevant course notation. Harmless extra tool decorations are different from misleading notation. Consistent omission of optional notation can be acceptable; intentional abstraction should be clear, whereas accidental omissions that misrepresent the design are defects. Do not assume zooming excuses tiny text. Incorrect requirements, missing important interaction cases, and unrealistic/unmet NFR claims can also be documentation defects or design flaws depending on what needs changing.

A UG need not contain every validation corner case when the product informs the user adequately. Conversely, claims must match behavior and important limitations must be disclosed. Choose bug type by the needed correction: change behavior/design for a product defect, or correct the guide for a documentation error. Neither a misleading guide nor a poor design is excused merely because the two agree.

The grading checklist also flags too few or too many visuals/examples, visuals disconnected from the explanation, repetitive content, unexplained audience-specific jargon, and descriptions that are too short or unnecessarily long. DG-specific risks include non-intuitive architecture symbols, indiscriminate double-headed arrows, low-level detail in an architecture view, an unsuitable diagram type, non-UML diagrams where UML would serve equally well, excessive code extracts, mismatched user-story parts, missing important stories/use cases, bad step numbering, unnecessary UI details in use cases, missing extensions, and vague or implausible NFRs. Check whether each glossary entry actually helps the intended reader.
