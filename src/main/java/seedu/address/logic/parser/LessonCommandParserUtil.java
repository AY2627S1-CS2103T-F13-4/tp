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
     * Tokenizes lesson fields without validating them.
     */
    static ArgumentMultimap tokenize(String args) {
        requireNonNull(args);
        return ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_LESSON_SLOT,
                PREFIX_RATE, PREFIX_MODE, PREFIX_LOCATION);
    }

    /** Rejects repeated prefixes before unknown prefixes, after index and required-field checks. */
    static void validatePrefixes(String args, ArgumentMultimap arguments, String usage) throws ParseException {
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_LESSON_SLOT, PREFIX_RATE, PREFIX_MODE,
                PREFIX_LOCATION);
        if (UNKNOWN_PREFIX.matcher(args).find()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
    }
}
