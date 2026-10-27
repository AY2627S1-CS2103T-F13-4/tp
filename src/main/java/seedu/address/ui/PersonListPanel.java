package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.SchoolingLevel.Stage;
import seedu.address.model.person.Student;

/**
 * Panel containing compact student cards and a sidebar for the selected student's details.
 */
public class PersonListPanel extends UiPart<Region> {
    /** Part of the visible height scrolled per page, leaving some overlap for context; also used by the week. */
    static final double PAGE_FRACTION = 0.85;

    private static final String FXML = "PersonListPanel.fxml";
    /** Width reserved for the index, gap, row padding and scroll bar. */
    private static final double LIST_CHROME_WIDTH = 26 + 20 + 32 + 14;
    private static final double MAX_SIDEBAR_WIDTH = 480;
    private static final Map<Stage, String> STAGE_NAMES = Map.of(
            Stage.PRIMARY, "Primary", Stage.SECONDARY, "Secondary", Stage.JC, "JC", Stage.OTHER, "Other");

    @FXML
    private ListView<Student> personListView;
    @FXML
    private Label listTitle;
    @FXML
    private Label listSummary;
    @FXML
    private Label nameHeading;
    @FXML
    private HBox studentContent;
    @FXML
    private StackPane personDetailsPlaceholder;

    private Runnable onAddStudent = () -> { };

    @FXML
    private Label emptyTitle;
    @FXML
    private Label emptyDetail;
    @FXML
    private Label emptyCommand;
    private final DoubleExpression summaryWidth;
    private final PersonDetailsPanel detailsPanel;
    private final boolean archived;
    /** The student whose details were open just before the list last changed. */
    private Student openBeforeChange;

    /**
     * Creates a {@code PersonListPanel} that shows {@code personList}, the list that commands' indices refer to.
     * {@code allPersons} is the unfiltered list, used only to count students and choose the empty-state message.
     */
    public PersonListPanel(ObservableList<Student> personList, ObservableIntegerValue studentCount) {
        this(personList, studentCount, false);
    }

    /** Creates the same student table for either active or archived students. */
    public PersonListPanel(ObservableList<Student> personList, ObservableIntegerValue studentCount, boolean archived) {
        super(FXML);
        this.archived = archived;
        listTitle.setText(archived ? "Archived students" : "Students");
        summaryWidth = Bindings.max(0.0, personListView.widthProperty().subtract(LIST_CHROME_WIDTH));
        PersonCard.size(nameHeading, summaryWidth);
        detailsPanel = new PersonDetailsPanel();
        personDetailsPlaceholder.getChildren().add(detailsPanel.getRoot());
        PersonCard.size(personDetailsPlaceholder,
                Bindings.min(MAX_SIDEBAR_WIDTH, Bindings.max(0.0, studentContent.widthProperty()
                        .subtract(studentContent.spacingProperty()).multiply(0.48))));
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) ->
                detailsPanel.showStudent(selected));

        // Added before setItems, so it runs before the list view moves its selection for the same change.
        personList.addListener((ListChangeListener<Student>) change ->
                openBeforeChange = personListView.getSelectionModel().getSelectedItem());
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());

        emptyCommand.setStyle(MonospaceFont.style());

        personList.addListener(this::followOpenStudent);
        ListChangeListener<Student> refresh = change -> {
            updateStatus(personList.size(), studentCount.intValue());
            // Group headings count their students, so a change in one row can change another row's heading.
            // Rows whose student, number and heading are unchanged keep their card (see PersonListViewCell).
            personListView.refresh();
        };
        personList.addListener(refresh);
        // When the shown rows change (after list, find, add or delete), fade them in to mark it.
        personList.addListener((ListChangeListener<Student>) change -> Motion.fadeIn(personListView, 0.4));
        updateStatus(personList.size(), studentCount.intValue());
        if (personList.size() == 1) {
            openStudent(0);
        }
    }

    /**
     * Shows the details of the student after (1) or before (-1) the selected one, or the first or last if none is open,
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
     * Updates the sidebar to the student at {@code zeroBasedIndex}, scrolling to their card, for {@code view}.
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
     * Scrolls the selected student's details, or the list when nothing is selected, by most of one screen.
     * Lets keyboard users read long records while focus stays in the command box.
     */
    public void scrollPage(int direction) {
        if (personListView.getSelectionModel().getSelectedItem() != null) {
            detailsPanel.scrollPage(direction);
        } else {
            withFlow(flow -> flow.scrollPixels(Math.signum(direction) * flow.getHeight() * PAGE_FRACTION));
        }
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
        if (open == null) {
            if (personListView.getItems().size() == 1) {
                openStudent(0);
            }
            return;
        }
        int retainedIndex = personListView.getItems().indexOf(open);
        if (retainedIndex >= 0) {
            // Filtering can temporarily clear JavaFX's selection even when the same student remains visible.
            personListView.getSelectionModel().select(retainedIndex);
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

    private void updateStatus(int shown, int total) {
        listSummary.setText(StudentListStatus.summary(shown, total));
        emptyTitle.setText(StudentListStatus.emptyTitle(total, archived));
        emptyDetail.setText(StudentListStatus.emptyDetail(total, archived));
        emptyCommand.setText(StudentListStatus.emptyCommand(total, archived));
    }

    /** Returns a group heading such as "Primary 2": a dot in the stage's colour, its name and how many it has. */
    private static HBox groupHeading(Stage stage, long size) {
        Region dot = new Region();
        dot.getStyleClass().addAll("nav-dot", PersonCard.stageStyleClass(stage));
        Label name = new Label(STAGE_NAMES.get(stage));
        name.getStyleClass().add("group-name");
        Label count = new Label(String.valueOf(size));
        count.getStyleClass().add("group-count");
        HBox heading = new HBox(10, dot, name, count);
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getStyleClass().add("group-heading");
        return heading;
    }

    /**
     * Custom {@code ListCell} that displays a {@code Student} as a {@code PersonCard} row.
     * Selecting a row updates the sidebar without changing the card's height.
     */
    class PersonListViewCell extends ListCell<Student> {
        private PersonCard card;
        private Student shownPerson;
        private int shownIndex = -1;
        private String shownHeading = "";

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
                shownHeading = "";
                setGraphic(null);
                setText(null);
            } else {
                // The first student of each school stage gets a heading, e.g. "Primary 2". The model lists students
                // by stage, so each stage is one run of rows.
                List<Student> shown = getListView().getItems();
                Stage stage = person.getStage();
                boolean startsGroup = getIndex() == 0 || shown.get(getIndex() - 1).getStage() != stage;
                long groupSize = startsGroup
                        ? shown.stream().filter(student -> student.getStage() == stage).count() : 0;
                String heading = startsGroup ? STAGE_NAMES.get(stage) + " " + groupSize : "";
                if (person == shownPerson && getIndex() == shownIndex && heading.equals(shownHeading)) {
                    // The list also redraws a row that has not changed, e.g. right after it opens.
                    // Keep the existing compact card when only selection changes.
                    return;
                }
                card = new PersonCard(person, getIndex() + 1);
                card.bindWidth(summaryWidth);
                shownPerson = person;
                shownIndex = getIndex();
                shownHeading = heading;
                setGraphic(startsGroup ? new VBox(groupHeading(stage, groupSize), card.getRoot()) : card.getRoot());
            }
        }

    }

}
