package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    /** Which screen the main window should show after the command. */
    public enum Screen { UNCHANGED, STUDENTS, WEEK }

    private final String feedbackToUser;
    private final boolean warning;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    private final Screen screen;

    /** The student whose details the student table should open, if any. */
    private final Optional<Index> studentToOpen;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, Screen.UNCHANGED, Optional.empty());
    }

    /**
     * Constructs a {@code CommandResult} that also switches the main window to {@code screen}.
     */
    public CommandResult(String feedbackToUser, Screen screen) {
        this(feedbackToUser, false, false, screen, Optional.empty());
    }

    /**
     * Constructs a {@code CommandResult} that shows the student table with the details of the student at
     * {@code studentToOpen} open.
     */
    public CommandResult(String feedbackToUser, Index studentToOpen) {
        this(feedbackToUser, false, false, Screen.STUDENTS, Optional.of(studentToOpen));
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, Screen screen,
            Optional<Index> studentToOpen) {
        this(feedbackToUser, showHelp, exit, screen, studentToOpen, false);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, Screen screen,
            Optional<Index> studentToOpen, boolean warning) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.screen = requireNonNull(screen);
        this.studentToOpen = requireNonNull(studentToOpen);
        this.warning = warning;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /** Creates successful feedback requiring the user's attention. */
    public static CommandResult warning(String feedback) {
        return new CommandResult(feedback, false, false, Screen.UNCHANGED, Optional.empty(), true);
    }

    public boolean isWarning() {
        return warning;
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    public Screen getScreen() {
        return screen;
    }

    public Optional<Index> getStudentToOpen() {
        return studentToOpen;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && warning == otherCommandResult.warning
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && screen == otherCommandResult.screen
                && studentToOpen.equals(otherCommandResult.studentToOpen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, screen, studentToOpen, warning);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("screen", screen)
                .add("studentToOpen", studentToOpen)
                .toString();
    }

}
