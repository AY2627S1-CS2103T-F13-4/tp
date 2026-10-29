package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.commands.NoteAddCommand.MESSAGE_DUPLICATE_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TEXT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;

/** Edits a dated progress note and opens the student's details while displayed. */
public class NoteEditCommand extends Command {
    public static final String COMMAND_WORD = "note-edit";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits one of a student's progress notes, numbered as in the student's details\n"
            + "Parameters: STUDENT_INDEX NOTE_INDEX [" + PREFIX_DATE + "YYYY-MM-DD] [" + PREFIX_TEXT + "TEXT]\n"
            + "Example: " + COMMAND_WORD + " 1 2 " + PREFIX_DATE + "2026-10-15 "
            + PREFIX_TEXT + "Finished the fractions worksheet";
    public static final String MESSAGE_EDIT_NOTE_SUCCESS = "Edited note %1$d for %2$s: %3$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided";

    private final Index studentIndex;
    private final Index noteIndex;
    private final EditNoteDescriptor editNoteDescriptor;

    /** Creates a command with a defensive copy of the supplied note edits. */
    public NoteEditCommand(Index studentIndex, Index noteIndex, EditNoteDescriptor editNoteDescriptor) {
        requireAllNonNull(studentIndex, noteIndex, editNoteDescriptor);
        checkArgument(editNoteDescriptor.isAnyFieldEdited(), MESSAGE_NOT_EDITED);
        this.studentIndex = studentIndex;
        this.noteIndex = noteIndex;
        this.editNoteDescriptor = new EditNoteDescriptor(editNoteDescriptor);
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
        int position = noteIndex.getZeroBased();
        if (position >= notes.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_NOTE_DISPLAYED_INDEX);
        }
        ProgressNote original = notes.get(position);
        ProgressNote edited = new ProgressNote(editNoteDescriptor.getDate().orElse(original.getDate()),
                editNoteDescriptor.getText().orElse(original.getText()));
        for (int i = 0; i < notes.size(); i++) {
            if (i != position && notes.get(i).equals(edited)) {
                throw new CommandException(MESSAGE_DUPLICATE_NOTE);
            }
        }
        notes.set(position, edited);
        Student updatedStudent = student.withNotes(notes);
        model.setPerson(student, updatedStudent);
        // Editing the date can move the note to a new position in the chronological list.
        int noteNumber = Index.fromZeroBased(updatedStudent.getNotes().indexOf(edited)).getOneBased();
        String feedback = String.format(MESSAGE_EDIT_NOTE_SUCCESS, noteNumber, student.getName(), edited);
        int shownAt = model.getFilteredPersonList().indexOf(updatedStudent);
        return shownAt < 0
                ? new CommandResult(feedback, CommandResult.Screen.STUDENTS)
                : new CommandResult(feedback, Index.fromZeroBased(shownAt));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteEditCommand command
                && studentIndex.equals(command.studentIndex) && noteIndex.equals(command.noteIndex)
                && editNoteDescriptor.equals(command.editNoteDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, noteIndex, editNoteDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).add("noteIndex", noteIndex)
                .add("editNoteDescriptor", editNoteDescriptor).toString();
    }

    /** Stores note fields to edit; absent fields keep their existing values. */
    public static class EditNoteDescriptor {
        private LocalDate date;
        private String text;

        public EditNoteDescriptor() {}

        /** Copies the supplied note edits. */
        public EditNoteDescriptor(EditNoteDescriptor toCopy) {
            date = toCopy.date;
            text = toCopy.text;
        }

        /** Returns whether at least one note field is edited. */
        public boolean isAnyFieldEdited() {
            return date != null || text != null;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Optional<LocalDate> getDate() {
            return Optional.ofNullable(date);
        }

        public void setText(String text) {
            this.text = text;
        }

        public Optional<String> getText() {
            return Optional.ofNullable(text);
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof EditNoteDescriptor descriptor
                    && Objects.equals(date, descriptor.date) && Objects.equals(text, descriptor.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, text);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).add("date", date).add("text", text).toString();
        }
    }
}
