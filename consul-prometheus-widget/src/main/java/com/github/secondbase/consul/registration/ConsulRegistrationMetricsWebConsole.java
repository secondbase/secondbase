package com.github.secondbase.consul.registration;

import com.github.secondbase.consul.ConsulModule;
import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.SecondBaseException;
import com.github.secondbase.core.config.SecondBaseModule;
import com.github.secondbase.webconsole.HttpWebConsole;

/**
 * Consul service registration for Prometheus WebConsole widget.
 */
public final class ConsulRegistrationMetricsWebConsole implements SecondBaseModule {

    private final HttpWebConsole webConsole;
    private final ConsulModule consulModule;

    @Override
    public void load(final SecondBase secondBase) {
        // nothing to configure
    }

    @Override
    public void init() throws SecondBaseException {
        consulModule.registerServiceInConsul(
                SecondBase.serviceName,
                webConsole.getPort(),
                SecondBase.environment,
                "/healthz",
                29L,
                "metrics"
        );
    }

    public ConsulRegistrationMetricsWebConsole(
            final HttpWebConsole webConsole,
            final ConsulModule consulModule) {
        this.webConsole = webConsole;
        this.consulModule = consulModule;
    }
}
