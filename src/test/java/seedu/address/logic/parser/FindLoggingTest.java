package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.testutil.PersonBuilder;

public class FindLoggingTest {

    @Test
    public void searchLogging_recordsDiagnosticsAndCountsWithoutSearchValues() throws Exception {
        FindCommandParser parser = new FindCommandParser();
        // Initialize both classes before attaching handlers: LogsCenter clears handlers on logger creation.
        parser.parse("name/SecretStudent");
        Logger parserLogger = Logger.getLogger("studentbook.FindCommandParser");
        Logger commandLogger = Logger.getLogger("studentbook.FindCommand");
        Level previousParserLevel = parserLogger.getLevel();
        Level previousCommandLevel = commandLogger.getLevel();
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        parserLogger.addHandler(handler);
        commandLogger.addHandler(handler);
        parserLogger.setLevel(Level.ALL);
        commandLogger.setLevel(Level.ALL);
        try {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("SecretStudent").build());
            parser.parse("name/SecretStudent guardian/SecretGuardian").execute(model);
            parser.parse("SecretStudent").execute(model);
            assertThrows(ParseException.class, () -> parser.parse(""));
            assertThrows(ParseException.class, () -> parser.parse("SecretStudent name/SecretStudent"));
            assertThrows(ParseException.class, () -> parser.parse("guardian/91234567 guardian/87654321"));
            assertThrows(ParseException.class, () -> parser.parse("guardian/"));
            assertThrows(ParseException.class, () -> parser.parse("day/SecretDay"));

            assertEquals(List.of(
                    "Search filters selected: [name/, guardian/]",
                    "Student search completed: matchedStudents=0",
                    "Search filters selected: [bare-name]",
                    "Student search completed: matchedStudents=1",
                    "Student search rejected: no search criteria",
                    "Student search rejected: bare keywords mixed with prefixes",
                    "Student search rejected: duplicate prefixes",
                    "Student search rejected: empty filter value",
                    "Student search rejected: invalid weekday"),
                    records.stream().map(LogRecord::getMessage).toList());
            assertEquals(List.of(Level.FINE, Level.INFO, Level.FINE, Level.INFO,
                    Level.FINE, Level.FINE, Level.FINE, Level.FINE, Level.FINE),
                    records.stream().map(LogRecord::getLevel).toList());
            assertFalse(records.stream().anyMatch(record -> record.getMessage().contains("Secret")
                    || record.getMessage().contains("91234567") || record.getMessage().contains("87654321")));
        } finally {
            parserLogger.removeHandler(handler);
            commandLogger.removeHandler(handler);
            parserLogger.setLevel(previousParserLevel);
            commandLogger.setLevel(previousCommandLevel);
        }
    }
}
