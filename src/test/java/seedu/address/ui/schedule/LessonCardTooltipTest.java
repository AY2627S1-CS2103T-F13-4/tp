package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
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
 * Checks that every lesson block carries its full details in a tooltip, which matters most when the block
 * is too short to show them. It reads the installed tooltip rather than hovering, so it needs no mouse timing.
 */
public class LessonCardTooltipTest {

    private ScheduleGrid schedule;

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @AfterEach
    public void disposeSchedule() throws Exception {
        if (schedule != null) {
            FxTestUtil.runOnFxThread(schedule::dispose);
        }
    }

    @Test
    public void tooltip_standardLesson_hasNumberNameAndFullDetails() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Region card = showLesson(LocalTime.of(10, 0), Optional.empty(), Optional.empty());

            Tooltip tooltip = installedTooltip(card);
            assertEquals("#1.1 Alice Tan\nMath, Monday 9.00am-10.00am, SGD 45.50/hour", tooltip.getText());
            assertTrue(tooltip.isWrapText());
        });
    }

    @Test
    public void tooltip_clippedOneMinuteLesson_hasDetailsMissingFromCard() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Region card = showLesson(LocalTime.of(9, 1), Optional.of(new LessonLocation("Clementi Library\nRoom 2")),
                    Optional.of(LessonMode.PHYSICAL));
            assertEquals(1, card.getHeight(), 0.001);
            assertEquals(1, card.getClip().getBoundsInLocal().getHeight(), 0.001);
            Label name = (Label) card.getChildrenUnmodifiable().get(0);
            assertTrue(name.getBoundsInParent().getMaxY() > card.getHeight());

            assertEquals("#1.1 Alice Tan\nMath, Monday 9.00am-9.01am, SGD 45.50/hour, "
                    + "Physical, at Clementi Library\nRoom 2", installedTooltip(card).getText());
        });
    }

    private Region showLesson(LocalTime end, Optional<LessonLocation> location, Optional<LessonMode> mode) {
        Lesson lesson = new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), end),
                new HourlyRate(new BigDecimal("45.50")), location, mode);
        Student student = new PersonBuilder().withName("Alice Tan").withLessons(lesson).build();
        schedule = new ScheduleGrid(FXCollections.observableArrayList(student));
        ScrollPane viewport = new ScrollPane(schedule.getRoot());
        viewport.setFitToWidth(true);
        Scene scene = new Scene(viewport, 900, 250);
        scene.getStylesheets().add(getClass().getResource("/view/LightTheme.css").toExternalForm());
        viewport.applyCss();
        viewport.resize(900, 250);
        viewport.layout();
        schedule.getRoot().layout();
        return schedule.getRoot().getChildren().stream()
                .filter(node -> node.getStyleClass().contains("lesson-card"))
                .map(Region.class::cast).findFirst().orElseThrow();
    }

    private static Tooltip installedTooltip(Region card) {
        return card.getProperties().values().stream().filter(Tooltip.class::isInstance).map(Tooltip.class::cast)
                .findFirst().orElseThrow();
    }
}
