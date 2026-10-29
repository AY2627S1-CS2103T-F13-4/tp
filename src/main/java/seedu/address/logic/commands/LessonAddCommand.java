package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/**
 * Adds a lesson to the address book.
 */
public class LessonAddCommand extends Command {
    public static final String COMMAND_WORD = "lesson-add";

    public static final String MESSAGE_DUPLICATE_LESSON = "This student already has an identical lesson";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a lesson to a student\n"
            + "Parameters: STUDENT_INDEX "
            + PREFIX_SUBJECT + "SUBJECT "
            + PREFIX_LESSON_SLOT + "LESSON_SLOT "
            + PREFIX_RATE + "HOURLY_RATE "
            + "[" + PREFIX_MODE + "MODE] "
            + "[" + PREFIX_LOCATION + "LOCATION]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_SUBJECT + "Math "
            + PREFIX_LESSON_SLOT + "Mon 2.00pm 3.00pm " + PREFIX_RATE + "45";

    public static final String MESSAGE_SUCCESS = "Added lesson for %1$s: %2$s";

    private final Index index;
    private final Lesson toAdd;

    /**
     * Creates a LessonAddCommand to add the specified {@code Lesson} to the student at a given index.
     */
    public LessonAddCommand(Index index, Lesson lesson) {
        requireAllNonNull(index, lesson);
        this.index = index;
        toAdd = lesson;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Student student = lastShownList.get(index.getZeroBased());
        List<Lesson> lessons = new ArrayList<>(student.getLessons());

        int addedLessonPosition = lessons.size();
        lessons.add(toAdd);

        if (lessons.stream().distinct().count() != lessons.size()) {
            throw new CommandException(MESSAGE_DUPLICATE_LESSON);
        }

        Student updatedStudent = student.withLessons(lessons);
        model.setPerson(student, updatedStudent);

        List<Student> students = updatedStudent.isArchived() ? model.getAddressBook().getArchivedPersonList()
                : model.getAddressBook().getPersonList();
        String warning = LessonClashes.warning(students, updatedStudent, addedLessonPosition);
        if (!warning.isEmpty()) {
            return CommandResult.warning(LessonClashes.MESSAGE_CLASH_WARNING + warning);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, student.getName(), toAdd));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonAddCommand command
                && index.equals(command.index) && toAdd.equals(command.toAdd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("toAdd", toAdd).toString();
    }
}
