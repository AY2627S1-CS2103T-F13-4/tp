package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Student;

/**
 * Edits the guardian of a student in the displayed list.
 */
public class GuardianEditCommand extends Command {
    public static final String COMMAND_WORD = "guardian-edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " INDEX "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567";

    public static final String MESSAGE_EDIT_GUARDIAN_SUCCESS = "Guardian updated for %1$s.";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NOT_FOUND = "This student has no guardian. Use guardian-add first.";

    private final Index studentIndex;
    private final EditGuardianDescriptor editGuardianDescriptor;

    /**
     * Creates a command with a defensive copy of the guardian edits.
     */
    public GuardianEditCommand(Index studentIndex, EditGuardianDescriptor editGuardianDescriptor) {
        requireAllNonNull(studentIndex, editGuardianDescriptor);
        checkArgument(editGuardianDescriptor.isAnyFieldEdited(), MESSAGE_NOT_EDITED);
        this.studentIndex = studentIndex;
        this.editGuardianDescriptor = new EditGuardianDescriptor(editGuardianDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = lastShownList.get(studentIndex.getZeroBased());
        Guardian guardianToEdit = student.getGuardian().orElseThrow(() -> new CommandException(MESSAGE_NOT_FOUND));
        Guardian editedGuardian = new Guardian(
                editGuardianDescriptor.getName().orElse(guardianToEdit.getName()),
                editGuardianDescriptor.getPhone().orElse(guardianToEdit.getPhone()));
        model.setPerson(student, student.withGuardian(Optional.of(editedGuardian)));
        return new CommandResult(String.format(MESSAGE_EDIT_GUARDIAN_SUCCESS, student.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianEditCommand command
                && studentIndex.equals(command.studentIndex)
                && editGuardianDescriptor.equals(command.editGuardianDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, editGuardianDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("editGuardianDescriptor", editGuardianDescriptor)
                .toString();
    }

    /**
     * Stores guardian fields to edit. Null fields leave the corresponding existing values unchanged.
     */
    public static class EditGuardianDescriptor {
        private Name name;
        private Phone phone;

        public EditGuardianDescriptor() {}

        /**
         * Copies the supplied guardian edits.
         */
        public EditGuardianDescriptor(EditGuardianDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
        }

        /**
         * Returns whether at least one guardian field is edited.
         */
        public boolean isAnyFieldEdited() {
            return name != null || phone != null;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof EditGuardianDescriptor descriptor
                    && Objects.equals(name, descriptor.name) && Objects.equals(phone, descriptor.phone);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, phone);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).add("name", name).add("phone", phone).toString();
        }
    }
}
