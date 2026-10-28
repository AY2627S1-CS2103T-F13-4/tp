package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.commands.DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS;

public class LessonDeleteCommand extends Command {
    public static final String COMMAND_WORD = "lesson-delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the lesson from the person identified by the respective index numbers used in the displayed"
            + "person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 1";

    public static final String MESSAGE_DELETE_LESSON_SUCCESS = "Deleted lesson: %1$s";

    private final Index studentIndex;
    private final Index lessonIndex;

    public LessonDeleteCommand(Index studentIndex, Index lessonIndex) {
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

        return new CommandResult("Lesson deleted for " + student.getName() + ".");
    }
}
