package org.secondbase.core;

import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.flags.Flag;
import org.secondbase.flags.Flags;

/**
 * Coordination class for SecondBase modules. Handles Flags parsing and cooperation between modules.
 */
public class SecondBase {

    @Flag(
            name = "service-name",
            description = "Name of service"
    )
    public static String serviceName = "";

    @Flag(
            name = "service-environment",
            description = "The environment the service runs in"
    )
    public static String environment = "testing";

    private Flags flags;

    public SecondBase(final String[] args, final SecondBaseModule[] modules)
            throws SecondBaseException{
        this(args, modules, new Flags());
    }

    public SecondBase(
            final String[] args,
            final SecondBaseModule[] modules,
            final Flags customFlags)
            throws SecondBaseException {
        flags = customFlags;
        flags.loadOpts(SecondBase.class);
        for(final SecondBaseModule module : modules) {
            module.load(this);
        }
        flags.parse(args);

        if (flags.helpFlagged()) {
            flags.printHelp(System.out);
            System.exit(0);
        }
        if (flags.versionFlagged()) {
            flags.printVersion(System.out);
            System.exit(0);
        }

        for (final SecondBaseModule module : modules) {
            module.init();
        }
    }

    /**
     * Get Flags instance used by Base.
     * @return {@link Flags} object
     */
    public Flags getFlags() {
        return flags;
    }
}
