package org.server.frontend.remote;

import java.rmi.RemoteException;

import org.server.core.ShutdownService;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.server.frontend.FrontendServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendMasterInterfaceImpl extends RMIServiceInterfaceImpl<FrontendServer> implements FrontendMasterInterface{
	
	private static final Logger logger = LoggerFactory.getLogger(FrontendMasterInterfaceImpl.class);
	
	private static final long serialVersionUID = 1L;
	
	public FrontendMasterInterfaceImpl(FrontendServer server) throws RemoteException {
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
