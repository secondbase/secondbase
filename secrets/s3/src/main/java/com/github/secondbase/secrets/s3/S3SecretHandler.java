package com.github.secondbase.secrets.s3;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.github.secondbase.secrets.SecretHandler;
import com.github.secondbase.secrets.SecretHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exchanges args on format secret:s3:bucket:key with the content of s3 url.
 */
public final class S3SecretHandler implements SecretHandler {

    private static final Logger LOG = LoggerFactory.getLogger(S3SecretHandler.class);
    private static AWSCredentialsProvider awsCredentialsProvider;

    private final Pattern p = Pattern.compile(".*(secret:s3:(.+):(.+)).*");

    private AmazonS3 s3Client;

    class SecretPath {
        String bucket;
        String key;
        String replaceString;
        SecretPath(final String bucket, final String key, final String replaceString) {
            this.bucket = bucket;
            this.key = key;
            this.replaceString = replaceString;
        }
    }

    @Override
    public String[] fetch(final String[] args) {
        final String[] ret = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            final Optional<SecretPath> s3Path = getS3Path(args[i]);
            if (s3Path.isPresent()) {
                LOG.info("Secret recognised: " + args[i]);
                try {
                    ret[i] = args[i].replaceAll(
                            s3Path.get().replaceString, getS3Value(s3Path.get()));
                    continue;
                } catch (final IOException e) {
                    throw new SecretHandlerException("Could not fetch secret from: " + args[i], e);
                }
            }
            ret[i] = args[i];
        }
        return ret;
    }

    /**
     * Set a custom client config for connecting to AWS S3. Will attempt to use credentials in
     * ~/.aws/credentials (or legacy ~/.aws/config) if not set.
     *
     * @param awsCredentialsProvider the custom s3 client configuration
     */
    public static void setS3CredentialsProvider(
            final AWSCredentialsProvider awsCredentialsProvider) {
        S3SecretHandler.awsCredentialsProvider = awsCredentialsProvider;
    }

    /**
     * Attempt to fetch a secret from S3.
     *
     * @param s3path where to fetch it from
     * @return the content of the file found on S3
     * @throws IOException on problems streaming the content of the file
     * @throws AmazonS3Exception on problems communicating with amazon
     */
    private String getS3Value(final SecretPath s3path) throws IOException, AmazonS3Exception {
        LOG.info("Fetching secret from s3://" + s3path.bucket + "/" + s3path.key);
        if (s3Client == null) {
            if (awsCredentialsProvider != null) {
                s3Client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider)
                        .build();
            } else {
                s3Client = AmazonS3ClientBuilder.standard().build();
            }
        }
        final S3Object s3object
                = s3Client.getObject(new GetObjectRequest(s3path.bucket, s3path.key));
        final BufferedReader reader
                = new BufferedReader(new InputStreamReader(s3object.getObjectContent()));
        final StringBuilder b = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            b.append(line);
        }
        LOG.info("Found secret");
        reader.close();
        return b.toString();
    }

    /**
     * Returns S3 path based on the syntax: secret:s3:bucket:key if found
     */
    protected Optional<SecretPath> getS3Path(final String path) {
        if (path == null || path.isEmpty()) {
            return Optional.empty();
        }
        final Matcher m = p.matcher(path);
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(new SecretPath(m.group(2), m.group(3), m.group(1)));
    }
}
