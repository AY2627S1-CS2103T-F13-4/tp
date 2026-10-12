# Workflow, code quality, and reuse

Read [README](README.md) first. Checked 12 October 2026. The weekly instructions can make an otherwise recommended practice mandatory for that week's exercise.

## Repository and project tracking

Use the team organization and its `tp` repository; do not rename that repository or the personal fork used for course tracking. The organization name follows the course convention, here `AY2627S1-CS2103T-F13-4`. Keep the organization on the free plan, use a developers team, and give members the access they need. One or two administrators with the other members having write access is the suggested arrangement. Branch protection with reviews and status checks is recommended, not an independently graded requirement to buy a paid plan.

Enable Issues, GitHub Actions, Codecov, and the project website. The default website uses Jekyll; MarkBind is an allowed alternative using the supplied migration branch and instructions. Maintain the course tracking PR from the team repository's `master` to the course repository's `master`. Its title is `[Team ID] ProductName`; its short plain-text description identifies the target user and value proposition. Keep it current when those change. Do not change a GitHub username during the course and grading period without notifying staff.

Create milestones `v1.1` through `v1.6`. Tasks should be small enough to estimate and review, typically a few hours rather than an entire multiweek feature. Give each task a clear outcome and one accountable owner. Multiple people may help, but ambiguous ownership makes tracking harder. Issues can represent coding, documentation, testing, administration, and other substantial work; ordinary trivial steps need not become separate issues. Use checklists or GitHub sub-issues for subtasks when helpful.

The suggested labels distinguish type (`Epic`, `Story`, `Enhancement`, `Task`/`Chore`, `Bug`), priority, and severity. Labels are useful metadata, not a reason to spend time classifying every tiny action. Match the actual team convention rather than renaming all labels to a sample scheme. A corresponding issue for every PR is generally encouraged; follow the specific weekly task when it explicitly requires one. PR authors already identify who did the work, so a PR assignee is not generally needed. The milestone is needed for iteration tracking.

Sources: [GitHub appendix](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixE-gitHub.html), [Week 6 setup](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w6.html), [Week 9 planning](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w9.html).

## The normal iteration

1. **Plan:** agree on the working product to deliver, split it into issues, identify dependencies and internal deadlines, assign owners and the milestone, and leave a buffer before the course deadline.
2. **Implement:** make a small useful change on a separate branch. Through v1.3, practise the personal-fork workflow. After v1.3, branches in the team repository with PRs are allowed. At least once, each member should have practised parallel PRs; do not manufacture dependent parallel changes merely to increase the count.
3. **Review:** open a descriptive PR, put it in the milestone, link the issue where applicable, and get a teammate to inspect it. Prefer code, tests, and related documentation together once behavior is stable.
4. **Integrate:** merge frequently to the team's `master`. Sync ongoing branches with new `master` changes. Keep tests and the product working. Only work merged to `master` is recognized by the normal grading scripts.
5. **Close:** close completed issues, move unfinished issues and PRs to a later milestone, publish the required release, then close the milestone. A milestone containing overdue unfinished work does not accurately represent the delivered iteration.

Small breadth-first increments matter more than merely following Git commands. Each increment should move the usable product forward. Long-lived feature branches are discouraged: if one is necessary, keep it synchronized and integrate it at least once per iteration. Avoid big-bang integration at the end.

Use merge commits where the course recommends them. Avoid rebasing or squashing away the original contribution timestamps used for tracking, and retain PR branches where the course workflow asks you to. The team decides who merges; that role does not remove the need for meaningful review of nontrivial changes. Do not rewrite other people's branches casually.

Initial iterations allow some deadline adjustment while learning. From v1.3, an iteration not wrapped up properly by Thursday 23:59 leaves the dashboard's on-time item red. Do not confuse the general Friday 16:00 weekly checkpoint with the Thursday release/iteration deadline. See [the weekly plan](02-weekly-plan.md).

## Review and CI

The course expects peer review and values useful comments attached to specific code locations. It does not prescribe a universal number of reviewers or an identical depth for every change. Adjust rigor to risk and the quality of automated checks. A passing test run does not establish that a change fits the target user or team design.

In reviews, explain the concern and its effect, ask about intent when unclear, and suggest an alternative when useful. Keep comments civil, specific, and understandable to future readers. Acknowledge sound choices where useful. Point out a repeated pattern with a few examples rather than flooding the PR. Authors should explain disagreements calmly and acknowledge reviewers' effort. [SE-EDU review guidance](https://se-education.org/guides/guidelines/PRs-reviewing.html).

