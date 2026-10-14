package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests lesson-card content and sizing through its public scene graph.
 */
public class LessonCardTest {

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void constructor_nullEntry_throwsNullPointerException() throws Exception {
        FxTestUtil.runOnFxThread(() -> assertThrows(NullPointerException.class, () -> new LessonCard(null)));
    }

    @Test
    public void constructor_standardLesson_showsStudentExactTimesAndCommandNumber() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Lesson lesson = new Lesson(new Subject("Math"),
                    new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 7), LocalTime.of(9, 22)),
                    new HourlyRate(new BigDecimal("45.50")));
            Region root = prepareCard(createCard("Alice Tan", lesson));
            resizeAndLayout(root, 250, 140);

            List<Label> labels = getLabels(root);
            assertEquals(List.of("Alice Tan", "9.07am-9.22am", "#1.1 \u00b7 Math"),
                    labels.stream().map(Label::getText).toList());
            for (Label label : labels) {
                assertTrue(label.isVisible());
                assertTrue(label.getWidth() > 0);
                assertTrue(label.getHeight() >= label.prefHeight(-1));
            }
            assertEquals("#1.1 Alice Tan\nMath, Monday 9.07am-9.22am, SGD 45.50/hour", root.getAccessibleText());
        });
    }

    @Test
    public void constructor_optionalDetails_includesRateModeAndLocationInAccessibleText() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Lesson physical = createLesson("Math", Optional.of(new LessonLocation("Clementi Library")),
                    Optional.of(LessonMode.PHYSICAL));
            Lesson virtual = createLesson("Math", Optional.empty(), Optional.of(LessonMode.VIRTUAL));
            Lesson locationOnly = createLesson("Math", Optional.of(new LessonLocation("Room 2")), Optional.empty());

            assertEquals("#1.1 Alice Tan\nMath, Monday 9.00am-10.00am, SGD 45.50/hour, Physical, at Clementi Library",
                    createCard("Alice Tan", physical).getRoot().getAccessibleText());
            assertEquals("#1.1 Alice Tan\nMath, Monday 9.00am-10.00am, SGD 45.50/hour, Virtual",
                    createCard("Alice Tan", virtual).getRoot().getAccessibleText());
            assertEquals("#1.1 Alice Tan\nMath, Monday 9.00am-10.00am, SGD 45.50/hour, at Room 2",
                    createCard("Alice Tan", locationOnly).getRoot().getAccessibleText());
        });
    }

    @Test
    public void constructor_longContent_preservesFullTextAfterShrinkingAndGrowing() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            String name = "Alexandra ".repeat(60) + "Tan";
            String subject = "Advanced Mathematics and Applied Physics";
            Lesson lesson = createLesson("  Advanced\t Mathematics and  Applied Physics  ",
                    Optional.empty(), Optional.empty());
            Region root = prepareCard(createCard(name, lesson));
            List<String> expectedLabels = List.of(name, "9.00am-10.00am", "#1.1 \u00b7 " + subject);
            String expectedDetails = "#1.1 " + name + "\n" + subject + ", Monday 9.00am-10.00am, SGD 45.50/hour";

            resizeAndLayout(root, 25, 2);
            assertEquals(expectedLabels, getLabels(root).stream().map(Label::getText).toList());
            assertEquals(expectedDetails, root.getAccessibleText());

            resizeAndLayout(root, 500, 140);
            assertEquals(expectedLabels, getLabels(root).stream().map(Label::getText).toList());
            assertEquals(expectedDetails, root.getAccessibleText());
        });
    }

    @Test
    public void constructor_multilineLocation_preservesLineBreaksInAccessibleDetails() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Lesson lesson = createLesson("Math", Optional.of(new LessonLocation("Clementi Library\nRoom 2")),
                    Optional.of(LessonMode.PHYSICAL));
            Region root = prepareCard(createCard("Alice Tan", lesson));
            resizeAndLayout(root, 100, 2);

            assertEquals(List.of("Alice Tan", "9.00am-10.00am", "#1.1 \u00b7 Math"),
                    getLabels(root).stream().map(Label::getText).toList());
            assertEquals("#1.1 Alice Tan\nMath, Monday 9.00am-10.00am, SGD 45.50/hour, Physical, "
                    + "at Clementi Library\nRoom 2", root.getAccessibleText());
        });
    }

    @Test
    public void resize_smallAndZeroSizes_keepsClipWithinAllocatedSpace() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Lesson lesson = createLesson("Math", Optional.empty(), Optional.empty());
            Region root = prepareCard(createCard("Alice Tan", lesson));
            Rectangle clip = assertInstanceOf(Rectangle.class, root.getClip());
            double[][] sizes = {{180, 100}, {80, 2}, {1, 1}, {0, 0}, {0, 15}, {20, 0}, {220, 140}};

            for (double[] size : sizes) {
                double width = size[0];
                double height = size[1];
                resizeAndLayout(root, width, height);

                assertEquals(width, root.getWidth(), 0.001);
                assertEquals(height, root.getHeight(), 0.001);
                assertEquals(width, clip.getWidth(), 0.001);
                assertEquals(height, clip.getHeight(), 0.001);
                assertTrue(root.getBoundsInLocal().getWidth() <= width + 0.001);
                assertTrue(root.getBoundsInLocal().getHeight() <= height + 0.001);
                assertEquals(List.of("Alice Tan", "9.00am-10.00am", "#1.1 \u00b7 Math"),
                        getLabels(root).stream().map(Label::getText).toList());
            }
        });
    }

    @Test
    public void constructor_sameLessonForDifferentStudents_createsIndependentCards() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Lesson lesson = createLesson("Math", Optional.empty(), Optional.empty());
            Region alice = prepareCard(createCard("Alice Tan", lesson));
            Region bob = prepareCard(createCard("Bob Lim", lesson));
            resizeAndLayout(alice, 100, 2);
            resizeAndLayout(bob, 240, 140);

            assertNotSame(alice, bob);
            assertNotSame(alice.getClip(), bob.getClip());
            assertNotSame(getLabels(alice).get(0), getLabels(bob).get(0));
            assertEquals(List.of("Alice Tan", "9.00am-10.00am", "#1.1 \u00b7 Math"),
                    getLabels(alice).stream().map(Label::getText).toList());
            assertEquals(List.of("Bob Lim", "9.00am-10.00am", "#1.1 \u00b7 Math"),
                    getLabels(bob).stream().map(Label::getText).toList());
            assertEquals("#1.1 Alice Tan\nMath, Monday 9.00am-10.00am, SGD 45.50/hour", alice.getAccessibleText());
            assertEquals("#1.1 Bob Lim\nMath, Monday 9.00am-10.00am, SGD 45.50/hour", bob.getAccessibleText());

            Rectangle bobClip = assertInstanceOf(Rectangle.class, bob.getClip());
            resizeAndLayout(alice, 0, 0);
            assertEquals(240, bob.getWidth(), 0.001);
            assertEquals(140, bob.getHeight(), 0.001);
            assertEquals(240, bobClip.getWidth(), 0.001);
            assertEquals(140, bobClip.getHeight(), 0.001);
        });
    }

    // Blocks use the same school-stage colours as the student table; any other level, or none, stays neutral.
    @Test
    public void constructor_schoolingLevel_coloursCardByStage() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Lesson lesson = createLesson("Math", Optional.empty(), Optional.empty());
            assertTrue(cardForLevel("P5", lesson).getRoot().getStyleClass().contains("level-primary"));
            assertTrue(cardForLevel("Sec 3", lesson).getRoot().getStyleClass().contains("level-secondary"));
            assertTrue(cardForLevel("JC1", lesson).getRoot().getStyleClass().contains("level-jc"));
            assertTrue(cardForLevel("Poly Y1", lesson).getRoot().getStyleClass().contains("level-other"));
            assertTrue(createCard("Alice Tan", lesson).getRoot().getStyleClass().contains("level-other"));
            assertEquals("#1.1 \u00b7 Sec 3 \u00b7 Math",
                    getLabels(cardForLevel("Sec 3", lesson).getRoot()).get(2).getText());
        });
    }

    private static LessonCard cardForLevel(String level, Lesson lesson) {
        Student student = new PersonBuilder().withName("Alice Tan").withSchoolingLevel(new SchoolingLevel(level))
                .withLessons(lesson).build();
        return new LessonCard(new ScheduleEntry(student, lesson, 1, 1));
    }

    private static LessonCard createCard(String name, Lesson lesson) {
        Student student = new PersonBuilder().withName(name).withLessons(lesson).build();
        return new LessonCard(new ScheduleEntry(student, lesson, 1, 1));
    }

    private static Lesson createLesson(String subject, Optional<LessonLocation> location, Optional<LessonMode> mode) {
        return new Lesson(new Subject(subject),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new HourlyRate(new BigDecimal("45.50")), location, mode);
    }

    private static Region prepareCard(LessonCard card) {
        Region root = card.getRoot();
        root.setManaged(false);
        Scene scene = new Scene(new Group(root));
        scene.getStylesheets().add(LessonCardTest.class.getResource("/view/LightTheme.css").toExternalForm());
        root.applyCss();
        return root;
    }

    private static void resizeAndLayout(Region root, double width, double height) {
        root.resize(width, height);
        root.layout();
    }

    private static List<Label> getLabels(Region root) {
        return root.getChildrenUnmodifiable().stream().filter(Label.class::isInstance).map(Label.class::cast).toList();
    }
}
