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
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/**
 * Deletes a lesson identified by its student and lesson indexes.
 */
public class LessonDeleteCommand extends Command {
    public static final String COMMAND_WORD = "lesson-delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes one of a student's lessons, numbered as in the student's details\n"
            + "Parameters: STUDENT_INDEX LESSON_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1 1";

    public static final String MESSAGE_DELETE_LESSON_SUCCESS = "Lesson deleted for %1$s";

    private final Index studentIndex;
    private final Index lessonIndex;

    /**
     * Creates a command to delete the lesson at the given displayed student and lesson indexes.
     */
    public LessonDeleteCommand(Index studentIndex, Index lessonIndex) {
        requireAllNonNull(studentIndex, lessonIndex);
        this.studentIndex = studentIndex;
        this.lessonIndex = lessonIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();

        if (studentIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Student student = lastShownList.get(studentIndex.getZeroBased());
        List<Lesson> lessons = new ArrayList<>(student.getLessons());

        if (lessonIndex.getZeroBased() >= lessons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }

        lessons.remove(lessonIndex.getZeroBased());

        Student updatedStudent = student.withLessons(lessons);
        model.setPerson(student, updatedStudent);

        return new CommandResult(String.format(MESSAGE_DELETE_LESSON_SUCCESS, student.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonDeleteCommand command
                && studentIndex.equals(command.studentIndex) && lessonIndex.equals(command.lessonIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, lessonIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).add("lessonIndex", lessonIndex).toString();
    }
}
