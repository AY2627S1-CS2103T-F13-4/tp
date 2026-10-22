package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
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
