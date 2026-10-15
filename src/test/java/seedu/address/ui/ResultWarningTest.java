package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import seedu.address.logic.commands.CommandResult;
import seedu.address.testutil.FxTestUtil;

public class ResultWarningTest {
    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void warning_isDistinctFromSuccess() {
        assertTrue(CommandResult.warning("Saved").isWarning());
        assertFalse(new CommandResult("Saved").isWarning());
        assertNotEquals(new CommandResult("Saved"), CommandResult.warning("Saved"));
        assertEquals(CommandResult.warning("Saved"), CommandResult.warning("Saved"));
    }

    @Test
    public void feedback_transitionsClearPreviousStylesAndIcons() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            display.setWarningToUser("Lesson saved - scheduling clash detected\nAlice, lesson 1");
            assertTrue(display.getRoot().getStyleClass().contains("result-warning"));
            Label icon = (Label) display.getRoot().lookup("#resultIcon");
            assertEquals("!", icon.getText());
            assertTrue(icon.isVisible());
            display.setFeedbackToUser("Listed all students.");
            assertFalse(display.getRoot().getStyleClass().contains("result-warning"));
            assertEquals("", icon.getText());
            display.setWarningToUser("Clash detected");
            display.setErrorToUser("Invalid input");
            assertFalse(display.getRoot().getStyleClass().contains("result-warning"));
            assertTrue(display.getRoot().getStyleClass().contains("result-error"));
            display.setWarningToUser("Clash detected");
            assertFalse(display.getRoot().getStyleClass().contains("result-error"));
            assertTrue(display.getRoot().getStyleClass().contains("result-warning"));
        });
    }
}
