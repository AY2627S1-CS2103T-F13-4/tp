package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.LessonMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.SchoolingLevelContainsKeywordPredicate;
import seedu.address.model.person.Student;
import seedu.address.model.person.StudentContactMatchesPredicate;
import seedu.address.model.person.StudentLessonMatchesPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        if (List.of(PREFIX_NAME, PREFIX_GUARDIAN, PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_DAY).stream()
                .anyMatch(prefix -> trimmedArgs.matches("(?s)(.*\\s)?"
                        + Pattern.quote(prefix.getPrefix()) + ".*"))) {
            return parsePrefixedSearch(" " + trimmedArgs.replaceAll("\\s", " "));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

    /** Parses prefixed searches. Each supplied criterion must match. */
    private FindCommand parsePrefixedSearch(String args) throws ParseException {
        Prefix[] prefixes = {PREFIX_NAME, PREFIX_GUARDIAN, PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_DAY};
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, prefixes);
        if (!arguments.getPreamble().isEmpty()) {
            throw invalidFormat();
        }
        arguments.verifyNoDuplicatePrefixesFor(prefixes);
        Optional<String> name = arguments.getValue(PREFIX_NAME);
        Optional<String> guardian = arguments.getValue(PREFIX_GUARDIAN);
        Optional<String> level = arguments.getValue(PREFIX_LEVEL);
        Optional<String> subject = arguments.getValue(PREFIX_SUBJECT);
        Optional<String> dayText = arguments.getValue(PREFIX_DAY);
        if (List.of(name, guardian, level, subject, dayText).stream()
                .anyMatch(value -> value.filter(String::isBlank).isPresent())) {
            throw invalidFormat();
        }
        Optional<DayOfWeek> day = dayText.isPresent() ? Optional.of(parseDay(dayText.orElseThrow())) : Optional.empty();
        LessonMatchesPredicate lessonPredicate = new LessonMatchesPredicate(subject, day);
        Predicate<Student> predicate;
        if (subject.isPresent() || day.isPresent()) {
            predicate = new StudentLessonMatchesPredicate(level, lessonPredicate);
        } else {
            predicate = level.<Predicate<Student>>map(SchoolingLevelContainsKeywordPredicate::new)
                    .orElse(Model.PREDICATE_SHOW_ALL_PERSONS);
        }
        if (name.isPresent() || guardian.isPresent()) {
            predicate = new StudentContactMatchesPredicate(name, guardian, predicate);
        }
        return subject.isPresent() || day.isPresent()
                ? new FindCommand(predicate, lessonPredicate) : new FindCommand(predicate);
    }

    private DayOfWeek parseDay(String value) throws ParseException {
        String text = value.toUpperCase(Locale.ROOT);
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.name().equals(text) || day.name().substring(0, 3).equals(text)) {
                return day;
            }
        }
        throw invalidFormat();
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

}
