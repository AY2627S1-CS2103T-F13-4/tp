package seedu.address.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Short animations that show what changed after a command or key press.
 * Each is brief so that fast typists are never kept waiting.
 */
final class Motion {

    private static final Duration SHORT = Duration.millis(160);
    private static final Duration RESIZE = Duration.millis(220);
    private static final String RUNNING_KEY = "motion.running";

    private Motion() {}

    /** Fades {@code node} in from {@code fromOpacity}, e.g. a new result or a filtered list. */
    static void fadeIn(Node node, double fromOpacity) {
        stop(node);
        FadeTransition fade = new FadeTransition(SHORT, node);
        fade.setFromValue(fromOpacity);
        fade.setToValue(1);
        play(node, fade);
    }

    /**
     * Grows {@code region} from nothing to its full height while it fades in, e.g. a row's details as they open.
     * The rows below slide down with it instead of jumping.
     */
    static void expand(Region region) {
        stop(region);
        clipToBounds(region);
        region.setMinHeight(0);
        region.setMaxHeight(0);
        region.setOpacity(0);
        // Measure the full height on every frame: before the first layout the width, and so the height, is wrong.
        DoubleProperty progress = new SimpleDoubleProperty(0);
        progress.addListener((unused, old, now) -> {
            double width = region.getWidth() > 0 ? region.getWidth() : -1;
            region.setMaxHeight(now.doubleValue() * region.prefHeight(width));
        });
        Timeline grow = new Timeline(new KeyFrame(RESIZE,
                new KeyValue(progress, 1, Interpolator.EASE_OUT),
                new KeyValue(region.opacityProperty(), 1, Interpolator.EASE_OUT)));
        grow.setOnFinished(event -> region.setMaxHeight(Region.USE_COMPUTED_SIZE));
        play(region, grow);
    }

    /**
     * Shrinks {@code region} to nothing while it fades out, then runs {@code afterwards}, e.g. to hide it.
     */
    static void collapse(Region region, Runnable afterwards) {
        stop(region);
        clipToBounds(region);
        region.setMinHeight(0);
        Timeline shrink = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(region.maxHeightProperty(), region.getHeight())),
                new KeyFrame(RESIZE,
                        new KeyValue(region.maxHeightProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(region.opacityProperty(), 0, Interpolator.EASE_IN)));
        shrink.setOnFinished(event -> {
            region.setMaxHeight(Region.USE_COMPUTED_SIZE);
            region.setOpacity(1);
            afterwards.run();
        });
        play(region, shrink);
    }

    /** Runs {@code action} once a grow or shrink started now has finished, e.g. to scroll to what it grew. */
    static void afterResize(Runnable action) {
        PauseTransition wait = new PauseTransition(RESIZE);
        wait.setOnFinished(event -> action.run());
        wait.play();
    }

    /** Hides whatever lies outside {@code region}, so content never spills over while it grows or shrinks. */
    private static void clipToBounds(Region region) {
        if (region.getClip() == null) {
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(region.widthProperty());
            clip.heightProperty().bind(region.heightProperty());
            region.setClip(clip);
        }
    }

    /** Nudges {@code node} sideways and back, so an error is noticed. */
    static void shake(Node node) {
        stop(node);
        TranslateTransition shake = new TranslateTransition(Duration.millis(45), node);
        shake.setFromX(0);
        shake.setByX(4);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setOnFinished(event -> node.setTranslateX(0));
        play(node, shake);
    }

    /** Stops any animation still running on {@code node} and puts it back at rest, so two never fight. */
    private static void stop(Node node) {
        if (node.getProperties().remove(RUNNING_KEY) instanceof Animation running) {
            running.stop();
            node.setOpacity(1);
            node.setTranslateX(0);
            node.setTranslateY(0);
        }
    }

    private static void play(Node node, Animation animation) {
        node.getProperties().put(RUNNING_KEY, animation);
        animation.play();
    }
}
