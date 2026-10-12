package seedu.address.ui;

import java.util.List;

import javafx.scene.text.Font;

/**
 * Picks a monospace font for commands, so prefixes such as {@code name/} line up and read clearly.
 */
final class MonospaceFont {

    private static final List<String> FAMILIES = List.of(
            "Menlo", "SF Mono", "Consolas", "Cascadia Mono", "DejaVu Sans Mono", "Liberation Mono");

    private MonospaceFont() {}

    /**
     * Returns an inline style setting the first installed font from a list that covers macOS, Windows and Linux.
     * JavaFX's generic "Monospaced" maps to Courier on macOS, which reads poorly at small sizes.
     */
    static String style() {
        List<String> installed = Font.getFamilies();
        String family = FAMILIES.stream().filter(installed::contains).findFirst().orElse("Monospaced");
        return "-fx-font-family: '" + family + "';";
    }
}
