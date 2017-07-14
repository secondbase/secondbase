package org.secondbase.flags;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author acidmoose
 */
public class SecretVaultTest {

    @Test
    public void getVaultPath() throws Exception {
        final String secretKey = "secret";
        testPath("secret/testpath", secretKey);
        testPath("path", secretKey);
        testPath("1-23/12/_!#%/$(3", secretKey);

        // Invalid flagged secrets or non-secret variables
        assertNull(new Flags().getVaultPath(""));
        assertNull(new Flags().getVaultPath("valid/vault/path/not/handled/as:secret"));
        assertNull(new Flags().getVaultPath("secret:vault:vault/path/without/key"));
    }

    private void testPath(final String path, final String key) {
        final SecretPath s3Path = new Flags().getVaultPath(
                "secret:vault:" + path + ":" + key);
        assertNotNull(s3Path);
        assertEquals(key, s3Path.getKey());
        assertEquals(path, s3Path.getPath());
    }
}
