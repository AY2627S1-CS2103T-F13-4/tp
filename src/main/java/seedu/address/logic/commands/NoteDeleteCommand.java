package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;

/**
 * Deletes a dated progress note and opens the student details.
 */
public class NoteDeleteCommand extends Command {
    public static final String COMMAND_WORD = "note-delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes one of a student's progress notes, numbered as in the student's details\n"
            + "Parameters: STUDENT_INDEX NOTE_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1 2";
    public static final String MESSAGE_DELETE_NOTE_SUCCESS = "Deleted note %1$d for %2$s: %3$s";

    private final Index studentIndex;
    private final Index noteIndex;

    /**
     * Creates a command with the specified student index and details.
     */
    public NoteDeleteCommand(Index studentIndex, Index noteIndex) {
        requireAllNonNull(studentIndex, noteIndex);
        this.studentIndex = studentIndex;
        this.noteIndex = noteIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = lastShownList.get(studentIndex.getZeroBased());
        List<ProgressNote> notes = new ArrayList<>(student.getNotes());
        if (noteIndex.getZeroBased() >= notes.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_NOTE_DISPLAYED_INDEX);
        }
        ProgressNote removed = notes.remove(noteIndex.getZeroBased());
        model.setPerson(student, student.withNotes(notes));
        // Show the removed note so the user can verify the selected index.
        return new CommandResult(String.format(MESSAGE_DELETE_NOTE_SUCCESS, noteIndex.getOneBased(),
                student.getName(), removed), studentIndex);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteDeleteCommand command
                && studentIndex.equals(command.studentIndex) && noteIndex.equals(command.noteIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, noteIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("noteIndex", noteIndex)
                .toString();
    }
}
