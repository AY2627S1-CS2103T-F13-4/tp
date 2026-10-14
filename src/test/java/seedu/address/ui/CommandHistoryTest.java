package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    @Test
    public void browsing_withNoHistory_doesNothing() {
        CommandHistory history = new CommandHistory();
        assertEquals(Optional.empty(), history.previous("half typed"));
        assertEquals(Optional.empty(), history.next());
    }

    // Up walks back from the newest command and stops at the oldest; Down walks forward again.
    @Test
    public void previousAndNext_walkCommandsInOrder() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("find alex");
        assertEquals(Optional.of("find alex"), history.previous(""));
        assertEquals(Optional.of("list"), history.previous("find alex"));
        assertEquals(Optional.empty(), history.previous("list"));
        assertEquals(Optional.of("find alex"), history.next());
        assertEquals(List.of("list", "find alex"), history.recent(3));
        assertEquals(List.of("find alex"), history.recent(1));
    }

    // A tutor who presses Up by accident must get their unsent text back with Down.
    @Test
    public void next_pastNewest_restoresUnsentText() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.previous("lesson-add 2 subject/Ma");
        assertEquals(Optional.of("lesson-add 2 subject/Ma"), history.next());
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void add_skipsBlankAndRepeatedCommands() {
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("list");
        history.add("  ");
        assertEquals(Optional.of("list"), history.previous(""));
        assertEquals(Optional.empty(), history.previous("list"));
    }
}
