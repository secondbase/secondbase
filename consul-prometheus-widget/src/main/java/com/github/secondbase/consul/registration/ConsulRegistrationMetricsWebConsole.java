package com.github.secondbase.consul.registration;

import com.github.secondbase.consul.ConsulModule;
import com.github.secondbase.consul.ConsulModuleConfiguration;
import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.SecondBaseException;
import com.github.secondbase.core.config.SecondBaseModule;
import com.github.secondbase.webconsole.HttpWebConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consul service registration for Prometheus WebConsole widget.
 */
public final class ConsulRegistrationMetricsWebConsole implements SecondBaseModule {

    private static final Logger LOG = LoggerFactory.getLogger(
            ConsulRegistrationMetricsWebConsole.class.getName());

    private final HttpWebConsole webConsole;
    private final ConsulModule consulModule;

    @Override
    public void load(final SecondBase secondBase) {
        // nothing to configure
    }

    @Override
    public void init() throws SecondBaseException {
        if (SecondBase.serviceName.isEmpty()) {
            LOG.info("No service name defined. Nothing to register yet.");
            return;
        }
        if (ConsulModuleConfiguration.servicePort == 0) {
            LOG.error("Service port needs to be defined in order to register a service in consul.");
            return;
        }
        if (SecondBase.environment.isEmpty()) {
            LOG.error("Environment needs to be defined in order to register a service in consul.");
            return;
        }
        if (ConsulModuleConfiguration.healthCheckPath.isEmpty()) {
            LOG.error("Health check path needs to be defined in order to register a service in "
                    + "consul.");
            return;
        }
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
