package org.secondbase.logging;

import org.secondbase.core.SecondBase;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.flags.Flag;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class JsonLoggerModule implements SecondBaseModule {

    @Flag(name = "environment",
            description = "The environment the service runs in (test|staging|production).", required = false)
    public static String environment;

    @Flag(name = "service", description = "The name of the service (testservice).", required = false)
    public static String service;

    @Flag(name = "datacenter",
            description = "The datacenter the service runs in (dc1|dc2).", required = false)
    public static String datacenter;

    @Override
    public void load(final SecondBase secondBase) {
        secondBase.getFlags().loadOpts(JsonLoggerModule.class);
    }

    @Override
    public void init() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        final String nullRequestLoggerName = null;
        SecondBaseLogger.setupLoggingStdoutOnly(JsonLoggerModule.environment,
                JsonLoggerModule.service, JsonLoggerModule.datacenter, nullRequestLoggerName);
    }
}
