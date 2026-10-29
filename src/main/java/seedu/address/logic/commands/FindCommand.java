package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SCHOOL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.function.Predicate;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/** Finds students by name or by combined contact, school, schooling level and lesson criteria. */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds students by name, guardian, school, schooling level, subject or lesson day\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " alice bob charlie\n"
            + "Parameters: [" + PREFIX_NAME + "TEXT] [" + PREFIX_GUARDIAN + "TEXT] [" + PREFIX_SCHOOL + "TEXT] ["
            + PREFIX_LEVEL + "TEXT] [" + PREFIX_SUBJECT + "TEXT] ["
            + PREFIX_DAY + "DAY]\n"
            + "Supply at least one prefix; all criteria must match. Name, guardian and school match substrings, "
            + "ignoring case; "
            + "bare keywords match whole name words. Guardian matches name or phone\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_LEVEL + "Secondary "
            + PREFIX_SUBJECT + "Math " + PREFIX_DAY + "Wed";

    public static final String MESSAGE_NONE = "No students match the search";

    private static final Logger logger = LogsCenter.getLogger(FindCommand.class);

    private final Predicate<Student> predicate;
    private final Predicate<Lesson> lessonPredicate;

    public FindCommand(Predicate<Student> predicate) {
        this(predicate, Model.PREDICATE_SHOW_ALL_LESSONS);
    }

    /** Creates a search that also filters individual lessons on the calendar. */
    public FindCommand(Predicate<Student> predicate, Predicate<Lesson> lessonPredicate) {
        this.predicate = predicate;
        this.lessonPredicate = lessonPredicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate, lessonPredicate);
        logger.info("Student search completed: matchedStudents=" + model.getFilteredPersonList().size());
        int count = model.getFilteredPersonList().size();
        String feedback = count == 0 ? MESSAGE_NONE : String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, count);
        return new CommandResult(feedback, CommandResult.Screen.STUDENTS);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate)
                && lessonPredicate.equals(otherFindCommand.lessonPredicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
