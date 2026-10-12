package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Commands entered this session, browsed with Up and Down in the command box like a terminal.
 * Browsing keeps the unsent text, so Down past the newest command restores it.
 */
final class CommandHistory {

    private final List<String> commands = new ArrayList<>();
    /** Position while browsing; equals {@code commands.size()} when not browsing. */
    private int position;
    private String draft = "";

    /** Records a submitted command and stops browsing. Repeats of the newest command are kept once. */
    void add(String command) {
        if (!command.isBlank() && (commands.isEmpty() || !commands.get(commands.size() - 1).equals(command))) {
            commands.add(command);
        }
        position = commands.size();
        draft = "";
    }

    /** Returns the command before the current position, or empty at the oldest one. */
    Optional<String> previous(String currentText) {
        if (position == 0) {
            return Optional.empty();
        }
        if (position == commands.size()) {
            draft = currentText;
        }
        position--;
        return Optional.of(commands.get(position));
    }

    /** Returns the command after the current position, the unsent draft after the newest, or empty if not browsing. */
    Optional<String> next() {
        if (position == commands.size()) {
            return Optional.empty();
        }
        position++;
        return Optional.of(position == commands.size() ? draft : commands.get(position));
    }
}
