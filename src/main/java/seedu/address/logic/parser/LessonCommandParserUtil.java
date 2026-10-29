package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Shared argument validation for lesson add and edit parsers.
 */
final class LessonCommandParserUtil {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile(
            "(?:^|\\s)(?!(?:subject|slot|rate|mode|location)/)[A-Za-z]+/");

    private LessonCommandParserUtil() {}

    /**
     * Tokenizes lesson fields, rejecting unknown or repeated prefixes.
     */
    static ArgumentMultimap tokenize(String args, String usage) throws ParseException {
        requireNonNull(args);
        if (UNKNOWN_PREFIX.matcher(args).find()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_LESSON_SLOT,
                PREFIX_RATE, PREFIX_MODE, PREFIX_LOCATION);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_LESSON_SLOT, PREFIX_RATE, PREFIX_MODE,
                PREFIX_LOCATION);
        return arguments;
    }
}
