package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.LessonEditCommand.EditLessonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

/**
 * Parses input arguments and creates a new LessonEditCommand
 */
public class LessonEditCommandParser implements Parser<LessonEditCommand> {
    /**
     * Parses the given {@code String} of arguments in the context of the LessonEditCommand
     * and returns a LessonEditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LessonEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_SUBJECT, PREFIX_LESSON_SLOT, PREFIX_RATE, PREFIX_MODE, PREFIX_LOCATION);
        Index studentIndex;
        Index lessonIndex;

        try {
            List<Index> indexes = ParserUtil.parseIndexes(argMultimap.getPreamble(), 2);
            studentIndex = indexes.get(0);
            lessonIndex = indexes.get(1);
        } catch (ParseException pe) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, LessonEditCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_SUBJECT, PREFIX_LESSON_SLOT, PREFIX_RATE, PREFIX_MODE, PREFIX_LOCATION);

        EditLessonDescriptor editLessonDescriptor = new EditLessonDescriptor();

        if (argMultimap.getValue(PREFIX_SUBJECT).isPresent()) {
            editLessonDescriptor.setSubject(ParserUtil.parseSubject(argMultimap.getValue(PREFIX_SUBJECT).get()));
        }

        if (argMultimap.getValue(PREFIX_LESSON_SLOT).isPresent()) {
            editLessonDescriptor.setLessonSlot(ParserUtil.parseWeeklyLessonSlot(
                    argMultimap.getValue(PREFIX_LESSON_SLOT).get()));
        }

        if (argMultimap.getValue(PREFIX_RATE).isPresent()) {
            editLessonDescriptor.setHourlyRate(ParserUtil.parseHourlyRate(argMultimap.getValue(PREFIX_RATE).get()));
        }

        if (argMultimap.getValue(PREFIX_MODE).isPresent()) {
            String mode = argMultimap.getValue(PREFIX_MODE).get();
            editLessonDescriptor.setLessonMode(mode.isBlank()
                    ? Optional.empty()
                    : Optional.of(ParserUtil.parseLessonMode(argMultimap.getValue(PREFIX_MODE).get())));
        }

        if (argMultimap.getValue(PREFIX_LOCATION).isPresent()) {
            String location = argMultimap.getValue(PREFIX_LOCATION).get();
            editLessonDescriptor.setLessonLocation(location.isBlank()
                    ? Optional.empty()
                    : Optional.of(ParserUtil.parseLessonLocation(argMultimap.getValue(PREFIX_LOCATION).get())));
        }

        if (!editLessonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(LessonEditCommand.MESSAGE_NOT_EDITED);
        }

        return new LessonEditCommand(studentIndex, lessonIndex, editLessonDescriptor);
    }
}
