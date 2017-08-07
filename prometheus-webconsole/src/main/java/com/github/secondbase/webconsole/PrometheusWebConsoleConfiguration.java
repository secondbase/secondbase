package com.github.secondbase.webconsole;

import com.github.secondbase.flags.Flag;

/**
 * Configuration parameters for the Prometheus WebConsole Widget.
 */
public final class PrometheusWebConsoleConfiguration {

    private PrometheusWebConsoleConfiguration() {
    }

    @Flag(
            name = "metrics-endpoint",
            description = "The http endpoint to host metrics on.")
    public static String endpoint = "/metrics";
}
