# Weekly TP plan, Weeks 5–13

Checked 12 October 2026. Times are Singapore time. These are course obligations, not a claim that the team has completed them. `Each` means every student; `Team` means a coordinated team deliverable, often submitted by one representative.

## Calendar and deadline rules

| Week | Monday | Published TP focus | Main dated requirement |
|---|---|---|---|
| 5 | 7 Sep 2026 | Requirements and MVP user stories | Stories before tutorial; MVP selection before/during tutorial |
| 6 | 14 Sep 2026 | Define MVP behavior; set up repo; learn AB3 | Feature-spec PDF by Sun 20 Sep, 23:59, time-sensitive |
| Recess | 21 Sep 2026 | No extra numbered TP week | Do not shift Week 7 to 21 Sep |
| 7 | 28 Sep 2026 | v1.1 workflow practice and documentation | Thu 1 Oct, 23:59 |
| 8 | 5 Oct 2026 | v1.2 first functional increments | Thu 8 Oct, 23:59 |
| 9 | 12 Oct 2026 | v1.3 working MVP release | Thu 15 Oct, 23:59 |
| 10 | 19 Oct 2026 | v1.4 alpha; intended final feature set | Thu 22 Oct, 23:59 |
| 11 | 26 Oct 2026 | Tentative; see links below | Recheck live instructions |
| 12 | 2 Nov 2026 | Tentative; see links below | Recheck live instructions |
| 13 | 9 Nov 2026 | Tentative; see links below | Recheck live instructions |

The general weekly-task deadline is Friday 16:00, the briefing start, unless a task gives another deadline. Iterations normally end Thursday 23:59. The Friday briefing reviews the current week and introduces the next; starting next week's work then creates a buffer. Read that week's learning topics before its project tasks, and aim to finish most work before the tutorial.

