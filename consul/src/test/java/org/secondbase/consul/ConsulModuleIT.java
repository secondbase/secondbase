package org.secondbase.consul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.health.Service;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.secondbase.core.SecondBaseException;

public final class ConsulModuleIT {

    private Consul consulClient;
    private ConsulModule consulModule;
    private HttpServer server;
    private final int healthPort = 8000;
    private final String healthEndpoint = "/healthz";

    private final class HealthzHandler implements HttpHandler {
        private static final String healthyMsg = "Healthy";

        @Override
        public void handle(final HttpExchange t) throws IOException {
            final byte [] response = healthyMsg.getBytes();
            t.sendResponseHeaders(200, response.length);
            final OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    @Before
    public void setUp() throws Exception, SecondBaseException {
        consulClient = ConsulModule.createConsulClient(ConsulModuleConfiguration.host);
        consulModule = new ConsulModule(consulClient);
        server = HttpServer.create();
        server.createContext(healthEndpoint, new HealthzHandler());
        final int useSystemDefaultBacklog = 0;
        server.bind(
                new InetSocketAddress(healthPort),
                useSystemDefaultBacklog);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop(0);
    }

    @Test
    public void registerService() throws SecondBaseException, Exception {
        final String serviceName = "myservice";
        final String environment = "testing";
        final long healthCheckIntervalSec = 1L;
        final String tagone = "tagone";
        final String tagtwo = "tagtwo";
        final String[] tags = {tagone, tagtwo};

        // First countdown happens on registration, so we need to count down twice
        final CountDownLatch cdl = new CountDownLatch(2);
        final ServiceHealthCache svHealth = ServiceHealthCache.newCache(consulClient.healthClient(), serviceName);
        svHealth.addListener(map -> {
            cdl.countDown();
        });
        svHealth.start();

        consulModule.registerServiceInConsul(
                serviceName,
                healthPort,
                environment,
                healthEndpoint,
                healthCheckIntervalSec,
                tags);
        // Wait for service check to complete and services to be healthy
        cdl.await(10, TimeUnit.SECONDS);

        // Verify service registry
        final Map<String, Service> services = consulClient.agentClient().getServices();
        boolean serviceRegistered = false;
        for (final Map.Entry<String, Service> serviceEntry : services.entrySet()) {
            if (! serviceEntry.getKey().startsWith(serviceName)) {
                continue;
            }
            serviceRegistered = true;
            assertEquals(serviceName, serviceEntry.getValue().getService());
            assertTrue(serviceEntry.getValue().getTags().contains(tagone));
            assertTrue(serviceEntry.getValue().getTags().contains(tagtwo));
            assertTrue(serviceEntry.getValue().getTags().contains(environment));
            assertEquals(healthPort, serviceEntry.getValue().getPort());
        }
        assertTrue(serviceRegistered);

        // Force deregister thread
        ConsulModule.deregisterThread.run();

        // Verify that service is no longer there
        final Map<String, Service> remainingServices = consulClient.agentClient().getServices();
        for (final Service serviceEntry : remainingServices.values()) {
            assertNotEquals(serviceName, serviceEntry.getService());
        }
    }
}
