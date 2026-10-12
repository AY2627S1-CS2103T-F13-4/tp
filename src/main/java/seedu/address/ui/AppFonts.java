package seedu.address.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javafx.scene.text.Font;
import seedu.address.commons.core.LogsCenter;

/**
 * Loads the bundled Geist fonts (SIL Open Font License, see {@code /fonts/OFL.txt}) so the stylesheet can use them.
 * JavaFX registers each weight as its own family, e.g. "Geist SemiBold".
 * If a font cannot be loaded, the stylesheet's families fall back to the system font.
 */
final class AppFonts {

    private static final List<String> FILES = List.of(
            "Geist-Regular.ttf", "Geist-Medium.ttf", "Geist-SemiBold.ttf", "GeistMono-Regular.ttf");
    private static final Logger logger = LogsCenter.getLogger(AppFonts.class);
    private static boolean loaded;

    private AppFonts() {}

    /** Loads the bundled fonts once. Call before any window using the stylesheet is shown. */
    static void load() {
        if (loaded) {
            return;
        }
        for (String file : FILES) {
            try (InputStream in = AppFonts.class.getResourceAsStream("/fonts/" + file)) {
                if (in == null || Font.loadFont(in, 13) == null) {
                    logger.warning("Could not load font " + file + "; using the system font instead.");
                }
            } catch (IOException e) {
                logger.warning("Could not read font " + file + ": " + e.getMessage());
            }
        }
        loaded = true;
    }
}
