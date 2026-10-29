package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class LessonClashesTest {
    private static final Lesson MATH = lesson("Math", "Mon 1.00pm 2.00pm");
    private static final Lesson SCIENCE = lesson("Science", "Mon 1.30pm 2.30pm");
    private static final Lesson ENGLISH = lesson("English", "Tue 1.00pm 2.00pm");
    private static final Lesson HISTORY = lesson("History", "Tue 1.30pm 2.30pm");
    private static final String SAVED_HEADING = "Lesson saved - scheduling clash detected";

    private static Lesson lesson(String subject, String slot) {
        return new Lesson(new Subject(subject), new WeeklyLessonSlot(slot), new HourlyRate(new BigDecimal("50")));
    }

    private static Student student(String name, Lesson... lessons) {
        return new PersonBuilder().withName(name).withLessons(lessons).build();
    }

    @Test
    public void resultForLesson_hiddenActivePeer_warnsWithoutChangingRecords() {
        Student owner = student("Owner", MATH);
        Student peer = student("Peer", SCIENCE);
        Student archivedPeer = student("Archived Peer", SCIENCE).withArchived(true);
        Model model = new ModelManager(new AddressBookBuilder().withPerson(owner).withPerson(peer)
                .withPerson(archivedPeer).build(), new UserPrefs());
        model.updateFilteredPersonList(owner::isSamePerson);
        AddressBook original = new AddressBook(model.getAddressBook());

        CommandResult result = LessonClashes.resultForLesson(model.getAddressBook(), owner, 0, "Added lesson");

        assertEquals(CommandResult.warning(SAVED_HEADING
                + "\nWarning: Lesson 1 for Owner overlaps with 1 lesson(s):"
                + "\n- Peer, lesson 1 (Science, Monday 1.30pm-2.30pm)"), result);
        assertEquals(original, model.getAddressBook());
        assertEquals(List.of(owner), model.getFilteredPersonList());
    }

    @Test
    public void resultForLesson_archivedOwner_checksArchivedPeersOnly() {
        Student owner = student("Owner", MATH).withArchived(true);
        Student activePeer = student("Active Peer", SCIENCE);
        Student archivedPeer = student("Archived Peer", SCIENCE).withArchived(true);
        AddressBook addressBook = new AddressBookBuilder().withPerson(owner).withPerson(activePeer)
                .withPerson(archivedPeer).build();

        CommandResult result = LessonClashes.resultForLesson(addressBook, owner, 0, "Edited lesson");

        assertEquals(CommandResult.warning(SAVED_HEADING
                + "\nWarning: Lesson 1 for Owner overlaps with 1 lesson(s):"
                + "\n- Archived Peer, lesson 1 (Science, Monday 1.30pm-2.30pm)"), result);
    }

    @Test
    public void resultForLesson_onlyOtherLessonClashes_returnsOriginalSuccess() {
        Student owner = student("Owner", MATH, ENGLISH);
        AddressBook addressBook = new AddressBookBuilder().withPerson(owner)
                .withPerson(student("Peer", HISTORY)).build();

        assertEquals(new CommandResult("Edited lesson for Owner"),
                LessonClashes.resultForLesson(addressBook, owner, 0, "Edited lesson for Owner"));
    }

    @Test
    public void resultForStudent_restoredWhileArchiveViewSelected_checksEveryLessonAgainstActivePeers() {
        Student owner = student("Owner", MATH, ENGLISH);
        Student activePeer = student("Peer", SCIENCE, HISTORY);
        Student archivedPeer = student("Archived Peer", SCIENCE, HISTORY).withArchived(true);
        Model model = new ModelManager(new AddressBookBuilder().withPerson(owner).withPerson(activePeer)
                .withPerson(archivedPeer).build(), new UserPrefs());
        model.setArchiveView(true);
        model.updateFilteredPersonList(student -> false);

        CommandResult result = LessonClashes.resultForStudent(model.getAddressBook(), owner,
                "Unarchived student: Owner");

        assertEquals(CommandResult.warning("Unarchived student: Owner"
                + "\nWarning: Lesson 1 for Owner overlaps with 1 lesson(s):"
                + "\n- Peer, lesson 1 (Science, Monday 1.30pm-2.30pm)"
                + "\nWarning: Lesson 2 for Owner overlaps with 1 lesson(s):"
                + "\n- Peer, lesson 2 (History, Tuesday 1.30pm-2.30pm)"), result);
        assertTrue(model.isArchiveView());
    }

    @Test
    public void resultForStudent_ownLessonsOverlap_retainsBothPerLessonWarnings() {
        Student owner = student("Owner", MATH, SCIENCE);
        AddressBook addressBook = new AddressBookBuilder().withPerson(owner).build();

        CommandResult result = LessonClashes.resultForStudent(addressBook, owner, "Unarchived student: Owner");

        assertEquals(CommandResult.warning("Unarchived student: Owner"
                + "\nWarning: Lesson 1 for Owner overlaps with 1 lesson(s):"
                + "\n- Owner, lesson 2 (Science, Monday 1.30pm-2.30pm)"
                + "\nWarning: Lesson 2 for Owner overlaps with 1 lesson(s):"
                + "\n- Owner, lesson 1 (Math, Monday 1.00pm-2.00pm)"), result);
    }

    @Test
    public void resultForStudent_noClashesOrLessons_returnsOriginalSuccess() {
        for (Student owner : List.of(student("Owner"), student("Owner", MATH))) {
            AddressBook addressBook = new AddressBookBuilder().withPerson(owner).build();
            assertEquals(new CommandResult("Unarchived student: Owner"),
                    LessonClashes.resultForStudent(addressBook, owner, "Unarchived student: Owner"));
        }
    }
}
