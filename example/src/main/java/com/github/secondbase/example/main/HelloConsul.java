package com.github.secondbase.example.main;

import com.github.secondbase.consul.ConsulModule;
import com.github.secondbase.consul.ConsulModuleConfiguration;
import com.github.secondbase.core.SecondBaseException;

/**
 * Example of how to use the {@link ConsulModule} to register a service.
 */
public final class HelloConsul {
    private HelloConsul() {}

    public static void main(final String[] args) throws SecondBaseException {
        final String serviceName = "myservice";
        final int servicePort = 8080;
        final String environment = "testing";
        final String healthCheckPath = "/health";
        final long healthCheckIntervalSec = 29L;
        final String[] tags = {"tagone", "tagtwo"};

        // Set consul endpoint
        ConsulModuleConfiguration.host = "localhost:8500";

        // Manually register a service in consul
        new ConsulModule().registerServiceInConsul(
                serviceName,
                servicePort,
                environment,
                healthCheckPath,
                healthCheckIntervalSec,
                tags
        );
    }
}
