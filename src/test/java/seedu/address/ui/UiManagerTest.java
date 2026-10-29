package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;

/**
 * Verifies that a problem found before the window opens, such as an unreadable data file, reaches the user.
 */
public class UiManagerTest {
    private static final String WARNING = "Could not read data/studentbook.json, so StudentBook started with no "
            + "students.";

    @TempDir
    public Path temporaryFolder;

    private Stage stage;

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @AfterEach
    public void closeWindow() throws Exception {
        runOnFxThread(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    @Test
    public void start_withStartupWarning_showsWarningInResultDisplay() throws Exception {
        runOnFxThread(() -> {
            start(WARNING);

            Node resultRow = stage.getScene().lookup("#resultRow");
            assertTrue(resultRow.getStyleClass().contains("result-warning"));
            assertTrue(containsText(resultContent(), WARNING));
        });
    }

    @Test
    public void start_withoutStartupWarning_showsNoWarning() throws Exception {
        runOnFxThread(() -> {
            start(null);

            assertFalse(stage.getScene().lookup("#resultRow").getStyleClass().contains("result-warning"));
            assertFalse(containsText(resultContent(), WARNING));
        });
    }

    private void start(String startupWarning) {
        Path dataFile = temporaryFolder.resolve("students.json");
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        stage = new Stage();
        stage.setOpacity(0);
        new UiManager(logic, dataFile, startupWarning, url -> { }).start(stage);
    }

    private Node resultContent() {
        return ((ScrollPane) stage.getScene().lookup("#resultScroll")).getContent();
    }

    private static boolean containsText(Node node, String text) {
        if (node instanceof Text textNode && textNode.getText().contains(text)) {
            return true;
        }
        if (node instanceof Parent parent) {
            return parent.getChildrenUnmodifiable().stream().anyMatch(child -> containsText(child, text));
        }
        return false;
    }
}
