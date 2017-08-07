package com.github.secondbase.secrets.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.github.secondbase.secrets.SecretHandler;
import com.github.secondbase.secrets.SecretHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exchanges args on format secret:vault:path/to/data:key with content from vault.
 */
public final class VaultSecretHandler implements SecretHandler {

    private static final Logger LOG = LoggerFactory.getLogger(VaultSecretHandler.class);
    private static VaultConfig vaultConfig;
    private final Pattern p = Pattern.compile("(secret:vault:(.*):(.*))");

    class SecretPath {
        String path;
        String value;
        String replaceString;
        SecretPath(final String path, final String value, final String replaceString) {
            this.path = path;
            this.value = value;
            this.replaceString = replaceString;
        }
    }

    @Override
    public String[] fetch(final String[] args) {
        final String[] ret = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            final Optional<SecretPath> vaultPath = getVaultPath(args[i]);
            if (vaultPath.isPresent()) {
                LOG.info("Secret recognised: " + args[i]);
                try {
                    ret[i] = args[i].replaceAll(
                            vaultPath.get().replaceString,
                            getVaultSecret(vaultPath.get()));
                    continue;
                } catch (final VaultException e) {
                    throw new SecretHandlerException("Could not fetch secret from: " + args[i], e);
                }
            }
            ret[i] = args[i];
        }
        return ret;
    }

    /**
     * Provide a custom config for Vault. If not provided, vault will attempt to find these values
     * from system environment:
     *
     * VAULT_ADDR
     * VAULT_TOKEN
     * VAULT_OPEN_TIMEOUT
     * VAULT_READ_TIMEOUT
     * VAULT_SSL_CERT
     * VAULT_SSL_VERIFY
     *
     * @param vaultConfig custom vault configuration
     */
    public static void setVaultConfig(final VaultConfig vaultConfig) {
        VaultSecretHandler.vaultConfig = vaultConfig;
    }

    /**
     * Returns vault path and key based on the syntax: secret:vault:path/to/data:key
     * @param path path to secret
     * @return a SecretPath if the path is a valid vault path
     */
    protected Optional<SecretPath> getVaultPath(final String path) {
        if (path == null || path.isEmpty()) {
            return Optional.empty();
        }
        final Matcher m = p.matcher(path);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(new SecretPath(m.group(2), m.group(3), m.group(1)));
    }

    /**
     * Get a value from vault based on a VaultPath
     *
     * @param vaultPath the details of where to find the value
     * @return the value found in vault
     * @throws VaultException if there were problems getting the value
     */
    private String getVaultSecret(final SecretPath vaultPath) throws VaultException {
        LOG.info("Fetching secret from Vault");
        final Vault vault = new Vault(
                (vaultConfig == null) ? new VaultConfig().build() : vaultConfig);
        final String secret = vault
                .logical()
                .read(vaultPath.path)
                .getData()
                .get(vaultPath.value);
        LOG.info("Found secret");
        return secret;
    }

}
