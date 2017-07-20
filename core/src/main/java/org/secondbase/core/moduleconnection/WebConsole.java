package org.secondbase.core.moduleconnection;

import java.io.IOException;

/**
 * A web server to host SecondBase modules.
 */
public interface WebConsole {
    /**
     * Start the webconsole server.
     * @throws IOException if there's trouble starting it
     */
    void start() throws IOException;

    /**
     * Shut down the webconsole server.
     * @throws IOException if there's trouble starting it
     */
    void shutdown() throws IOException;

    /**
     * Get the port the webconsole is running on.
     * @return the webconsole port
     */
    int getPort();
}
