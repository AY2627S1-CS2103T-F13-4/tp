package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonCommand;
import seedu.address.logic.commands.LessonCommand.Operation;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

/** Parses lesson commands separately from student edits. */
public class LessonCommandParser implements Parser<LessonCommand> {
    private static final Pattern UNKNOWN_PREFIX = Pattern.compile("(?:^|\\s)(?![sir]/)[^\\s/]+/");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private final Operation operation;

    public LessonCommandParser(Operation operation) {
        this.operation = requireNonNull(operation);
    }

    @Override
    public LessonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String usage = switch (operation) {
            case ADD -> "lesson-add STUDENT_INDEX s/SUBJECT i/LESSON_SLOT r/HOURLY_RATE";
            case EDIT -> "lesson-edit STUDENT_INDEX LESSON_INDEX [s/SUBJECT] [i/LESSON_SLOT] [r/HOURLY_RATE] "
                    + "(at least one field required)";
            case DELETE -> "lesson-delete STUDENT_INDEX LESSON_INDEX";
        };
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
        if (UNKNOWN_PREFIX.matcher(args).find()) {
            throw new ParseException(formatError);
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_LESSON_SLOT,
                PREFIX_RATE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_LESSON_SLOT, PREFIX_RATE);

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
        boolean hasAllFields = subjectValue.isPresent() && slotValue.isPresent() && rateValue.isPresent();
        boolean hasAnyField = subjectValue.isPresent() || slotValue.isPresent() || rateValue.isPresent();
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
        return new LessonCommand(studentIndex, operation, lessonIndex, subject, slot, rate);
    }
}
