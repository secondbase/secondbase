package org.secondbase.secrets;

import java.io.IOException;

/**
 * Thrown by SecretHandler when a secret could not be understood or fetched.
 */
public class SecretHandlerException extends RuntimeException {
    public SecretHandlerException(final String message, final Exception e) {
        super(message, e);
    }
    public SecretHandlerException(final String message) {
        super(message);
    }
}
