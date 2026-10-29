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
 * Adds a dated progress note and opens the student details.
 */
public class NoteAddCommand extends Command {
    public static final String COMMAND_WORD = "note-add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a dated progress note to a student\n"
            + "Parameters: STUDENT_INDEX date/YYYY-MM-DD text/TEXT\n"
            + "Example: " + COMMAND_WORD + " 1 date/2026-10-15 text/Finished the fractions worksheet";
    public static final String MESSAGE_SUCCESS = "Added note for %2$s: %1$d";
    public static final String MESSAGE_DUPLICATE_NOTE = "This student already has this note on that date";

    private final Index studentIndex;
    private final ProgressNote toAdd;

    /**
     * Creates a command with the specified student index and details.
     */
    public NoteAddCommand(Index studentIndex, ProgressNote toAdd) {
        requireAllNonNull(studentIndex, toAdd);
        this.studentIndex = studentIndex;
        this.toAdd = toAdd;
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
        if (notes.contains(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_NOTE);
        }
        notes.add(toAdd);
        Student updatedStudent = student.withNotes(notes);
        model.setPerson(student, updatedStudent);
        // Notes are sorted by date, so the added note may appear before existing notes.
        int notePosition = updatedStudent.getNotes().lastIndexOf(toAdd);
        assert notePosition >= 0 : "The added note should be among the student's notes";
        int noteNumber = Index.fromZeroBased(notePosition).getOneBased();
        return new CommandResult(String.format(MESSAGE_SUCCESS, noteNumber, student.getName()), studentIndex);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteAddCommand command
                && studentIndex.equals(command.studentIndex) && toAdd.equals(command.toAdd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("toAdd", toAdd)
                .toString();
    }
}
