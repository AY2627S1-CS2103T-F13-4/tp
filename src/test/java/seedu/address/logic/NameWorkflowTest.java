package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Student;
import seedu.address.storage.JsonAddressBookStorage;

/** Exercises special names through student and guardian commands and saved records. */
public class NameWorkflowTest {
    @TempDir
    public Path testFolder;

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void specialNames_addEditAndReload_preservesStudentAndGuardianNames() throws Exception {
        for (String name : List.of("Mary-Ann O'Brien", "Ravi s/o Kumar", "Devi d/o Raj", "Raj S/O Kumar",
                "Zoë Ng", "Zoe\u0308 Ng", "李小龙", "D’Arcy Tan", "A. Rahman")) {
            Model model = new ModelManager();
            parser.parseCommand("add name/" + name + " phone/91234567 email/test@example.com address/Test Road")
                    .execute(model);
            parser.parseCommand("edit 1 name/" + name + " Junior").execute(model);
            parser.parseCommand("guardian-add 1 name/" + name + " phone/81234567").execute(model);
            parser.parseCommand("guardian-edit 1 name/" + name + " Senior").execute(model);

            Student student = model.getFilteredPersonList().get(0);
            assertEquals(name + " Junior", student.getName().fullName);
            assertEquals(name + " Senior", student.getGuardian().orElseThrow().getName().fullName);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("students.json"));
            storage.saveAddressBook(model.getAddressBook());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void specialNameWithExtraSpaces_duplicateStudentRejected() throws Exception {
        Model model = new ModelManager();
        parser.parseCommand("add name/Mary-Ann O'Brien phone/91234567 email/test@example.com address/Test Road")
                .execute(model);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> parser.parseCommand(
                "add name/Mary-Ann   O'Brien phone/91234567 email/other@example.com address/Other Road")
                .execute(model));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void relationshipTerms_unknownGuardianPrefixesStillRejected() {
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                        "guardian-add 1 name/Ravi s/o Kumar phone/81234567 rate/50"));
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                        "guardian-edit 1 name/Devi d/o Raj x1/value"));
    }
}
