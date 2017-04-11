package org.server.backend;

import org.server.startup.StartupServer;

public class ConsoleStartup {
	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "keys\\denny.keystore");
        System.setProperty("javax.net.ssl.keyStore", "keys\\denny.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "awfva21.");
        System.setProperty("javax.net.ssl.keyStorePassword", "awfva21.");

        StartupServer startupServer = new StartupServer("org.server.backend.BackendServer");
        startupServer.run();
    }
}
