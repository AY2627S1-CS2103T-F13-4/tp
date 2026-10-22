package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;

/** Changes only the progress notes of a student in the displayed list, then shows that student's details. */
public class NoteCommand extends Command {
    public static final String ADD_COMMAND_WORD = "note-add";
    public static final String MESSAGE_ADD_USAGE = ADD_COMMAND_WORD
            + ": Adds a dated progress note to a student.\n"
            + "Parameters: STUDENT_INDEX date/YYYY-MM-DD text/TEXT\n"
            + "Example: " + ADD_COMMAND_WORD + " 1 date/2026-10-15 text/Finished the fractions worksheet";
    public static final String MESSAGE_DUPLICATE_NOTE = "This student already has this note on that date.";
    public static final String MESSAGE_ADDED = "Added note %1$d for %2$s.";

    /** Supported note mutations. Reading uses the student's details. */
    public enum Operation { ADD }

    private final Index studentIndex;
    private final Operation operation;
    private final ProgressNote note;

    /** Creates a command that adds {@code note} to the student at {@code studentIndex}. */
    public NoteCommand(Index studentIndex, Operation operation, ProgressNote note) {
        this.studentIndex = requireNonNull(studentIndex);
        this.operation = requireNonNull(operation);
        this.note = requireNonNull(note);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (studentIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = model.getFilteredPersonList().get(studentIndex.getZeroBased());
        List<ProgressNote> notes = new ArrayList<>(student.getNotes());
        if (notes.contains(note)) {
            throw new CommandException(MESSAGE_DUPLICATE_NOTE);
        }
        notes.add(note);

        Student replacement = new Student(student.getName(), student.getPhone(), student.getEmail(),
                student.getAddress(), student.getTags(), student.getSchool(), student.getSchoolingLevel(),
                student.getGuardian(), student.getLessons(), student.isArchived(), notes);
        model.setPerson(student, replacement);
        // The student keeps notes oldest first, so a note dated earlier than others lands before them.
        int noteNumber = replacement.getNotes().lastIndexOf(note) + 1;
        assert noteNumber > 0 : "The added note should be among the student's notes";
        return new CommandResult(String.format(MESSAGE_ADDED, noteNumber, student.getName()), studentIndex);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteCommand command
                && studentIndex.equals(command.studentIndex) && operation == command.operation
                && note.equals(command.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, operation, note);
    }
}
