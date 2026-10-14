package seedu.address.testutil;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;

/**
 * Runs JavaFX assertions on the application thread without opening windows.
 */
public final class FxTestUtil {
    private static final int TIMEOUT_SECONDS = 10;
    private static boolean initialized;

    private FxTestUtil() {
    }

    /**
     * Starts the shared toolkit once and keeps it running between UI tests.
     */
    public static synchronized void initialize() throws InterruptedException {
        if (initialized) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            Platform.setImplicitExit(false);
            initialized = true;
            return;
        }

        CountDownLatch started = new CountDownLatch(1);
        Runnable start = () -> {
            Platform.setImplicitExit(false);
            started.countDown();
        };
        try {
            Platform.startup(start);
        } catch (IllegalStateException e) {
            // Another UI test may already have initialized the process-wide toolkit.
            Platform.runLater(start);
        }
        if (!started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX did not initialize within " + TIMEOUT_SECONDS + " seconds");
        }
        initialized = true;
    }

    /**
     * Runs an action on the JavaFX thread and propagates assertion failures to the calling test.
     */
    public static void runOnFxThread(Runnable action) throws Exception {
        callOnFxThread(() -> {
            action.run();
            return null;
        });
    }

    /**
     * Runs an action on the JavaFX thread and returns its result, using a bounded wait.
     */
    public static <T> T callOnFxThread(Callable<T> action) throws Exception {
        initialize();
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        try {
            return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            if (e.getCause() instanceof Exception exception) {
                throw exception;
            }
            throw new AssertionError(e.getCause());
        } catch (TimeoutException e) {
            task.cancel(false);
            throw new AssertionError("JavaFX action did not finish within " + TIMEOUT_SECONDS + " seconds", e);
        }
    }
}
