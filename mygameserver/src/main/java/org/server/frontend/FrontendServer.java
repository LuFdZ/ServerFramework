package org.server.frontend;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.server.backend.core.Schedule;
import org.server.backend.remote.BackendRMIServerInterface;
import org.server.core.AbstractRMIServerClient;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.server.frontend.jmx.Frontend;
import org.server.frontend.jmx.FrontendConnectTable;
import org.server.frontend.mina.CodecFactory;
import org.server.frontend.mina.FrontendServerHandler;
import org.server.frontend.mina.FrontendSocketEventHandler;
import org.server.frontend.remote.FrontendBackendInterface;
import org.server.frontend.remote.FrontendBackendInterfaceImpl;
import org.server.startup.StartupServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendServer extends AbstractRMIServerClient<FrontendServerConfig> {
	
	static final Logger logger = LoggerFactory.getLogger(FrontendServer.class);
	
	// 连接前台服务
	BackendRMIServerInterface _backendServer;
	// 前台服务锁
	final Object _backendLock = new Object();
	// 套接字适配器
	NioSocketAcceptor _acceptor;
	
	// 远程服务对象
	FrontendBackendInterface _remoteServerObject = null;

	// 套接字事件处理器
	final FrontendSocketEventHandler _frontendSocketEventHandler = new FrontendSocketEventHandler(this);
		
	// 前台服务节点编号
	int _nodeId = 0;
	
	// 连接错误提示
	boolean _singleErrorTip = false;
	
	// 定时连接器
	ScheduledFuture<?> timer;
	
	public static void main(String[] args) {
		StartupServer startupServer = new StartupServer(
				"org.server.frontend.FrontendServer");
		startupServer.run();
	}
	
	public FrontendServer() {
		super(new FrontendServerConfig());

		int checkTimeMillis =  getServerConfig().getTestConnectionTimer();
		timer = Schedule.scheduleAtFixedRate(this::tryTestConnection, LocalDateTime.now(),Duration.ofMillis(checkTimeMillis));
	}
	
	/**
	 * 获得节点编号
	 *
	 * @return
	 */
	public int getNodeId() {
		return _nodeId;
	}

	/**
	 * 设置节点编号
	 *
	 * @param _nodeId
	 *            新节点编号
	 */
	public void setNodeId(int _nodeId) {
		this._nodeId = _nodeId;
	}
	
	/**
	 * 获得套接字事件处理器
	 *
	 * @return
	 */
	public FrontendSocketEventHandler getFrontendSocketEventHandler() {
		return _frontendSocketEventHandler;
	}

	@Override
	protected RMIServiceInterfaceImpl<?> createMasterInterfaceImpl() throws RemoteException {
		return null;
	}
	
	@Override
	protected boolean start0(){
		
		boolean success = true;
		// 启动 Mina Core
		success &= startupMinaCore();
		success &= registerMBean("Frontend", new Frontend(this));
		return success;
	} 
	
	/**
	 * 获得前台后台交互远程服务对象
	 *
	 * @return
	 */
	public FrontendBackendInterface getRemoteServerObject() {
		try {
			if (_remoteServerObject == null) {
				_remoteServerObject = new FrontendBackendInterfaceImpl(this);
			}
		} catch (RemoteException ex) {
			logger.error("实例化远程代理对象失败：", ex);
		}
		return _remoteServerObject;
	}

	/**
	 * 获得前台后台交互远程服务对象
	 *
	 * @return
	 */
	protected UnicastRemoteObject createRemoteObject() throws RemoteException {
		return null;
	}

	@Override
	protected String getRemoteObjectName() {
		return null;
	}
	
	/**
	 * 获得套接字接收器
	 *
	 * @return 接收器实例
	 */
	public NioSocketAcceptor getAcceptor() {
		return _acceptor;
	}
	
	/**
	 * 尝试测试与后台服务器的连接
	 */
	public void tryTestConnection() {
		try {
			getBackendServer().ping();
		} catch (Exception e) {
			createBackendServerConnection();
		}
	}
	
	public BackendRMIServerInterface getBackendServer(){
		synchronized (_backendLock) {
			if (_backendServer == null) {
				createBackendServerConnection();
			}
			return _backendServer;
		}
	}
	
	/**
	 * 创建后台服务器连接
	 * 
	 * @return 服务器连接
	 */
	boolean createBackendServerConnection(){
		
		String address = getServerConfig().getBackendServerAddress();
		int port = getServerConfig().getBackendServerPort();
		
		synchronized (_backendLock) {
			try {
				Registry masterRegistry = LocateRegistry.getRegistry(address,port,new SslRMIClientSocketFactory());
				_backendServer = (BackendRMIServerInterface)masterRegistry.lookup("BackendServer");
				_backendServer.ping();
				_backendServer.registerFrontendCore(getRemoteServerObject());
				
				logger.info("自动连接前台服务成功！....");

				// 下次连接错误继续报错一次
				_singleErrorTip = false;
			} catch (NotBoundException | RemoteException e) {
				if (_singleErrorTip) {
					logger.error(" FrontendServer  自动连接前台服务失败 ::({}:{}:{})",address,port,e.getMessage());
					_singleErrorTip = true;
				}
			}
			return false;
		}
	}
	
	/**
	 * 创建 Mina2 Core 服务核心并启动
	 *
	 * @return
	 */
	private boolean startupMinaCore() {
		
		_acceptor = new NioSocketAcceptor();
		
		// 禁用算法
		_acceptor.getSessionConfig().setTcpNoDelay(true);
		
		// 添加过滤链处理项
		_acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CodecFactory()));
		
		// 设置处理器
		_acceptor.setHandler(new FrontendServerHandler(this));
		
		try{
			_acceptor.bind(new InetSocketAddress(getServerConfig()
					.getFrontendPort()));
		}catch(IOException e){
			logger.error("启动 Mina Core 监听失败：", e);
			return false;
		}
		logger.error("Mina Core Startup Success On Port:"
				+ getServerConfig().getFrontendPort());
		return true;
	}
	
	/**
	 * 获得服务连接表
	 *
	 * @return 连接表
	 */
	public List<FrontendConnectTable> getServerConnectTable() {
		List<FrontendConnectTable> list = new LinkedList<>();
		for (IoSession s : getAcceptor().getManagedSessions().values()) {
			list.add(new FrontendConnectTable(s.getId(), s.getWrittenBytes(), s
					.getReadBytes(), s.getWrittenMessages(), s
					.getReadMessages()));
		}
		return list;
	}
}
