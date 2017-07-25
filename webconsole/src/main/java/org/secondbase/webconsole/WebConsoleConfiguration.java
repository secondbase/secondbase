package org.secondbase.webconsole;

import org.secondbase.flags.Flag;

/**
 * Configuration parameters for the WebConsole.
 */
public final class WebConsoleConfiguration {
    @Flag(
            name = "webconsole-port",
            description = "The port used by the webconsole (default 0 will select an available " +
                    "port between X and Y)")

    public static int port = 5060;

    @Flag(
            name = "webconsole-shutdown-delay",
            description = "Time, in seconds, from requesting shutdown on the webconsole until " +
                    "the server stops forcefully")
    public static int stopTimeout = 0;
}
