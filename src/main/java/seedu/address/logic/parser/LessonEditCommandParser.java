package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.LessonEditCommand.EditLessonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new LessonEditCommand
 */
public class LessonEditCommandParser implements Parser<LessonEditCommand> {
    private static final int REQUIRED_INDEX_COUNT = 2;

    /**
     * Parses the given {@code String} of arguments in the context of the LessonEditCommand
     * and returns a LessonEditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public LessonEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = LessonCommandParserUtil.tokenize(args, LessonEditCommand.MESSAGE_USAGE);
        Index studentIndex;
        Index lessonIndex;

        try {
            List<Index> indexes = ParserUtil.parseIndexes(argMultimap.getPreamble(), REQUIRED_INDEX_COUNT);
            studentIndex = indexes.getFirst();
            lessonIndex = indexes.getLast();
        } catch (ParseException pe) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, LessonEditCommand.MESSAGE_USAGE), pe);
        }

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
                    : Optional.of(ParserUtil.parseLessonMode(mode)));
        }

        if (argMultimap.getValue(PREFIX_LOCATION).isPresent()) {
            String location = argMultimap.getValue(PREFIX_LOCATION).get();
            editLessonDescriptor.setLessonLocation(location.isBlank()
                    ? Optional.empty()
                    : Optional.of(ParserUtil.parseLessonLocation(location)));
        }

        if (!editLessonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(LessonEditCommand.MESSAGE_NOT_EDITED);
        }

        return new LessonEditCommand(studentIndex, lessonIndex, editLessonDescriptor);
    }
}
