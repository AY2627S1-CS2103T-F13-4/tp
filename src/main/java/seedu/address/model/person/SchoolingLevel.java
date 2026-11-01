package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** An immutable, normalized tuition label. */
public final class SchoolingLevel {

    /** School stage of a level, in the order students are listed. */
    public enum Stage { PRIMARY, SECONDARY, JC, OTHER }

    public static final String MESSAGE_CONSTRAINTS =
            "Schooling levels must contain 1-30 characters, start with a letter or digit, include a letter, "
            + "and use only letters, digits, spaces, apostrophes, hyphens, periods or parentheses";
    private static final String VALIDATION_REGEX = "[A-Za-z0-9][A-Za-z0-9 '.()\\-]*";

    // Levels are free text, so only clear forms such as "P5", "Primary 5", "Sec 3 Express" or "JC1" get a stage.
    // Anything else (e.g. "Poly Y1", "IB") is OTHER, so a level is never put in the wrong stage.
    private static final Pattern PRIMARY = Pattern.compile("(?i)(p|pri|primary)\\s*[1-6](\\s.*)?");
    private static final Pattern SECONDARY = Pattern.compile("(?i)(s|sec|secondary)\\s*[1-5](\\s.*)?");
    private static final Pattern JC = Pattern.compile("(?i)(j|jc|junior college)\\s*[1-3](\\s.*)?");
    // Splits text such as "Sec 3 Express" or "prim" into its leading stage word and the rest.
    private static final Pattern STAGE_WORD = Pattern.compile("(?i)(junior college|[a-z]+)\\s*(.*)");

    public final String value;

    /** Creates a validated label, trimming and collapsing spaces and tabs. */
    public SchoolingLevel(String text) {
        requireNonNull(text);
        String normalized = normalize(text);
        checkArgument(isValidSchoolingLevel(text), MESSAGE_CONSTRAINTS);
        value = normalized;
    }

    /** Returns whether the normalized input is a valid label. */
    public static boolean isValidSchoolingLevel(String text) {
        requireNonNull(text);
        String normalized = normalize(text);
        return normalized.length() <= 30 && normalized.matches(VALIDATION_REGEX)
                && normalized.matches(".*[A-Za-z].*");
    }

    /** Returns the school stage of this level, or {@code OTHER} if it is not a recognised form. */
    public Stage stage() {
        return stageOf(value);
    }

    /** Returns the school stage of {@code level}, or {@code OTHER} if it is not a recognised form. */
    public static Stage stageOf(String level) {
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
     * Returns whether {@code text} names the stage of {@code level}, by any of its words or their starts, with
     * the rest of {@code text} starting what follows the level's stage word. E.g. "prim" and "Secondary 3" name
     * "P5" and "Sec 3 Express", but "Secondary 3" does not name "Sec 2 G3".
     */
    public static boolean isStageNamedBy(String level, String text) {
        Stage stage = stageOf(level);
        Matcher query = STAGE_WORD.matcher(text.strip());
        if (stage == Stage.OTHER || !query.matches() || stageNamed(query.group(1)) != stage) {
            return false;
        }
        Matcher rest = STAGE_WORD.matcher(level.strip());
        return rest.matches() && rest.group(2).toLowerCase(Locale.ROOT)
                .startsWith(query.group(2).toLowerCase(Locale.ROOT));
    }

    private static Stage stageNamed(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        if ("primary".startsWith(lower)) {
            return Stage.PRIMARY;
        }
        if ("secondary".startsWith(lower)) {
            return Stage.SECONDARY;
        }
        if (lower.equals("jc") || "junior college".startsWith(lower)) {
            return Stage.JC;
        }
        return Stage.OTHER;
    }

    /** Returns the year number in this level, e.g. 5 for "P5", or {@code Integer.MAX_VALUE} if it has none. */
    public int number() {
        if (stage() == Stage.OTHER) {
            return Integer.MAX_VALUE;
        }
        // A recognised form always has its year as the first digit, e.g. "Sec 3 Express".
        return Character.getNumericValue(value.chars().filter(Character::isDigit).findFirst().getAsInt());
    }

    private static String normalize(String text) {
        return text.replaceAll("^[ \t]+|[ \t]+$", "").replaceAll("[ \t]+", " ");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SchoolingLevel label && value.equals(label.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
