package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import seedu.address.model.person.School;
import seedu.address.model.person.Student;
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

    private static Region prepareCard(PersonCard card, DoubleProperty detailsWidth) {
        DoubleProperty columnWidth = new SimpleDoubleProperty(100);
        card.bindColumns(new PersonListPanel.Columns(columnWidth, columnWidth, columnWidth, columnWidth,
                columnWidth, detailsWidth, new SimpleBooleanProperty(false)));
        Region root = card.getRoot();
        root.setManaged(false);
        Scene scene = new Scene(new Group(root));
        scene.getStylesheets().add(PersonCardTest.class.getResource("/view/LightTheme.css").toExternalForm());
        root.applyCss();
        return root;
    }
}
