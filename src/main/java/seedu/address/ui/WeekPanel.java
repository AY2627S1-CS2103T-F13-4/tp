package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Student;
import seedu.address.ui.schedule.ScheduleGrid;

/**
 * The "This week" screen: a page header with the number of lessons, and the weekly calendar of every
 * listed student's lessons. This is the screen the app opens on.
 */
public class WeekPanel extends UiPart<Region> {

    static final String EMPTY_DETAIL = "Add one with: lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT "
            + "rate/HOURLY_RATE";

    private static final String FXML = "WeekPanel.fxml";
    /** Share of the visible height that Page Up and Page Down scroll, as in the student table. */
    private static final double PAGE_FRACTION = 0.85;

    private final ObservableList<Student> students;
    private final ScheduleGrid scheduleGrid;
    private final ListChangeListener<Student> summaryUpdater = change -> updateSummary();

    @FXML
    private Label weekSummary;
    @FXML
    private ScrollPane scheduleScroll;
    @FXML
    private VBox emptyState;
    @FXML
    private Label emptyDetail;

    /**
     * Creates the screen for the lessons of {@code students}, the list that commands use.
     */
    public WeekPanel(ObservableList<Student> students) {
        super(FXML);
        this.students = requireNonNull(students);
        scheduleGrid = new ScheduleGrid(students);
        scheduleScroll.setContent(scheduleGrid.getRoot());
        // Keep the day headings in view while the hours scroll under them.
        scheduleScroll.vvalueProperty().addListener((unused, oldValue, newValue) -> pinHeadings());
        scheduleScroll.viewportBoundsProperty().addListener((unused, oldValue, newValue) -> pinHeadings());
        scheduleGrid.getRoot().heightProperty().addListener((unused, oldValue, newValue) -> pinHeadings());
        emptyDetail.setText(EMPTY_DETAIL);
        students.addListener(summaryUpdater);
        updateSummary();
    }

    private void updateSummary() {
        long lessons = students.stream().mapToLong(student -> student.getLessons().size()).sum();
        weekSummary.setText(lessons == 1 ? "1 lesson" : lessons + " lessons");
        boolean empty = lessons == 0;
        emptyState.setVisible(empty);
        scheduleScroll.setVisible(!empty);
    }

    private void pinHeadings() {
        double hidden = scheduleGrid.getRoot().getHeight() - scheduleScroll.getViewportBounds().getHeight();
        scheduleGrid.setHeadingOffset(Math.max(0, hidden) * scheduleScroll.getVvalue());
    }

    /**
     * Scrolls the calendar by most of one screen: up for a negative {@code direction}, down otherwise.
     */
    public void scrollPage(int direction) {
        double contentHeight = scheduleGrid.getRoot().getHeight();
        double viewportHeight = scheduleScroll.getViewportBounds().getHeight();
        if (contentHeight <= viewportHeight) {
            return;
        }
        double step = viewportHeight * PAGE_FRACTION / (contentHeight - viewportHeight);
        double next = scheduleScroll.getVvalue() + Math.signum(direction) * step;
        scheduleScroll.setVvalue(Math.max(0, Math.min(1, next)));
    }

    /** Stops following the student list. Call when the window closes. */
    public void dispose() {
        students.removeListener(summaryUpdater);
        scheduleGrid.dispose();
    }
}
