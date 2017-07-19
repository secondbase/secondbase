package org.secondbase.core;

import java.util.logging.Logger;
import org.secondbase.flags.Flags;

/**
 * Coordination class for SecondBase modules. Handles Flags parsing and cooperation between modules.
 */
public class SecondBase {

    private Flags flags;

    /**
     * Set up SecondBase with default values.
     * @param args command line arguments
     */
    public SecondBase(final String[] args) {
        init(args, new Flags());
    }

    /**
     * Set up SecondBase.
     * @param args command line arguments
     * @param flags preloaded Flags class
     */
    public SecondBase(final String[] args, final Flags flags) {
        init(args, flags);
    }

    private void init(final String[] args, final Flags flags) {
        this.flags = flags;

        new ModuleConfigManager(flags);

        flags.parse(args);

        if (flags.helpFlagged()) {
            flags.printHelp(System.out);
            System.exit(0);
        }
        if (flags.versionFlagged()) {
            flags.printVersion(System.out);
            System.exit(0);
        }
    }

    /**
     * Get Flags instance used by Base.
     * @return Flags
     */
    public Flags getFlags() {
        return flags;
    }
}
