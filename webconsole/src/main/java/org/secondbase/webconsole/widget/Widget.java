package org.secondbase.webconsole.widget;

import com.sun.net.httpserver.HttpHandler;

/**
 * Interface which WebConsole widgets have to implement in order to be hosted by the WebConsole.
 */
public interface Widget {
    String getPath();
    HttpHandler getServlet();
}
