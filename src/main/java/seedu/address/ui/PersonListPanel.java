package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Student;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    /** Width of the list taken by the # column, row padding, the gaps between columns and the scroll bar. */
    private static final double ROW_CHROME_WIDTH = 140;
    /** Below this list width the Guardian column is folded into the Student column, so all columns fit. */
    private static final double WIDE_TABLE_WIDTH = 1100;
    /** Shares of the free width for the Lessons and Guardian columns; the Student column takes the rest. */
    private static final double LESSONS_SHARE_WIDE = 0.40;
    private static final double LESSONS_SHARE_NARROW = 0.58;
    private static final double GUARDIAN_SHARE_WIDE = 0.22;
    /** Part of the visible height scrolled per page, leaving some overlap for context. */
    private static final double PAGE_FRACTION = 0.85;

    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Student> personListView;
    @FXML
    private Label listSummary;
    @FXML
    private Label studentHeading;
    @FXML
    private Label lessonsHeading;
    @FXML
    private Label guardianHeading;

    private final BooleanBinding wide;
    private final DoubleBinding studentWidth;
    private final DoubleBinding lessonsWidth;
    private final DoubleBinding guardianWidth;

    private final Label emptyTitle = new Label();
    private final Label emptyDetail = new Label();

    /**
     * Creates a {@code PersonListPanel} that shows {@code personList}, the list that commands' indices refer to.
     * {@code allPersons} is the unfiltered list, used only to count students and choose the empty-state message.
     */
    public PersonListPanel(ObservableList<Student> personList, ObservableList<Student> allPersons) {
        super(FXML);
        ReadOnlyDoubleProperty listWidth = personListView.widthProperty();
        wide = listWidth.greaterThanOrEqualTo(WIDE_TABLE_WIDTH);
        // The Lessons column is never narrower than a lesson line, so its columns never overlap the next column.
        lessonsWidth = Bindings.createDoubleBinding(() -> Math.max(PersonCard.LESSON_GRID_WIDTH,
                free(listWidth) * (isWide(listWidth) ? LESSONS_SHARE_WIDE : LESSONS_SHARE_NARROW)), listWidth);
        guardianWidth = Bindings.createDoubleBinding(() ->
                isWide(listWidth) ? free(listWidth) * GUARDIAN_SHARE_WIDE : 0, listWidth);
        studentWidth = Bindings.createDoubleBinding(() ->
                Math.max(0, free(listWidth) - lessonsWidth.get() - guardianWidth.get()), listWidth);
        sizeHeading(studentHeading, studentWidth);
        sizeHeading(lessonsHeading, lessonsWidth);
        sizeHeading(guardianHeading, guardianWidth);
        guardianHeading.visibleProperty().bind(wide);
        guardianHeading.managedProperty().bind(wide);

        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());

        emptyTitle.getStyleClass().add("empty-title");
        emptyDetail.getStyleClass().add("empty-detail");
        emptyDetail.setWrapText(true);
        VBox emptyState = new VBox(emptyTitle, emptyDetail);
        emptyState.getStyleClass().add("empty-state");
        personListView.setPlaceholder(emptyState);

        ListChangeListener<Student> refresh = change -> updateStatus(personList.size(), allPersons.size());
        personList.addListener(refresh);
        allPersons.addListener(refresh);
        updateStatus(personList.size(), allPersons.size());
    }

    /**
     * Scrolls the list by most of one screen: up for a negative {@code direction}, down otherwise.
     * Lets keyboard users page through the list while focus stays in the command box.
     */
    public void scrollPage(int direction) {
        VirtualFlow<?> flow = (VirtualFlow<?>) personListView.lookup(".virtual-flow");
        if (flow != null) {
            flow.scrollPixels(Math.signum(direction) * flow.getHeight() * PAGE_FRACTION);
        }
    }

    /** Returns the list width left for the Student, Lessons and Guardian columns. */
    private static double free(ReadOnlyDoubleProperty listWidth) {
        return Math.max(0, listWidth.get() - ROW_CHROME_WIDTH);
    }

    private static boolean isWide(ReadOnlyDoubleProperty listWidth) {
        return listWidth.get() >= WIDE_TABLE_WIDTH;
    }

    private static void sizeHeading(Label heading, DoubleBinding width) {
        heading.minWidthProperty().bind(width);
        heading.prefWidthProperty().bind(width);
        heading.maxWidthProperty().bind(width);
    }

    private void updateStatus(int shown, int total) {
        listSummary.setText(StudentListStatus.summary(shown, total));
        emptyTitle.setText(StudentListStatus.emptyTitle(total));
        emptyDetail.setText(StudentListStatus.emptyDetail(total));
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Student} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                PersonCard card = new PersonCard(person, getIndex() + 1);
                card.bindColumns(studentWidth, lessonsWidth, guardianWidth, wide);
                setGraphic(card.getRoot());
            }
        }
    }

}
