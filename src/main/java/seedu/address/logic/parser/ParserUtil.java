package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer";
    public static final String MESSAGE_INVALID_INDEX_COUNT = "Incorrect number of indexes supplied, "
            + "expected %1$s, got %2$s";
    public static final String MESSAGE_FUTURE_NOTE_DATE = "Note dates cannot be later than today";
    public static final LocalDate EARLIEST_NOTE_DATE = LocalDate.of(2000, 1, 1);
    public static final String MESSAGE_EARLY_NOTE_DATE = "Note dates cannot be earlier than 2000-01-01";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code oneBasedIndexes} into a list of {@code Index}es of expected length {@code length} and returns it.
     * Leading and trailing whitespaces will be trimmed.
     * @throws ParseException if any of the specified indexes are invalid (not a non-zero unsigned integer).
     */
    public static List<Index> parseIndexes(String oneBasedIndexes, int length) throws ParseException {
        ArrayList<Index> indexes = new ArrayList<>();
        for (String oneBasedIndex : oneBasedIndexes.trim().split("\\s+")) {
            String trimmedIndex = oneBasedIndex.trim();
            if (trimmedIndex.isEmpty()) {
                continue;
            }

            indexes.add(parseIndex(trimmedIndex));
        }

        if (indexes.size() != length) {
            throw new ParseException(String.format(MESSAGE_INVALID_INDEX_COUNT, length, indexes.size()));
        }

        return indexes;
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        // Collapse inner whitespace so "Alex  Tan" cannot coexist with "Alex Tan"
        String trimmedName = name.trim().replaceAll("\\s+", " ");
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /** Parses and normalizes an optional subject value when its prefix is supplied. */
    public static Subject parseSubject(String value) throws ParseException {
        requireNonNull(value);
        if (!Subject.isValidSubject(value)) {
            throw new ParseException(Subject.MESSAGE_CONSTRAINTS);
        }
        return new Subject(value);
    }

    /** Parses subject labels in input order, keeping the first spelling of repeated labels. */
    public static Set<Subject> parseSubjects(Collection<String> values) throws ParseException {
        requireNonNull(values);
        Set<Subject> subjects = new LinkedHashSet<>();
        for (String value : values) {
            subjects.add(parseSubject(value));
        }
        return subjects;
    }

    /**
     * Parses a {@code String school} into a {@code School}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code school} is invalid.
     */
    public static School parseSchool(String school) throws ParseException {
        requireNonNull(school);
        String trimmedSchool = school.trim();
        if (!School.isValidSchool(trimmedSchool)) {
            throw new ParseException(School.MESSAGE_CONSTRAINTS);
        }
        return new School(trimmedSchool);
    }

    /**
     * Parses a {@code String level} into a {@code SchoolingLevel}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code level} is invalid.
     */
    public static SchoolingLevel parseSchoolingLevel(String level) throws ParseException {
        requireNonNull(level);
        String trimmedLevel = level.trim();
        if (!SchoolingLevel.isValidSchoolingLevel(trimmedLevel)) {
            throw new ParseException(SchoolingLevel.MESSAGE_CONSTRAINTS);
        }
        return new SchoolingLevel(trimmedLevel);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a {@code String weeklyLessonSlot} into a {@code WeeklyLessonSlot}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code weeklyLessonSlot} is invalid.
     */
    public static WeeklyLessonSlot parseWeeklyLessonSlot(String weeklyLessonSlot) throws ParseException {
        requireNonNull(weeklyLessonSlot);
        String trimmedWeeklyLessonSlot = weeklyLessonSlot.trim();

        if (!WeeklyLessonSlot.isValidFormat(trimmedWeeklyLessonSlot)) {
            throw new ParseException(WeeklyLessonSlot.MESSAGE_INVALID_FORMAT);
        }
        if (!WeeklyLessonSlot.isValidWeeklyLessonSlot(trimmedWeeklyLessonSlot)) {
            throw new ParseException(WeeklyLessonSlot.MESSAGE_END_NOT_AFTER_START);
        }

        return new WeeklyLessonSlot(trimmedWeeklyLessonSlot);
    }

    /** Parses an SGD hourly rate with up to two decimal places. */
    public static HourlyRate parseHourlyRate(String rate) throws ParseException {
        requireNonNull(rate);
        String trimmedRate = rate.trim();
        if (!HourlyRate.isValidHourlyRate(trimmedRate)) {
            throw new ParseException(HourlyRate.MESSAGE_CONSTRAINTS);
        }
        return new HourlyRate(new BigDecimal(trimmedRate));
    }

    /** Parses a physical or virtual lesson mode, ignoring case and surrounding spaces. */
    public static LessonMode parseLessonMode(String mode) throws ParseException {
        requireNonNull(mode);
        try {
            return LessonMode.fromString(mode);
        } catch (IllegalArgumentException e) {
            throw new ParseException(LessonMode.MESSAGE_CONSTRAINTS);
        }
    }

    /** Parses and normalizes a lesson location. */
    public static LessonLocation parseLessonLocation(String location) throws ParseException {
        requireNonNull(location);
        if (!LessonLocation.isValidLocation(location)) {
            throw new ParseException(LessonLocation.MESSAGE_CONSTRAINTS);
        }
        return new LessonLocation(location);
    }

    /**
     * Parses a progress note from its date in YYYY-MM-DD format and its text.
     * A note records something that already happened, so a date after {@code today} is rejected.
     * A date before {@link #EARLIEST_NOTE_DATE} is rejected as a mistyped year, such as 0026 for 2026.
     *
     * @throws ParseException if the date or text is invalid.
     */
    public static ProgressNote parseProgressNote(String date, String text, LocalDate today) throws ParseException {
        requireAllNonNull(date, text, today);
        return new ProgressNote(parseProgressNoteDate(date, today), parseProgressNoteText(text));
    }

    /** Parses a real note date between {@link #EARLIEST_NOTE_DATE} and {@code today}, inclusive. */
    public static LocalDate parseProgressNoteDate(String date, LocalDate today) throws ParseException {
        requireAllNonNull(date, today);
        LocalDate noteDate;
        try {
            noteDate = ProgressNote.parseDate(date);
        } catch (IllegalArgumentException e) {
            throw new ParseException(ProgressNote.MESSAGE_DATE_CONSTRAINTS);
        }
        if (noteDate.isAfter(today)) {
            throw new ParseException(MESSAGE_FUTURE_NOTE_DATE);
        }
        if (noteDate.isBefore(EARLIEST_NOTE_DATE)) {
            throw new ParseException(MESSAGE_EARLY_NOTE_DATE);
        }
        return noteDate;
    }

    /** Parses single-line note text, trimming surrounding whitespace. */
    public static String parseProgressNoteText(String text) throws ParseException {
        requireNonNull(text);
        if (!ProgressNote.isValidText(text)) {
            throw new ParseException(ProgressNote.MESSAGE_TEXT_CONSTRAINTS);
        }
        return text.strip();
    }

    /**
     * Parses the text of a guardian follow-up, trimming spaces around it and collapsing repeated spaces inside it.
     *
     * @throws ParseException if the text is blank, longer than 200 characters or spans several lines.
     */
    public static FollowUp parseFollowUp(String text) throws ParseException {
        requireNonNull(text);
        // Collapse inner spaces so "Call  mum" cannot coexist with "Call mum"
        String collapsedText = text.replaceAll(" {2,}", " ");
        if (!FollowUp.isValidText(collapsedText)) {
            throw new ParseException(FollowUp.MESSAGE_TEXT_CONSTRAINTS);
        }
        return new FollowUp(collapsedText);
    }
}
