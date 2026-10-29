package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.RateSortOrder;
import seedu.address.model.person.Student;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/** Checks duplicate errors across both lists, independently of visible search results. */
public class DuplicateStudentCommandTest {

    @Test
    public void execute_duplicate_reportsLocationAndPreservesState() {
        for (boolean edit : List.of(false, true)) {
            for (boolean archiveView : List.of(false, true)) {
                for (boolean archivedDuplicate : List.of(false, true)) {
                    for (boolean hiddenDuplicate : List.of(false, true)) {
                        assertDuplicateRejected(edit, archiveView, archivedDuplicate, hiddenDuplicate);
                    }
                }
            }
        }
    }

    private void assertDuplicateRejected(boolean edit, boolean archiveView,
            boolean archivedDuplicate, boolean hiddenDuplicate) {
        Model model = new ModelManager();
        Student target = new PersonBuilder().withName("Target Student").build().withArchived(archiveView);
        Student duplicate = new PersonBuilder().withName("Existing Student").build().withArchived(archivedDuplicate);
        model.addPerson(target);
        model.addPerson(duplicate);
        // Set both searches so the duplicate can be hidden even in the list that is not currently open.
        model.updateFilteredPersonList(student -> !hiddenDuplicate || student.isSamePerson(target));
        model.setArchiveView(true);
        model.updateFilteredPersonList(student -> !hiddenDuplicate || student.isSamePerson(target));
        model.setArchiveView(archiveView);
        model.setRateSortOrder(RateSortOrder.DESCENDING);

        AddressBook before = new AddressBook(model.getAddressBook());
        var activeBefore = List.copyOf(model.getFilteredActivePersonList());
        var archivedBefore = List.copyOf(model.getFilteredArchivedPersonList());
        var lessonsBefore = model.getLessonPredicate().getValue();
        int targetIndex = model.getFilteredPersonList().indexOf(target);
        Command command = edit
                ? new EditCommand(Index.fromZeroBased(targetIndex), new EditPersonDescriptorBuilder()
                        .withName(duplicate.getName().fullName).build())
                : new AddCommand(new PersonBuilder(duplicate).withPhone("98888888").build().withArchived(false));
        String expected = archivedDuplicate ? Messages.MESSAGE_DUPLICATE_ARCHIVED_STUDENT
                : Messages.MESSAGE_DUPLICATE_ACTIVE_STUDENT;

        assertThrows(CommandException.class, expected, () -> command.execute(model));

        assertEquals(before, model.getAddressBook());
        assertEquals(activeBefore, model.getFilteredActivePersonList());
        assertEquals(archivedBefore, model.getFilteredArchivedPersonList());
        assertSame(lessonsBefore, model.getLessonPredicate().getValue());
        assertEquals(archiveView, model.isArchiveView());
        assertEquals(RateSortOrder.DESCENDING, model.getRateSortOrder().getValue());
    }

    @Test
    public void execute_editOwnName_doesNotRejectAsDuplicate() throws CommandException {
        for (boolean archived : List.of(false, true)) {
            assertOwnNameAccepted(archived);
        }
    }

    private void assertOwnNameAccepted(boolean archived) throws CommandException {
        Model model = new ModelManager();
        Student student = new PersonBuilder().build().withArchived(archived);
        model.addPerson(student);
        model.setArchiveView(archived);

        new EditCommand(Index.fromZeroBased(0), new EditPersonDescriptorBuilder()
                .withName(student.getName().fullName).withPhone("98888888").build()).execute(model);

        Student updated = model.getFilteredPersonList().get(0);
        assertEquals("98888888", updated.getPhone().value);
        assertEquals(archived, updated.isArchived());
    }
}
