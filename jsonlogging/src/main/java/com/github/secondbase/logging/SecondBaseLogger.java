package com.github.secondbase.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Map.Entry;
import net.logstash.logback.composite.ContextJsonProvider;
import net.logstash.logback.composite.GlobalCustomFieldsJsonProvider;
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.joda.time.DateTimeZone;
import org.slf4j.LoggerFactory;

public final class SecondBaseLogger {

    private static final Level LOG_LEVEL = Level.INFO;

    // package-private for testing
    static final String SERVICE_CONSOLE_APPENDER = "SERVICECONSOLEAPPENDER";
    private static final String REQUESTLOG_CONSOLE_APPENDER = "REQUESTLOGCONSOLEAPPENDER";

    private SecondBaseLogger() {
    };

    /**
     * Set up logback logging for service and request logs. We use only ONE appender here to write
     * normal log lines to stdout for both types. Logging context is set from the keys and values
     * parameters. They must be corresponding in length and all elements must be non-null and
     * non-empty. However, the request logger is set up slightly differently. To guarantee that
     * everything works as expected this method must be called with the fully qualified class name
     * of the request logger that will be used in jetty.
     * @param keys to add to all log lines
     * @param values to add to all log lines
     * @param requestLoggerName the request logger class name
     */
    public static void setupLoggingStdoutOnly(
            final String[] keys, final String[] values, final String requestLoggerName) {
        setupLoggingStdoutOnly(keys, values, requestLoggerName, true);
    }

