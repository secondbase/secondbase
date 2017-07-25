package org.secondbase.logging;

import org.secondbase.core.SecondBase;
import org.secondbase.core.config.SecondBaseModule;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class JsonLoggerModule implements SecondBaseModule {

    @Override
    public void load(final SecondBase secondBase) {
        secondBase.getFlags().loadOpts(JsonLoggerConfiguration.class);
    }

    @Override
    public void init() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        SecondBaseLogger.setupLoggingStdoutOnly(
                JsonLoggerConfiguration.environment,
                JsonLoggerConfiguration.service,
                JsonLoggerConfiguration.datacenter,
                JsonLoggerConfiguration.requestLoggerClassName);
    }
}
