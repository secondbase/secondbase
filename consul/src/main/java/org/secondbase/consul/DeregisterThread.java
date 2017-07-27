package org.secondbase.consul;

import com.orbitz.consul.AgentClient;
import java.util.HashMap;
import java.util.Map;

/**
 * Deregister a list of services. To be used for deregistering consul services when
 * shutting down.
 */
final class DeregisterThread extends Thread {
    private final Map<String, AgentClient> services = new HashMap<>();

    /**
     * Record a service id to be deregistered at shutdown
     * @param serviceId the service id to be deregistered
     * @param agentClient the consul client to use for deregistration
     */
    void add(final String serviceId, final AgentClient agentClient) {
        services.put(serviceId, agentClient);
    }

    @Override
    public void run() {
        for (final Map.Entry<String, AgentClient> serviceReg : services.entrySet()) {
            serviceReg.getValue().deregister(serviceReg.getKey());
        }
    }
}
