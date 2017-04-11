package org.server.master.remote;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;

import org.server.core.IPAddress;
import org.server.core.remote.RMIServiceInterface;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.server.master.ManagedServerInformationNode;
import org.server.master.MasterServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterRMIServerInterfaceImpl extends RMIServiceInterfaceImpl<MasterServer> implements MasterRMIServerInterface {

	private static final long serialVersionUID = 1137310333137030754L;
	private static final Logger logger = LoggerFactory.getLogger(MasterRMIServerInterfaceImpl.class);
	
	public MasterRMIServerInterfaceImpl(MasterServer server) throws RemoteException {
		super(server);
	}

	@Override
	public boolean registerServer(String key, RMIServiceInterface server) throws RemoteException {
		for (ManagedServerInformationNode node : getServer().getServerConfig().getManagedServers().values()) {
			if (node.getServerKey().equals(key)) {
				try {
					// 注册服务
					node.setRemoteService(server);
					node.setServerRMIAddress(server.getServerRMIAddress());
					// 输出日志
					logger.info(
                            String.format("服务节点 [%s] 上线.服务地址为 [%s] .",
                                    node.getServerType(),
                                    RemoteServer.getClientHost()));
                    return true;
				} catch (ServerNotActiveException ex) {
					logger.error("尝试注册服务失败：", ex);
				}
			}
		}
		return false;
	}

	@Override
	public IPAddress getRMIAddressByKey(String key) throws RemoteException {
		for (ManagedServerInformationNode node
                : getServer().
                getServerConfig().
                getManagedServers().
                values()) {
            if (node.getServerKey().equals(key)) {
                return node.getServerRMIAddress();
            }
        }
        return null;
	}
	
}
