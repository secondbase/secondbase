package org.secondbase.webconsole;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.secondbase.core.SecondBase;
import org.secondbase.core.SecondBaseException;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.webconsole.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A webserver for hosting secondbase servlets using Sun's {@link HttpServer}.
 */
public final class HttpWebConsole implements SecondBaseModule {

    private static final Logger LOG = LoggerFactory.getLogger(HttpWebConsole.class);
    private final HttpServer server;
    private final Widget[] widgets;

    /**
     * Basic /healthz endpoint, returning 200 OK.
     */
    private final class HealthzHandler implements HttpHandler {
        private static final String healthyMsg = "Healthy";

        @Override
        public void handle(final HttpExchange t) throws IOException {
            final byte[] response = healthyMsg.getBytes();
            t.sendResponseHeaders(200, response.length);
            final OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    /**
     * Set up the webconsole without widgets using port from {@link WebConsoleConfiguration}.
     * @throws IOException if server can't start on a given port
     */
    public HttpWebConsole() throws IOException {
        this(new Widget[]{});
    }

    /**
     * Set up the webconsole with the given widgets.
     * @param widgets to use
     * @throws IOException if the server can't start on a given port
     */
    public HttpWebConsole(final Widget[] widgets) throws IOException {
        server = HttpServer.create();
        server.createContext("/healthz", new HealthzHandler());
        this.widgets = widgets;
    }

    /**
     * Load WebConsole Flags and set the secondbase webconsole to "this".
     * @param secondBase module coordinator
     */
    @Override
    public void load(final SecondBase secondBase) {
        secondBase.getFlags().loadOpts(WebConsoleConfiguration.class);
    }

    @Override
    public void init() throws SecondBaseException {
        try {
            start();
        } catch (final IOException e) {
            throw new SecondBaseException("Could not start webconsole.", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                    try {
                        shutdown();
                    } catch (final IOException e) {
                        System.err.println("Could not shutdown webconsole: " + e.getMessage());
                    }
            }
        });
    }

    public void start() throws IOException {
        if (WebConsoleConfiguration.port == 0) {
            return;
        }
        final int useSystemDefaultBacklog = 0;
        server.bind(
                new InetSocketAddress(WebConsoleConfiguration.port),
                useSystemDefaultBacklog);
        LOG.info("Starting webconsole on port " + WebConsoleConfiguration.port);
        for (final Widget widget : widgets) {
            LOG.info("Adding webconsole widget " + widget.getPath());
            server.createContext(widget.getPath(), widget.getServlet());
        }
        server.start();
    }

    public void shutdown() throws IOException {
        if (WebConsoleConfiguration.port == 0) {
            return;
        }
        LOG.info("Shutting down webconsole.");
        server.stop(WebConsoleConfiguration.stopTimeout);
    }

    public int getPort() {
        return WebConsoleConfiguration.port;
    }

    /**
     * Get the server implementation.
     * @return HttpServer
     */
    public HttpServer getServer() {
        return server;
    }
}
