package org.secondbase.core.config;

import org.secondbase.core.SecondBase;

/**
 * Interface SecondBase modules must implement. Enables loading of Flags and
 * setting active SecondBase modules.
 */
public interface SecondBaseModule {

    /**
     * Load a ConfigurableModule. Should load additional flags and handle other
     * initialisation.
     */
    void load(final SecondBase secondBase);
}
