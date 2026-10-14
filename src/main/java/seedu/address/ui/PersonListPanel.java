package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Student;

/**
 * Panel containing the table of students.
 */
public class PersonListPanel extends UiPart<Region> {
    /** Part of the visible height scrolled per page, leaving some overlap for context; also used by the week. */
    static final double PAGE_FRACTION = 0.85;

    private static final String FXML = "PersonListPanel.fxml";
    /** Below this list width the Subjects and Guardian columns are hidden; the details still show them. */
    private static final double WIDE_TABLE_WIDTH = 900;
    /**
     * Width of the list not available to the columns: the # column, row padding, gaps between the visible
     * columns and the scroll bar. Narrow tables have two columns, so two gaps, fewer.
     */
    private static final double CHROME_WIDE = 26 + 5 * 20 + 32 + 14;
    private static final double CHROME_NARROW = 26 + 3 * 20 + 32 + 14;
    /** Width of the list not available to a row's details: row padding, the details' indent and the scroll bar. */
    private static final double DETAILS_CHROME_WIDTH = 100;

    /** Widths of the table's columns, shared by the header and every row. */
    record Columns(DoubleExpression name, DoubleExpression subjects, DoubleExpression lesson,
            DoubleExpression rate, DoubleExpression guardian, DoubleExpression details, BooleanExpression wide) {}

    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Student> personListView;
    @FXML
    private Label listSummary;
    @FXML
    private Label nameHeading;
    @FXML
    private Label subjectsHeading;
    @FXML
    private Label lessonHeading;
    @FXML
    private Label rateHeading;
    @FXML
    private Label guardianHeading;

    private Runnable onAddStudent = () -> { };

    @FXML
    private Label emptyTitle;
    @FXML
    private Label emptyDetail;
    @FXML
    private Label emptyCommand;
    private final Columns columns;
    /** The student whose details were open just before the list last changed. */
    private Student openBeforeChange;

    /**
     * Creates a {@code PersonListPanel} that shows {@code personList}, the list that commands' indices refer to.
     * {@code allPersons} is the unfiltered list, used only to count students and choose the empty-state message.
     */
    public PersonListPanel(ObservableList<Student> personList, ObservableList<Student> allPersons) {
        super(FXML);
        ReadOnlyDoubleProperty listWidth = personListView.widthProperty();
        BooleanExpression wide = listWidth.greaterThanOrEqualTo(WIDE_TABLE_WIDTH);
        columns = new Columns(column(listWidth, 0.30, 0.50), column(listWidth, 0.22, 0),
                column(listWidth, 0.20, 0.34), column(listWidth, 0.08, 0.16), column(listWidth, 0.20, 0),
                listWidth.subtract(DETAILS_CHROME_WIDTH), wide);
        PersonCard.size(nameHeading, columns.name());
        PersonCard.size(subjectsHeading, columns.subjects());
        PersonCard.size(lessonHeading, columns.lesson());
        PersonCard.size(rateHeading, columns.rate());
        PersonCard.size(guardianHeading, columns.guardian());
        for (Label optional : new Label[] {subjectsHeading, guardianHeading}) {
            optional.visibleProperty().bind(wide);
            optional.managedProperty().bind(wide);
        }

        // Added before setItems, so it runs before the list view moves its selection for the same change.
        personList.addListener((ListChangeListener<Student>) change ->
                openBeforeChange = personListView.getSelectionModel().getSelectedItem());
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());

        emptyCommand.setStyle(MonospaceFont.style());

