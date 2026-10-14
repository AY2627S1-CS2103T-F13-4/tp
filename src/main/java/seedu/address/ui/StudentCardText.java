package seedu.address.ui;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Student;
import seedu.address.model.person.WeeklyLessonSlot;

/**
 * Formats a student's lessons and optional details for {@code PersonCard}, and decides their colour coding.
 */
final class StudentCardText {

    static final String NOT_SET = "Not set";
    static final String NO_LESSONS = "No lessons yet";

    /** School stage of a schooling level, which sets the colour of its chip. */
    enum Stage { PRIMARY, SECONDARY, JC, OTHER }

    /**
     * One lesson, split into the parts the card lines up in columns. The number is the lesson's LESSON_INDEX.
     */
    record LessonLine(int number, String subject, String slot, String rate,
            Optional<String> location, Optional<LessonMode> mode) {}

    // Levels are free text, so only clear forms such as "P5", "Primary 5", "Sec 3 Express" or "JC1" get a stage
    // colour. Anything else (e.g. "Poly Y1", "IB") is OTHER, so a level is never shown in the wrong colour.
    private static final Pattern PRIMARY = Pattern.compile("(?i)(p|pri|primary)\\s*[1-6](\\s.*)?");
    private static final Pattern SECONDARY = Pattern.compile("(?i)(s|sec|secondary)\\s*[1-5](\\s.*)?");
    private static final Pattern JC = Pattern.compile("(?i)(j|jc|junior college)\\s*[1-3](\\s.*)?");

    private StudentCardText() {}

    /** Returns the school stage of {@code level}, or {@code OTHER} if it is not a recognised form. */
    static Stage stageOf(String level) {
        String trimmed = level.strip();
        if (PRIMARY.matcher(trimmed).matches()) {
            return Stage.PRIMARY;
        }
        if (SECONDARY.matcher(trimmed).matches()) {
            return Stage.SECONDARY;
        }
        if (JC.matcher(trimmed).matches()) {
            return Stage.JC;
        }
        return Stage.OTHER;
    }

    /**
     * Returns one entry per lesson, numbered from 1 in stored order so the numbers match {@code LESSON_INDEX},
     * e.g. 1, "Math", "Tue 4.00pm-5.30pm", "SGD 60/h", location "Library", mode Physical.
     * Returns an empty list when the student has no lessons.
     */
    static List<LessonLine> lessons(Student student) {
        List<LessonLine> lines = new ArrayList<>();
        List<Lesson> lessons = student.getLessons();
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            lines.add(new LessonLine(i + 1, lesson.getSubject().toString(), shortSlot(lesson.getWeeklyLessonSlot()),
                    shortRate(lesson.getHourlyRate()), lesson.getLocation().map(Object::toString),
                    lesson.getMode()));
        }
        return lines;
    }

    /**
     * Returns the lesson that starts soonest after {@code now} in the weekly cycle, or empty if there are none.
     * A lesson starting at exactly {@code now} counts as next; one that has started counts from next week.
     */
    static Optional<LessonLine> nextLesson(List<LessonLine> lines, List<WeeklyLessonSlot> slots, LocalDateTime now) {
        LessonLine next = null;
        long soonest = Long.MAX_VALUE;
        for (int i = 0; i < lines.size(); i++) {
            WeeklyLessonSlot slot = slots.get(i);
            long minutes = minutesUntil(slot.getDay(), slot.getStart(), now);
            if (minutes < soonest) {
                soonest = minutes;
                next = lines.get(i);
            }
        }
        return Optional.ofNullable(next);
    }

    /** Returns the student's lesson slots, in the same order as {@link #lessons(Student)}. */
    static List<WeeklyLessonSlot> slots(Student student) {
        return student.getLessons().stream().map(Lesson::getWeeklyLessonSlot).toList();
    }

    private static long minutesUntil(DayOfWeek day, LocalTime start, LocalDateTime now) {
        long minutesInWeek = 7L * 24 * 60;
        long target = (day.getValue() - 1) * 24L * 60 + start.getHour() * 60L + start.getMinute();
        long current = (now.getDayOfWeek().getValue() - 1) * 24L * 60 + now.getHour() * 60L + now.getMinute();
        return Math.floorMod(target - current, minutesInWeek);
    }

    /** Returns a lesson's day and start time, e.g. "Sat 10.00am", from its short slot "Sat 10.00am-12.00pm". */
    static String dayAndStart(LessonLine line) {
        int dash = line.slot().indexOf('-');
        return dash < 0 ? line.slot() : line.slot().substring(0, dash);
    }

    /** Returns a slot with a three-letter day, e.g. "Tue 4.00pm-5.30pm", so lesson rows stay short. */
    static String shortSlot(WeeklyLessonSlot slot) {
        String full = slot.toString();
        return full.substring(0, 3) + full.substring(full.indexOf(' '));
    }

    /** Returns a rate per hour without needless decimals, e.g. "SGD 60/h" or "SGD 45.50/h". */
    static String shortRate(HourlyRate rate) {
        BigDecimal value = rate.value;
        String amount = value.stripTrailingZeros().scale() <= 0 ? value.setScale(0).toPlainString()
                : value.toPlainString();
        return "SGD " + amount + "/h";
    }

    /** Returns up to two initials for the name badge, e.g. "AT" for "Alex Tan" or "M" for "Madonna". */
    static String initials(String name) {
        String[] words = name.strip().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(2, words.length); i++) {
            initials.appendCodePoint(words[i].codePointAt(0));
        }
        return initials.toString().toUpperCase(Locale.ROOT);
    }

    /** Returns the student's subjects across their lessons, in lesson order without repeats, e.g. "Math, A-Math". */
    static Optional<String> subjects(Student student) {
        List<String> subjects = student.getLessons().stream().map(lesson -> lesson.getSubject().toString())
                .distinct().toList();
        return subjects.isEmpty() ? Optional.empty() : Optional.of(String.join(", ", subjects));
    }

    /** Returns a rate for the table's narrow Rate column, e.g. "$60/h" for "SGD 60/h". */
    static String tableRate(LessonLine line) {
        return line.rate().replace("SGD ", "$");
    }

}
