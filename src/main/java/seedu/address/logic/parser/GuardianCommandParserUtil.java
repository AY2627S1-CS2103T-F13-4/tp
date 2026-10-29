package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Shared argument validation for guardian add and edit parsers.
 */
final class GuardianCommandParserUtil {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?:^|\\s)(?!(?:name|phone)/)[^\\s/]+/");

    private GuardianCommandParserUtil() {}

    /**
     * Tokenizes guardian fields without validating them.
     */
    static ArgumentMultimap tokenize(String args) {
        requireNonNull(args);
        return ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE);
    }

    /** Rejects repeated prefixes before unknown prefixes, after index and required-field checks. */
    static void validatePrefixes(String args, ArgumentMultimap arguments, String usage) throws ParseException {
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE);
        if (UNKNOWN_PREFIX.matcher(args).find()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
    }
}
