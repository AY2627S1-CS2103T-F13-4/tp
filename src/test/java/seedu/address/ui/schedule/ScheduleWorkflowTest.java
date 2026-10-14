package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.FxTestUtil.callOnFxThread;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises command and storage workflows while the schedule observes the application's filtered list.
 */
public class ScheduleWorkflowTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private ScheduleGrid schedule;

    @BeforeEach
    public void setUp() throws Exception {
        callOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addPerson(new PersonBuilder().withName("Alice").build());
            book.addPerson(new PersonBuilder().withName("Bob").build());
            storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
            connectSchedule(book);
            return null;
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (schedule != null) {
            runOnFxThread(schedule::dispose);
        }
    }

    @Test
    public void execute_lessonCrud_updatesCardDetailsAndPlacement() throws Exception {
        callOnFxThread(() -> {
            logic.execute("lesson-add 1 subject/Math slot/Mon 9.10am 10.05am rate/45.50"
                    + " mode/physical location/Library");
            Region math = card("Alice", "Math");
            assertText(math, List.of("Alice", "Math", "09:10-10:05"),
                    "Alice\nMath, Monday 9.10am-10.05am, SGD 45.50/hour, Physical, at Library");
            assertPlacement(math, 1, 1, 3, 20, 50);

            logic.execute("lesson-add 1 subject/English slot/Thu 2.00pm 3.00pm rate/40");
            assertEquals(2, cards().size());
            logic.execute("lesson-edit 1 1 subject/Science slot/Wed 11.15am 11.45am rate/60"
                    + " mode/virtual location/Zoom");
            Region science = card("Alice", "Science");
            assertText(science, List.of("Alice", "Science", "11:15-11:45"),
                    "Alice\nScience, Wednesday 11.15am-11.45am, SGD 60.00/hour, Virtual, at Zoom");
            assertPlacement(science, 3, 1, 2, 30, 30);
            assertPlacement(card("Alice", "English"), 4, 7, 2, 0, 0);

            logic.execute("lesson-edit 1 1 mode/ location/");
            assertText(card("Alice", "Science"), List.of("Alice", "Science", "11:15-11:45"),
                    "Alice\nScience, Wednesday 11.15am-11.45am, SGD 60.00/hour");

            logic.execute("lesson-delete 1 1");
            assertEquals(1, cards().size());
            assertText(card("Alice", "English"), List.of("Alice", "English", "14:00-15:00"),
                    "Alice\nEnglish, Thursday 2.00pm-3.00pm, SGD 40.00/hour");
            assertPlacement(card("Alice", "English"), 4, 1, 2, 0, 0);
            logic.execute("lesson-delete 1 1");
            assertEmptySchedule();
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            return null;
        });
    }

    @Test
    public void execute_findEditAndList_usesFilteredStudentIndicesAndRestoresOtherCards() throws Exception {
        callOnFxThread(() -> {
            logic.execute("lesson-add 1 subject/Math slot/Mon 9.00am 10.00am rate/45");
            logic.execute("lesson-add 2 subject/English slot/Fri 2.00pm 3.00pm rate/50");
            logic.execute("find Bob");
            assertEquals(List.of("Bob"), cardNames());
            assertPlacement(card("Bob", "English"), 5, 1, 2, 0, 0);

            // Index one now denotes Bob, although Alice is first in the backing address book.
            logic.execute("lesson-edit 1 1 subject/Science slot/Tue 10.10am 10.40am");
            assertEquals(List.of("Bob"), cardNames());
            assertText(card("Bob", "Science"), List.of("Bob", "Science", "10:10-10:40"),
                    "Bob\nScience, Tuesday 10.10am-10.40am, SGD 50.00/hour");
            assertPlacement(card("Bob", "Science"), 2, 1, 2, 20, 40);

            logic.execute("list");
            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertText(card("Alice", "Math"), List.of("Alice", "Math", "09:00-10:00"),
                    "Alice\nMath, Monday 9.00am-10.00am, SGD 45.00/hour");
            assertPlacement(card("Alice", "Math"), 1, 1, 2, 0, 0);
            assertPlacement(card("Bob", "Science"), 2, 3, 2, 20, 40);

            logic.execute("find Missing");
            assertEmptySchedule();
            logic.execute("list");
            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            return null;
        });
    }

    @Test
    public void execute_renameDeleteAndClear_updatesOwnersAndReusesColumns() throws Exception {
        callOnFxThread(() -> {
            logic.execute("lesson-add 1 subject/Math slot/Mon 9.00am 10.00am rate/45");
            logic.execute("lesson-add 2 subject/Math slot/Mon 9.00am 10.00am rate/45");
            assertPlacement(card("Bob", "Math"), 2, 1, 2, 0, 0);

            logic.execute("edit 2 name/Robert");
            assertEquals(List.of("Alice", "Robert"), cardNames());
            assertText(card("Robert", "Math"), List.of("Robert", "Math", "09:00-10:00"),
                    "Robert\nMath, Monday 9.00am-10.00am, SGD 45.00/hour");

            logic.execute("delete 1");
            assertEquals(List.of("Robert"), cardNames());
            assertPlacement(card("Robert", "Math"), 1, 1, 2, 0, 0);
            assertEquals(8, schedule.getRoot().getColumnConstraints().size());

            logic.execute("clear");
            assertEmptySchedule();
            assertEquals(0, model.getAddressBook().getPersonList().size());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            return null;
        });
    }

    @Test
    public void savedLessons_reloadIntoNewModel_restoresCardsAndRemainsObservable() throws Exception {
        callOnFxThread(() -> {
            logic.execute("lesson-add 1 subject/Math slot/Mon 9.10am 9.25am rate/45.50"
                    + " mode/physical location/Room A");
            logic.execute("lesson-add 2 subject/English slot/Sun 11.45pm 11.59pm rate/50 mode/virtual");
            logic.execute("find Alice");
            assertEquals(List.of("Alice"), cardNames());

            // Saving a filtered view must still retain the hidden student's lessons.
            ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
            assertEquals(model.getAddressBook(), loaded);
            connectSchedule(loaded);
            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertText(card("Alice", "Math"), List.of("Alice", "Math", "09:10-09:25"),
                    "Alice\nMath, Monday 9.10am-9.25am, SGD 45.50/hour, Physical, at Room A");
            assertPlacement(card("Alice", "Math"), 1, 1, 1, 20, 10);
            assertText(card("Bob", "English"), List.of("Bob", "English", "23:45-23:59"),
                    "Bob\nEnglish, Sunday 11.45pm-11.59pm, SGD 50.00/hour, Virtual");
            assertPlacement(card("Bob", "English"), 7, 30, 1, 30, 2);
            assertEquals(31, schedule.getRoot().getRowConstraints().size());

            logic.execute("lesson-edit 1 1 subject/Physics");
            assertText(card("Alice", "Physics"), List.of("Alice", "Physics", "09:10-09:25"),
                    "Alice\nPhysics, Monday 9.10am-9.25am, SGD 45.50/hour, Physical, at Room A");
            assertPlacement(card("Alice", "Physics"), 1, 1, 1, 20, 10);
            return null;
        });
    }

    @Test
    public void execute_invalidLessonCommands_preservesRenderedContentAndSavedData() throws Exception {
        callOnFxThread(() -> {
            logic.execute("lesson-add 1 subject/Math slot/Mon 9.10am 9.25am rate/45.50");
            ReadOnlyAddressBook original = new AddressBook(model.getAddressBook());
            Path dataFile = temporaryFolder.resolve("students.json");
            String savedData = Files.readString(dataFile);
            List<String> originalTimeLabels = timeLabels();

            assertThrows(ParseException.class, () -> logic.execute("lesson-edit 1 1 slot/Mon 10.00am 9.00am"));
            assertThrows(CommandException.class, () -> logic.execute("lesson-delete 3 1"));

            assertEquals(1, cards().size());
            assertText(card("Alice", "Math"), List.of("Alice", "Math", "09:10-09:25"),
                    "Alice\nMath, Monday 9.10am-9.25am, SGD 45.50/hour");
            assertPlacement(card("Alice", "Math"), 1, 1, 1, 20, 10);
            assertEquals(originalTimeLabels, timeLabels());
            assertEquals(original, model.getAddressBook());
            assertEquals(savedData, Files.readString(dataFile));
            return null;
        });
    }

    private void connectSchedule(ReadOnlyAddressBook book) {
        if (schedule != null) {
            schedule.dispose();
        }
        model = new ModelManager(book, new UserPrefs());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        schedule = new ScheduleGrid(logic.getFilteredPersonList());
    }

    private List<Region> cards() {
        return schedule.getRoot().getChildren().stream().filter(Region.class::isInstance).map(Region.class::cast)
                .filter(node -> node.getStyleClass().contains("lesson-card")).toList();
    }

    private Region card(String student, String subject) {
        return cards().stream().filter(node -> labelText(node).subList(0, 2).equals(List.of(student, subject)))
                .findFirst().orElseThrow();
    }

    private List<String> cardNames() {
        return cards().stream().map(node -> labelText(node).get(0)).toList();
    }

    private List<String> timeLabels() {
        return schedule.getRoot().getChildren().stream().filter(Label.class::isInstance).map(Label.class::cast)
                .filter(label -> Integer.valueOf(0).equals(GridPane.getColumnIndex(label)))
                .map(Label::getText).toList();
    }

    private void assertEmptySchedule() {
        assertEquals(List.of(), cards());
        assertEquals(List.of(), timeLabels());
        assertEquals(1, schedule.getRoot().getRowConstraints().size());
        assertEquals(8, schedule.getRoot().getColumnConstraints().size());
        assertEquals(7, schedule.getRoot().getChildren().stream().filter(Label.class::isInstance)
                .filter(node -> Integer.valueOf(0).equals(GridPane.getRowIndex(node))).count());
    }

    private static List<String> labelText(Region card) {
        return card.getChildrenUnmodifiable().stream().filter(Label.class::isInstance).map(Label.class::cast)
                .map(Label::getText).toList();
    }

    private static void assertText(Region card, List<String> expectedLabels, String expectedDetails) {
        assertEquals(expectedLabels, labelText(card));
        assertEquals(expectedDetails, card.getAccessibleText());
    }

    private static void assertPlacement(Region card, int column, int row, int span, double top, double bottom) {
        assertEquals(Integer.valueOf(column), GridPane.getColumnIndex(card));
        assertEquals(Integer.valueOf(row), GridPane.getRowIndex(card));
        assertEquals(Integer.valueOf(span), GridPane.getRowSpan(card));
        Insets margin = GridPane.getMargin(card);
        assertEquals(top, margin.getTop(), 0.001);
        assertEquals(bottom, margin.getBottom(), 0.001);
    }
}
