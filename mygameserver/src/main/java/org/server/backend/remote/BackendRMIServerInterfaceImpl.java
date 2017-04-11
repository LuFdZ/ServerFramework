package org.server.backend.remote;

import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.server.backend.BackendServer;
import org.server.backend.io.IMessageProcess;
import org.server.backend.io.Transport;
import org.server.backend.session.BackendSession;
import org.server.backend.session.GameSessionProvider;
import org.server.core.io.SessionIdleType;
import org.server.core.io.SessionMessage;
import org.server.core.remote.RMIServiceInterfaceImpl;
import org.server.core.remote.RemoteObjectAction;
import org.server.core.remote.RemoteObjectFunc;
import org.server.frontend.remote.FrontendBackendInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendRMIServerInterfaceImpl extends RMIServiceInterfaceImpl<BackendServer> implements BackendRMIServerInterface {

	private static final long serialVersionUID = 1L;
	
	static final Logger logger = LoggerFactory.getLogger(BackendRMIServerInterfaceImpl.class);
	
	/**
	 * 服务集合
	 */
	final Map<Integer, FrontendBackendInterface> _frontendServer = new HashMap<>();
	final AtomicInteger _idAtomic = new AtomicInteger();

	IMessageProcess handleComponent;
	
	public BackendRMIServerInterfaceImpl(BackendServer server) throws RemoteException {
		super(server);
	}
	
	public IMessageProcess getHandleComponent() {
		return handleComponent;
	}

	public void setHandleComponent(IMessageProcess handleComponent) {
		this.handleComponent = handleComponent;
	}

	@Override
	public void registerFrontendCore(FrontendBackendInterface service) throws RemoteException {
		try {
			// 测试通讯
			service.ping();
			// 设定编号
			service.setId(_idAtomic.incrementAndGet());
			// 缓存集合
			synchronized (_frontendServer) {
				_frontendServer.put(service.getId(), service);
			}
			
		} catch (RemoteException e) {
			logger.error("远程通信对象无效：", e);
		}
		
	}

	@Override
	public void onSessionCreated(long id, int nodeId) throws RemoteException {
	}

	@Override
	public void onSessionOpend(long id, int nodeId) throws RemoteException {
	}

	@Override
	public void onSessionClosed(long id, int nodeId) throws RemoteException {
		GameSessionProvider.getInstance().dispose(new BackendSession(nodeId, id));
	}

	@Override
	public void onSessionIdle(long id, SessionIdleType status, int nodeId) throws RemoteException {
	}

	@Override
	public void onSessionReceived(long id, SessionMessage data, int nodeId) throws RemoteException {
		if (handleComponent != null){
			handleComponent.fireMessageReceived(nodeId, id, data);
		}
		Transport.fireMessageReceived(nodeId, id, data);
	}
	
	/**
	 * 获得会话地址
	 *
	 * @param session
	 *            会话地址
	 * @return 远程客户端地址
	 */
	public SocketAddress getAddress(BackendSession session) {
		return callRemoteFunc(session.getFrontendServerId(),f -> f.getAddress(session.getSessionId()));
	}
	
	/**
	 * 发送消息
	 *
	 * @param session
	 *            会话对象
	 * @param message
	 *            消息模块
	 */
	public void write(BackendSession session, SessionMessage message) {
		callRemote(session.getFrontendServerId(),f -> f.write(session.getSessionId(), message));
	}
	
	/**
	 * 调用远程对象方法
	 *
	 * @param frontendId
	 *            前台服务编号
	 * @param action
	 *            执行函数
	 */
	private void callRemote(int frontendId,
			RemoteObjectAction<FrontendBackendInterface> action) {
		try {
			FrontendBackendInterface frontendBackendInterface = _frontendServer.get(frontendId);
			if (frontendBackendInterface != null) {
				action.run(frontendBackendInterface);
			}
		} catch (java.rmi.ConnectException e) {
			_frontendServer.remove(frontendId);// 删除前台引用
			logger.error("[BackendServer] 连接前台服务对象错误："+ e.getLocalizedMessage());
		} catch (RemoteException e) {
			logger.error("[BackendServer] 调用前台远程对象错误：", e);
		}
	}
	
	/**
	 * 调用远程对象方法
	 *
	 * @param frontendId
	 *            前台服务编号
	 * @param action
	 *            执行函数
	 */
	private <R> R callRemoteFunc(int frontendId,
			RemoteObjectFunc<FrontendBackendInterface, R> action) {
		try {
			FrontendBackendInterface frontendBackendInterface = _frontendServer.get(frontendId);
			if (frontendBackendInterface != null) {
				return action.run(frontendBackendInterface);
			}
		} catch (java.rmi.ConnectException e) {
			_frontendServer.remove(frontendId);// 删除前台引用
			logger.error("[BackendServer] 连接前台服务对象错误："+ e.getLocalizedMessage());
		} catch (RemoteException e) {
			logger.error("[BackendServer] 调用前台远程对象错误：", e);
		}
		return null;
	}
}
