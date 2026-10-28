package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.RateSortOrder;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests student-card content and sizing through its public scene graph.
 */
public class PersonCardTest {

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
        FxTestUtil.runOnFxThread(AppFonts::load);
    }

    @Test
    public void open_maximumLengthSchool_wrapsWithinAvailableWidthAfterResize() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            String schoolName = "W".repeat(60);
            Student student = new PersonBuilder().withSchool(new School(schoolName)).build();
            DoubleProperty availableWidth = new SimpleDoubleProperty(240);
            PersonCard card = new PersonCard(student, 1);
            Region root = prepareCard(card, availableWidth);
            card.setOpen(true);
            root.applyCss();

            FlowPane details = assertInstanceOf(FlowPane.class, root.lookup("#details"));
            Label caption = details.lookupAll(".detail-caption").stream().filter(Label.class::isInstance)
                    .map(Label.class::cast).filter(label -> label.getText().equals("School")).findFirst().orElseThrow();
            Text school = caption.getParent().getChildrenUnmodifiable().stream().filter(Text.class::isInstance)
                    .map(Text.class::cast).findFirst().orElseThrow();
            Text unwrapped = new Text(schoolName);
            unwrapped.setFont(school.getFont());

            for (double width : new double[] {240, 160, 400}) {
                availableWidth.set(width);
                double padding = root.getInsets().getLeft() + root.getInsets().getRight()
                        + details.getInsets().getLeft() + details.getInsets().getRight();
                root.resize(width + padding, root.prefHeight(width + padding));
                root.layout();

                assertEquals(schoolName, school.getText());
                assertTrue(details.isVisible());
                assertTrue(details.isManaged());
                assertTrue(school.getWrappingWidth() > 0);
                assertTrue(school.getWrappingWidth() <= width);
                assertTrue(school.getLayoutBounds().getWidth() <= width + 0.001);
                assertTrue(school.getLayoutBounds().getHeight() > unwrapped.getLayoutBounds().getHeight());
            }
        });
    }

    @Test
    public void open_followUps_listedInOrderAndLongItemWraps() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            String longItem = "Send the term 4 progress report " + "W".repeat(150);
            Student student = new PersonBuilder().withFollowUps("Confirm the make-up lesson", longItem).build();
            DoubleProperty availableWidth = new SimpleDoubleProperty(320);
            PersonCard card = new PersonCard(student, 1);
            Region root = prepareCard(card, availableWidth);
            card.setOpen(true);
            root.applyCss();
            double padding = root.getInsets().getLeft() + root.getInsets().getRight();
            root.resize(320 + padding, root.prefHeight(320 + padding));
            root.layout();

            List<String> texts = sectionTexts(root, "Follow-ups");
            assertEquals(List.of("1.", "Confirm the make-up lesson", "2.", longItem), texts);
            Text wrapped = sectionNodes(root, "Follow-ups").stream().filter(Text.class::isInstance)
                    .map(Text.class::cast).filter(text -> text.getText().equals(longItem)).findFirst().orElseThrow();
            assertTrue(wrapped.getWrappingWidth() > 0);
            assertTrue(wrapped.getWrappingWidth() <= 320);
        });
    }

    @Test
    public void open_noFollowUps_showsPlaceholder() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            Region root = prepareCard(card, new SimpleDoubleProperty(320));
            card.setOpen(true);
            root.applyCss();

            assertEquals(List.of(StudentCardText.NO_FOLLOW_UPS), sectionTexts(root, "Follow-ups"));
        });
    }

    // A subject can be 40 characters; it must stay readable without running into the lesson's day and time.
    @Test
    public void open_longSubject_wrapsWithinItsColumn() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            String subjectName = "Additional Mathematics Paper 2 Revision";
            Lesson lesson = new Lesson(new Subject(subjectName),
                    new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)),
                    new HourlyRate(new BigDecimal("45")));
            PersonCard card = new PersonCard(new PersonBuilder().withLessons(lesson).build(), 1);
            Region root = prepareCard(card, new SimpleDoubleProperty(800));
            card.setOpen(true);
            layOut(root, 800);

            List<Text> cells = sectionNodes(root, "Lessons").stream().filter(Text.class::isInstance)
                    .map(Text.class::cast).toList();
            Text subject = cells.get(1);
            Text slot = cells.get(2);
            assertEquals(subjectName, subject.getText());
            assertTrue(subject.getBoundsInParent().getMaxX() <= slot.getBoundsInParent().getMinX(),
                    "The subject should end before the day and time start");
            assertEquals(slot.getBoundsInParent().getMinY(), subject.getBoundsInParent().getMinY(), 0.5,
                    "The day and time should stay level with the subject's first line");
        });
    }

    // In a narrow Name column, a long level must not squeeze a long name until it breaks inside words.
    @Test
    public void bindColumns_longNameAndLevel_nameWrapsBetweenWordsBesideLevel() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            String fullName = "Alexander Bartholomew Christopherson Davidovich Konstantinopoulos";
            Student student = new PersonBuilder().withName(fullName)
                    .withSchoolingLevel(new SchoolingLevel("Secondary 4 Express Stream One")).build();
            PersonCard card = new PersonCard(student, 1);
            Region root = prepareCard(card, new SimpleDoubleProperty(800), new SimpleDoubleProperty(250));
            layOut(root, 800);

            Region nameCell = assertInstanceOf(Region.class, root.lookup("#nameCell"));
            Text name = assertInstanceOf(Text.class, root.lookup("#name"));
            Text level = assertInstanceOf(Text.class, root.lookup("#schoolingLevel"));

            assertTrue(name.getWrappingWidth() >= longestWord(fullName, name), "Each name word should fit a line");
            assertTrue(level.getWrappingWidth() >= longestWord(level.getText(), level),
                    "Each level word should fit a line");
            assertTrue(name.getLayoutBounds().getWidth() > level.getLayoutBounds().getWidth(),
                    "The name should keep the larger share of the cell");
            assertTrue(name.getBoundsInParent().getMaxX() <= level.getBoundsInParent().getMinX());
            // Layout snaps the level to whole pixels.
            assertTrue(level.getBoundsInParent().getMaxX() <= nameCell.getWidth() + 1);
        });
    }

    // A long one-word name must not squeeze the level into letter fragments; the level keeps its longest word.
    @Test
    public void bindColumns_longOneWordName_levelKeepsWholeWords() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Student student = new PersonBuilder().withName("Wolfeschlegelsteinhausenb")
                    .withSchoolingLevel(new SchoolingLevel("Secondary 4")).build();
            PersonCard card = new PersonCard(student, 1);
            Region root = prepareCard(card, new SimpleDoubleProperty(800), new SimpleDoubleProperty(228));
            layOut(root, 800);

            Text level = assertInstanceOf(Text.class, root.lookup("#schoolingLevel"));
            assertTrue(level.getWrappingWidth() >= longestWord(level.getText(), level),
                    "Each level word should fit a line");
            assertNameAndLevelFit(root);
        });
    }

    // Even when the longest words cannot both fit, the name and level stay apart and inside their cell.
    @Test
    public void bindColumns_wordsWiderThanCell_nameAndLevelStayInCell() throws Exception {
        for (double column : new double[] {218, 300, 500}) {
            FxTestUtil.runOnFxThread(() -> {
                Student student = new PersonBuilder().withName("W".repeat(40))
                        .withSchoolingLevel(new SchoolingLevel("Secondary4ExpressStreamOneAbcd")).build();
                PersonCard card = new PersonCard(student, 1);
                Region root = prepareCard(card, new SimpleDoubleProperty(800), new SimpleDoubleProperty(column));
                layOut(root, 800);

                assertNameAndLevelFit(root);
            });
        }
    }

    // Short values must stay on one line, as wrapping text always fills its whole width.
    @Test
    public void bindColumns_shortNameAndLevel_neitherWraps() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Student student = new PersonBuilder().withName("Mei")
                    .withSchoolingLevel(new SchoolingLevel("Sec 3")).build();
            PersonCard card = new PersonCard(student, 1);
            Region root = prepareCard(card, new SimpleDoubleProperty(800), new SimpleDoubleProperty(230));
            layOut(root, 800);

            assertEquals(0, assertInstanceOf(Text.class, root.lookup("#name")).getWrappingWidth());
            assertEquals(0, assertInstanceOf(Text.class, root.lookup("#schoolingLevel")).getWrappingWidth());
        });
    }

    @Test
    public void rate_sortedByRate_showsTheRateTheSortRanksBy() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Student student = new PersonBuilder().withLessons(lesson("60"), lesson("52.5")).build();
            assertEquals("$60/h", rateText(new PersonCard(student, 1)));
            assertEquals("$60/h", rateText(new PersonCard(student, 1, RateSortOrder.NONE)));
            assertEquals("$52.50/h", rateText(new PersonCard(student, 1, RateSortOrder.ASCENDING)));
            assertEquals("$60/h", rateText(new PersonCard(student, 1, RateSortOrder.DESCENDING)));
        });
    }

    @Test
    public void setSortOrder_afterCreation_updatesRateInPlace() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Student student = new PersonBuilder().withLessons(lesson("60"), lesson("52")).build();
            PersonCard card = new PersonCard(student, 1, RateSortOrder.ASCENDING);
            prepareCard(card, new SimpleDoubleProperty(320));
            assertEquals("$52/h", assertInstanceOf(Text.class, card.getRoot().lookup("#rate")).getText());
            card.setSortOrder(RateSortOrder.DESCENDING);
            assertEquals("$60/h", assertInstanceOf(Text.class, card.getRoot().lookup("#rate")).getText());
            card.setSortOrder(RateSortOrder.NONE);
            assertEquals("$60/h", assertInstanceOf(Text.class, card.getRoot().lookup("#rate")).getText());
        });
    }

    @Test
    public void rate_sortedByRateWithoutLessons_showsPlaceholder() throws Exception {
        FxTestUtil.runOnFxThread(() -> assertEquals("\u2014",
                rateText(new PersonCard(new PersonBuilder().build(), 1, RateSortOrder.ASCENDING))));
    }

    private static Lesson lesson(String rate) {
        return new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)),
                new HourlyRate(new BigDecimal(rate)));
    }

    private static String rateText(PersonCard card) {
        Region root = prepareCard(card, new SimpleDoubleProperty(320));
        return assertInstanceOf(Text.class, root.lookup("#rate")).getText();
    }

    @Test
    public void setSortOrder_studentWithoutLessons_keepsDash() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withLessons().build(), 1, RateSortOrder.ASCENDING);
            Region root = prepareCard(card, new SimpleDoubleProperty(240));
            card.setSortOrder(RateSortOrder.DESCENDING);
            assertEquals("\u2014", assertInstanceOf(Text.class, root.lookup("#rate")).getText());
        });
    }

    @Test
    public void setSortOrder_severalLessons_showsLowestThenHighestThenNextLesson() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Student student = new PersonBuilder().withLessons(lesson("60"), lesson("52")).build();
            PersonCard card = new PersonCard(student, 1);
            Region root = prepareCard(card, new SimpleDoubleProperty(240));
            Text rate = assertInstanceOf(Text.class, root.lookup("#rate"));
            card.setSortOrder(RateSortOrder.ASCENDING);
            assertEquals("$52/h", rate.getText());
            card.setSortOrder(RateSortOrder.DESCENDING);
            assertEquals("$60/h", rate.getText());
            card.setSortOrder(RateSortOrder.NONE);
            assertEquals("$60/h", rate.getText());
        });
    }

    /** Returns the content of the details section with the given caption: the content node and its children. */
    private static List<Node> sectionNodes(Region root, String caption) {
        FlowPane details = assertInstanceOf(FlowPane.class, root.lookup("#details"));
        Label label = details.lookupAll(".detail-caption").stream().filter(Label.class::isInstance)
                .map(Label.class::cast).filter(found -> found.getText().equals(caption)).findFirst().orElseThrow();
        // A section holds its caption, then its content.
        Node content = label.getParent().getChildrenUnmodifiable().get(1);
        return content instanceof Parent parent ? parent.getChildrenUnmodifiable() : List.of(content);
    }

    /** Returns the text shown in the details section with the given caption, in layout order. */
    private static List<String> sectionTexts(Region root, String caption) {
        return sectionNodes(root, caption).stream().filter(Text.class::isInstance).map(Text.class::cast)
                .map(Text::getText).toList();
    }

    /** Checks that the level wraps, follows the name without overlapping it and ends inside the name cell. */
    private static void assertNameAndLevelFit(Region root) {
        Region nameCell = assertInstanceOf(Region.class, root.lookup("#nameCell"));
        Text name = assertInstanceOf(Text.class, root.lookup("#name"));
        Text level = assertInstanceOf(Text.class, root.lookup("#schoolingLevel"));
        assertTrue(level.getWrappingWidth() > 0, "The level should wrap rather than run on");
        assertTrue(name.getBoundsInParent().getMaxX() <= level.getBoundsInParent().getMinX());
        // Layout snaps the level to whole pixels.
        assertTrue(level.getBoundsInParent().getMaxX() <= nameCell.getWidth() + 1,
                "The level should end inside the name cell");
    }

    /** Returns the width of the widest word of {@code value} in the font of {@code shown}. */
    private static double longestWord(String value, Text shown) {
        Text word = new Text();
        word.setFont(shown.getFont());
        return Arrays.stream(value.split(" ")).mapToDouble(part -> {
            word.setText(part);
            return word.getLayoutBounds().getWidth();
        }).max().orElseThrow();
    }

    private static void layOut(Region root, double width) {
        root.applyCss();
        double padding = root.getInsets().getLeft() + root.getInsets().getRight();
        root.resize(width + padding, root.prefHeight(width + padding));
        root.layout();
    }

    private static Region prepareCard(PersonCard card, DoubleProperty detailsWidth) {
        return prepareCard(card, detailsWidth, new SimpleDoubleProperty(100));
    }

    private static Region prepareCard(PersonCard card, DoubleProperty detailsWidth, DoubleProperty nameWidth) {
        DoubleProperty columnWidth = new SimpleDoubleProperty(100);
        card.bindColumns(new PersonListPanel.Columns(nameWidth, columnWidth, columnWidth, columnWidth,
                columnWidth, detailsWidth, new SimpleBooleanProperty(false)));
        Region root = card.getRoot();
        root.setManaged(false);
        Scene scene = new Scene(new Group(root));
        scene.getStylesheets().add(PersonCardTest.class.getResource("/view/LightTheme.css").toExternalForm());
        root.applyCss();
        return root;
    }
}
