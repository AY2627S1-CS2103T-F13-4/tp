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
import javafx.scene.layout.HBox;
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
            assertText(math, List.of("Alice", "9.10am-10.05am", "#1.1 \u00b7 Math"),
                    "#1.1 Alice\nMath, Monday 9.10am-10.05am, SGD 45.50/hour, Physical, at Library");
            assertPlacement(math, 1, 1, 2, 10, 55);

            logic.execute("lesson-add 1 subject/English slot/Thu 2.00pm 3.00pm rate/40");
            assertEquals(2, cards().size());
            logic.execute("lesson-edit 1 1 subject/Science slot/Wed 11.15am 11.45am rate/60"
                    + " mode/virtual location/Zoom");
            Region science = card("Alice", "Science");
            assertText(science, List.of("Alice", "11.15am-11.45am", "#1.1 \u00b7 Science"),
                    "#1.1 Alice\nScience, Wednesday 11.15am-11.45am, SGD 60.00/hour, Virtual, at Zoom");
            assertPlacement(science, 3, 3, 1, 15, 15);
            assertPlacement(card("Alice", "English"), 4, 6, 1, 0, 0);

            logic.execute("lesson-edit 1 1 mode/ location/");
            assertText(card("Alice", "Science"), List.of("Alice", "11.15am-11.45am", "#1.1 \u00b7 Science"),
                    "#1.1 Alice\nScience, Wednesday 11.15am-11.45am, SGD 60.00/hour");

            // Deleting the first lesson renumbers the second, as lesson-edit would see it.
            logic.execute("lesson-delete 1 1");
            assertEquals(1, cards().size());
            assertText(card("Alice", "English"), List.of("Alice", "2.00pm-3.00pm", "#1.1 \u00b7 English"),
                    "#1.1 Alice\nEnglish, Thursday 2.00pm-3.00pm, SGD 40.00/hour");
            assertPlacement(card("Alice", "English"), 4, 6, 1, 0, 0);
            logic.execute("lesson-delete 1 1");
            assertEmptySchedule();
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            return null;
        });
    }

    // The number on each lesson is the student's index in the shown list, so it changes with find and list.
    @Test
    public void execute_findEditAndList_numbersFollowShownStudentIndices() throws Exception {
        callOnFxThread(() -> {
            logic.execute("lesson-add 1 subject/Math slot/Mon 9.00am 10.00am rate/45");
            logic.execute("lesson-add 2 subject/English slot/Fri 2.00pm 3.00pm rate/50");
            logic.execute("find Bob");
            assertEquals(List.of("Bob"), cardNames());
            assertPlacement(card("Bob", "English"), 5, 6, 1, 0, 0);

            // Index one now denotes Bob, although Alice is first in the backing address book.
            logic.execute("lesson-edit 1 1 subject/Science slot/Tue 10.10am 10.40am");
            assertEquals(List.of("Bob"), cardNames());
            assertText(card("Bob", "Science"), List.of("Bob", "10.10am-10.40am", "#1.1 \u00b7 Science"),
                    "#1.1 Bob\nScience, Tuesday 10.10am-10.40am, SGD 50.00/hour");
            assertPlacement(card("Bob", "Science"), 2, 2, 1, 10, 20);

            logic.execute("list");
            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertText(card("Alice", "Math"), List.of("Alice", "9.00am-10.00am", "#1.1 \u00b7 Math"),
                    "#1.1 Alice\nMath, Monday 9.00am-10.00am, SGD 45.00/hour");
            assertText(card("Bob", "Science"), List.of("Bob", "10.10am-10.40am", "#2.1 \u00b7 Science"),
                    "#2.1 Bob\nScience, Tuesday 10.10am-10.40am, SGD 50.00/hour");
            assertPlacement(card("Alice", "Math"), 1, 1, 1, 0, 0);
            assertPlacement(card("Bob", "Science"), 2, 2, 1, 10, 20);

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
            assertPlacement(card("Bob", "Math"), 2, 1, 1, 0, 0);

            logic.execute("edit 2 name/Robert");
            assertEquals(List.of("Alice", "Robert"), cardNames());
            assertText(card("Robert", "Math"), List.of("Robert", "9.00am-10.00am", "#2.1 \u00b7 Math"),
                    "#2.1 Robert\nMath, Monday 9.00am-10.00am, SGD 45.00/hour");

            logic.execute("delete 1");
            assertEquals(List.of("Robert"), cardNames());
            assertText(card("Robert", "Math"), List.of("Robert", "9.00am-10.00am", "#1.1 \u00b7 Math"),
                    "#1.1 Robert\nMath, Monday 9.00am-10.00am, SGD 45.00/hour");
            assertPlacement(card("Robert", "Math"), 1, 1, 1, 0, 0);
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
            assertText(card("Alice", "Math"), List.of("Alice", "9.10am-9.25am", "#1.1 \u00b7 Math"),
                    "#1.1 Alice\nMath, Monday 9.10am-9.25am, SGD 45.50/hour, Physical, at Room A");
            assertPlacement(card("Alice", "Math"), 1, 1, 1, 10, 35);
            assertText(card("Bob", "English"), List.of("Bob", "11.45pm-11.59pm", "#2.1 \u00b7 English"),
                    "#2.1 Bob\nEnglish, Sunday 11.45pm-11.59pm, SGD 50.00/hour, Virtual");
            assertPlacement(card("Bob", "English"), 7, 15, 1, 45, 1);
            assertEquals(16, schedule.getRoot().getRowConstraints().size());

            logic.execute("lesson-edit 1 1 subject/Physics");
            assertText(card("Alice", "Physics"), List.of("Alice", "9.10am-9.25am", "#1.1 \u00b7 Physics"),
                    "#1.1 Alice\nPhysics, Monday 9.10am-9.25am, SGD 45.50/hour, Physical, at Room A");
            assertPlacement(card("Alice", "Physics"), 1, 1, 1, 10, 35);
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
            assertText(card("Alice", "Math"), List.of("Alice", "9.10am-9.25am", "#1.1 \u00b7 Math"),
                    "#1.1 Alice\nMath, Monday 9.10am-9.25am, SGD 45.50/hour");
            assertPlacement(card("Alice", "Math"), 1, 1, 1, 10, 35);
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
        return cards().stream().filter(node -> labelText(node).get(0).equals(student)
                && labelText(node).get(2).endsWith(" " + subject)).findFirst().orElseThrow();
    }

    private List<String> cardNames() {
        return cards().stream().map(node -> labelText(node).get(0)).toList();
    }

    private List<String> timeLabels() {
        return schedule.getRoot().getChildren().stream().filter(Label.class::isInstance).map(Label.class::cast)
                .filter(label -> Integer.valueOf(0).equals(GridPane.getColumnIndex(label)))
                .map(Label::getText).toList();
    }

    /** No lessons: the seven days and the 9am to 8pm rows stay, with no cards on them. */
    private void assertEmptySchedule() {
        assertEquals(List.of(), cards());
        assertEquals(11, timeLabels().size());
        assertEquals(12, schedule.getRoot().getRowConstraints().size());
        assertEquals(8, schedule.getRoot().getColumnConstraints().size());
        assertEquals(7, schedule.getRoot().getChildren().stream().filter(HBox.class::isInstance)
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
