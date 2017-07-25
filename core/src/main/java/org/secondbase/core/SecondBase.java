package org.secondbase.core;

import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.flags.Flags;

/**
 * Coordination class for SecondBase modules. Handles Flags parsing and cooperation between modules.
 */
public class SecondBase {


    private Flags flags;

    public SecondBase(final String[] args, final SecondBaseModule[] modules)
            throws SecondBaseException{
        this(args, modules, new Flags());
    }

    public SecondBase(final String[] args, final SecondBaseModule[] modules, final Flags flags)
            throws SecondBaseException {
        this.flags = flags;
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
     * @return Flags
     */
    public Flags getFlags() {
        return flags;
    }
}
