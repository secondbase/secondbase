package com.github.secondbase.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.Registration;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.SecondBaseException;
import com.github.secondbase.core.config.SecondBaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consul module for SecondBase. Handles registration of services in consul.
 */
public final class ConsulModule implements SecondBaseModule {

    private static final Logger LOG = LoggerFactory.getLogger(ConsulModule.class.getName());

    private static final ScheduledExecutorService consulKeepAlive
            = Executors.newSingleThreadScheduledExecutor();
    private static final long keepAliveInitialDelaySec = 0L;
    private static final long keepAlivePeriodSec = 30L;
    static final DeregisterThread deregisterThread = new DeregisterThread();
    private static final AtomicBoolean isDeregisterThreadActive = new AtomicBoolean(false);

    private Consul consulClient;

    @Override
    public void load(final SecondBase secondBase) {
        secondBase.getFlags().loadOpts(ConsulModuleConfiguration.class);
    }

    @Override
    public void init() throws SecondBaseException {
        if (SecondBase.serviceName.isEmpty()) {
            LOG.info("No service name defined. Nothing to register yet.");
            return;
        }
        if (ConsulModuleConfiguration.servicePort == 0) {
            LOG.error("Service port needs to be defined in order to register a service in consul.");
            return;
        }
        if (SecondBase.environment.isEmpty()) {
            LOG.error("Environment needs to be defined in order to register a service in consul.");
            return;
        }
        if (ConsulModuleConfiguration.healthCheckPath.isEmpty()) {
            LOG.error("Health check path needs to be defined in order to register a service in "
                    + "consul.");
            return;
        }
        final String[] tags = (ConsulModuleConfiguration.tags.isEmpty())
                ? new String[]{}
                : ConsulModuleConfiguration.tags.split(",");

        registerServiceInConsul(
                SecondBase.serviceName,
                ConsulModuleConfiguration.servicePort,
                SecondBase.environment,
                ConsulModuleConfiguration.healthCheckPath,
                ConsulModuleConfiguration.healthCheckIntervalSec,
                tags);
    }

    /**
     * Instantiate a ConsulModule.
     */
    public ConsulModule() {}

    /**
     * Instantiate a ConsulModule.
     * @param consulClient custom {@link Consul} client
     */
    public ConsulModule(final Consul consulClient) {
        this.consulClient = consulClient;
    }

    public Consul getConsulClient() {
        if (consulClient == null) {
            consulClient = createConsulClient(ConsulModuleConfiguration.host);
        }
        return consulClient;
    }

    /**
     * Create a consul connection to an agent runnig on localhost on the default port 8500.
     * @return Consul
     */
    public static Consul createLocalhostConsulClient() {
        return ConsulModule.createConsulClient("localhost:8500");
    }

    /**
     * Create a consul connection to a specified Consul server.
     * @param consulServer String with host:port
     * @return Consul
     */
    public static Consul createConsulClient(final String consulServer) {
        final HostAndPort hostAndPort = HostAndPort.fromString(consulServer);
        return Consul.builder().withHostAndPort(hostAndPort).build();
    }

    /**
     * Register the service endpoint with a specific service ID in consul.
     * @param serviceName the name of the service
     * @param serviceId the id of the service
     * @param servicePort the port of the service
     * @param environment the environment of the service (it will always added as a tag in addition)
     * @param healthCheckPath the api path which consul can use for health checks
     * @param healthCheckIntervalSec the interval between consul health checks
     * @param consulCustomTags optionally register more consul tags with the service
     * @throws SecondBaseException if required arguments are missing
     */
    public void registerServiceInConsul(
            final String serviceName,
            final String serviceId,
            final int servicePort,
            final String environment,
            final String healthCheckPath,
            final long healthCheckIntervalSec,
            final String... consulCustomTags) throws SecondBaseException {

        if (healthCheckPath == null) {
            throw new SecondBaseException(
                    "Must provide health check path to register with consul.");
        }

        // Register service
        final String heathCheckPath = "http://localhost:"
                + servicePort
                + ((healthCheckPath.startsWith("/"))
                ? ""
                : "/")
                + healthCheckPath;
        final Registration.RegCheck serviceRegCheck = Registration.RegCheck.http(
                heathCheckPath,
                healthCheckIntervalSec);
        final String[] tagsArray = Arrays.copyOf(consulCustomTags, consulCustomTags.length + 1);
        tagsArray[tagsArray.length - 1] = environment;

        consulKeepAlive.scheduleAtFixedRate(
                createRegisterTask(
                        servicePort,
                        serviceRegCheck,
                        serviceName,
                        serviceId,
                        tagsArray),
                keepAliveInitialDelaySec,
                keepAlivePeriodSec,
                TimeUnit.SECONDS);
    }

    /**
     * Register the service endpoint in consul.
     * @param serviceName the name of the service
     * @param servicePort the port of the service
     * @param environment the environment of the service (it will always added as a tag in addition)
     * @param healthCheckPath the api path which consul can use for health checks
     * @param healthCheckIntervalSec the interval between consul health checks
     * @param consulCustomTags optionally register more consul tags with the service
     * @throws SecondBaseException if required arguments are missing
     */
    public void registerServiceInConsul(
            final String serviceName,
            final int servicePort,
            final String environment,
            final String healthCheckPath,
            final long healthCheckIntervalSec,
            final String... consulCustomTags) throws SecondBaseException {
        final String serviceId = serviceName + "-" + UUID.randomUUID();
        registerServiceInConsul(
                serviceName,
                serviceId,
                servicePort,
                environment,
                healthCheckPath,
                healthCheckIntervalSec,
                consulCustomTags);
    }

    private Runnable createRegisterTask(
            final int webconsolePort,
            final Registration.RegCheck regCheck,
            final String serviceName,
            final String serviceId,
            final String... tags) {
        // Attempt to deregister cleanly on shutdown
        deregisterThread.add(serviceId, getConsulClient().agentClient());
        if (!isDeregisterThreadActive.get()) {
            Runtime.getRuntime().addShutdownHook(deregisterThread);
            isDeregisterThreadActive.set(true);
        }
        LOG.info("Registering service in consul. Service name: " + serviceName
                + ". Service ID: "+ serviceId
                + ". Tags: " + Arrays.toString(tags)
                + ". RegCheck: " + regCheck.toString());
        return () -> {
            try {
                if (!getConsulClient().agentClient().isRegistered(serviceId)){
                    getConsulClient().agentClient().register(
                            webconsolePort,
                            regCheck,
                            serviceName,
                            serviceId,
                            tags);
                }
            } catch (final Exception e) {
                LOG.warn("Unable contact consul, trying to check " + serviceName, e);
            }
        };
    }
}
