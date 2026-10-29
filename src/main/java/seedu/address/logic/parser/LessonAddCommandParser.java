package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.Optional;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

/**
 * Parses input arguments and creates a new LessonAddCommand object
 */
public class LessonAddCommandParser implements Parser<LessonAddCommand> {
    /**
     * Parses the given {@code String} of arguments in the context of the LessonAddCommand
     * and returns a LessonAddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public LessonAddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = LessonCommandParserUtil.tokenize(args, LessonAddCommand.MESSAGE_USAGE);
        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonAddCommand.MESSAGE_USAGE), pe);
        }

        if (!arePrefixesPresent(argMultimap, PREFIX_SUBJECT, PREFIX_LESSON_SLOT, PREFIX_RATE)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonAddCommand.MESSAGE_USAGE));
        }

        Subject subject = ParserUtil.parseSubject(argMultimap.getValue(PREFIX_SUBJECT).get());
        WeeklyLessonSlot lessonSlot = ParserUtil.parseWeeklyLessonSlot(argMultimap.getValue(PREFIX_LESSON_SLOT).get());
        HourlyRate rate = ParserUtil.parseHourlyRate(argMultimap.getValue(PREFIX_RATE).get());

        Optional<LessonMode> mode = Optional.empty();
        if (argMultimap.getValue(PREFIX_MODE).isPresent()) {
            mode = Optional.of(ParserUtil.parseLessonMode(argMultimap.getValue(PREFIX_MODE).get()));
        }

        Optional<LessonLocation> location = Optional.empty();
        if (argMultimap.getValue(PREFIX_LOCATION).isPresent()) {
            location = Optional.of(ParserUtil.parseLessonLocation(argMultimap.getValue(PREFIX_LOCATION).get()));
        }

        Lesson lesson = new Lesson(subject, lessonSlot, rate, location, mode);

        return new LessonAddCommand(index, lesson);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
