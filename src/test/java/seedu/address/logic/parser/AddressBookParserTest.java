package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpAddCommand;
import seedu.address.logic.commands.FollowUpDoneCommand;
import seedu.address.logic.commands.FollowUpEditCommand;
import seedu.address.logic.commands.FollowUpListCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListArchiveCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteAddCommand;
import seedu.address.logic.commands.NoteDeleteCommand;
import seedu.address.logic.commands.NoteEditCommand;
import seedu.address.logic.commands.NoteEditCommand.EditNoteDescriptor;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.WeekCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.RateSortOrder;
import seedu.address.model.person.Student;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Student person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_archive() throws Exception {
        ArchiveCommand command = (ArchiveCommand) parser.parseCommand(ArchiveCommand.COMMAND_WORD + " 2");
        Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(),
                new UserPrefs());
        Model expectedModel = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON.withArchived(true)).build(),
                new UserPrefs());
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String expectedMessage = String.format(ArchiveCommand.MESSAGE_ARCHIVE_PERSON_SUCCESS, Messages.format(BENSON));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Student person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_noteAdd() throws Exception {
        NoteAddCommand command = (NoteAddCommand) parser.parseCommand(
                NoteAddCommand.COMMAND_WORD + " 1 date/2026-10-15 text/Fractions");
        assertEquals(new NoteAddCommand(INDEX_FIRST_PERSON,
                new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions")), command);
    }

    @Test
    public void parseCommand_noteDelete() throws Exception {
        NoteDeleteCommand command = (NoteDeleteCommand) parser.parseCommand(NoteDeleteCommand.COMMAND_WORD + " 1 1");
        assertEquals(new NoteDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_noteEdit() throws Exception {
        EditNoteDescriptor descriptor = new EditNoteDescriptor();
        descriptor.setDate(LocalDate.of(2026, 10, 15));
        descriptor.setText("Finished fractions");
        assertEquals(new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor), parser.parseCommand(
                "note-edit 1 1 date/2026-10-15 text/Finished fractions"));
    }

    @Test
    public void parseCommand_followUpCommands() throws Exception {
        assertEquals(new FollowUpAddCommand(INDEX_FIRST_PERSON, new FollowUp("Send report")),
                parser.parseCommand(FollowUpAddCommand.COMMAND_WORD + " 1 text/Send report"));
        assertEquals(new FollowUpEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, new FollowUp("Call")),
                parser.parseCommand(FollowUpEditCommand.COMMAND_WORD + " 1 1 text/Call"));
        assertEquals(new FollowUpDoneCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON),
                parser.parseCommand(FollowUpDoneCommand.COMMAND_WORD + " 1 1"));
        assertTrue(parser.parseCommand(FollowUpListCommand.COMMAND_WORD) instanceof FollowUpListCommand);
        assertTrue(parser.parseCommand(FollowUpListCommand.COMMAND_WORD + " 3") instanceof FollowUpListCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertEquals(new SortCommand(RateSortOrder.DESCENDING),
                parser.parseCommand(SortCommand.COMMAND_WORD + " rate desc"));
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_listArchive() throws Exception {
        assertTrue(parser.parseCommand(ListArchiveCommand.COMMAND_WORD) instanceof ListArchiveCommand);
        assertTrue(parser.parseCommand(ListArchiveCommand.COMMAND_WORD + " 3") instanceof ListArchiveCommand);
    }

    @Test
    public void parseCommand_unarchive() throws Exception {
        UnarchiveCommand command = (UnarchiveCommand) parser.parseCommand(UnarchiveCommand.COMMAND_WORD + " 1");
        Model model = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON.withArchived(true)).build(),
                new UserPrefs());
        Model expectedModel = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(), new UserPrefs());
        String expectedMessage = String.format(UnarchiveCommand.MESSAGE_UNARCHIVE_PERSON_SUCCESS,
                Messages.format(BENSON.withArchived(true)));
        model.setArchiveView(true);
        expectedModel.setArchiveView(true);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void parseCommand_view() throws Exception {
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON),
                parser.parseCommand(ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()));
    }

    @Test
    public void parseCommand_week() throws Exception {
        assertTrue(parser.parseCommand(WeekCommand.COMMAND_WORD) instanceof WeekCommand);
        assertTrue(parser.parseCommand(WeekCommand.COMMAND_WORD + " 3") instanceof WeekCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    private void assertEditDescriptorRoundTrip(EditPersonDescriptor descriptor) throws ParseException {
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }
}
