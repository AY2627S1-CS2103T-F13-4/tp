package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises installed tooltips through mouse events and their displayed popup windows.
 */
public class LessonCardTooltipTest {
    private static final int TIMEOUT_SECONDS = 10;

    private Stage stage;
    private ScheduleGrid schedule;
    private Region card;
    private ListChangeListener<Window> windowListener;
    private CompletableFuture<Tooltip> shownTooltip;

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @AfterEach
    public void closeWindows() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            if (windowListener != null) {
                Window.getWindows().removeListener(windowListener);
            }
            if (card != null) {
                // Cancel tooltip timers as well as any popup left by a failed assertion.
                fireMouseEvent(card, MouseEvent.MOUSE_PRESSED);
            }
            if (stage != null) {
                stage.close();
            }
            if (schedule != null) {
                schedule.dispose();
            }
        });
    }

    @Test
    public void hover_standardLesson_showsFullDetailsAndHidesOnExit() throws Exception {
        FxTestUtil.runOnFxThread(() -> showLesson(LocalTime.of(10, 0), Optional.empty(), Optional.empty()));

        Tooltip tooltip = hoverCard();
        assertDisplayedDetails(tooltip, "Alice Tan\nMath, Monday 9.00am-10.00am, SGD 45.50/hour");
        leaveCardAndAssertHidden(tooltip);
    }

    @Test
    public void hover_clippedOneMinuteLesson_showsDetailsMissingFromCard() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            showLesson(LocalTime.of(9, 1), Optional.of(new LessonLocation("Clementi Library\nRoom 2")),
                    Optional.of(LessonMode.PHYSICAL));
            assertEquals(2, card.getHeight(), 0.001);
            assertEquals(2, card.getClip().getBoundsInLocal().getHeight(), 0.001);
            Label name = assertInstanceOf(Label.class, card.getChildrenUnmodifiable().get(0));
            assertTrue(name.getBoundsInParent().getMaxY() > card.getHeight());
        });

        Tooltip tooltip = hoverCard();
        assertDisplayedDetails(tooltip, "Alice Tan\nMath, Monday 9.00am-9.01am, SGD 45.50/hour, "
                + "Physical, at Clementi Library\nRoom 2");
        leaveCardAndAssertHidden(tooltip);
    }

    private void showLesson(LocalTime end, Optional<LessonLocation> location, Optional<LessonMode> mode) {
        Lesson lesson = new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), end),
                new HourlyRate(new BigDecimal("45.50")), location, mode);
        Student student = new PersonBuilder().withName("Alice Tan").withLessons(lesson).build();
        schedule = new ScheduleGrid(FXCollections.observableArrayList(student));
        ScrollPane viewport = new ScrollPane(schedule.getRoot());
        viewport.setFitToWidth(true);
        viewport.setFitToHeight(true);
        Scene scene = new Scene(viewport, 900, 250);
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        stage = new Stage();
        stage.setTitle("LessonCard tooltip test");
        stage.setScene(scene);
        stage.show();
        viewport.applyCss();
        viewport.layout();
        schedule.getRoot().layout();
        card = schedule.getRoot().getChildren().stream()
                .filter(node -> node.getStyleClass().contains("lesson-card"))
                .map(Region.class::cast).findFirst().orElseThrow();

        shownTooltip = new CompletableFuture<>();
        windowListener = change -> {
            while (change.next()) {
                for (Window window : change.getAddedSubList()) {
                    if (window instanceof Tooltip tooltip && tooltip.getOwnerWindow() == stage) {
                        // JavaFX also briefly opens an invisible popup while applying tooltip CSS.
                        Platform.runLater(() -> {
                            if (tooltip.isShowing() && tooltip.getOpacity() > 0) {
                                shownTooltip.complete(tooltip);
                            }
                        });
                    }
                }
            }
        };
        Window.getWindows().addListener(windowListener);
    }

    private Tooltip hoverCard() throws Exception {
        FxTestUtil.runOnFxThread(() -> fireMouseEvent(card, MouseEvent.MOUSE_MOVED));
        return shownTooltip.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void assertDisplayedDetails(Tooltip tooltip, String expected) throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            assertTrue(tooltip.isShowing());
            assertTrue(tooltip.getOpacity() > 0);
            assertEquals(stage, tooltip.getOwnerWindow());
            assertEquals(expected, tooltip.getText());
            assertTrue(tooltip.isWrapText());
            Label displayed = assertInstanceOf(Label.class, tooltip.getSkin().getNode());
            assertEquals(expected, displayed.getText());
            assertTrue(displayed.isVisible());
            assertTrue(displayed.getWidth() > 0);
            assertTrue(displayed.getHeight() > 0);
        });
    }

    private void leaveCardAndAssertHidden(Tooltip tooltip) throws Exception {
        CompletableFuture<Void> hidden = new CompletableFuture<>();
        FxTestUtil.runOnFxThread(() -> {
            tooltip.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> hidden.complete(null));
            fireMouseEvent(card, MouseEvent.MOUSE_EXITED);
        });
        hidden.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        FxTestUtil.runOnFxThread(() -> assertFalse(tooltip.isShowing()));
    }

    private static void fireMouseEvent(Node target, EventType<MouseEvent> type) {
        Point2D screen = target.localToScreen(1, 1);
        Event.fireEvent(target, new MouseEvent(type, 1, 1, screen.getX(), screen.getY(), MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false,
                new PickResult(target, 1, 1)));
    }
}
