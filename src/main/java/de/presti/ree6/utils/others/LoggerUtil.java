package de.presti.ree6.utils.others;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A utility class that provides usefull methods regarding the {@link org.slf4j.Logger}.
 */
public class LoggerUtil {

    private final LevelFilter standardFiler;
    private final LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

    public LoggerUtil() {
        standardFiler = new LevelFilter();
        standardFiler.setLevel(Level.DEBUG);
        standardFiler.setContext(ctx);
        standardFiler.setOnMatch(FilterReply.DENY);
        standardFiler.start();
    }

    /**
     * Inits the {@link org.slf4j.Logger}.
     */
    public void initLogger() {
        new File("logs/archives").mkdirs();
        new File("logs/debug/archives").mkdirs();

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%d{HH:mm:ss.SSS}  %boldCyan[%thread] %highlight(%-6level) %boldGreen(%-15.-15logger{35}) - %msg %n");
        ple.setContext(ctx);
        ple.start();

        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setEncoder(ple);
        ca.setContext(ctx);
        ca.addFilter(standardFiler);
        ca.start();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        FileAppender<ILoggingEvent> fa = new FileAppender<>();
        fa.setEncoder(ple);
        fa.setContext(ctx);
        fa.setFile(String.format("logs/archives/log-%s.log.gz", formatter.format(new Date())));
        fa.addFilter(standardFiler);
        fa.start();

        FileAppender<ILoggingEvent> debugFa = new FileAppender<>();
        debugFa.setEncoder(ple);
        debugFa.setContext(ctx);
        debugFa.setFile(String.format("logs/debug/archives/log-%s.log.gz", formatter.format(new Date())));
        debugFa.start();

        Logger logger = ctx.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(ca);
        logger.addAppender(fa);
        logger.addAppender(debugFa);
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);
    }

    /**
     * Sets the logger into debug or info mode.
     * @param debugMode The debug mode.
     */
    public static void setDebugLoggerMode(boolean debugMode, LoggerUtil loggerUtil) {
        loggerUtil.standardFiler.stop();
        loggerUtil.standardFiler.setOnMatch(debugMode ? FilterReply.ACCEPT : FilterReply.DENY);
        loggerUtil.standardFiler.start();
    }
}
