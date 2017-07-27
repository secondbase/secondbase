package org.secondbase.secrets;

/**
 * Thrown by SecretHandler when a secret could not be understood or fetched.
 */
public final class SecretHandlerException extends RuntimeException {
    public SecretHandlerException(final String message, final Exception e) {
        super(message, e);
    }
    public SecretHandlerException(final String message) {
        super(message);
    }
}