Occasional ordinary weekly-task lateness can be repaired within a few days; scripts generally stop checking interim work about a week later. This flexibility does not override explicit/time-sensitive deadlines. Initial v1.1/v1.2 iteration overruns have a learning allowance. **From v1.3, an iteration not properly wrapped up by its deadline leaves its "on time" dashboard item red.** Do not treat earlier tolerance as permission to defer v1.3. Source: [weekly schedule](https://nus-cs2103-ay2627-s1.github.io/website/admin/weeklySchedule.html) and the weekly TP pages.

## Week 5: gather requirements

Sources: [summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week5/index.html), [TP instructions](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w5.html), [project tab](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week5/project.html).

- **Team, before tutorial:** Follow the textbook's user-story brainstorming recipe. Record the persona, problem scope, narrative/user journey, and intermediate reasoning in the collaborative notes. Think broadly, even beyond the semester's likely delivery scope. Include needs that AB3 already supports.
- Aim for at least **30 user stories**, including obvious and less obvious needs. Preserve the prescribed role, capability, and benefit structure. Include the benefit in most stories so priorities can be justified. Break large epics into work a person could eventually implement in roughly one or two days.
- Use a publicly viewable online list or sheet. Link it from the main notes document if separate. GitHub issues are an optional alternative, requiring the team repo and tracker first.
- **Team, before/during tutorial:** Choose the smallest set of needs without which the product would be unusable. Inconvenience alone does not make something mandatory. Mark deferred stories rather than deleting them. Divide the first screening among members, then discuss the remainder together. Keep nice-to-have stories separate.
- Do not turn this step into UI, syntax, or implementation design. There is no requirement for the selected stories to be new relative to AB3. If work distribution later needs more features, expand then. MVP implementation is not this week's task.
- Recommended AI use: write the persona/journey yourselves, then ask AI to identify weak assumptions, missing users, missing stages, confusion, and abandonment points. Create any persona card only after agreement and do not invent user traits.

**Completion evidence:** Main notes clearly expose the persona/scope/journey, the public story list, and the selected MVP subset.

Related obligations: the preliminary TEAMMATES evaluation was compulsory by **Thu 10 Sep, 23:59**; received ratings were for practice, not grading. Optional course feedback was due Sat 12 Sep. The tutorial covers story quality, prioritization, and, if time permits, the MVP feature list. [Admin](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week5/admin.html) · [Tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week5/tutorial.html).

## Week 6: define the MVP and prepare to code

Sources: [summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/index.html), [TP instructions](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w6.html), [project tab](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/project.html).

- **Team:** Translate the chosen stories into the smallest usable feature list and record it in project notes.
- **Team, one submitter, Sun 20 Sep 23:59:** Produce the complete MVP **feature specification** as `{team-id}.pdf`, which for this team is `CS2103T-F13-4.pdf`, and upload to the designated Canvas assignment. This is time-sensitive with no grace period. Retain the submission receipt; merely having a local PDF proves nothing about submission.
- For every required feature, specify its name and purpose, exact syntax, examples, each parameter's accepted values, whitespace/case rules, validation rationale and exact error message, exact successful output and GUI effects, and each failure outcome. Cover missing, repeated, invalid, and conflicting inputs. Define contact-duplicate matching and acceptance/rejection behavior. Include other possible errors and low-fidelity UI mockups, or AB3 screenshots if the UI truly stays identical.
- This is an internal behavior specification, not a polished UG and not an implementation design. Avoid internal classes/algorithms. Include retained must-have AB3 features even if unchanged; optional AB3 features may be omitted. New features are not mandatory. Restrictions may be justified compromises, but record their consequences and workarounds. Mark finer details as must-have or nice-to-have where useful.
- Agree cross-feature consistency. Divide specification writing so each person contributes meaningfully, preferably for the feature they will implement and document later.
- **One member/team:** Create the correctly named GitHub organization, `developers` team, forked `tp` team repo, issue tracker, Actions, Codecov, GitHub Pages site, appropriate member permissions, milestones, and the tracking PR to the course repository. See [workflow](03-workflow-and-quality.md).
- **Each:** Watch and fork the team repo without renaming it, clone it, and configure the development environment. Preserve `src/main/java`, `src/test/java`, and `docs`.
- **Each, ideally Week 6, allowed through Week 7:** Complete the command-tracing tutorial and create an issue titled exactly `Tutorial: tracing code` with 2–3 debugger screenshots. It may then be closed. Complete the adding-command tutorial on a branch exactly `tutorial-adding-command`, push it to the personal fork, and open a PR to team `master`. That tutorial PR may be closed without merging. Updating tests is recommended for learning, not mandatory for this tutorial. Removing a field is optional, but skim it; no submission is required.

**Completion evidence:** Public feature list/specification, correct Canvas receipt, repository/site/tracking PR, and individual tutorial issue/PR artifacts.

Related tutorial: demonstrate a breakpoint and stepping in an IDE; save the screenshot and put it in `T6-Workspace.pptx` when directed. Conditional breakpoints are optional. Do the sequence-diagram exercises. [Tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week6/tutorial.html).

## Week 7: v1.1 workflow practice

Sources: [summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week7/index.html), [TP instructions](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w7.html), [project tab](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week7/project.html).

- **Team:** Set v1.1's milestone deadline at or before **Thu 1 Oct, 23:59**. Practise the workflow together, preferably synchronously.
- **Each:** Create a photo task issue, self-assign it, assign v1.1, implement on a separate personal-fork branch, assign the PR to v1.1, obtain teammate review, and merge. Photo path is `docs/images/<lowercase-github-username>.png`. The page explicitly accepts that `.png` filename even if the image originally came as JPEG. Review the name, path, branch/fork origin, and commit messages.
- **Each:** Create another issue and parallel PR updating your own `AboutUs.md` information. Wait for members to send their PRs, review them, then merge one at a time and resolve conflicts. If someone cannot complete the task despite reasonable help, the rest can proceed and inform the tutor. Record GitHub/profile links and roles/responsibilities. A homepage is optional; PPP links can be removed until needed. Do not split the prescribed file.
- **Team, with individual PR contributions:** Update README branding/content, the Actions badge, and AB3 attribution. Create `docs/images/Ui.png` as one intended-product UI mockup with roughly the original proportions. Update site-wide product naming, using the appropriate Jekyll or MarkBind settings. Confirm the published site and course project list show the images correctly.
- **Team, individual authorship:** Add DG target user, value proposition, prioritized stories, representative multistep textual use cases, relevant achievable NFRs, and glossary. Include the wider requirements, including deferred ones; distinguish them from delivered features. Put primary content in `docs/DeveloperGuide.md` and UG content in `docs/UserGuide.md`, without splitting those files.
- Ideally complete all three main task groups in v1.1. The FAQ allows unfinished README/DG work to move to v1.2. Stronger teams may start the next iteration early. Use the tutorial's requirements-review exercise to improve the DG afterward in the appropriate next milestone.

**Completion evidence:** Each person's photo and AboutUs PRs/issues, teammate reviews and conflict resolution where needed, published branding/images, DG requirements, and a properly closed milestone.

[Tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week7/tutorial.html) also requires preparation of PR Tracker stories, NFRs, glossary terms, and a use case, followed by review of a sample answer and application of lessons to your own DG.

## Week 8: v1.2 first functional increments

Sources: [summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/index.html), [TP instructions](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w8.html), [project tab](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/project.html).

- **Team:** Reconfirm the MVP and simplify both the feature set and each implementation. Allocate features to people rather than splitting all work by architectural layer. Package/class renaming is optional; if wanted, this is the suggested time and one person should coordinate it.
- **Each, Thu 8 Oct 23:59:** Merge at least one meaningful functional-code PR toward your allocated feature. A small safe step is enough; a whole feature need not be completed in one PR or within this iteration. Keep existing behavior/tests working.
- For each increment: create a task issue, self-assign and attach v1.2, implement on a separate personal-fork branch, attach the PR to v1.2, get review, merge, and close its issue. Repeat in small increments; independent work may use parallel PRs.
- **Team:** Move unfinished issues/PRs to v1.3 and close v1.2. **A product/JAR release is not required for v1.2.**
- Read the reuse policy and check method JavaDoc phrasing. Consider the published bug-triage guidance while designing validation, duplicate detection, error messages, long-input handling, and manual-file-edit behavior. See [quality](03-workflow-and-quality.md) and [PE](05-assessment-and-pe.md).

**Completion evidence:** A merged functional contribution by each member, correct task/PR ownership and milestones, reviews, and milestone closure. Do not infer compliance from an open PR alone.

Related obligations: midterm TEAMMATES evaluation by **Sun 11 Oct, 23:59**. Low interim ratings need improvement before the final evaluation. The tutorial page separately requires hand-drawn class/object and sequence diagrams plus the recorded tutorial's in-video quiz by **Mon 12 Oct, 23:59**. These are tutorial obligations, not v1.2 release criteria. [Admin](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/admin.html) · [Tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/tutorial.html).

## Week 9: v1.3 working MVP

Sources: [summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/index.html), [TP instructions](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w9.html), [project tab](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/project.html).

- **Team, start of iteration:** Reassess the smallest workable MVP using v1.2 experience. Enumerate tasks, dependencies, order, and per-task timing. Create issues, assign owners and v1.3, and record any internal due dates in descriptions or labels.
- **Each:** Deliver assigned work through reviewed PRs. Give substantive reviews, including useful comments at relevant lines. Update the UG alongside behavior changes where possible.
- **Team, by Thu 15 Oct 23:59:** Deliver a working MVP, make the GitHub release, attach the runnable JAR, write reasonably detailed release notes describing changes since AB3, and include screenshots or recordings demonstrating those changes. Staff check both notes quality and evidence of MVP functionality.
- Adjust scope and reschedule pending work early enough to release on time. After the release, reconcile completed and pending issues/PRs with milestones and close v1.3 by the deadline. A closed milestone without the required release is not the whole deliverable.
- Avoid drastic `build.gradle` changes and adding web libraries merely to display help. Investigate local-versus-CI failures and integration failures; do not assume pre-merge success proves the merged product works.
- Record whether delivery was on time and how effort estimates compared to actual effort; Week 10 uses those observations.

**Completion evidence:** Owner-assigned iteration plan, merged/tested features, versioned release with JAR and illustrated release notes, correct milestone contents and timely closure.

Related [tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week9/tutorial.html): prepare an IDE coverage screenshot from the iP, a hand-drawn conceptual class diagram, and an activity diagram. Post them in the tutorial workspace when directed. Model the problem domain in a CCD rather than implementation classes.

## Week 10: v1.4 alpha and documentation practice

Sources: [summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week10/index.html), [TP instructions](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w10.html), [project tab](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week10/project.html).

- **Team, before tutorial:** Hold a v1.3 postmortem. Record what helped/hurt the process and how the next iteration will improve it in the collaborative notes.
- **Team:** Plan as though only one week remains for implementing the intended final feature set. Set scope, priority, dependencies, timing, issues, and owners. Aim to deliver all intended features at alpha quality; reserve the next iteration for testing and polish.
- **Each/team, Thu 22 Oct 23:59:** Implement incrementally, maintaining a working product. Release v1.4 with a JAR and detailed changes-since-last-release notes. Apply the postmortem's process improvements.
- After completing v1.3, using branches directly in the team repo is allowed if desired; PRs are still required. Until then, retain the personal-fork workflow.
- **Each, during the TP:** Have overlapping/parallel PRs at least once. Use separate branches and minimize overlap so they can be reviewed independently.
- Maintain appropriate assertions, exceptions, logging, and defensive checks. Enable assertions in Gradle and the IDE's actual run configuration. Avoid auto-sized windows that break on other displays.
- **Each, recommended:** Update at least one relevant UML diagram in the DG. This is a separate weekly task, not part of the Thursday v1.4 release deadline. Its ordinary deadline is **Fri 23 Oct, 16:00**, with the usual short catch-up allowance. Text updates can follow later. Use the UML/diagram guidance in [deliverables](04-deliverables.md).
- Read the feature-freeze rules now, because they constrain the next release cycle. See [workflow](03-workflow-and-quality.md#feature-freeze-rule-published-in-week-10).

**Completion evidence:** Postmortem, planned/owned alpha tasks, tested v1.4 JAR release, parallel-PR evidence for each student, and diagram contributions.

Related [tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week10/tutorial.html): demonstrate a deliberately triggered assertion failure locally and save a screenshot, prepare defensive-programming examples, and review `T10-DG-Extracts.pdf` from Canvas. Remove the deliberate failure afterward; do not commit it as product behavior.

## Weeks 11–13: links only until republished

These weekly summaries explicitly marked the material tentative on 12 October. The sidebar currently labels the stages v1.5 release candidate, v1.6 public release, and post-release tasks, respectively. Those are orientation labels, not confirmed submission instructions.

| Week | Pages to reread before planning |
|---|---|
| 11 | [Summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/index.html) · [TP](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w11.html) · [Project](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/project.html) · [Admin](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/admin.html) · [Tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week11/tutorial.html) |
| 12 | [Summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/index.html) · [TP](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w12.html) · [Project](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/project.html) · [Admin](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/admin.html) · [Tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week12/tutorial.html) |
| 13 | [Summary](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week13/index.html) · [TP](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w13.html) · [Project](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week13/project.html) · [Admin](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week13/admin.html) · [Tutorial](https://nus-cs2103-ay2627-s1.github.io/website/schedule/week13/tutorial.html) |

When a week becomes current, remove the link-only status only after checking its summary banner, project page, admin page, tutorial, Canvas announcements, and all relevant dropdowns. Then record exact deadlines, submission destinations/formats, responsible people, verification evidence, and any freeze restrictions. Do not derive dates by blindly adding seven days to a previous release.
