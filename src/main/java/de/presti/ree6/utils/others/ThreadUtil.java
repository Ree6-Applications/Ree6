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
    static ExecutorService executorService = Executors.newFixedThreadPool(50);

    /**
     * Creates a ASyncThread with a Consumer.
     * @param success the Consumer, that will be executed, when the Thread is finished.
     * @param failure the Consumer, that will be executed, when the Thread failed.
     * @param duration the delay duration of the Thread.
     */
    public static void createNewASyncThread(Consumer<Void> success, Consumer<Throwable> failure, Duration duration) {
        executorService.submit(() ->  {
            while(!Thread.currentThread().isInterrupted()) {
                success.accept(null);

                try {
                    Thread.sleep(duration.toMillis());
                } catch (InterruptedException e) {
                    failure.accept(e);
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

}
