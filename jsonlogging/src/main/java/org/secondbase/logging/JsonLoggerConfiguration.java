package org.secondbase.logging;

import org.secondbase.flags.Flag;

public class JsonLoggerConfiguration {

    private JsonLoggerConfiguration() {
    }

    @Flag(
            name = "environment",
            description = "The environment the service runs in (test|staging|production)."
    )
    public static String environment;

    @Flag(
            name = "service",
            description = "The name of the service (testservice)."
    )
    public static String service;

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