        personList.addListener(this::followOpenStudent);
        ListChangeListener<Student> refresh = change -> {
            updateStatus(personList.size(), allPersons.size());
            openOnlyRow();
        };
        personList.addListener(refresh);
        allPersons.addListener(refresh);
        // When the shown rows change (after list, find, add or delete), fade them in to mark it.
        personList.addListener((ListChangeListener<Student>) change -> Motion.fadeIn(personListView, 0.4));
        updateStatus(personList.size(), allPersons.size());
        openOnlyRow();
    }

    /**
     * Opens the details of the student after (1) or before (-1) the open one, or the first or last if none is open,
     * scrolling only if that row is out of view. Lets keyboard users browse details from the command box.
     */
    public void moveDetails(int direction) {
        int size = personListView.getItems().size();
        if (size == 0) {
            return;
        }
        int current = personListView.getSelectionModel().getSelectedIndex();
        openStudent(current < 0 ? (direction > 0 ? 0 : size - 1) : Math.clamp(current + direction, 0, size - 1));
    }

    /**
     * Opens the details of the student at {@code zeroBasedIndex} in the list, scrolling to it, for {@code view}.
     */
    public void openStudent(int zeroBasedIndex) {
        personListView.getSelectionModel().select(zeroBasedIndex);
        withFlow(flow -> flow.scrollTo(zeroBasedIndex));
    }

    /** Sets what the Add student button does. */
    public void setOnAddStudent(Runnable onAddStudent) {
        this.onAddStudent = onAddStudent;
    }

    @FXML
    private void handleAddStudent() {
        onAddStudent.run();
    }

    /** Closes any open student details. */
    public void closeDetails() {
        personListView.getSelectionModel().clearSelection();
    }

    /**
     * Scrolls the list by most of one screen: up for a negative {@code direction}, down otherwise.
     * Lets keyboard users page through the list while focus stays in the command box.
     */
    public void scrollPage(int direction) {
        withFlow(flow -> flow.scrollPixels(Math.signum(direction) * flow.getHeight() * PAGE_FRACTION));
    }

    /** Runs {@code action} on the list's scrolling area, which exists once the list has been shown. */
    private void withFlow(Consumer<VirtualFlow<?>> action) {
        VirtualFlow<?> flow = (VirtualFlow<?>) personListView.lookup(".virtual-flow");
        if (flow != null) {
            action.accept(flow);
        }
    }

    /**
     * Keeps the open details on the same student when the list changes. The list view keeps the selected row
     * number, so without this, deleting the open student would open whoever takes their row.
     * An edited student is a new object, so if the open one is gone, the details follow the one student the change
     * added in its place; a delete adds no one, so the details close.
     */
    private void followOpenStudent(ListChangeListener.Change<? extends Student> change) {
        Student open = openBeforeChange;
        if (open == null || personListView.getItems().stream().anyMatch(student -> student == open)) {
            return;
        }
        List<Student> added = new ArrayList<>();
        while (change.next()) {
            added.addAll(change.getAddedSubList());
        }
        if (added.size() == 1) {
            openStudent(personListView.getItems().indexOf(added.get(0)));
        } else {
            closeDetails();
        }
    }

    /** Opens the only row when a search leaves one student, and closes details whose row has gone. */
    private void openOnlyRow() {
        int size = personListView.getItems().size();
        if (size == 1) {
            personListView.getSelectionModel().select(0);
        } else if (personListView.getSelectionModel().getSelectedIndex() >= size) {
            personListView.getSelectionModel().clearSelection();
        }
    }

    /** Returns a column's width: its share of the list's free width, which depends on whether the table is wide. */
    private static DoubleExpression column(ReadOnlyDoubleProperty listWidth, double wideShare, double narrowShare) {
        return Bindings.createDoubleBinding(() -> {
            boolean wide = listWidth.get() >= WIDE_TABLE_WIDTH;
            double free = Math.max(0, listWidth.get() - (wide ? CHROME_WIDE : CHROME_NARROW));
            return free * (wide ? wideShare : narrowShare);
        }, listWidth);
    }

    private void updateStatus(int shown, int total) {
        listSummary.setText(StudentListStatus.summary(shown, total));
        emptyTitle.setText(StudentListStatus.emptyTitle(total));
        emptyDetail.setText(StudentListStatus.emptyDetail(total));
        emptyCommand.setText(StudentListStatus.emptyCommand(total));
    }

    /**
     * Custom {@code ListCell} that displays a {@code Student} as a {@code PersonCard} row.
     * A selected row shows its details; selecting another only toggles those two rows.
     */
    class PersonListViewCell extends ListCell<Student> {
        private PersonCard card;
        private Student shownPerson;
        private int shownIndex = -1;

        PersonListViewCell() {
            // Clicking the open row closes it, the mouse version of Esc.
            addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!isEmpty() && isSelected()) {
                    getListView().getSelectionModel().clearSelection();
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(Student person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                card = null;
                shownPerson = null;
                shownIndex = -1;
                setGraphic(null);
                setText(null);
            } else if (person != shownPerson || getIndex() != shownIndex) {
                // The list also redraws a row that has not changed, e.g. right after it opens.
                // Keeping its card then lets an open or close animation finish.
                card = new PersonCard(person, getIndex() + 1);
                card.bindColumns(columns);
                // Read the selection for this index: the cell's own flag may still be for the row it showed before.
                card.setOpen(getListView().getSelectionModel().isSelected(getIndex()));
                shownPerson = person;
                shownIndex = getIndex();
                setGraphic(card.getRoot());
            }
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            if (card != null) {
                // A row redrawn while scrolling already shows its state, so only a real change animates.
                card.animateOpen(selected);
            }
        }
    }

}
