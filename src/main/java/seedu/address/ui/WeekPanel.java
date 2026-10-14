package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.OptionalDouble;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.Model;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.ui.schedule.ScheduleGrid;

/**
 * The "This week" screen: a page header with the number of lessons, and the weekly calendar of every
 * listed student's lessons. This is the screen the app opens on.
 */
public class WeekPanel extends UiPart<Region> {

    static final String EMPTY_DETAIL = "Add a lesson to a student, for example:";
    /** Typed as shown, it adds a lesson to the first student in the list. */
    static final String EMPTY_COMMAND = "lesson-add 1 subject/Math slot/Wed 1.00pm 5.00pm rate/45.50";

    private static final String FXML = "WeekPanel.fxml";
    /** How far down the visible calendar the current time is placed when it opens. */
    private static final double NOW_POSITION = 0.25;

    private final ObservableList<Student> students;
    private final ObservableValue<Predicate<Lesson>> lessonPredicate;
    private final ScheduleGrid scheduleGrid;
    private final ListChangeListener<Student> summaryUpdater = change -> updateSummary();
    private final ChangeListener<Predicate<Lesson>> filterUpdater = (unused, oldValue, newValue) -> updateSummary();

    @FXML
    private Label weekSummary;
    @FXML
    private ScrollPane scheduleScroll;
    @FXML
    private VBox emptyState;
    @FXML
    private Label emptyDetail;
    @FXML
    private Label emptyCommand;

    /**
     * Creates the screen for the lessons of {@code students}, the list that commands use.
     */
    public WeekPanel(ObservableList<Student> students) {
        this(students, new SimpleObjectProperty<>(Model.PREDICATE_SHOW_ALL_LESSONS));
    }

    /** Creates a week screen that follows both the shown students and the active lesson filter. */
    public WeekPanel(ObservableList<Student> students, ObservableValue<Predicate<Lesson>> lessonPredicate) {
        super(FXML);
        this.students = requireNonNull(students);
        this.lessonPredicate = requireNonNull(lessonPredicate);
        scheduleGrid = new ScheduleGrid(students, lessonPredicate);
        scheduleScroll.setContent(scheduleGrid.getRoot());
        // Keep the day headings in view while the hours scroll under them.
        scheduleScroll.vvalueProperty().addListener((unused, oldValue, newValue) -> pinHeadings());
        scheduleScroll.viewportBoundsProperty().addListener((unused, oldValue, newValue) -> pinHeadings());
        scheduleGrid.getRoot().heightProperty().addListener((unused, oldValue, newValue) -> pinHeadings());
        emptyDetail.setText(EMPTY_DETAIL);
        emptyCommand.setText(EMPTY_COMMAND);
        emptyCommand.setStyle(MonospaceFont.style());
        students.addListener(summaryUpdater);
        lessonPredicate.addListener(filterUpdater);
        updateSummary();
    }

    private void updateSummary() {
        long lessons = students.stream().flatMap(student -> student.getLessons().stream())
                .filter(lessonPredicate.getValue()).count();
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
        double step = viewportHeight * PersonListPanel.PAGE_FRACTION / (contentHeight - viewportHeight);
        double next = scheduleScroll.getVvalue() + Math.signum(direction) * step;
        scheduleScroll.setVvalue(Math.clamp(next, 0, 1));
    }

    /**
     * Scrolls so the current time is near the top, so opening the calendar shows what is on now without
     * scrolling. Does nothing when the current time is outside the calendar's hours.
     */
    public void scrollToCurrentTime() {
        // Wait for the layout, which places the current-time line.
        Platform.runLater(() -> {
            scheduleScroll.layout();
            double hidden = scheduleGrid.getRoot().getHeight() - scheduleScroll.getViewportBounds().getHeight();
            OptionalDouble offset = scheduleGrid.currentTimeOffset();
            if (hidden <= 0 || offset.isEmpty()) {
                return;
            }
            double target = offset.getAsDouble() - scheduleScroll.getViewportBounds().getHeight() * NOW_POSITION;
            scheduleScroll.setVvalue(Math.clamp(target / hidden, 0, 1));
        });
    }

    /** Stops following the student list. Call when the window closes. */
    public void dispose() {
        students.removeListener(summaryUpdater);
        lessonPredicate.removeListener(filterUpdater);
        scheduleGrid.dispose();
    }
}
