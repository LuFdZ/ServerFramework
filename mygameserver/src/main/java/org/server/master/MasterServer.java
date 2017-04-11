package org.server.master;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.server.core.AbstractRMIServerClient;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.server.master.remote.MasterRMIServerInterfaceImpl;
import org.server.startup.StartupServer;

public class MasterServer extends AbstractRMIServerClient<MasterServerConfig> {

	public MasterServer() {
		super(new MasterServerConfig());
	}
	
	public static void main(String[] args) {
		StartupServer startupServer = new StartupServer("org.server.master.MasterServer");
		startupServer.run();
	}
	
	@Override
	protected boolean stop0() {
		for (ManagedServerInformationNode node :getServerConfig().getManagedServers().values()) {
			node.clearService();
		}
		return true;
	};

	@Override
	protected RMIServiceInterfaceImpl<?> createMasterInterfaceImpl() throws RemoteException {
		return null;
	}

	@Override
	protected MasterRMIServerInterfaceImpl createRemoteObject() throws RemoteException {
		return new MasterRMIServerInterfaceImpl(this);
	}

	@Override
	protected String getRemoteObjectName() {
		return "MasterServer";
	}

}
