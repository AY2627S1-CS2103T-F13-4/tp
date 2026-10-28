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
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

/**
 * Adds a lesson to the address book.
 */
public class LessonAddCommand extends Command {
    public static final String COMMAND_WORD = "lesson-add";

    public static final String MESSAGE_DUPLICATE_LESSON = "This student already has an identical lesson.";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a lesson to a student. "
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_SUBJECT + "SUBJECT "
            + PREFIX_LESSON_SLOT + "LESSON_SLOT "
            + PREFIX_RATE + "HOURLY_RATE "
            + "[" + PREFIX_MODE + "MODE] "
            + "[" + PREFIX_LOCATION + "LOCATION]";

    public static final String MESSAGE_SUCCESS = "New lesson added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This student already has an identical lesson.";

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

        lessons.add(toAdd);

        if (lessons.stream().distinct().count() != lessons.size()) {
            throw new CommandException(MESSAGE_DUPLICATE_LESSON);
        }

        Student updatedStudent = student.withLessons(lessons);
        model.setPerson(student, updatedStudent);

        List<Student> students = updatedStudent.isArchived() ? model.getAddressBook().getArchivedPersonList()
                : model.getAddressBook().getPersonList();
        String warning = LessonClashes.warning(students, updatedStudent, lessons.size() - 1);
        if (!warning.isEmpty()) {
            return CommandResult.warning("New lesson clashes with existing lessons: " + warning);
        }
        return new CommandResult("Lesson added for" + student.getName() + ".");
    }
}
