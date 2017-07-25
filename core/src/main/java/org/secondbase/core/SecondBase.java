package org.secondbase.core;

import java.util.ServiceLoader;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.core.moduleconnection.WebConsole;
import org.secondbase.flags.Flags;

/**
 * Coordination class for SecondBase modules. Handles Flags parsing and cooperation between modules.
 */
public class SecondBase {

    private Flags flags;
    private WebConsole webConsole;

    private final ServiceLoader<SecondBaseModule> configurableModule
            = ServiceLoader.load(SecondBaseModule.class);

    /**
     * Set up SecondBase with default values.
     * @param args command line arguments
     */
    public SecondBase(final String[] args) throws SecondBaseException {
        init(args, new Flags());
    }

    /**
     * Set up SecondBase.
     * @param args command line arguments
     * @param flags preloaded Flags class
     */
    public SecondBase(final String[] args, final Flags flags) throws SecondBaseException {
        init(args, flags);
    }

    private void init(final String[] args, final Flags flags) throws SecondBaseException {
        this.flags = flags;

        for (final SecondBaseModule module : configurableModule) {
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

        for (final SecondBaseModule module : configurableModule) {
            module.init();
        }
    }

    /**
     * Get Flags instance used by Base.
     * @return Flags
     */
    public Flags getFlags() {
        return flags;
    }

    /**
     * Set the webconsole implementation.
     */
    public void setWebConsole(final WebConsole webConsole) {
        this.webConsole = webConsole;
    }
}
