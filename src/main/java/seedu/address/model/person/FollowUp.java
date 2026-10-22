package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, outstanding item the tutor has to follow up on with a student's guardian, such as sending an update.
 * Completing an item removes it, so every recorded item is outstanding.
 */
public final class FollowUp {
    public static final int MAX_TEXT_LENGTH = 200;
    public static final String MESSAGE_TEXT_CONSTRAINTS =
            "Follow-up text must contain 1-" + MAX_TEXT_LENGTH + " characters on one line and cannot be blank.";

    private final String text;

    /** Creates a follow-up, trimming spaces around {@code text}. */
    public FollowUp(String text) {
        requireNonNull(text);
        checkArgument(isValidText(text), MESSAGE_TEXT_CONSTRAINTS);
        this.text = text.strip();
    }

    /**
     * Returns whether {@code text}, once trimmed, is 1 to 200 characters on a single line.
     * Characters are counted as code points, so an emoji counts as one.
     */
    public static boolean isValidText(String text) {
        requireNonNull(text);
        String trimmed = text.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        // Control characters and Unicode line or paragraph separators would split the item over lines.
        return length > 0 && length <= MAX_TEXT_LENGTH && !trimmed.matches("(?s).*[\\p{Cc}\\p{Zl}\\p{Zp}].*");
    }

    public String getText() {
        return text;
    }

    /** Returns true if both items have the same text, ignoring letter case, so they would read as duplicates. */
    public boolean isSameFollowUp(FollowUp other) {
        return other != null && text.equalsIgnoreCase(other.text);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FollowUp followUp && text.equals(followUp.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public String toString() {
        return text;
    }
}
