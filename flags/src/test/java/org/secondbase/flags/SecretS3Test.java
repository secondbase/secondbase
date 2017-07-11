package org.secondbase.flags;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author acidmoose
 */
public class SecretS3Test {
    private final String bucket = "testbucket";
    private final String subfolder = "testfolder";

    @Test
    public void getS3Path() throws Exception {
        final String secret = "secret";
        final SecretPath s3Path = new Flags().getS3Path(
                "secret:s3://" + bucket + "/" + subfolder + "/" + secret);
        assertNotNull(s3Path);
        assertEquals(subfolder + "/" + secret, s3Path.getKey());
        assertEquals(bucket, s3Path.getPath());

        // Invalid flagged secrets or non-secret variables
        assertNull(new Flags().getS3Path(""));
        assertNull(new Flags().getS3Path("s3://valid/s3/path/not/handled/as/secret"));
        assertNull(new Flags().getS3Path("secret:s3:/invalid/s3/path"));
        assertNull(new Flags().getS3Path("secret:s3:invalid/s3/path"));
    }

}