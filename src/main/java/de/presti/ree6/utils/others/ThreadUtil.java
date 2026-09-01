package de.presti.ree6.utils.others;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This util class is used to create ASyncThreads with consumers in a Thread-pool
 */
@Slf4j
public class ThreadUtil {

    /**
     * The Thread-pool used to create ASyncThreads.
     */
    static ExecutorService executorService = Executors.newFixedThreadPool(150, namedFactory("Ree6-Worker"));

    /**
     * Scheduler used for delayed and repeating work, so a delay never occupies a worker.
     */
    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4, namedFactory("Ree6-Scheduler"));

    /**
     * Create a Thread-factory producing named daemon threads.
     *
     * @param prefix the name prefix of the created Threads.
     * @return the {@link ThreadFactory}.
     */
    private static ThreadFactory namedFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Run a Consumer, routing anything it throws to the failure Consumer, so a throwing task can
     * never silently kill a loop.
     *
     * @param success the Consumer to run.
     * @param failure the Consumer to inform about a failure.
     * @return true if the Consumer completed without throwing.
     */
    private static boolean runSafely(Consumer<Void> success, Consumer<Throwable> failure) {
        try {
            success.accept(null);
            return true;
        } catch (Throwable throwable) {
            log.error("An error occurred while running a Thread task!", throwable);
            try {
                failure.accept(throwable);
            } catch (Throwable failureThrowable) {
                log.error("The failure handler of a Thread task threw as well!", failureThrowable);
            }
            return false;
        }
    }

    /**
     * Creates a Thread with a Consumer.
     *
     * @param success the Consumer, that will be executed, when the Thread is finished.
     * @return the Future of the Thread.
     */
    public static Future<?> createThread(Consumer<Void> success) {
        return createThread(success, Sentry::captureException);
    }

    /**
     * Creates a Thread with a Consumer.
     *
     * @param success the Consumer, that will be executed, when the Thread is finished.
     * @param failure the Consumer, that will be executed, when the Thread failed.
     * @return the Future of the Thread.
     */
    public static Future<?> createThread(Consumer<Void> success, Consumer<Throwable> failure) {
        return createThread(success, failure, null, false, true);
    }

    /**
     * Creates a Thread with a Consumer.
     *
     * @param success the Consumer, that will be executed, when the Thread is finished.
     * @param duration the delay duration of the Thread.
     * @param loop     if the Thread should be looped.
     * @param pre      the Consumer, that will be executed, before the Thread is going into the sleep state.
     * @return the Future of the Thread.
     */
    public static Future<?> createThread(Consumer<Void> success, Duration duration, boolean loop, boolean pre) {
        return createThread(success, Sentry::captureException, duration, loop, pre);
    }

    /**
     * Creates a Thread with a Consumer.
     *
     * @param success  the Consumer, that will be executed, when the Thread is finished.
     * @param failure  the Consumer, that will be executed, when the Thread failed.
     * @param duration the delay duration of the Thread.
     * @param loop     if the Thread should be looped.
     * @param pre      the Consumer, that will be executed, before the Thread is going into the sleep state.
     * @return the Future of the Thread.
     */
    public static Future<?> createThread(Consumer<Void> success, Consumer<Throwable> failure, Duration duration, boolean loop, boolean pre) {
        if (failure == null) failure = Sentry::captureException;

        Consumer<Throwable> finalFailure = failure;

        if (!loop) {
            if (!pre && duration != null) {
                return scheduler.schedule(() -> executorService.submit(() -> runSafely(success, finalFailure)),
                        duration.toMillis(), TimeUnit.MILLISECONDS);
            }

            return executorService.submit(() -> runSafely(success, finalFailure));
        }

        long period = duration != null ? duration.toMillis() : 0L;

        if (period <= 0L) {
            return executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    runSafely(success, finalFailure);
                }
            });
        }

        // Body runs on the worker pool so a long run cannot occupy a scheduler thread.
        return new RepeatingTask(success, finalFailure, period, pre);
    }

    /**
     * A repeating task whose body runs on the worker pool and which re-arms itself after each run.
     */
    private static final class RepeatingTask implements Future<Void> {

        private final Consumer<Void> success;
        private final Consumer<Throwable> failure;
        private final long period;
        private volatile boolean cancelled;
        private volatile Future<?> current;

        private RepeatingTask(Consumer<Void> success, Consumer<Throwable> failure, long period, boolean runNow) {
            this.success = success;
            this.failure = failure;
            this.period = period;

            if (runNow) {
                submitRun();
            } else {
                armNext();
            }
        }

        /**
         * Wait one period, then run again.
         */
        private void armNext() {
            if (cancelled) return;
            current = scheduler.schedule(this::submitRun, period, TimeUnit.MILLISECONDS);
        }

        /**
         * Run the body on the worker pool, then re-arm.
         */
        private void submitRun() {
            if (cancelled) return;
            current = executorService.submit(() -> {
                try {
                    runSafely(success, failure);
                } finally {
                    armNext();
                }
            });
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            Future<?> running = current;
            return running != null && running.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return cancelled;
        }

        @Override
        public Void get() {
            throw new UnsupportedOperationException("A repeating task never completes.");
        }

        @Override
        public Void get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException("A repeating task never completes.");
        }
    }

    /**
     * Schedule a one-shot task after the given delay without occupying a worker while waiting.
     *
     * @param task  the task to run.
     * @param delay how long to wait before running it.
     * @return the Future of the scheduled task.
     */
    public static Future<?> schedule(Runnable task, Duration delay) {
        return scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Throwable throwable) {
                log.error("An error occurred while running a scheduled task!", throwable);
                Sentry.captureException(throwable);
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Shut down both pools. Called from the shutdown hook.
     */
    public static void shutdown() {
        scheduler.shutdownNow();
        executorService.shutdownNow();
    }
}
