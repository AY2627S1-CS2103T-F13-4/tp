package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.commands.LessonAddCommand.MESSAGE_DUPLICATE_LESSON;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
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

/**
 * Edits a lesson identified by its student and lesson indexes.
 */
public class LessonEditCommand extends Command {
    public static final String COMMAND_WORD = "lesson-edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits one of a student's lessons, numbered as in the student's details\n"
            + "Parameters: STUDENT_INDEX LESSON_INDEX "
            + "[" + PREFIX_SUBJECT + "SUBJECT] "
            + "[" + PREFIX_LESSON_SLOT + "LESSON_SLOT] "
            + "[" + PREFIX_RATE + "HOURLY_RATE] "
            + "[" + PREFIX_MODE + "MODE] "
            + "[" + PREFIX_LOCATION + "LOCATION]\n"
            + "Use an empty " + PREFIX_MODE + " or " + PREFIX_LOCATION + " to clear that field\n"
            + "Example: " + COMMAND_WORD + " 1 1 " + PREFIX_RATE + "50";

    public static final String MESSAGE_EDIT_LESSON_SUCCESS = "Lesson edited for %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided";

    private final Index studentIndex;
    private final Index lessonIndex;
    private final EditLessonDescriptor editLessonDescriptor;

    /**
     * @param studentIndex of the person in the filtered person list to edit
     * @param lessonIndex of the lesson in the person's list of lessons
     * @param editLessonDescriptor details to edit the lesson with
     */
    public LessonEditCommand(Index studentIndex, Index lessonIndex, EditLessonDescriptor editLessonDescriptor) {
        requireAllNonNull(studentIndex, lessonIndex, editLessonDescriptor);
        checkArgument(editLessonDescriptor.isAnyFieldEdited(), MESSAGE_NOT_EDITED);

        this.studentIndex = studentIndex;
        this.lessonIndex = lessonIndex;
        this.editLessonDescriptor = new EditLessonDescriptor(editLessonDescriptor);
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

        Lesson lessonToEdit = lessons.get(lessonIndex.getZeroBased());
        Lesson editedLesson = createEditedLesson(lessonToEdit, editLessonDescriptor);

        lessons.set(lessonIndex.getZeroBased(), editedLesson);

        if (lessons.stream().distinct().count() != lessons.size()) {
            throw new CommandException(MESSAGE_DUPLICATE_LESSON);
        }

        Student updatedStudent = student.withLessons(lessons);
        model.setPerson(student, updatedStudent);

        String successMessage = String.format(MESSAGE_EDIT_LESSON_SUCCESS, student.getName());
        return LessonClashes.resultForLesson(model.getAddressBook(), updatedStudent,
                lessonIndex.getZeroBased(), successMessage);
    }

    /**
     * Creates and returns a {@code Lesson} with the details of {@code lessonToEdit}
     * edited with {@code editLessonDescriptor}.
     */
    private static Lesson createEditedLesson(Lesson lessonToEdit, EditLessonDescriptor editLessonDescriptor) {
        assert lessonToEdit != null;

        Subject updatedSubject = editLessonDescriptor.getSubject().orElse(lessonToEdit.getSubject());
        WeeklyLessonSlot updatedLessonSlot = editLessonDescriptor.getLessonSlot().orElse(lessonToEdit.getLessonSlot());
        HourlyRate updatedHourlyRate = editLessonDescriptor.getHourlyRate().orElse(lessonToEdit.getHourlyRate());
        Optional<LessonMode> updatedLessonMode = editLessonDescriptor.getLessonMode().orElse(lessonToEdit.getMode());
        Optional<LessonLocation> updatedLessonLocation = editLessonDescriptor.getLessonLocation().orElse(
                lessonToEdit.getLocation());

        return new Lesson(updatedSubject, updatedLessonSlot, updatedHourlyRate, updatedLessonLocation,
                updatedLessonMode);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonEditCommand command
                && studentIndex.equals(command.studentIndex) && lessonIndex.equals(command.lessonIndex)
                && editLessonDescriptor.equals(command.editLessonDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, lessonIndex, editLessonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).add("lessonIndex", lessonIndex)
                .add("editLessonDescriptor", editLessonDescriptor).toString();
    }

    /**
     * Stores the details to edit the lesson with. Each non-empty field value will replace the
     * corresponding field value of the lesson. Null leaves a field unchanged; an empty
     * optional clears the mode or location.
     */
    public static class EditLessonDescriptor {
        private Subject subject;
        private WeeklyLessonSlot lessonSlot;
        private HourlyRate hourlyRate;
        private Optional<LessonMode> lessonMode;
        private Optional<LessonLocation> lessonLocation;

        public EditLessonDescriptor() {}

        /**
         * Copy constructor.
         */
        public EditLessonDescriptor(EditLessonDescriptor toCopy) {
            setSubject(toCopy.subject);
            setLessonSlot(toCopy.lessonSlot);
            setHourlyRate(toCopy.hourlyRate);
            setLessonMode(toCopy.lessonMode);
            setLessonLocation(toCopy.lessonLocation);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(subject, lessonSlot, hourlyRate, lessonMode, lessonLocation);
        }

        public void setSubject(Subject subject) {
            this.subject = subject;
        }

        public Optional<Subject> getSubject() {
            return Optional.ofNullable(subject);
        }

        public void setLessonSlot(WeeklyLessonSlot lessonSlot) {
            this.lessonSlot = lessonSlot;
        }

        public Optional<WeeklyLessonSlot> getLessonSlot() {
            return Optional.ofNullable(lessonSlot);
        }

        public void setHourlyRate(HourlyRate hourlyRate) {
            this.hourlyRate = hourlyRate;
        }

        public Optional<HourlyRate> getHourlyRate() {
            return Optional.ofNullable(hourlyRate);
        }

        public void setLessonMode(Optional<LessonMode> lessonMode) {
            this.lessonMode = lessonMode;
        }

        public Optional<Optional<LessonMode>> getLessonMode() {
            return Optional.ofNullable(lessonMode);
        }

        public void setLessonLocation(Optional<LessonLocation> lessonLocation) {
            this.lessonLocation = lessonLocation;
        }

        public Optional<Optional<LessonLocation>> getLessonLocation() {
            return Optional.ofNullable(lessonLocation);
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof EditLessonDescriptor descriptor
                    && Objects.equals(subject, descriptor.subject)
                    && Objects.equals(lessonSlot, descriptor.lessonSlot)
                    && Objects.equals(hourlyRate, descriptor.hourlyRate)
                    && Objects.equals(lessonMode, descriptor.lessonMode)
                    && Objects.equals(lessonLocation, descriptor.lessonLocation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subject, lessonSlot, hourlyRate, lessonMode, lessonLocation);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).add("subject", subject).add("lessonSlot", lessonSlot)
                    .add("hourlyRate", hourlyRate).add("lessonMode", lessonMode)
                    .add("lessonLocation", lessonLocation).toString();
        }
    }
}
