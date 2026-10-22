package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;

/** Changes only the progress notes of a student in the displayed list, then shows that student's details. */
public class NoteCommand extends Command {
    public static final String ADD_COMMAND_WORD = "note-add";
    public static final String DELETE_COMMAND_WORD = "note-delete";
    public static final String MESSAGE_ADD_USAGE = ADD_COMMAND_WORD
            + ": Adds a dated progress note to a student.\n"
            + "Parameters: STUDENT_INDEX date/YYYY-MM-DD text/TEXT\n"
            + "Example: " + ADD_COMMAND_WORD + " 1 date/2026-10-15 text/Finished the fractions worksheet";
    public static final String MESSAGE_DELETE_USAGE = DELETE_COMMAND_WORD
            + ": Deletes one of a student's progress notes, numbered as in the student's details.\n"
            + "Parameters: STUDENT_INDEX NOTE_INDEX\n"
            + "Example: " + DELETE_COMMAND_WORD + " 1 2";
    public static final String MESSAGE_DUPLICATE_NOTE = "This student already has this note on that date.";
    public static final String MESSAGE_INVALID_NOTE_DISPLAYED_INDEX = "The note index provided is invalid.";
    public static final String MESSAGE_ADDED = "Added note %1$d for %2$s.";
    public static final String MESSAGE_DELETED = "Deleted note %1$d for %2$s.";

    /** Supported note mutations. Reading uses the student's details. */
    public enum Operation { ADD, DELETE }

    private final Index studentIndex;
    private final Operation operation;
    private final Optional<Index> noteIndex;
    private final Optional<ProgressNote> note;

    /**
     * Creates a validated note mutation. Adding takes only {@code note}; deleting takes only {@code noteIndex}.
     */
    public NoteCommand(Index studentIndex, Operation operation, Optional<Index> noteIndex,
            Optional<ProgressNote> note) {
        this.studentIndex = requireNonNull(studentIndex);
        this.operation = requireNonNull(operation);
        this.noteIndex = requireNonNull(noteIndex);
        this.note = requireNonNull(note);
        if ((operation == Operation.ADD && (note.isEmpty() || noteIndex.isPresent()))
                || (operation == Operation.DELETE && (noteIndex.isEmpty() || note.isPresent()))) {
            throw new IllegalArgumentException("Invalid fields for note operation.");
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (studentIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = model.getFilteredPersonList().get(studentIndex.getZeroBased());
        List<ProgressNote> notes = new ArrayList<>(student.getNotes());
        switch (operation) {
            case ADD -> {
                if (notes.contains(note.orElseThrow())) {
                    throw new CommandException(MESSAGE_DUPLICATE_NOTE);
                }
                notes.add(note.orElseThrow());
            }
            case DELETE -> {
                int position = noteIndex.orElseThrow().getZeroBased();
                if (position >= notes.size()) {
                    throw new CommandException(MESSAGE_INVALID_NOTE_DISPLAYED_INDEX);
                }
                notes.remove(position);
            }
            default -> throw new AssertionError(operation);
        }

        Student replacement = new Student(student.getName(), student.getPhone(), student.getEmail(),
                student.getAddress(), student.getTags(), student.getSchool(), student.getSchoolingLevel(),
                student.getGuardian(), student.getLessons(), notes);
        model.setPerson(student, replacement);
        if (operation == Operation.DELETE) {
            return new CommandResult(String.format(MESSAGE_DELETED, noteIndex.orElseThrow().getOneBased(),
                    student.getName()), studentIndex);
        }
        // The student keeps notes oldest first, so a note dated earlier than others lands before them.
        int noteNumber = replacement.getNotes().lastIndexOf(note.orElseThrow()) + 1;
        assert noteNumber > 0 : "The added note should be among the student's notes";
        return new CommandResult(String.format(MESSAGE_ADDED, noteNumber, student.getName()), studentIndex);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteCommand command
                && studentIndex.equals(command.studentIndex) && operation == command.operation
                && noteIndex.equals(command.noteIndex) && note.equals(command.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, operation, noteIndex, note);
    }
}
