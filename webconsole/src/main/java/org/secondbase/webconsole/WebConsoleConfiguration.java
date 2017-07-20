package org.secondbase.webconsole;

import org.secondbase.flags.Flag;

/**
 * Configuration parameters for the WebConsole.
 */
public final class WebConsoleConfiguration {
    @Flag(name = "enable-webconsole", description = "Start the web console")
    public static boolean enableWebConsole = false;
    @Flag(
            name = "webconsole-port",
            description = "The port used by the webconsole (default 0 will select an available " +
                    "port between X and Y)")
    public static int port = 5060;
}
