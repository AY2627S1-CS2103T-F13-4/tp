package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonCommand;
import seedu.address.logic.commands.LessonCommand.Operation;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

/** Parses lesson commands separately from student edits. */
public class LessonCommandParser implements Parser<LessonCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile(
            "(?:^|\\s)(?!(?:subject|slot|rate|mode|location)/)[A-Za-z]+/");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private final Operation operation;

    public LessonCommandParser(Operation operation) {
        this.operation = requireNonNull(operation);
    }

    @Override
    public LessonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String usage = switch (operation) {
            case ADD -> "lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE "
                    + "[mode/MODE] [location/LOCATION]";
            case EDIT -> "lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] [slot/LESSON_SLOT] "
                    + "[rate/HOURLY_RATE] [mode/MODE] [location/LOCATION] (at least one field required)";
            case DELETE -> "lesson-delete STUDENT_INDEX LESSON_INDEX";
        };
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
        if (UNKNOWN_PREFIX.matcher(args).find()) {
            throw new ParseException(formatError);
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_LESSON_SLOT,
                PREFIX_RATE, PREFIX_MODE, PREFIX_LOCATION);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_LESSON_SLOT, PREFIX_RATE, PREFIX_MODE,
                PREFIX_LOCATION);

        Index studentIndex;
        Optional<Index> lessonIndex = Optional.empty();
        try {
            String preamble = arguments.getPreamble().trim();
            if (operation == Operation.ADD) {
                studentIndex = ParserUtil.parseIndex(preamble);
            } else {
                String[] indexes = WHITESPACE.split(preamble);
                if (indexes.length != 2) {
                    throw new ParseException(formatError);
                }
                studentIndex = ParserUtil.parseIndex(indexes[0]);
                lessonIndex = Optional.of(ParserUtil.parseIndex(indexes[1]));
            }
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }

        Optional<String> subjectValue = arguments.getValue(PREFIX_SUBJECT);
        Optional<String> slotValue = arguments.getValue(PREFIX_LESSON_SLOT);
        Optional<String> rateValue = arguments.getValue(PREFIX_RATE);
        Optional<String> modeValue = arguments.getValue(PREFIX_MODE);
        Optional<String> locationValue = arguments.getValue(PREFIX_LOCATION);
        boolean clearMode = operation == Operation.EDIT && modeValue.isPresent() && modeValue.get().isBlank();
        boolean clearLocation = operation == Operation.EDIT && locationValue.isPresent()
                && locationValue.get().isBlank();
        boolean hasAllFields = subjectValue.isPresent() && slotValue.isPresent() && rateValue.isPresent();
        boolean hasAnyField = subjectValue.isPresent() || slotValue.isPresent() || rateValue.isPresent()
                || modeValue.isPresent() || locationValue.isPresent();
        if ((operation == Operation.ADD && !hasAllFields)
                || (operation == Operation.EDIT && !hasAnyField)
                || (operation == Operation.DELETE && hasAnyField)) {
            throw new ParseException(formatError);
        }

        Optional<Subject> subject = Optional.empty();
        if (subjectValue.isPresent()) {
            subject = Optional.of(ParserUtil.parseSubject(subjectValue.get()));
        }
        Optional<WeeklyLessonSlot> slot = Optional.empty();
        if (slotValue.isPresent()) {
            slot = Optional.of(ParserUtil.parseWeeklyLessonSlot(slotValue.get()));
        }
        Optional<HourlyRate> rate = Optional.empty();
        if (rateValue.isPresent()) {
            rate = Optional.of(ParserUtil.parseHourlyRate(rateValue.get()));
        }
        Optional<LessonMode> mode = Optional.empty();
        if (modeValue.isPresent() && !clearMode) {
            mode = Optional.of(ParserUtil.parseLessonMode(modeValue.get()));
        }
        Optional<LessonLocation> location = Optional.empty();
        if (locationValue.isPresent() && !clearLocation) {
            location = Optional.of(ParserUtil.parseLessonLocation(locationValue.get()));
        }
        return new LessonCommand(studentIndex, operation, lessonIndex, subject, slot, rate, location, mode,
                clearLocation, clearMode);
    }
}
