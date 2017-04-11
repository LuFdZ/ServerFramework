package org.server.core;

import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.server.core.remote.RMIServiceInterface;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.server.master.remote.MasterRMIServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRMIServerClient<C extends AbstractServerConfig> extends AbstractStandardServer<C> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRMIServerClient.class);

	// client var
	private MasterRMIServerInterface _masterServer;
	private RMIServiceInterface _masterClientObj;

	// server var
	private UnicastRemoteObject _remoteObject;
	private Registry _registry;

	/**
	 * 构造一个RMI服务类
	 *
	 * @param _serverConfig
	 *            服务器配置对象
	 */
	public AbstractRMIServerClient(C _serverConfig) {
		super(_serverConfig);
	}

	@Override
	protected final boolean beforeStart() {
		boolean success = true;
		try {
			// config IP address To Master Address
			if (getServerConfig().getMasterAddress() != null) {
				System.setProperty("java.rmi.server.hostname", getServerConfig().getMasterAddress());
			}
			_masterClientObj = createMasterInterfaceImpl();
			if (_masterClientObj !=null) {
				success &= createRMIClient();//success = success && createRMIClient();
			}
			_remoteObject = createRemoteObject();
			if (_remoteObject!=null) {
				success &= createRMIServer();
			}
		} catch (RemoteException ex) {
			logger.error("处理 RMI 配置事件失败：", ex);
			success = false;
		}
		return success;
	}
	
	@Override
	protected final boolean afterStop(){
		try {
			if (_remoteObject !=null) {
				UnicastRemoteObject.unexportObject(_remoteObject, true);
				_remoteObject = null;
			}
			
			if (_registry != null) {
				UnicastRemoteObject.unexportObject(_registry, true);
				_registry = null;
			}
			
		} catch (NoSuchObjectException e) {
			logger.error("解除绑定RMI异常：", e);
		}
		return true;
	}
	
	/**
	 * 创建管理接口实体通信对象
	 *
	 * @return
	 * @throws java.rmi.RemoteException
	 */	
	protected abstract RMIServiceInterfaceImpl<?> createMasterInterfaceImpl() throws RemoteException;
	
	private boolean createRMIClient(){
		// ready address and port
		String address = getServerConfig().getMasterAddress();
		int port = getServerConfig().getMasterPort();
		
		// try to connect
		try {
			logger.info("正在创建 RMI 客户端....");
			Registry masterRegistry = LocateRegistry.getRegistry(address,port,new SslRMIClientSocketFactory());
			_masterServer = (MasterRMIServerInterface)masterRegistry.lookup("MasterServer");
			_masterServer.ping();
			boolean success = _masterServer.registerServer(getServerConfig().getServerKey(), _masterClientObj);
			if (success) {
				logger.info("已经成功注册上管理服务...");
				return true;
			}else{
				logger.error("注册服务失败！...");
			}
		} catch (ConnectException connectException) {
			logger.error(" 尝试连接到     MasterServer  连接失败  :: ({}:{})[{}]", address,
					port, connectException.getMessage());
		} catch (RemoteException | NotBoundException ex) {
			logger.error("注册服务失败：", ex);
		}
		return false;
	}
	
	/**
	 * 创建RMI服务器
	 *
	 * @return 创建是否成功
	 */
	private boolean createRMIServer(){
		try {
			logger.info("正在创建 RMI 服务端....");
			_registry = LocateRegistry.createRegistry(getServerConfig().getRMIAddress().isValid()?getServerConfig().getRMIAddress().getPort():getServerConfig().getMasterPort(), new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
			_registry.rebind(getRemoteObjectName(), _remoteObject);
		} catch (RemoteException ex) {
			logger.error("绑定RMI异常：", ex);
			return false;
		}
		return true;
	}
	
	/**
	 * 创建远程代理对象
	 *
	 * @return
	 * @throws RemoteException
	 */	
	protected abstract UnicastRemoteObject createRemoteObject() throws RemoteException;

	/**
	 * 获得远程代理对象名称
	 *
	 * @return 名称
	 */
	protected abstract String getRemoteObjectName();
}
