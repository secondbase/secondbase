package org.secondbase.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class SecondBaseLoggerTest {

    private static final String[] KEYS = new String[]  {"environment", "service", "datacenter"};
    private static final String[] VALUES = new String[] {"environment", "service" ,"datacenter"};

    @Test
    public void contextShouldBeSetProperly() throws IOException {
        SecondBaseLogger.setupLoggingStdoutOnly(KEYS, VALUES, null);

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Map<String, String> propertyMap = ImmutableMap.of(
                "environment", "environment",
                "service", "service",
                "datacenter", "datacenter");
        assertThat(loggerContext.getCopyOfPropertyMap(), is(propertyMap));
    }

    @Test
    public void customFieldsShouldBeLogged() throws IOException {
        final ArgumentCaptor<LoggingEvent> captorLoggingEvent = ArgumentCaptor.forClass(
                LoggingEvent.class);

        final LoggerContext loggerContext = SecondBaseLogger.getLoggerContext(
                new String[] {"environment", "service", "datacenter"},
                new String[] {"environment", "service", "datacenter"}
        );
        final Appender<ILoggingEvent> mockAppender = spy(
                SecondBaseLogger.createJsonConsoleAppender(
                        SecondBaseLogger.SERVICE_CONSOLE_APPENDER, loggerContext, true));
        final Logger rootLogger = (Logger) LoggerFactory.getLogger(
                org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(mockAppender);
        final org.slf4j.Logger logger = LoggerFactory.getLogger(SecondBaseLoggerTest.class);
        logger.info("log message");

        verify(mockAppender).doAppend(captorLoggingEvent.capture());

        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        final LoggingEventCompositeJsonEncoder encoder = SecondBaseLogger.getEncoder(
                loggerContext, true);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        encoder.start();
        outputStream.write(encoder.encode(loggingEvent));
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree(outputStream.toByteArray());

        assertThat(node.get("message").asText(), is("log message"));
        assertThat(node.get("environment").asText(), is("environment"));
        assertThat(node.get("datacenter").asText(), is("datacenter"));
        assertThat(node.get("service").asText(), is("service"));
        assertThat(node.get("level").asText(), is("INFO"));
        assertThat(node.has("timestamp"), is(true));
        assertThat(node.get("type").asText(), is("servicelog"));
        encoder.stop();
        outputStream.close();
    }

    @Test
    public void customFieldsShouldBeLoggedWithPatternLayout() throws IOException {
        final ArgumentCaptor<LoggingEvent> captorLoggingEvent = ArgumentCaptor.forClass(
                LoggingEvent.class);

        final LoggerContext loggerContext = SecondBaseLogger.getLoggerContext(
                new String[] {"environment", "service", "datacenter"},
                new String[] {"environment", "service", "datacenter"}
        );
        final Appender<ILoggingEvent> mockAppender = spy(
                SecondBaseLogger.createPatternLayoutConsoleAppender(
                        SecondBaseLogger.SERVICE_CONSOLE_APPENDER, loggerContext, true));
        final Logger rootLogger = (Logger) LoggerFactory.getLogger(
                org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(mockAppender);
        final org.slf4j.Logger logger = LoggerFactory.getLogger(SecondBaseLoggerTest.class);
        logger.info("log message");

        verify(mockAppender).doAppend(captorLoggingEvent.capture());

        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        final LoggingEventCompositeJsonEncoder encoder = SecondBaseLogger.getEncoder(
                loggerContext, true);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        encoder.start();
        outputStream.write(encoder.encode(loggingEvent));
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree(outputStream.toByteArray());

        assertThat(node.get("message").asText(), is("log message"));
        assertThat(node.get("environment").asText(), is("environment"));
        assertThat(node.get("datacenter").asText(), is("datacenter"));
        assertThat(node.get("service").asText(), is("service"));
        assertThat(node.get("level").asText(), is("INFO"));
        assertThat(node.has("timestamp"), is(true));
        assertThat(node.get("type").asText(), is("servicelog"));
        encoder.stop();
        outputStream.close();
    }

    @Test
    public void shouldSetConsoleAppender() {
        final String nullRequestLoggerName = null;
        SecondBaseLogger.setupLoggingStdoutOnly(KEYS, VALUES, nullRequestLoggerName);
        final ch.qos.logback.classic.Logger rootLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                        org.slf4j.Logger.ROOT_LOGGER_NAME);
        final Appender<ILoggingEvent> serviceConsoleAppender = rootLogger.getAppender(
                "SERVICECONSOLEAPPENDER");

        assertThat(serviceConsoleAppender, notNullValue());
    }

    @Test
    public void shouldSetConsoleAppenderForServiceAndRequestLogs() {
        final String requestLoggerName = "requestlogger";
        SecondBaseLogger.setupLoggingStdoutOnly(KEYS, VALUES, requestLoggerName);
        final ch.qos.logback.classic.Logger rootLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                        org.slf4j.Logger.ROOT_LOGGER_NAME);
        final Appender<ILoggingEvent> serviceConsoleAppender = rootLogger.getAppender(
                "SERVICECONSOLEAPPENDER");

        assertThat(serviceConsoleAppender, notNullValue());

        final ch.qos.logback.classic.Logger requestLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(requestLoggerName);
        final Appender<ILoggingEvent> requestLogConsoleAppender = requestLogger.getAppender(
                "REQUESTLOGCONSOLEAPPENDER");

        assertThat(requestLogConsoleAppender, notNullValue());
    }

    @Test
    public void shouldSetConsoleAndServiceAppenders() {
        SecondBaseLogger.setupLoggingStdoutOnly(KEYS, VALUES, null);
        final ch.qos.logback.classic.Logger rootLogger
                = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                            org.slf4j.Logger.ROOT_LOGGER_NAME);
        final Appender<ILoggingEvent> consoleAppender = rootLogger.getAppender(
                "SERVICECONSOLEAPPENDER");

        assertThat(consoleAppender, notNullValue());
    }

    @Test
    public void shouldSetConsoleAndServiceLayoutAppenders() {
        final boolean json = false;
        SecondBaseLogger.setupLoggingStdoutOnly(KEYS, VALUES, null, json);
        final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final Appender<ILoggingEvent> consoleAppender = rootLogger
                .getAppender("SERVICECONSOLEAPPENDER");

        assertThat(consoleAppender, notNullValue());
    }

    @Test
    public void correctParametersShouldBeParsed() throws IOException {
        assertThat(SecondBaseLogger.parametersOk(
                new String[] {"a"}, new String[] {"1"}), is(true));
        assertThat(SecondBaseLogger.parametersOk(
                new String[] {"a,b"}, new String[] {"1,2"}), is(true));
    }

    @Test
    public void incorrectParametersShouldNotBeParsed() throws IOException {
        assertThat(SecondBaseLogger.parametersOk(
                new String[] {""}, new String[] {""}), is(false));
        assertThat(SecondBaseLogger.parametersOk(
                null, null), is(false));
        assertThat(SecondBaseLogger.parametersOk(
                null, new String[] {""}), is(false));
        assertThat(SecondBaseLogger.parametersOk(
                new String[] {""}, null), is(false));
        assertThat(SecondBaseLogger.parametersOk(
                new String[] {"a"}, new String[] {""}), is(false));
        assertThat(SecondBaseLogger.parametersOk(
                new String[] {"a", "b"}, new String[] {"1"}), is(false));
    }
}
