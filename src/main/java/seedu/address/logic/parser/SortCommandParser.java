package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.RateSortOrder;

/** Parses input arguments and creates a new SortCommand object. */
public class SortCommandParser implements Parser<SortCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the SortCommand.
     * @throws ParseException if the arguments are not {@code rate asc} or {@code rate desc}
     */
    public SortCommand parse(String args) throws ParseException {
        String[] words = args.trim().toLowerCase(Locale.ROOT).split("\\s+");
        if (words.length == 2 && words[0].equals("rate")) {
            if (words[1].equals("asc")) {
                return new SortCommand(RateSortOrder.ASCENDING);
            }
            if (words[1].equals("desc")) {
                return new SortCommand(RateSortOrder.DESCENDING);
            }
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
    }
}
