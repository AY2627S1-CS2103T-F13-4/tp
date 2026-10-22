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
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

/** Changes only the lessons of a student in the displayed list. */
public class LessonCommand extends Command {
    public static final String ADD_COMMAND_WORD = "lesson-add";
    public static final String EDIT_COMMAND_WORD = "lesson-edit";
    public static final String DELETE_COMMAND_WORD = "lesson-delete";
    public static final String MESSAGE_DUPLICATE_LESSON = "This student already has an identical lesson.";
    public static final String MESSAGE_INVALID_LESSON_DISPLAYED_INDEX = "The lesson index provided is invalid.";

    /** Supported lesson mutations. Reading uses the existing student list. */
    public enum Operation { ADD, EDIT, DELETE }

    private final Index studentIndex;
    private final Operation operation;
    private final Optional<Index> lessonIndex;
    private final Optional<Subject> subject;
    private final Optional<WeeklyLessonSlot> weeklyLessonSlot;
    private final Optional<HourlyRate> hourlyRate;
    private final Optional<LessonLocation> location;
    private final Optional<LessonMode> mode;
    private final boolean clearLocation;
    private final boolean clearMode;

    /** Creates a validated lesson mutation that clears nothing. */
    public LessonCommand(Index studentIndex, Operation operation, Optional<Index> lessonIndex,
            Optional<Subject> subject, Optional<WeeklyLessonSlot> weeklyLessonSlot,
            Optional<HourlyRate> hourlyRate, Optional<LessonLocation> location, Optional<LessonMode> mode) {
        this(studentIndex, operation, lessonIndex, subject, weeklyLessonSlot, hourlyRate, location, mode,
                false, false);
    }

    /**
     * Creates a validated lesson mutation. {@code clearLocation} and {@code clearMode} remove a recorded
     * value on edit, and cannot be combined with a new value for the same field.
     */
    public LessonCommand(Index studentIndex, Operation operation, Optional<Index> lessonIndex,
            Optional<Subject> subject, Optional<WeeklyLessonSlot> weeklyLessonSlot,
            Optional<HourlyRate> hourlyRate, Optional<LessonLocation> location, Optional<LessonMode> mode,
            boolean clearLocation, boolean clearMode) {
        this.studentIndex = requireNonNull(studentIndex);
        this.operation = requireNonNull(operation);
        this.lessonIndex = requireNonNull(lessonIndex);
        this.subject = requireNonNull(subject);
        this.weeklyLessonSlot = requireNonNull(weeklyLessonSlot);
        this.hourlyRate = requireNonNull(hourlyRate);
        this.location = requireNonNull(location);
        this.mode = requireNonNull(mode);
        this.clearLocation = clearLocation;
        this.clearMode = clearMode;
        boolean hasAllFields = subject.isPresent() && weeklyLessonSlot.isPresent() && hourlyRate.isPresent();
        boolean hasAnyField = subject.isPresent() || weeklyLessonSlot.isPresent() || hourlyRate.isPresent()
                || location.isPresent() || mode.isPresent() || clearLocation || clearMode;
        if ((clearLocation && location.isPresent()) || (clearMode && mode.isPresent())
                || (operation != Operation.EDIT && (clearLocation || clearMode))) {
            throw new IllegalArgumentException("Invalid fields for lesson operation.");
        }
        if ((operation == Operation.ADD && (lessonIndex.isPresent() || !hasAllFields))
                || (operation == Operation.EDIT && (lessonIndex.isEmpty() || !hasAnyField))
                || (operation == Operation.DELETE && (lessonIndex.isEmpty() || hasAnyField))) {
            throw new IllegalArgumentException("Invalid fields for lesson operation.");
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (studentIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = model.getFilteredPersonList().get(studentIndex.getZeroBased());
        List<Lesson> lessons = new ArrayList<>(student.getLessons());

        switch (operation) {
            case ADD -> lessons.add(new Lesson(subject.orElseThrow(), weeklyLessonSlot.orElseThrow(),
                    hourlyRate.orElseThrow(), location, mode));
            case EDIT -> {
                int position = requireLessonPosition(lessons);
                Lesson existing = lessons.get(position);
                lessons.set(position, new Lesson(subject.orElse(existing.getSubject()),
                        weeklyLessonSlot.orElse(existing.getWeeklyLessonSlot()),
                        hourlyRate.orElse(existing.getHourlyRate()),
                        clearLocation ? Optional.empty() : location.or(existing::getLocation),
                        clearMode ? Optional.empty() : mode.or(existing::getMode)));
            }
            case DELETE -> lessons.remove(requireLessonPosition(lessons));
            default -> throw new AssertionError(operation);
        }

        if (lessons.stream().distinct().count() != lessons.size()) {
            throw new CommandException(MESSAGE_DUPLICATE_LESSON);
        }
        Student replacement = new Student(student.getName(), student.getPhone(), student.getEmail(),
                student.getAddress(), student.getTags(), student.getSchool(), student.getSchoolingLevel(),
                student.getGuardian(), lessons, student.isArchived(), student.getNotes(),
                student.getFollowUps());
        model.setPerson(student, replacement);
        String action = switch (operation) {
            case ADD -> "added";
            case EDIT -> "updated";
            case DELETE -> "removed";
        };
        String warning = operation == Operation.DELETE ? "" : LessonClashes.warning(
                model.getAddressBook().getPersonList(), replacement,
                operation == Operation.ADD ? lessons.size() - 1 : lessonIndex.orElseThrow().getZeroBased());
        if (!warning.isEmpty()) {
            return CommandResult.warning("Lesson saved - scheduling clash detected" + warning);
        }
        return new CommandResult("Lesson " + action + " for " + student.getName() + ".");
    }

    private int requireLessonPosition(List<Lesson> lessons) throws CommandException {
        int position = lessonIndex.orElseThrow().getZeroBased();
        if (position >= lessons.size()) {
            throw new CommandException(MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }
        return position;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonCommand command
                && studentIndex.equals(command.studentIndex) && operation == command.operation
                && lessonIndex.equals(command.lessonIndex) && subject.equals(command.subject)
                && weeklyLessonSlot.equals(command.weeklyLessonSlot) && hourlyRate.equals(command.hourlyRate)
                && location.equals(command.location) && mode.equals(command.mode)
                && clearLocation == command.clearLocation && clearMode == command.clearMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, operation, lessonIndex, subject, weeklyLessonSlot, hourlyRate, location,
                mode, clearLocation, clearMode);
    }
}
