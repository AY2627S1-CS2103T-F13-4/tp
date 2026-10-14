# Standing TP rules

Checked 12 October 2026. Read [Overview](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-overview.html), [Expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html), [Constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html), and their nested FAQs for authoritative wording.

## Product direction and scope

Evolve AddressBook Level 3 into a better contact-management product for a clearly specified, narrow target user. Contacts must remain the main focus even if the product also manages related entities such as lessons, tasks, or payments owed. Define the problem, value proposition, and scope boundary clearly. Narrow the audience only where the product will actually support that specialization.

Fast typists who prefer typed commands are the required audience. A CLI inside a GUI is appropriate; a terminal-only interface is not required. Optimize frequent actions for efficient one-shot commands. Mouse input can supplement commands. Multistep prompts can help beginners or rare workflows, but regular users should have an efficient direct route.

Market size and novelty do not earn extra marks. Even a single-user target market can be acceptable if evaluators can understand it. A coherent, reliable product matters more than unusual or numerous features. Scope changes require team agreement. Do not add an unrelated feature solely to increase contribution statistics.

Develop breadth-first in working increments. Plan each iteration precisely using what is known now, while keeping the overall direction stable. Do not complete the entire project in one burst and leave later weeks empty.

## Mandatory constraints

| Official constraint | Required behavior / practical check |
|---|---|
| Brownfield | Evolve the supplied codebase. Major redesigns are allowed through small working increments. |
| Typing-Preferred | Target users who type quickly and prefer typing. |
| Single-User | One user's application and data. A shared computer used by different users, or routine sharing of one user's data file with another, is outside the permitted model. |
| Incremental | Maintain reasonably consistent delivery across the project duration, using breadth-first increments. |
| Human-Editable-File | Store data locally in human-editable text. Manual editing support must be at least AB3's. Encryption is discouraged and permitted only with easy decryption, manual editing, and re-encryption. A secure personal computer may be assumed. |
| Data-Sandbox | Every application file read/write must remain in the directory containing the JAR or its descendants. Do not change OS configuration. The process working directory is not necessarily the JAR directory. |
| No-DBMS | Do not use a database management system for storage. |
| OO | Use object-oriented design as the main approach; justified use of other styles is allowed. |
| Platform-Independent | Run on Windows, Linux, and macOS. Avoid platform-dependent libraries or behavior even if manual access to every OS is unavailable. |
| Java-Version | Run on a machine with Java 25 and no other Java version installed. |
| Portable | Require no installer. |
| No-Remote-Server | Do not depend on a server operated by the team. |
| External-Software | Libraries/frameworks must be free, open source, and permissively licensed. The open-source condition does not apply to services. Users must not need extra installations. Avoid service-account requirements. Obtain teaching-team approval through the forum before adopting a library/service; an existing approval for another team is sufficient. Other constraints still apply. |
| Screen-Resolution | Work comfortably at 1920×1080 and above with 100% and 125% scaling. Keep every function usable at 1280×720 and above with 150% scaling, even if less comfortable. |
| Single-File | Deliver one executable JAR. If packaging everything into a JAR is impossible, one ZIP containing the JAR and necessary files is the fallback. |
| File-Size | Product JAR/ZIP must not exceed 100 MB. Remove unnecessary libraries/assets and excessive image quality; avoidable bloat can itself be reported as a bug. |

The WebView caution matters: adding JavaFX web support merely to display an HTML User Guide can greatly inflate the JAR. Verify the actual packaged artifact, not only dependency declarations.

## Recommendations with grading implications

- Keep network use minimal. Reliable public APIs are possible, but provide a useful offline fallback. Inability to test a feature because the network fails can count as a major bug. Obtain NUS IT's approval before using/scraping NUS data; course staff cannot obtain it on the team's behalf.
- Make features easy to test manually and automatically. Remote API credentials, account setup, and audio features create problems for testers, especially under exam conditions.
- Use realistic target scenarios suited to a small standalone application. Do not claim suitability for an organization whose actual scale would require a different system.
- Clarify uncertain constraint interpretations through the course forum or professor. This reference does not grant an exception.

## Individual and team expectations

The effort reference is approximately half each person's iP implementation effort. The 500–600 functional LoC estimate is only a rough secondary indicator, excluding tests and docs. It is neither a minimum, a target, a cap, nor a guarantee of marks. Copying many lines with small changes does not imply equivalent effort. Exceeding the effort threshold brings no automatic extra credit; quality is the main differentiator.

Each person should contribute functional code, automated tests, UG and DG content, reviews, project roles, and a meaningful share of common team work. There is no prescribed coverage percentage, but weak tests increase regression and PE risk. User-visible enhancements are preferred, not mandatory.

