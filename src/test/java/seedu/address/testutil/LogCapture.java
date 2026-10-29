package seedu.address.testutil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import seedu.address.commons.core.LogsCenter;

/** Captures application diagnostics at every level, including exception details, until closed. */
public class LogCapture extends Handler implements AutoCloseable {
    private final Logger logger;
    private final Level previousLevel;
    private final List<String> entries = new ArrayList<>();
    private final SimpleFormatter formatter = new SimpleFormatter();

    /** Starts capturing application logs and temporarily enables all diagnostic levels. */
    public LogCapture() {
        // Initialize LogsCenter before attaching to its parent logger.
        LogsCenter.getLogger(LogCapture.class);
        logger = Logger.getLogger("studentbook");
        previousLevel = logger.getLevel();
        logger.setLevel(Level.ALL);
        setLevel(Level.ALL);
        logger.addHandler(this);
    }

    @Override
    public void publish(LogRecord record) {
        entries.add(formatter.format(record));
    }

    public String text() {
        return String.join("", entries);
    }

    @Override
    public void flush() {}

    @Override
    public void close() {
        logger.removeHandler(this);
        logger.setLevel(previousLevel);
    }
}
