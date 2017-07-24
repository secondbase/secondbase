package org.secondbase.example.main;

import org.secondbase.core.SecondBase;
import org.secondbase.core.SecondBaseException;
import org.secondbase.flags.Flags;

public final class HelloJsonLogging {

    public static void main(final String[] args) throws SecondBaseException {
        final HelloJsonLogging helloJsonLogging = new HelloJsonLogging();
        final String[] realArgs = new String[] {
                "--service", "test",
                "--datacenter", "dc1"
        };
        new SecondBase(realArgs, new Flags().loadOpts(helloJsonLogging));

        final org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(
                HelloJsonLogging.class);
        slf4jLogger.info("Hello world slf4j");

        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
                HelloJsonLogging.class.getName());
        logger.info("Hello world jul");
    }
}
