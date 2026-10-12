package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Student;

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
     * e.g. 1, "Math", "Tuesday 4.00pm-5.30pm", "SGD 60.00/hour", location "Library", mode Physical.
     * Returns an empty list when the student has no lessons.
     */
    static List<LessonLine> lessons(Student student) {
        List<LessonLine> lines = new ArrayList<>();
        List<Lesson> lessons = student.getLessons();
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            lines.add(new LessonLine(i + 1, lesson.getSubject().toString(), lesson.getWeeklyLessonSlot().toString(),
                    "SGD " + lesson.getHourlyRate() + "/hour", lesson.getLocation().map(Object::toString),
                    lesson.getMode()));
        }
        return lines;
    }

    /** Returns the guardian's name and phone, e.g. "Janet Tan (91234567)", or empty if none is recorded. */
    static Optional<String> guardian(Student student) {
        return student.getGuardian().map(guardian -> guardian.getName() + " (" + guardian.getPhone() + ")");
    }
}
