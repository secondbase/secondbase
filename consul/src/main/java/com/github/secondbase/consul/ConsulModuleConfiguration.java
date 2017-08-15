package com.github.secondbase.consul;

import com.github.secondbase.flags.Flag;

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
            name = "service-port",
            description = "Port of service to register in consul"
    )
    public static int servicePort = 0;

    @Flag(
            name = "consul-health-check-path",
            description = "Http path for consul to run health check towards"
    )
    public static String healthCheckPath = "";

    @Flag(
            name = "consul-tags",
            description = "Comma separated list of tags to register with the service"
    )
    public static String tags = "";

    @Flag(
            name = "consul-health-check-interval",
            description = "Interval, in seconds, between health checks performed by consul"
    )
    public static long healthCheckIntervalSec = 29L;

    @Flag(
            name = "enable-consul",
            description = "Set to enable consul integration"
    )
    public static boolean enabled = false;
}
