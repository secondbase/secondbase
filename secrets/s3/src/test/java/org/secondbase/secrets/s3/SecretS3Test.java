package org.secondbase.secrets.s3;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;
import org.secondbase.secrets.s3.S3SecretHandler.SecretPath;

/**
 * @author acidmoose
 */
public class SecretS3Test {

    @Test
    public void getS3Path() throws Exception {
        final String bucket = "testbucket";
        final String key = "secret";
        final String longKey = "testfolder/subfolder/subsubfolder/secret";
        {
            final Optional<SecretPath> s3Path = new S3SecretHandler().getS3Path(
                    "secret:s3:" + bucket + ":" + key);
            assertTrue(s3Path.isPresent());
            assertEquals(key, s3Path.get().key);
            assertEquals(bucket, s3Path.get().bucket);
        }
        {
            final Optional<SecretPath> s3Path = new S3SecretHandler().getS3Path(
                    "--secret=secret:s3:" + bucket + ":" + longKey);
            assertTrue(s3Path.isPresent());
            assertEquals(longKey, s3Path.get().key);
            assertEquals(bucket, s3Path.get().bucket);
        }

        // Invalid flagged secrets or non-secret variables
        assertFalse(new S3SecretHandler().getS3Path("").isPresent());
        assertFalse(new S3SecretHandler().getS3Path("s3:valid:but/not/secret").isPresent());
        assertFalse(new S3SecretHandler().getS3Path("secret:s3:missingkey:").isPresent());
        assertFalse(new S3SecretHandler().getS3Path("secret:s3:missingkey").isPresent());
        assertFalse(new S3SecretHandler().getS3Path("secret:s3::missingbucket").isPresent());
    }

}