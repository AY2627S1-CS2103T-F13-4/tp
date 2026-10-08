package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;

import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.SchoolingLevelContainsKeywordPredicate;

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

        if (trimmedArgs.matches("(?s)(.*\\s)?" + Pattern.quote(PREFIX_LEVEL.getPrefix()) + ".*")) {
            return parseLevelSearch(args);
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

    /** Parses {@code find l/TEXT}, which must not be combined with name keywords. */
    private FindCommand parseLevelSearch(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_LEVEL);
        if (!argMultimap.getPreamble().isEmpty()
                || argMultimap.getValue(PREFIX_LEVEL).get().isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_LEVEL);
        String level = argMultimap.getValue(PREFIX_LEVEL).get().trim();
        return new FindCommand(new SchoolingLevelContainsKeywordPredicate(level));
    }

}
