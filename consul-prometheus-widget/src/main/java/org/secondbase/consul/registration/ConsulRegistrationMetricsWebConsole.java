package org.secondbase.consul.registration;

import org.secondbase.consul.ConsulModule;
import org.secondbase.core.SecondBase;
import org.secondbase.core.SecondBaseException;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.webconsole.HttpWebConsole;

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
