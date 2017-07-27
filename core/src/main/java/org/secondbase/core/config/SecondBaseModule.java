package org.secondbase.core.config;

import org.secondbase.core.SecondBase;
import org.secondbase.core.SecondBaseException;

/**
 * Interface SecondBase modules must implement. Enables loading of Flags and
 * setting active SecondBase modules.
 */
public interface SecondBaseModule {

    /**
     * Load a ConfigurableModule. Should load additional flags and handle other
     * initialisation.
     * @param secondBase object to load flags into
     */
    void load(final SecondBase secondBase);

    /**
     * This method will be called after flags are parsed. Initialisation code
     * that depends on supplied flags should go here.
     * @throws SecondBaseException if errors occur during initialisation
     */
    void init() throws SecondBaseException;
}
