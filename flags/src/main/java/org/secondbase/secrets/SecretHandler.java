package org.secondbase.secrets;

/**
 * Interface used by classes that can fetch secrets based on command line arguments.
 */
public interface SecretHandler {
    /**
     * Implementation looks through arguments and fetches secrets it recognizes
     * @param args Command line args passed from main method
     * @return Argument list where secrets have been fetched
     * @throws SecretHandlerException When a secret could not be understood or fetched
     */
    String[] fetch(final String[] args) throws SecretHandlerException;
}
