package org.secondbase.example.main;

import io.prometheus.client.Counter;
import java.io.IOException;
import org.secondbase.consul.ConsulModule;
import org.secondbase.core.SecondBase;
import org.secondbase.core.SecondBaseException;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.flags.Flag;
import org.secondbase.flags.Flags;
import org.secondbase.logging.JsonLoggerModule;
import org.secondbase.webconsole.HttpWebConsole;
import org.secondbase.webconsole.PrometheusWebConsole;
import org.secondbase.webconsole.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloAll {
    @Flag(name="variable")
    private static String var = "default";
    @Flag(name="counter")
    private static int counter = 1;

    private static final Logger LOG = LoggerFactory.getLogger(HelloAll.class.getName());

    private HelloAll() {}

    public static void main(final String[] args) throws SecondBaseException, IOException {

        final String[] realArgs = {
                // SecondBase settings
                "--service-name=HelloAll",
                "--service-environment=testing",

                // Consul settings
                "--consul-host=localhost:8500",
                "--service-port=8000",
                "--consul-health-check-path=/healthz",
                "--consul-tags=tagone,tagtwo",

                // Logging settings
                "--datacenter=local",

                // Webconsole settings
                "--webconsole-port=8000"
        };

        // Set up json logger module first, since it can define how the other modules do logging.
        final SecondBaseModule jsonLogger = new JsonLoggerModule();

        final Counter mycounter = Counter.build("mycounter", "a counter").register();

        final Widget prometheusWidget = new PrometheusWebConsole();
        final Widget[] widgets = {prometheusWidget};

        final SecondBaseModule consul = new ConsulModule();
        final SecondBaseModule webconsole = new HttpWebConsole(widgets);
        final SecondBaseModule[] modules = {consul, webconsole, jsonLogger};

        final Flags flags = new Flags().loadOpts(HelloAll.class);

        new SecondBase(realArgs, modules, flags);

        mycounter.inc(counter);

        LOG.info(var);
    }
}
