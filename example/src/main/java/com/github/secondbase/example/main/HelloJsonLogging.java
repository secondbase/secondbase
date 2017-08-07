package com.github.secondbase.example.main;

import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.SecondBaseException;
import com.github.secondbase.core.config.SecondBaseModule;
import com.github.secondbase.logging.JsonLoggerModule;

public final class HelloJsonLogging {

    private HelloJsonLogging() {
    }

    public static void main(final String[] args) throws SecondBaseException {
        final String[] realArgs = new String[] {"--keys", "a,b", "--values", "1,2"};
        final SecondBaseModule[] modules = new SecondBaseModule[] {new JsonLoggerModule()};
        new SecondBase(realArgs, modules);

        final org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(
                HelloJsonLogging.class);
        slf4jLogger.info("Hello world slf4j");

        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
                HelloJsonLogging.class.getName());
        logger.info("Hello world jul");
    }
}
