package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.GuardianCommand;
import seedu.address.logic.commands.GuardianCommand.Operation;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/** Parses guardian commands separately from student edits. */
public class GuardianCommandParser implements Parser<GuardianCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?:^|\\s)(?![np]/)[^\\s/]+/");
    private final Operation operation;

    public GuardianCommandParser(Operation operation) {
        this.operation = requireNonNull(operation);
    }

    @Override
    public GuardianCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String usage = switch (operation) {
            case ADD -> "guardian-add INDEX n/NAME p/PHONE";
            case EDIT -> "guardian-edit INDEX [n/NAME] [p/PHONE] (at least one field required)";
            case DELETE -> "guardian-delete INDEX";
        };
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
        if (operation == Operation.DELETE) {
            try {
                return new GuardianCommand(ParserUtil.parseIndex(args), operation, Optional.empty(), Optional.empty());
            } catch (ParseException e) {
                throw new ParseException(formatError, e);
            }
        }
        if (UNKNOWN_PREFIX.matcher(args).find()) {
            throw new ParseException(formatError);
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE);
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        boolean hasName = arguments.getValue(PREFIX_NAME).isPresent();
        boolean hasPhone = arguments.getValue(PREFIX_PHONE).isPresent();
        if ((operation == Operation.ADD && (!hasName || !hasPhone))
                || (operation == Operation.EDIT && !hasName && !hasPhone)) {
            throw new ParseException(formatError);
        }
        Optional<Name> name = hasName ? Optional.of(ParserUtil.parseName(arguments.getValue(PREFIX_NAME).orElseThrow()))
                : Optional.empty();
        Optional<Phone> phone = hasPhone
                ? Optional.of(ParserUtil.parsePhone(arguments.getValue(PREFIX_PHONE).orElseThrow())) : Optional.empty();
        return new GuardianCommand(index, operation, name, phone);
    }
}
