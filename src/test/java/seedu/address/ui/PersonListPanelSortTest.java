package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.RateSortOrder;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/** Tests that the table's Rate column follows the sort order as it changes. */
public class PersonListPanelSortTest {

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
        FxTestUtil.runOnFxThread(AppFonts::load);
    }

    private static Lesson lesson(String rate) {
        return lesson(rate, 10);
    }

    private static Lesson lesson(String rate, int hour) {
        return new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0)),
                new HourlyRate(new BigDecimal(rate)));
    }

    /** Returns the rates shown in the table, top to bottom. */
    private static List<String> shownRates(PersonListPanel panel) {
        ListView<?> listView = (ListView<?>) panel.getRoot().lookup("#personListView");
        listView.applyCss();
        listView.layout();
        return listView.lookupAll(".list-cell").stream().filter(ListCell.class::isInstance)
                .map(node -> (ListCell<?>) node).filter(cell -> !cell.isEmpty())
                .sorted(Comparator.comparingInt(ListCell::getIndex))
                .map(cell -> ((Text) cell.lookup("#rate")).getText()).toList();
    }

    @Test
    public void rateColumn_sortOrderChanges_showsRateOfCurrentOrder() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addPerson(new PersonBuilder().withName("Rizwan").withPhone("91111111")
                    .withSchoolingLevel(new SchoolingLevel("Sec 4")).withLessons(lesson("60"), lesson("52")).build());
            book.addPerson(new PersonBuilder().withName("Chong").withPhone("92222222")
                    .withSchoolingLevel(new SchoolingLevel("Sec 5")).withLessons(lesson("53")).build());
            book.addPerson(new PersonBuilder().withName("Wong").withPhone("93333333")
                    .withSchoolingLevel(new SchoolingLevel("Sec 2")).withLessons(lesson("44")).build());
            Model model = new ModelManager(book, new UserPrefs());
            PersonListPanel panel = new PersonListPanel(model.getFilteredActivePersonList(),
                    model.getAddressBook().getPersonList(), false, model.getRateSortOrder());
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 900, 600));
            stage.show();

            new SortCommand(RateSortOrder.ASCENDING).execute(model);
            assertEquals(List.of("$44/h", "$52/h", "$53/h"), shownRates(panel));
            new SortCommand(RateSortOrder.DESCENDING).execute(model);
            assertEquals(List.of("$60/h", "$53/h", "$44/h"), shownRates(panel));
            stage.hide();
        });
    }

    @Test
    public void refreshTime_activeAndArchivedRows_updatesSummaryAndKeepsOpenStudent() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            for (boolean archived : List.of(false, true)) {
                AddressBook book = new AddressBook();
                book.addPerson(new PersonBuilder().withLessons(lesson("40", 10), lesson("80", 12))
                        .build().withArchived(archived));
                Model model = new ModelManager(book, new UserPrefs());
                PersonListPanel panel = new PersonListPanel(archived ? model.getFilteredArchivedPersonList()
                        : model.getFilteredActivePersonList(), archived ? book.getArchivedPersonList()
                        : book.getPersonList(), archived, model.getRateSortOrder());
                Stage stage = new Stage();
                stage.setOpacity(0);
                stage.setScene(new Scene(panel.getRoot(), 1000, 600));
                try {
                    stage.show();
                    assertEquals(1, shownRates(panel).size());
                    Student open = panel.getOpenStudent();
                    Node details = panel.getRoot().lookup("#details");
                    panel.refreshTime(LocalDateTime.of(2026, 11, 2, 9, 0));
                    assertEquals(List.of("$40/h"), shownRates(panel));
                    panel.refreshTime(LocalDateTime.of(2026, 11, 2, 11, 30));
                    assertEquals("Mon 12.00pm", ((Text) panel.getRoot().lookup("#nextTime")).getText());
                    assertEquals(List.of("$80/h"), shownRates(panel));
                    assertSame(open, panel.getOpenStudent());
                    assertSame(details, panel.getRoot().lookup("#details"));
                } finally {
                    stage.close();
                }
            }
        });
    }

    @Test
    public void panel_withoutSortOrder_showsPlainRateHeading() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addPerson(new PersonBuilder().withLessons(lesson("60")).build());
            Model model = new ModelManager(book, new UserPrefs());
            PersonListPanel panel = new PersonListPanel(model.getFilteredActivePersonList(),
                    model.getAddressBook().getPersonList(), false);
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 900, 600));
            stage.show();
            assertEquals(List.of("$60/h"), shownRates(panel));
            stage.hide();
        });
    }

    @Test
    public void cell_sortOrderChangesWhileEmpty_keepsCellEmpty() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addPerson(new PersonBuilder().withLessons(lesson("60")).build());
            Model model = new ModelManager(book, new UserPrefs());
            SimpleObjectProperty<RateSortOrder> sortOrder = new SimpleObjectProperty<>(RateSortOrder.NONE);
            PersonListPanel panel = new PersonListPanel(model.getFilteredActivePersonList(),
                    model.getAddressBook().getPersonList(), false, sortOrder);
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
            cell.updateItem(null, true);

            sortOrder.set(RateSortOrder.DESCENDING);
            assertNull(cell.getGraphic());
        });
    }

    @Test
    public void cell_redrawnWithSameStudentAndOrder_keepsCard() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addPerson(new PersonBuilder().withLessons(lesson("60")).build());
            Model model = new ModelManager(book, new UserPrefs());
            PersonListPanel panel = new PersonListPanel(model.getFilteredActivePersonList(),
                    model.getAddressBook().getPersonList(), false, model.getRateSortOrder());
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 900, 600));
            stage.show();
            ListView<?> listView = (ListView<?>) panel.getRoot().lookup("#personListView");
            listView.applyCss();
            listView.layout();
            PersonListPanel.PersonListViewCell cell = (PersonListPanel.PersonListViewCell) listView
                    .lookupAll(".list-cell").stream().filter(PersonListPanel.PersonListViewCell.class::isInstance)
                    .filter(node -> !((ListCell<?>) node).isEmpty()).findFirst().orElseThrow();
            Node graphic = cell.getGraphic();

            cell.updateItem(cell.getItem(), false);
            assertSame(graphic, cell.getGraphic());
            stage.hide();
        });
    }

    @Test
    public void panel_listEmptiedThenSorted_redrawsWithoutError() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            Student student = new PersonBuilder().withLessons(lesson("60")).build();
            book.addPerson(student);
            Model model = new ModelManager(book, new UserPrefs());
            PersonListPanel panel = new PersonListPanel(model.getFilteredActivePersonList(),
                    model.getAddressBook().getPersonList(), false, model.getRateSortOrder());
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 900, 600));
            stage.show();
            assertEquals(List.of("$60/h"), shownRates(panel));

            model.deletePerson(student);
            new SortCommand(RateSortOrder.ASCENDING).execute(model);
            assertEquals(List.of(), shownRates(panel));
            model.addPerson(student);
            new SortCommand(RateSortOrder.ASCENDING).execute(model);
            new ListCommand().execute(model);
            assertEquals(List.of("$60/h"), shownRates(panel));
            stage.hide();
        });
    }
}
