package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Shared argument validation for guardian add and edit parsers.
 */
final class GuardianCommandParserUtil {
    private GuardianCommandParserUtil() {}

    /**
     * Tokenizes guardian fields without validating them.
     * Other slash-containing text stays in the values for name and phone validation.
     */
    static ArgumentMultimap tokenize(String args) {
        requireNonNull(args);
        return ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE);
    }

    /** Rejects repeated prefixes, after index and required-field checks. */
    static void validatePrefixes(ArgumentMultimap arguments) throws ParseException {
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE);
    }
}
