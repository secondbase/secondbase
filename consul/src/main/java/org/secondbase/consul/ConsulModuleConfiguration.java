package org.secondbase.consul;

import org.secondbase.flags.Flag;

/**
 * Configuration parameters for the WebConsole.
 */
public final class ConsulModuleConfiguration {

    private ConsulModuleConfiguration() {}

    /**
     * Location of Consul server.
     */
    @Flag (
            name = "consul-host",
            description="Consul host[:port]")
    public static String host = "localhost:8500";

    @Flag(
            name = "service-name",
            description = "Name of service to register in consul"
    )
    public static String serviceName = "";

    @Flag(
            name = "service-port",
            description = "Port of service to register in consul"
    )
    public static int servicePort = 0;

    @Flag(
            name = "service-environment",
            description = "The environment the service runs in"
    )
    public static String environment = "testing";

    @Flag(
            name = "service-health-check-path",
            description = "Http path for consul to run health check towards"
    )
    public static String healthCheckPath = "";

    @Flag(
            name = "service-tags",
            description = "Comma separated list of tags to register with the service"
    )
    public static String tags = "";

    @Flag(
            name = "service-health-check-interval",
            description = "Interval, in seconds, between health checks performed by consul"
    )
    public static long healthCheckIntervalSec = 29L;
}
