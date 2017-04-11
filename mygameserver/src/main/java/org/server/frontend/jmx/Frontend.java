package org.server.frontend.jmx;

import org.server.frontend.FrontendServer;

public class Frontend implements FrontendMXBean{

	private FrontendServer server;
	
	public Frontend(FrontendServer server) {
        this.server = server;
    }
	
	@Override
	public String getServerName() {
		return server.getServerConfig().getServerKey();
	}

	@Override
	public FrontendConnectTable[] getConnectTable() {
		return server.getServerConnectTable().toArray(new FrontendConnectTable[0]);
	}

}
