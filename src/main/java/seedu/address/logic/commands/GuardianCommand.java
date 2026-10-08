package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;

/** Changes only the guardian contact of a student in the displayed list. */
public class GuardianCommand extends Command {
    public static final String ADD_COMMAND_WORD = "guardian-add";
    public static final String EDIT_COMMAND_WORD = "guardian-edit";
    public static final String DELETE_COMMAND_WORD = "guardian-delete";
    public static final String MESSAGE_ALREADY_EXISTS = "This student already has a guardian. Use guardian-edit.";
    public static final String MESSAGE_NOT_FOUND = "This student has no guardian. Use guardian-add first.";

    /** Supported guardian mutations. Reading uses the existing student list. */
    public enum Operation { ADD, EDIT, DELETE }

    private final Index index;
    private final Operation operation;
    private final Optional<Name> name;
    private final Optional<Phone> phone;

    /** Creates a validated guardian mutation. */
    public GuardianCommand(Index index, Operation operation, Optional<Name> name, Optional<Phone> phone) {
        this.index = requireNonNull(index);
        this.operation = requireNonNull(operation);
        this.name = requireNonNull(name);
        this.phone = requireNonNull(phone);
        if ((operation == Operation.ADD && (name.isEmpty() || phone.isEmpty()))
                || (operation == Operation.EDIT && name.isEmpty() && phone.isEmpty())
                || (operation == Operation.DELETE && (name.isPresent() || phone.isPresent()))) {
            throw new IllegalArgumentException("Invalid fields for guardian operation.");
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person student = model.getFilteredPersonList().get(index.getZeroBased());
        Optional<GuardianContact> existing = student.getGuardianContact();
        if (operation == Operation.ADD && existing.isPresent()) {
            throw new CommandException(MESSAGE_ALREADY_EXISTS);
        }
        if (operation != Operation.ADD && existing.isEmpty()) {
            throw new CommandException(MESSAGE_NOT_FOUND);
        }
        Optional<GuardianContact> updated = switch (operation) {
            case ADD -> Optional.of(new GuardianContact(name.orElseThrow(), phone.orElseThrow()));
            case EDIT -> Optional.of(new GuardianContact(name.orElse(existing.orElseThrow().getName()),
                    phone.orElse(existing.orElseThrow().getPhone())));
            case DELETE -> Optional.empty();
        };
        Person replacement = new Person(student.getName(), student.getPhone(), student.getEmail(),
                student.getAddress(), student.getTags(), student.getSubjects(), student.getSchoolingLevel(),
                updated, student.getWeeklyLessonSlot(), student.getHourlyRate());
        model.setPerson(student, replacement);
        String action = switch (operation) {
            case ADD -> "added";
            case EDIT -> "updated";
            case DELETE -> "removed";
        };
        return new CommandResult("Guardian " + action + " for " + student.getName() + ".");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianCommand command
                && index.equals(command.index) && operation == command.operation
                && name.equals(command.name) && phone.equals(command.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, operation, name, phone);
    }
}
