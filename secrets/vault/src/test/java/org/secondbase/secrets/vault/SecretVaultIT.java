package org.secondbase.secrets.vault;

import static org.junit.Assert.*;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

/**
 * Requires environment variables:
 *
 * VAULT_ADDR - The uri of the vault instance
 * VAULT_TOKEN - The token used to write and read secrets
 *
 * @author acidmoose
 */
public class SecretVaultIT {
    private Vault vault;
    private String testFolder;

    @Before
    public void setup() throws VaultException {
        final VaultConfig vaultConfig = new VaultConfig().build();
        vault = new Vault(vaultConfig);
        testFolder = UUID.randomUUID().toString();
        VaultSecretHandler.setVaultConfig(vaultConfig);
    }

    @Test
    public void getSecret() throws VaultException {
        final String secretKey = "secretkey";
        final String secret = "secret value";
        final String anotherSecretKey = "secretkey2";
        final String anotherSecret = "another secret value";
        final String path = "secret/" + testFolder;

        // Put some secrets in vault first
        final Map<String, String> secrets = new HashMap<>();
        secrets.put(secretKey, secret);
        secrets.put(anotherSecretKey, anotherSecret);
        vault.logical().write(path, secrets);

        assertEquals(
                secret,
                new VaultSecretHandler().fetch(new String[]{
                        "--teststring",
                        "secret:vault:" + path + ":" + secretKey})[1]);

        final String[] twoSecrets = new String[]{
                "--teststring",
                "secret:vault:" + path + ":" + secretKey,
                "--teststring2",
                "secret:vault:" + path + ":" + anotherSecretKey
        };
        assertEquals(
                secret,
                new VaultSecretHandler().fetch(twoSecrets)[1]);
        assertEquals(
                anotherSecret,
                new VaultSecretHandler().fetch(twoSecrets)[3]);
    }
}
