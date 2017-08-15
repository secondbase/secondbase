package com.github.secondbase.example.main;

import com.sun.net.httpserver.HttpServer;
import io.prometheus.client.Counter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.github.secondbase.consul.ConsulModule;
import com.github.secondbase.consul.registration.ConsulRegistrationMetricsWebConsole;
import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.SecondBaseException;
import com.github.secondbase.core.config.SecondBaseModule;
import com.github.secondbase.flags.Flag;
import com.github.secondbase.flags.Flags;
import com.github.secondbase.logging.JsonLoggerModule;
import com.github.secondbase.webconsole.HttpWebConsole;
import com.github.secondbase.webconsole.PrometheusWebConsole;
import com.github.secondbase.webconsole.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloAll {
    @Flag(name="variable")
    private static String var = "default";
    @Flag(name="counter")
    private static int counter = 1;

    private static final Logger log = LoggerFactory.getLogger(HelloAll.class.getName());
    private static final Counter mycounter = Counter.build("mycounter", "a counter").register();

    private HelloAll() {}

    /**
     * Start HelloAll service.
     */
    public static void startHelloAllService() throws IOException {
        mycounter.inc(counter);
        log.info(var);

        // Start a basic http server with a health check and a service endpoint.
        final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/healthcheck", httpExchange -> {
            final byte[] response = "This is my service health check".getBytes();
            httpExchange.sendResponseHeaders(200, response.length);
            final OutputStream os = httpExchange.getResponseBody();
            os.write(response);
            os.close();
        });
        server.createContext("/", httpExchange -> {
            final byte[] response = "This is my service response".getBytes();
            httpExchange.sendResponseHeaders(200, response.length);
            final OutputStream os = httpExchange.getResponseBody();
            os.write(response);
            os.close();
        });
        server.start();
    }

    public static void main(final String[] args) throws SecondBaseException, IOException {
        final String[] realArgs = {
                // SecondBase settings
                "--service-name=HelloAll",
                "--service-environment=testing",

                // Consul settings (register HelloAll service)
                "--enable-consul",
                "--consul-host=localhost:8500",
                "--service-port=8000",
                "--consul-health-check-path=/healthcheck",
                "--consul-tags=tagone,tagtwo",

                // Logging settings
                "--datacenter=local",

                // Webconsole settings
                "--webconsole-port=8001",

                // HelloAll settings
                "--variable=Hello, World!",
                "--counter=42"
        };

        final SecondBaseModule jsonLogger = new JsonLoggerModule();

        final PrometheusWebConsole prometheusWidget = new PrometheusWebConsole();
        final Widget[] widgets = {prometheusWidget};
        final HttpWebConsole webConsole = new HttpWebConsole(widgets);

        final ConsulModule consul = new ConsulModule();
        final ConsulRegistrationMetricsWebConsole registerMetrics
                = new ConsulRegistrationMetricsWebConsole(webConsole, consul);

        final SecondBaseModule[] modules = {
                jsonLogger, // Put jsonLogger first, since it can define how the other modules log.
                consul,
                prometheusWidget,
                webConsole,
                registerMetrics};

        final Flags flags = new Flags().loadOpts(HelloAll.class);

        new SecondBase(realArgs, modules, flags);

        startHelloAllService();
    }
}
