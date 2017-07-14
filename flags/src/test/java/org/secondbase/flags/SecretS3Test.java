package org.secondbase.flags;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author acidmoose
 */
public class SecretS3Test {

    @Test
    public void getS3Path() throws Exception {
        final String bucket = "testbucket";
        final String subfolder = "testfolder";
        final String subfolderWithSubPaths = "testfolder/subfolder/subsubfolder";
        final String secret = "secret";
        {
            final SecretPath s3Path = new Flags().getS3Path(
                    "secret:s3://" + bucket + "/" + subfolder + "/" + secret);
            assertNotNull(s3Path);
            assertEquals(subfolder + "/" + secret, s3Path.getKey());
            assertEquals(bucket, s3Path.getPath());
        }
        {
            final SecretPath s3Path = new Flags().getS3Path(
                    "secret:s3://" + bucket + "/" + subfolderWithSubPaths + "/" + secret);
            assertNotNull(s3Path);
            assertEquals(subfolderWithSubPaths + "/" + secret, s3Path.getKey());
            assertEquals(bucket, s3Path.getPath());
        }

        // Invalid flagged secrets or non-secret variables
        assertNull(new Flags().getS3Path(""));
        assertNull(new Flags().getS3Path("s3://valid/s3/path/not/handled/as/secret"));
        assertNull(new Flags().getS3Path("secret:s3:/invalid/s3/path"));
        assertNull(new Flags().getS3Path("secret:s3:invalid/s3/path"));
    }

}