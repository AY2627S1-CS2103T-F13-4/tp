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
     * Tokenizes name and phone fields, rejecting repeated prefixes.
     * Other slash-containing text stays in the values for name and phone validation.
     */
    static ArgumentMultimap tokenize(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE);
        return arguments;
    }
}
