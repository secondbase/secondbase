package org.secondbase.logging;

import com.google.common.base.Strings;
import java.util.LinkedList;
import java.util.List;
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
        final List<String> keyList = new LinkedList<>();
        final List<String> valueList = new LinkedList<>();
        if (!Strings.isNullOrEmpty(SecondBase.serviceName)) {
            keyList.add("service");
            valueList.add(SecondBase.serviceName);
        }
        if (!Strings.isNullOrEmpty(SecondBase.environment)) {
            keyList.add("environment");
            valueList.add(SecondBase.environment);
        }
        if (!Strings.isNullOrEmpty(JsonLoggerConfiguration.datacenter)) {
            keyList.add("datacenter");
            valueList.add(JsonLoggerConfiguration.datacenter);
        }
        SecondBaseLogger.setupLoggingStdoutOnly(
                keyList.toArray(new String[] {}),
                valueList.toArray(new String[] {}),
                JsonLoggerConfiguration.requestLoggerClassName,
                true);
    }
}
