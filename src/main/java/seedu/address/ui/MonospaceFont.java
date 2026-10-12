package seedu.address.ui;

import java.util.List;

import javafx.scene.text.Font;

/**
 * Picks a monospace font for commands, so prefixes such as {@code name/} line up and read clearly.
 */
final class MonospaceFont {

    private static final List<String> FAMILIES = List.of(
            "Menlo", "SF Mono", "Consolas", "Cascadia Mono", "DejaVu Sans Mono", "Liberation Mono");

    /** Chosen on first use; the installed fonts do not change while the app runs. */
    private static String family;

    private MonospaceFont() {}

    /**
     * Returns the first installed font from a list that covers macOS, Windows and Linux.
     * JavaFX's generic "Monospaced" maps to Courier on macOS, which reads poorly at small sizes.
     */
    static String family() {
        if (family == null) {
            List<String> installed = Font.getFamilies();
            family = FAMILIES.stream().filter(installed::contains).findFirst().orElse("Monospaced");
        }
        return family;
    }

    /** Returns an inline style that sets this font family. */
    static String style() {
        return "-fx-font-family: '" + family() + "';";
    }
}
