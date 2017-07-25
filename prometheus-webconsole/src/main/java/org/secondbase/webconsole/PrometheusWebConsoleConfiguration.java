package org.secondbase.webconsole;

import org.secondbase.flags.Flag;

/**
 * Configuration parameters for the Prometheus WebConsole Widget.
 */
public final class PrometheusWebConsoleConfiguration {
    @Flag(
            name = "metrics-endpoint",
            description = "The http endpoint to host metrics on.")
    public static String endpoint = "/metrics";
}
