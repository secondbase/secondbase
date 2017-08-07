package com.github.secondbase.webconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrometheusWebConsoleTest {

    @Test
    public void defaultMetricsEndpoint() {
        assertEquals("/metrics", new PrometheusWebConsole().getPath());
    }

    @Test
    public void parseMetrics() throws IOException {
        final Counter counter = Counter
                .build("testcounter", "testdescription")
                .labelNames("one", "two")
                .register();
        counter.labels("1", "2").inc();
        final HttpExchange mockExchange = mock(HttpExchange.class);
        final OutputStream outputStream = new ByteArrayOutputStream();
        when(mockExchange.getResponseBody()).thenReturn(outputStream);
        final Headers headers = new Headers();
        when(mockExchange.getResponseHeaders()).thenReturn(headers);

        new PrometheusWebConsole().getServlet().handle(mockExchange);

        assertEquals(TextFormat.CONTENT_TYPE_004, headers.get("Content-Type").get(0));

        final String response = outputStream.toString();
        assertTrue(response.contains("# HELP testcounter testdescription"));
        assertTrue(response.contains("# TYPE testcounter counter"));
        assertTrue(response.contains("one=\"1\""));
        assertTrue(response.contains("two=\"2\""));
    }
}
