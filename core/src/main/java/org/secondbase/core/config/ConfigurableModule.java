package org.secondbase.core.config;

import org.secondbase.flags.Flags;

/**
 * Implemented by modules that has configurable Flags. SecondBase will call this
 * to add options to the used Flags class.
 */
public interface ConfigurableModule {
    /**
     * Load options for a ConfigurableModule.
     * @param flags The Flags class to load options into
     */
    void loadOpts(final Flags flags);
}
