package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SCHOOL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LessonMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.StudentSearchPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Logger logger = LogsCenter.getLogger(FindCommandParser.class);
    private static final Prefix[] PREFIXES = {
        PREFIX_NAME, PREFIX_GUARDIAN, PREFIX_SCHOOL, PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_DAY
    };

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw invalidFormat("no search criteria");
        }

        if (Arrays.stream(PREFIXES)
                .anyMatch(prefix -> trimmedArgs.matches("(?s)(.*\\s)?"
                        + Pattern.quote(prefix.getPrefix()) + ".*"))) {
            return parsePrefixedSearch(" " + trimmedArgs);
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        logger.fine("Search filters selected: [bare-name]");
        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

    /** Parses all prefixed student criteria into one search predicate. */
    private FindCommand parsePrefixedSearch(String args) throws ParseException {
        // The tokenizer recognizes spaces before prefixes; accept other whitespace at those boundaries too.
        for (Prefix prefix : PREFIXES) {
            args = args.replaceAll("\\s(?=" + Pattern.quote(prefix.getPrefix()) + ")", " ");
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIXES);
        if (!arguments.getPreamble().isEmpty()) {
            throw invalidFormat("bare keywords mixed with prefixes");
        }
        try {
            arguments.verifyNoDuplicatePrefixesFor(PREFIXES);
        } catch (ParseException e) {
            logger.fine("Student search rejected: duplicate prefixes");
            throw e;
        }
        Optional<String> name = arguments.getValue(PREFIX_NAME);
        Optional<String> guardian = arguments.getValue(PREFIX_GUARDIAN);
        Optional<String> school = arguments.getValue(PREFIX_SCHOOL);
        Optional<String> level = arguments.getValue(PREFIX_LEVEL);
        Optional<String> subject = arguments.getValue(PREFIX_SUBJECT);
        Optional<String> dayText = arguments.getValue(PREFIX_DAY);
        if (List.of(name, guardian, school, level, subject, dayText).stream()
                .anyMatch(value -> value.filter(String::isBlank).isPresent())) {
            throw invalidFormat("empty filter value");
        }
        Optional<DayOfWeek> day = dayText.isPresent() ? Optional.of(parseDay(dayText.orElseThrow())) : Optional.empty();
        LessonMatchesPredicate lessonPredicate = new LessonMatchesPredicate(subject, day);
        boolean filtersLessons = subject.isPresent() || day.isPresent();
        StudentSearchPredicate predicate = new StudentSearchPredicate(name, guardian, school, level,
                filtersLessons ? Optional.of(lessonPredicate) : Optional.empty());
        logger.fine("Search filters selected: " + Arrays.stream(PREFIXES)
                .filter(prefix -> arguments.getValue(prefix).isPresent())
                .map(Prefix::getPrefix).toList());
        return filtersLessons ? new FindCommand(predicate, lessonPredicate) : new FindCommand(predicate);
    }

    private DayOfWeek parseDay(String value) throws ParseException {
        String text = value.toUpperCase(Locale.ROOT);
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.name().equals(text) || day.name().substring(0, 3).equals(text)) {
                return day;
            }
        }
        throw invalidFormat("invalid weekday");
    }

    private ParseException invalidFormat(String reason) {
        logger.fine("Student search rejected: " + reason);
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

}
