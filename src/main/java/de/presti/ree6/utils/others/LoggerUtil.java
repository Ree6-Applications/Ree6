package de.presti.ree6.utils.others;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A utility class that provides usefull methods regarding the {@link org.slf4j.Logger}.
 */
//TODO replace loggers with the @Slf4j lombok annotation
public class LoggerUtil {

    private LoggerUtil() {}

    /**
     * Inits the {@link org.slf4j.Logger}.
     */
    public static void initLogger() {
        new File("logs/archives").mkdirs();
        new File("logs/debug/archives").mkdirs();

        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%d{HH:mm:ss.SSS}  %boldCyan[%thread] %highlight(%-6level) %boldGreen(%-15.-15logger{35}) - %msg %n");
        ple.setContext(ctx);
        ple.start();

        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setEncoder(ple);
        ca.setContext(ctx);
        ca.start();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        FileAppender<ILoggingEvent> fa = new FileAppender<>();
        fa.setEncoder(ple);
        fa.setContext(ctx);
        fa.setFile(String.format("logs/archives/log-%s.log.gz", formatter.format(new Date())));
        fa.start();

        FileAppender<ILoggingEvent> faDebug = new FileAppender<>();
        faDebug.setEncoder(ple);
        faDebug.setContext(ctx);
        faDebug.setFile(String.format("logs/debug/archives/log-%s.log.gz", formatter.format(new Date())));
        faDebug.start();

        Logger logger = ctx.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(ca);
        logger.addAppender(fa);
        logger.addAppender(faDebug);
        logger.setAdditive(false);
        logger.setLevel(Level.INFO);
    }

    /**
     * Sets the logger into debug or info mode.
     * @param debugMode The debug mode.
     */
    public static void setDebugLoggerMode(boolean debugMode) {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(debugMode ? Level.DEBUG : Level.INFO);
    }
}
