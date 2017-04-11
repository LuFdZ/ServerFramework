package org.server.frontend.remote;

import java.net.SocketAddress;
import java.rmi.RemoteException;

import org.server.core.io.SessionMessage;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.server.frontend.FrontendServer;

public class FrontendBackendInterfaceImpl extends RMIServiceInterfaceImpl<FrontendServer> implements FrontendBackendInterface{

	public FrontendBackendInterfaceImpl(FrontendServer server) throws RemoteException {
		super(server);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1804735434478541407L;

	@Override
	public int getId() throws RemoteException {
		return getServer().getNodeId();
	}

	@Override
	public void setId(int id) throws RemoteException {
		getServer().setNodeId(id);
		
	}

	@Override
	public SocketAddress getAddress(long sessionId) throws RemoteException {
		return getServer().getFrontendSocketEventHandler().getAddress(sessionId);
	}

	@Override
	public void write(long sessionId, SessionMessage message) throws RemoteException {
		
		getServer().getFrontendSocketEventHandler().write(sessionId, message);
		
	}

}
