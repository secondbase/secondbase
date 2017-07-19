package org.secondbase.core;

import java.util.ServiceLoader;
import org.secondbase.core.config.ConfigurableModule;
import org.secondbase.flags.Flags;

/**
 * Manages configuration of submodules.
 */
public class ModuleConfigManager {
    // ServiceLoaded for classes that have configuration Flags
    private final ServiceLoader<ConfigurableModule> configurableModule
            = ServiceLoader.load(ConfigurableModule.class);

    /**
     * Load and configure modules.
     * @param flags Flags instance to load Flagged variables into
     */
    public ModuleConfigManager(final Flags flags) {
        // Load Flags from all modules that have them
        for (final ConfigurableModule module : configurableModule) {
            module.loadOpts(flags);
        }
    }
}
