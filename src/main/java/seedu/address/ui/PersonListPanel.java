package seedu.address.ui;

import java.util.logging.Logger;

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
    /** Width taken by the card's index column, padding and the list's scroll bar, beside the wrapping rows. */
    private static final double CARD_CHROME_WIDTH = 100;
    /** Part of the visible height scrolled per page, leaving some overlap for context. */
    private static final double PAGE_FRACTION = 0.85;

    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Student> personListView;
    @FXML
    private Label listSummary;

    private final Label emptyTitle = new Label();
    private final Label emptyDetail = new Label();

    /**
     * Creates a {@code PersonListPanel} that shows {@code personList}, the list that commands' indices refer to.
     * {@code allPersons} is the unfiltered list, used only to count students and choose the empty-state message.
     */
    public PersonListPanel(ObservableList<Student> personList, ObservableList<Student> allPersons) {
        super(FXML);
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
                card.bindWrapWidth(personListView.widthProperty().subtract(CARD_CHROME_WIDTH));
                setGraphic(card.getRoot());
            }
        }
    }

}
