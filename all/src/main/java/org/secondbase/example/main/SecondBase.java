package org.secondbase.example.main;

import java.io.IOException;
import org.secondbase.consul.ConsulModule;
import org.secondbase.core.SecondBaseException;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.flags.Flags;
import org.secondbase.logging.JsonLoggerModule;
import org.secondbase.secrets.SecretHandler;
import org.secondbase.secrets.s3.S3SecretHandler;
import org.secondbase.secrets.vault.VaultSecretHandler;
import org.secondbase.webconsole.HttpWebConsole;
import org.secondbase.webconsole.PrometheusWebConsole;
import org.secondbase.webconsole.widget.Widget;

/**
 * SecondBase implementation which will set up all submodules
 */
public class SecondBase {
    /**
     * Initiates {@link org.secondbase.core.SecondBase} with all {@link SecondBaseModule} available.
     * @param args command line arguments to be parsed
     * @throws IOException if WebConsole fails to start
     * @throws SecondBaseException if {@link org.secondbase.core.SecondBase} fails to start
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
     * Initiates {@link org.secondbase.core.SecondBase} with all {@link SecondBaseModule} available.
     * @param args command line arguments to be parsed
     * @param flags custom {@link Flags}. NB! Needs custom setup of {@link SecretHandler[]}
     * @throws IOException if WebConsole fails to start
     * @throws SecondBaseException if {@link org.secondbase.core.SecondBase} fails to start
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

        new org.secondbase.core.SecondBase(args, modules, flags);

        //TODO: Register webconsole in consul.
    }
}