    /**
     * Set up logback logging for service and request logs. We use only ONE appender here to write
     * normal log lines to stdout for both types. Logging context is set from the keys and values
     * parameters. They must be corresponding in length and all elements must be non-null and
     * non-empty. However, the request logger is set up slightly differently. To guarantee that
     * everything works as expected this method must be called with the fully qualified class name
     * of the request logger that will be used in jetty.
     * @param keys to add to all log lines
     * @param values to add to all log lines
     * @param requestLoggerName the request logger class name
     * @param json true for json output, false for plain test
     */
     public static void setupLoggingStdoutOnly(
            final String[] keys,
            final String[] values,
            final String requestLoggerName,
            final boolean json) {
        if(!parametersOk(keys, values)) {
            throw new IllegalArgumentException(
                    "Context keys and/or values are not properly formatted.");
        }
        final LoggerContext loggerContext = getLoggerContext(keys, values);
        final Appender<ILoggingEvent> consoleAppender = json
                ? createJsonConsoleAppender(SERVICE_CONSOLE_APPENDER, loggerContext, true)
                : createPatternLayoutConsoleAppender(SERVICE_CONSOLE_APPENDER, loggerContext, true);

        // specifically cast to logback version so we set it up
        final Logger rootLogger = (Logger) LoggerFactory.getLogger(
                org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(consoleAppender);
        rootLogger.setLevel(LOG_LEVEL);

        if (!Strings.isNullOrEmpty(requestLoggerName)) {
            final Logger requestLogger = (Logger) LoggerFactory.getLogger(requestLoggerName);
            requestLogger
                    .addAppender(json
                            ? createJsonConsoleAppender(
                                    REQUESTLOG_CONSOLE_APPENDER, loggerContext, false)
                            : createPatternLayoutConsoleAppender(
                                    REQUESTLOG_CONSOLE_APPENDER, loggerContext, false));
            requestLogger.setAdditive(false);
            requestLogger.setLevel(LOG_LEVEL);
        }
    }

    static boolean parametersOk(final String[] keys, final String[] values) {
        if (keys == null || values == null) {
            return false;
        }
        if (keys.length != values.length) {
            return false;
        }
        for (final String key : keys) {
            if (Strings.isNullOrEmpty(key)) {
                return false;
            }
        }
        for (final String value : values) {
            if (Strings.isNullOrEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    static LoggerContext getLoggerContext(
            final String[] keys,
            final String[] values) {
        // specifically cast to logback version so we can add advanced stuff (i.e. properties)
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            final String value = values[i];
            if (!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(value)) {
                loggerContext.putProperty(key, value);
            }
        }
        return loggerContext;
    }

     /**
     * Set up a {@link ConsoleAppender} using a {@link LoggingEventCompositeJsonEncoder} to log to
     * stdout. Package-private for testing.
     * @param loggerContext to use for setup
     * @param appenderName name of the appender
     * @param serviceLog return appender for service logs if true, else return one for request logs
     * @return console appender
     */
    static ConsoleAppender<ILoggingEvent> createJsonConsoleAppender(
            final String appenderName,
            final LoggerContext loggerContext,
            final boolean serviceLog) {

        final LoggingEventCompositeJsonEncoder jsonEncoder = getEncoder(loggerContext, serviceLog);

        final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName(appenderName);
        consoleAppender.setEncoder(jsonEncoder);
        consoleAppender.start();

        return consoleAppender;
    }

    /**
     * Get a {@link LoggingEventCompositeJsonEncoder}. The returned appender
     * adds a timestamp field and sets the type to "servicelog" if serviceLog is
     * true. If set to false, we return a request log appender which sets the
     * timestamp field internally to guarantee that timestamp of the request log
     * event and the resultant json structure stay in sync.
     * @param loggerContext to use for setup
     * @param serviceLog return appender for service logs if true, else return one for request logs
     * @return corresponding {@link LoggingEventCompositeJsonEncoder}
     */
    static LoggingEventCompositeJsonEncoder getEncoder(
            final LoggerContext loggerContext, final boolean serviceLog) {
        final LoggingEventCompositeJsonEncoder jsonEncoder = new LoggingEventCompositeJsonEncoder();
        jsonEncoder.setContext(loggerContext);
        final LoggingEventJsonProviders jsonProviders = getCommonJsonProviders(loggerContext);

        final GlobalCustomFieldsJsonProvider<ILoggingEvent> customFieldsJsonProvider
                = new GlobalCustomFieldsJsonProvider<>();
        if (serviceLog) {
            final LoggingEventFormattedTimestampJsonProvider timeStampProvider
                    = new LoggingEventFormattedTimestampJsonProvider();
            timeStampProvider.setTimeZone(DateTimeZone.UTC.getID());
            timeStampProvider.setFieldName("timestamp");
            jsonProviders.addTimestamp(timeStampProvider);
            customFieldsJsonProvider.setCustomFields("{\"type\":\"servicelog\"}");
        } else {
            customFieldsJsonProvider.setCustomFields("{\"type\":\"requestlog\"}");
        }
        jsonProviders.addGlobalCustomFields(customFieldsJsonProvider);
        jsonProviders.addStackTrace(new StackTraceJsonProvider());
        jsonProviders.addLogLevel(new LogLevelJsonProvider());
        jsonEncoder.setProviders(jsonProviders);
        jsonEncoder.start();
        return jsonEncoder;
    }

    /**
     * Set up a {@link PatternLayoutEncoder} to log plaintext. Package-private for testing.
     * @param loggerContext to use for setup
     * @param appenderName name of the appender
     * @param serviceLog return appender for service logs if true, else return one for request logs
     * @return console appender
     */
    static ConsoleAppender<ILoggingEvent> createPatternLayoutConsoleAppender(
            final String appenderName,
            final LoggerContext loggerContext,
            final boolean serviceLog) {
        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        final StringBuilder sb = new StringBuilder();
        Map<String, String> copyOfPropertyMap = loggerContext.getCopyOfPropertyMap();
        for(Entry<String, String> entry : copyOfPropertyMap.entrySet()) {
            sb.append("%property{" + entry.getKey()+ "} ");
        }
        final String logType = serviceLog ? "servicelog" : "requestlog";
        final String pattern = sb.toString().trim().length() == 0
                ? ""
                : sb.toString().trim() + " ";
        encoder.setPattern(
                "%-5level " + "[" + pattern + logType + "] "
                        + "[%thread]: %message%n");
        encoder.start();

        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.setName(appenderName);
        appender.start();

        return appender;
    }

    private static LoggingEventJsonProviders getCommonJsonProviders(
            final LoggerContext loggerContext) {
        final LoggingEventJsonProviders jsonProviders = new LoggingEventJsonProviders();
        jsonProviders.addPattern(new LoggingEventPatternJsonProvider());
        jsonProviders.addArguments(new ArgumentsJsonProvider());
        jsonProviders.addMessage(new MessageJsonProvider());
        jsonProviders.addContext(new ContextJsonProvider<ILoggingEvent>());
        jsonProviders.addMdc(new MdcJsonProvider());
        jsonProviders.setContext(loggerContext);
        return jsonProviders;
    }
}
