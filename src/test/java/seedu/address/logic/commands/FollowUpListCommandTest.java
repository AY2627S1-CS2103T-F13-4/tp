package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.DayOfWeek;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class FollowUpListCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

    /** Gives the students at the given zero-based positions one follow-up each, in both models. */
    private void giveFollowUps(int... positions) {
        for (int position : positions) {
            Student student = model.getFilteredPersonList().get(position);
            Student withItem = new PersonBuilder(student).withFollowUps("Send report").build();
            model.setPerson(student, withItem);
            expectedModel.setPerson(student, withItem);
        }
    }

    @Test
    public void execute_someOutstanding_showsOnlyThoseStudents() {
        giveFollowUps(1, 3);
        expectedModel.updateFilteredPersonList(FollowUpListCommand.PREDICATE_HAS_FOLLOW_UPS);
        CommandResult expected = new CommandResult(String.format(FollowUpListCommand.MESSAGE_SUCCESS, 2),
                CommandResult.Screen.STUDENTS);
        assertCommandSuccess(new FollowUpListCommand(), model, expected, expectedModel);
        assertTrue(model.getFilteredPersonList().stream().allMatch(Student::hasFollowUps));
    }

    @Test
    public void execute_archivedStudentWithFollowUps_notListed() {
        giveFollowUps(0, 1);
        Student archived = model.getFilteredPersonList().get(1);
        model.setPerson(archived, archived.withArchived(true));
        new ListArchiveCommand().execute(model);

        new FollowUpListCommand().execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().stream().noneMatch(Student::isArchived));
    }

    @Test
    public void execute_noneOutstanding_showsEmptyListWithHint() {
        expectedModel.updateFilteredPersonList(FollowUpListCommand.PREDICATE_HAS_FOLLOW_UPS);
        CommandResult expected = new CommandResult(FollowUpListCommand.MESSAGE_NONE, CommandResult.Screen.STUDENTS);
        assertCommandSuccess(new FollowUpListCommand(), model, expected, expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_afterLessonSearch_replacesSearchAndShowsEveryLesson() {
        giveFollowUps(0);
        model.updateFilteredPersonList(student -> true, lesson -> lesson.getDay() == DayOfWeek.MONDAY);

        new FollowUpListCommand().execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(Model.PREDICATE_SHOW_ALL_LESSONS, model.getLessonPredicate().getValue());
    }

    @Test
    public void execute_listAfterFollowUps_showsEveryStudentAgain() {
        giveFollowUps(0);
        new FollowUpListCommand().execute(model);
        new ListCommand().execute(model);
        assertEquals(getTypicalAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void equals() {
        FollowUpListCommand command = new FollowUpListCommand();
        assertEquals(command, command);
        assertEquals(command, new FollowUpListCommand());
        assertEquals(command.hashCode(), new FollowUpListCommand().hashCode());
        assertNotEquals(command, new ListCommand());
        assertNotEquals(command, null);
    }
}
