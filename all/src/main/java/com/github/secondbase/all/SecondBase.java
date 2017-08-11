package com.github.secondbase.all;

import java.io.IOException;
import com.github.secondbase.consul.ConsulModule;
import com.github.secondbase.core.SecondBaseException;
import com.github.secondbase.core.config.SecondBaseModule;
import com.github.secondbase.flags.Flags;
import com.github.secondbase.logging.JsonLoggerModule;
import com.github.secondbase.secrets.SecretHandler;
import com.github.secondbase.secrets.s3.S3SecretHandler;
import com.github.secondbase.secrets.vault.VaultSecretHandler;
import com.github.secondbase.webconsole.HttpWebConsole;
import com.github.secondbase.webconsole.PrometheusWebConsole;
import com.github.secondbase.webconsole.widget.Widget;

/**
 * SecondBase implementation which will set up all submodules
 */
public class SecondBase {
    /**
     * Initiates {@link com.github.secondbase.core.SecondBase} with all {@link SecondBaseModule}
     * available.
     * @param args command line arguments to be parsed
     * @throws IOException if WebConsole fails to start
     * @throws SecondBaseException if {@link com.github.secondbase.core.SecondBase} fails to start
     */
    public SecondBase(final String[] args) throws IOException, SecondBaseException {
        new SecondBase(
                args,
                new Flags(
                        new SecretHandler[]{
                                new S3SecretHandler(),
                                new VaultSecretHandler()}));
    }

    /**
     * Initiates {@link com.github.secondbase.core.SecondBase} with all {@link SecondBaseModule}
     * available.
     * @param args command line arguments to be parsed
     * @param flags custom {@link Flags}. NB! Needs custom setup of {@link SecretHandler}
     * @throws IOException if WebConsole fails to start
     * @throws SecondBaseException if {@link com.github.secondbase.core.SecondBase} fails to start
     */
    public SecondBase(
            final String[] args,
            final Flags flags)
            throws IOException, SecondBaseException {
        final SecondBaseModule jsonLogger = new JsonLoggerModule();

        final Widget prometheusWidget = new PrometheusWebConsole();
        final Widget[] widgets = {prometheusWidget};

        final ConsulModule consul = new ConsulModule();
        final SecondBaseModule webconsole = new HttpWebConsole(widgets);
        final SecondBaseModule[] modules = {consul, webconsole, jsonLogger};

        new com.github.secondbase.core.SecondBase(args, modules, flags);

        //TODO: Register webconsole in consul.
    }
}
