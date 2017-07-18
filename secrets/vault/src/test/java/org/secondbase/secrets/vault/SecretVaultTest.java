package org.secondbase.secrets.vault;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;
import org.secondbase.secrets.vault.VaultSecretHandler.SecretPath;

/**
 * Test vault secret recognition and substitution
 */
public class SecretVaultTest {

    @Test
    public void getVaultPath() throws Exception {
        final String secretKey = "secret";
        testPath("secret/testpath", secretKey);
        testPath("path", secretKey);
        testPath("1-23/12/_!#%/$(3", secretKey);

        // Invalid flagged secrets or non-secret variables
        assertFalse(new VaultSecretHandler().getVaultPath("")
                .isPresent());
        assertFalse(new VaultSecretHandler().getVaultPath("valid/vault/path/not/handled/as:secret")
                .isPresent());
        assertFalse(new VaultSecretHandler().getVaultPath("secret:vault:vault/path/without/key")
                .isPresent());
    }

    private void testPath(final String path, final String key) {
        final Optional<SecretPath> vaultPath = new VaultSecretHandler().getVaultPath(
                "secret:vault:" + path + ":" + key);
        assertTrue(vaultPath.isPresent());
        assertEquals(key, vaultPath.get().value);
        assertEquals(path, vaultPath.get().path);
    }
}