package org.secondbase.flags;

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
    private Flags flags;
    private TestClass testClass;

    private class TestClass {
        @Flag(name="teststring")
        String testString;
    }

    @Before
    public void setup() throws VaultException {
        final VaultConfig vaultConfig = new VaultConfig().build();
        vault = new Vault(vaultConfig);
        testFolder = UUID.randomUUID().toString();
        testClass = new TestClass();
        flags = new Flags()
                .setVaultConfig(vaultConfig)
                .loadOpts(testClass);
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

        flags.parse(new String[]{
                "--teststring",
                "secret:vault:" + path + ":" + secretKey
        });
        assertEquals(secret, testClass.testString);
        flags.parse(new String[]{
                "--teststring",
                "secret:vault:" + path + ":" + anotherSecretKey
        });
        assertEquals(anotherSecret, testClass.testString);
    }
}