AI may assist with reviews, but the student must vet the findings before posting. A noisy or incorrect AI review is still the student's graded contribution.

When CI fails, first identify which check, environment, and change caused it. Coverage warnings can be an informed exception when a part is unsuitable for automated testing or tests are deliberately deferred. Codecov targets can be adjusted; Codecov itself is not graded. This is not a blanket reason to ignore compilation failures, broken behavior, or style violations. Restore a useful passing CI state promptly. Occasional failures do not automatically incur a direct penalty; prolonged failures remove regression protection and can reveal separately penalized problems.

If local tests pass but a PR fails, compare OS behavior and reproduce the PR's merge with current `master`. PR CI tests the proposed combined state, not necessarily the exact standalone branch tested locally. If `master` changed after CI passed, synchronize and rerun before merging. File case, separators, locale/time assumptions, and platform-sensitive behavior deserve attention; do not assume a filesystem's case sensitivity solely from its OS name.

Sources: [Week 8 testing/CI FAQs](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w8.html), [Week 9 review and CI FAQs](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w9.html).

## Required coding and commit conventions

The course requires SE-EDU's **basic and intermediate Java rules**. Advanced Java rules, the Markdown guide, and Google's documentation style are optional. Checkstyle checks only part of the required standard. The live standard is the complete reference; read it when reviewing code rather than treating this reminder as exhaustive. [Course conventions](https://nus-cs2103-ay2627-s1.github.io/website/admin/standardsAndConventions.html).

Java review reminders: use meaningful English names, lower-case packages, PascalCase types, camelCase members, uppercase underscore-separated constants, Boolean-sounding predicates, and plural collection names. Use four-space indentation, eight extra spaces for continuations, and a 120-character hard limit (110 preferred). Use braces, consistent explicit imports, narrow variable scope, and encapsulated fields. Keep layout and whitespace consistent. Document classes and public methods; exemptions include ordinary accessors, tests, and overrides whose inherited contract applies unchanged. Javadoc opens on its own line, starts a method summary with a third-person verb such as "Returns," and sits immediately above the declaration. Include useful parameter/return/exception details, with all parameters documented or none under the standard's omission rule. Use American English. [Complete Java standard](https://se-education.org/guides/conventions/java/intermediate.html).

Every commit needs an imperative, initially capitalized subject without a final period. Aim for 50 characters, with 72 as the hard limit; a meaningful scope/category prefix is allowed. A body is optional for this course. If present, separate it with a blank line, wrap at 72 characters, and use paragraphs explaining the change and its reason. More elaborate conventions are optional. [Git conventions](https://se-education.org/guides/conventions/git.html).

## Code quality and testing expectations

Each member should contribute functional code and some automated tests, plus their own UG/DG updates. There is **no minimum coverage percentage** and no requirement to automate GUI testing. Choose tests by risk and usefulness. Manual GUI/system testing remains valuable. A useful strategy checks ordinary flows, invalid inputs, boundary cases, interactions, persistence, and regressions introduced by a change.

Early iterations may preserve existing tests without immediately adding new ones. Temporary disabling during volatile development or a draft PR is discussed as an option, with explicit risks: hidden regressions, accumulating test debt, and forgotten re-enablement. Removing tests that are not worth maintaining is also a tradeoff, not a free quality improvement. By final assessment, weak tests can cost marks through escaped bugs and lack of evidence of testing skill.

Code-quality assessment looks for sensible abstraction levels, manageable methods and nesting, little duplication (including tests), and appropriate use of exceptions, assertions, logging, and defensive programming. Do not insert meaningless assertions or log messages to tick boxes. Inspect the relevant code beyond the changed lines and check the behavior the feature promises.

GUI automation is optional because maintaining it across desktop/CI environments is difficult. AB4 can be studied for TestFX examples, but cite reused AB4 code. Tests may be selectively enabled in environments where they work.

AB3 is a starting point, not a flawless reference solution or an architecture for every kind of product. Investigate its design rationale through documentation, comments, history, and small experiments before major refactoring. Make independent reversible changes. Avoid drastic `build.gradle` rewrites without a concrete need; Week 9 specifically warns that unnecessary web-display dependencies can make the JAR exceed the size limit.

Sources: [Expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html), [Grading](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html), [Week 9 cautions](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w9.html).

## Reuse, attribution, and AI

The reuse policy covers code, comments, prose, diagrams, images, and other deliverables. Work publicly shared by classmates or past students can be reused with credit. External work also requires compliance with its license. Public availability alone is not permission to reuse an image or audio asset. Third-party libraries need course approval; an existing approval for another student/team is sufficient, but check its applicability. Licensing remains the reuser's responsibility. Record sources immediately, while the origin is known. [Official reuse policy](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html#policy-reuse).

| Kind of reuse | Course attribution practice |
|---|---|
| Studied an external solution, then wrote your implementation | Nearby comment identifying the source and that it inspired the solution |
| Copied and substantially adapted a solution | Nearby comment identifying the source and adaptation |
| Nontrivial copied code, with at most minor edits | Source comment plus a delimited `@@author <github-username>-reused` block, ending with `@@author` |
| Third-party library | Accurate DG Acknowledgements; mention significant relevance in a PPP if one is being provided |
| External feature idea, documentation, or diagrams | Identify source, link, and extent of inspiration/adaptation in the relevant acknowledgement/location |
| Reused image/media | Use an appropriately licensed asset and credit its source where it appears; the guide gives the first relevant UG screenshot as an example, with README acknowledgement also possible |
| Normal AB3 brownfield design/code | No per-block credit needed merely for retaining AB3's structure; preserve the required project-origin acknowledgement |
| AB4 code | Treat as external reuse and cite it |
| Course teaching resources | Explicit credit is optional under the course policy |
| Work reused within this team | No explicit source annotation required, but account for it fairly when estimating each person's effort |
| AI used in a few localized methods/classes | Cite the tool's use near those locations |
| Widespread AI assistance | DG Acknowledgements states the tool, who used it, and the extent of use; this replaces repetitive local AI annotations |

Do not count another author's work as your own implementation effort. Students remain accountable for generated code, explanations, tests, documentation, and reviews. Keep contributions understandable enough to explain in course assessment. Do not silently remove required attribution or claim it exists without checking the actual public artifact.

## Feature freeze: rule published in Week 10

Week 10 introduces the freeze that starts at the **v1.5 deadline**. The future weekly pages are tentative, so obtain the final start/end dates from their live instructions before acting. This section preserves the already-published mechanism, not a fixed Week 11/12 submission schedule. [Week 10 feature-freeze rules and FAQs](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w10.html).

Each member's functional-code changes during the freeze must stay within the greater of **15% of that person's final functional LoC or 150 lines**. Functional code is code files under `src/main`; blanks and comments in those files count. Test files, documentation, and non-code assets do not count in that category.

The numerator counts lines present in the final version, attributed to that member, that were added or edited during the freeze. Repeated edits to the same surviving line count once. Deletion does not add changed lines but can shrink the final denominator. Cosmetic refactoring still changes lines. Do not move code, alter attribution, or borrow a teammate's allowance to evade the rule. Legitimate help is allowed with honest attribution.

The published rules describe a five-percentage-point tolerance and a case-dependent penalty with a minimum of two marks beyond it. Plan to the stated allowance, not to the tolerance. The dashboards update periodically (about three hours); use the functional-code and freeze views and inspect anomalies rather than trusting a stale figure. Author annotations can affect automated attribution.

There is no separate categorical ban on a particular kind of edit in the published freeze rule, but new features are strongly discouraged. Prioritize fixes by user impact, confidence, and remaining allowance. Documented workarounds and lower-priority improvements can affect bug triage, but do not make an actual serious defect disappear. If a proposed fix crosses the allowance, compare the risks rather than automatically expanding scope; the rule notes that an individual bug penalty does not exceed two marks. Reverting a change before the final submission can remove that change from the freeze calculation.

## Checklist for a task

- Before starting: check the current week, milestone, feature owner, agreed behavior and relevant course rule, and talk to the owner.
- While working: keep the change focused, run the checks that match its behavior, and review the code that calls it.
- In the PR: say what you checked. Keep course planning updates in this directory and product instructions in the UG/DG.

Before marking a task done, link the evidence: the merged PR, release or submission. A local edit is not proof of a merged PR, published release, or submitted assignment. Update the relevant issue and milestone when the work reaches the required state.
