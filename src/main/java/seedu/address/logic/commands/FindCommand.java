package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/** Finds students by name or by combined schooling level and lesson criteria. */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds students by name, schooling level, subject or lesson day.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " alice bob charlie\n"
            + "Parameters: [" + PREFIX_LEVEL + "LEVEL] [" + PREFIX_SUBJECT + "SUBJECT] ["
            + PREFIX_DAY + "DAY]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_LEVEL + "Secondary "
            + PREFIX_SUBJECT + "Math " + PREFIX_DAY + "Wed";

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
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()),
                CommandResult.Screen.STUDENTS);
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
