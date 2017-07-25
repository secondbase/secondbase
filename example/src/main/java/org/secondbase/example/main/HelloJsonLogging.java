package org.secondbase.example.main;

import org.secondbase.core.SecondBase;
import org.secondbase.core.SecondBaseException;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.logging.JsonLoggerModule;

public final class HelloJsonLogging {

    public static void main(final String[] args) throws SecondBaseException {
        final SecondBaseModule[] modules = new SecondBaseModule[] { new JsonLoggerModule() };
        new SecondBase(args, modules);

        final org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(
                HelloJsonLogging.class);
        slf4jLogger.info("Hello world slf4j");

        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
                HelloJsonLogging.class.getName());
        logger.info("Hello world jul");
    }
}
