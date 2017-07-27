package org.secondbase.logging;

import org.secondbase.flags.Flag;

public class JsonLoggerConfiguration {

    private JsonLoggerConfiguration() {
    }

    @Flag(
            name = "datacenter",
            description = "The datacenter the service runs in (dc1|dc2)."
    )
    public static String datacenter;

    @Flag(
            name = "request-logger-class-name",
            description = "Name of the request logger class."
    )
    public static String requestLoggerClassName;
}