Divide implementation mainly by features so an owner can deliver code, tests, and documentation end to end. Also assign component maintainers who understand and review changes to their component; that role does not make them its exclusive author. Assign one clearly responsible person for each important project role, optionally with a backup. Examples include coordination, documentation, testing, code quality, integration, releases/deadlines, scheduling, and tool expertise.

Team duties include maintaining a cohesive product and consistent docs, protecting existing behavior, managing issues and milestones, integrating work, and releasing correctly. Shared work can include branding, tool setup, general documentation, release management, and issue maintenance. Every member must do a fair share rather than leaving all coordination to one person.

## Team formation, meetings, and early setup

Sources: [Forming teams](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-teams.html), [Week 3](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w3.html), [Week 4](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w4.html).

Teams normally have five people in the same confirmed tutorial. Students may form partial teams of two to four or teams of five, but not self-form six-person teams. Staff can assign additional members up to six or modify teams. Do not switch tutorials to join preferred teammates. At most one exchange student is permitted. Single-nationality teams are disallowed unless English is the only language common to all members, with the published impossibility exception for remaining tutorial students.

Obtain members' agreement. One person submits the Canvas team form using names exactly as in Canvas; the same person resubmits corrections. Use the assigned course tutorial/team ID, not Canvas's tutorial group code. For this team it is CS2103T-F13-4. The current tutorial table lists CS2103T-F13 at Friday 13:00, COM3-01-20, with Chen Feng for teams 3/4; reread the live table before relying on logistics.

Use English in spoken and written team communication. Set a weekly 1–2 hour synchronous meeting by the end of the Week 3 tutorial and attend it. Saturday–Tuesday is recommended so planning follows the Friday briefing and precedes deadlines. Tutorials after Weeks 3/4 are face-to-face and also use Zoom for sharing, responses, and recordings. Remote attendance requires permission for the specific week; joining Zoom without it does not count for participation. Prepare and join Zoom en route if unavoidable lateness occurs. Canvas supplies the meeting links.

Week 3 includes trying the released AB3, agreeing a meeting time, and checking members' iP progress. Week 4 starts regular meetings and a publicly viewable collaborative notes document. Every additional project-notes file must be linked from that document and publicly readable. Use a view link, never a public edit link. The TEAMMATES direction submission includes product name, target-user profile, a value proposition of at most 50 words, and the notes-document URL. The initial published deadline was 5 September, 23:59. Later name/profile/value changes go through the original form while open, or the course email after its deadline. Link these planning guides from the main project-notes document if the team uses them as additional project notes.

## AI use and accountability

Sources: [TP expectations](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-expectations.html#expectations-on-ai-use-in-the-tp), [course AI guidance](https://nus-cs2103-ay2627-s1.github.io/website/admin/courseExpectations.html#use-of-ai-section), [Week 7](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w7.html), [Week 8](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w8.html).

Non-trivial AI use is expected; the course suggests about 20% of course-work time on average. Hand-coding first and using AI feedback meets the minimum. Learn unfamiliar tasks manually before increasing automation. Be able to explain the generated code. Full delegation with no code review is discouraged because the student remains responsible for code quality.

Use generated code sparingly during the first one or two coding weeks while the team learns AB3. Increase use only as understanding permits. Every AI-generated commit must use the responsible student's human Git author identity, not an AI tool's name. The student must vet AI review comments before posting them; superficial or spammy reviews remain the student's responsibility. Read the acknowledgement and reuse policy linked in the workflow guide before changing public documentation.

## Supervision, feedback, and decisions

The tutor is also the project supervisor. Their role is to observe, support self/peer learning, and evaluate; the team must manage its own work. Keep the tutor informed of significant difficulties so they can take appropriate action. Do not expect the tutor to code, debug, manage delivery, or choose the team's design.

Staff cannot provide ad-hoc feedback on yet-to-be-graded project components. This preserves independent work and comparable support across teams. The course instead provides systematic feedback and exercises that build the ability to judge work, including one's own. General questions can be raised in the public forum, where a professor can answer in a way that benefits everyone, or sent to the course email when appropriate. Do not interpret the policy as a ban on asking for procedural clarification.

For design decisions, try small prototypes, compare effects on the intended user, discuss with the team, and prefer a simple adequate solution within current resources. Inspect what others have done for ideas, with proper attribution. Check whether the chosen design makes code harder to change, test, or maintain. Correct a poor choice when worthwhile; if it is adequate for this project, record the tradeoff and lesson rather than rebuilding solely to chase an ideal architecture.

Learning from classmates is allowed, with public forum discussion preferred to private sharing between teams. Outsiders must not do the project work. Voluntary user feedback is allowed, and learning from others is allowed so long as they do not perform the assessed work themselves. Course authorization for AI use is described separately above.

Sources: [TP supervision/guidance](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-supervision.html), [feedback policy](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html#policy-feedbackOnProjectWork), [outsider-help policy](https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html#policy-outsiderHelp).
