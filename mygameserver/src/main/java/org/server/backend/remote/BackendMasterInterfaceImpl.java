package org.server.backend.remote;

import java.rmi.RemoteException;

import org.server.backend.BackendServer;
import org.server.core.ShutdownService;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendMasterInterfaceImpl extends RMIServiceInterfaceImpl<BackendServer> implements BackendMasterInterface {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(BackendMasterInterfaceImpl.class);
	
	public BackendMasterInterfaceImpl(BackendServer server) throws RemoteException {
		super(server);
	}

	@Override
	public boolean shutdown() throws RemoteException {
		logger.info("接收管理服务关机命令...");

        ShutdownService service = new ShutdownService(getServer());
        service.shutdown();

        return true;
	}

}
