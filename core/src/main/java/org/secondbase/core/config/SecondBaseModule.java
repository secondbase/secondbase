package org.secondbase.core.config;

import org.secondbase.core.SecondBase;

/**
 * Implemented by SecondBase modules. Enables loading of Flags and setting active SecondBase
 * modules.
 */
public interface SecondBaseModule {
    /**
     * Load a ConfigurableModule.
     */
    void load(final SecondBase secondBase);
}
