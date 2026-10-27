package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the compact student-card content and wrapping through its public scene graph.
 */
public class PersonCardTest {

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
        FxTestUtil.runOnFxThread(AppFonts::load);
    }

    @Test
    public void constructor_showsSummaryWithoutFullDetails() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Student student = new PersonBuilder().withName("Alice Pauline").withSchoolingLevel(new SchoolingLevel("P5"))
                    .withFollowUps("Contact guardian").withTags("Priority").build();
            PersonCard card = new PersonCard(student, 3);
            Region root = card.getRoot();

            assertEquals("3", label(root, "#id").getText());
            assertEquals("Alice Pauline", label(root, "#name").getText());
            assertEquals("P5", label(root, "#schoolingLevel").getText());
            assertEquals(StudentCardText.subjects(student).orElse("No lessons yet"),
                    label(root, "#subjects").getText());
            assertNull(root.lookup("#details"));
            assertTrue(root.lookupAll(".detail-caption").isEmpty());
            assertNull(root.lookup("#guardianName"));
            assertNull(root.lookup("#rate"));
        });
    }

    @Test
    public void constructor_noLevel_hidesLevel() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            Label level = label(card.getRoot(), "#schoolingLevel");
            assertFalse(level.isVisible());
            assertFalse(level.isManaged());
        });
    }

    @Test
    public void resize_longName_wrapsWithinSummaryWidth() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            String longName = "W".repeat(60);
            PersonCard card = new PersonCard(new PersonBuilder().withName(longName)
                    .withSchoolingLevel(new SchoolingLevel("W".repeat(30))).build(), 1);
            DoubleProperty availableWidth = new SimpleDoubleProperty(240);
            card.bindWidth(availableWidth);
            Region root = card.getRoot();
            root.setManaged(false);
            Scene scene = new Scene(new Group(root));
            scene.getStylesheets().add(PersonCardTest.class.getResource("/view/LightTheme.css").toExternalForm());
            root.applyCss();
            Label name = label(root, "#name");

            for (double width : new double[] {240, 160, 400}) {
                availableWidth.set(width);
                root.resize(width + 78, root.prefHeight(width + 78));
                root.layout();
                Region summary = assertInstanceOf(Region.class, root.lookup("#summary"));
                assertEquals(longName, name.getText());
                assertTrue(name.isWrapText());
                assertTrue(name.getWidth() <= summary.getWidth() + 0.001);
                assertTrue(name.getHeight() > name.getFont().getSize() * 1.5);
                Label level = label(root, "#schoolingLevel");
                assertTrue(level.getWidth() <= summary.getWidth() + 0.001);
            }
        });
    }

    private static Label label(Region root, String selector) {
        return assertInstanceOf(Label.class, root.lookup(selector));
    }
}
