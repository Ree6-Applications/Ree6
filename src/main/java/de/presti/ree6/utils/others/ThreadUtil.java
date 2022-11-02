package de.presti.ree6.utils.others;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This util class is used to create ASyncThreads with consumers in a Thread-pool
 */
public class ThreadUtil {

    /**
     * The Thread-pool used to create ASyncThreads.
     */
    static ExecutorService executorService = Executors.newFixedThreadPool(150);

    /**
     * Creates a Thread with a Consumer.
     *
     * @param success  the Consumer, that will be executed, when the Thread is finished.
     * @param failure  the Consumer, that will be executed, when the Thread failed.
     */
    public static void createThread(Consumer<Void> success, Consumer<Throwable> failure) {
        createThread(success, failure, null, false, true);
    }

    /**
     * Creates a Thread with a Consumer.
     *
     * @param success  the Consumer, that will be executed, when the Thread is finished.
     * @param failure  the Consumer, that will be executed, when the Thread failed.
     * @param duration the delay duration of the Thread.
     * @param loop     if the Thread should be looped.
     * @param pre      the Consumer, that will be executed, before the Thread is going into the sleep state.
     */
    public static void createThread(Consumer<Void> success, Consumer<Throwable> failure, Duration duration, boolean loop, boolean pre) {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (pre) {
                    success.accept(null);
                    if (!loop)
                        Thread.currentThread().interrupt();
                }

                try {
                    if (duration != null)
                        Thread.sleep(duration.toMillis());
                } catch (InterruptedException e) {
                    failure.accept(e);
                    Thread.currentThread().interrupt();
                }

                if (!pre) {
                    success.accept(null);
                    if (!loop)
                        Thread.currentThread().interrupt();
                }
            }
        });
    }

}
