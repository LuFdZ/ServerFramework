package org.server.frontend.mina;

import java.net.SocketAddress;
import java.rmi.RemoteException;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.server.backend.remote.BackendRMIServerInterface;
import org.server.core.io.SessionIdleType;
import org.server.core.io.SessionMessage;
import org.server.core.remote.RemoteObjectAction;
import org.server.frontend.FrontendServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 前台服务套接字处理对象
 *
 * @author Administrator
 */
public class FrontendSocketEventHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(FrontendSocketEventHandler.class);

	private final FrontendServer _server;

	public FrontendSocketEventHandler(FrontendServer server) {
		_server = server;
	}

	public FrontendServer getServer() {
		return _server;
	}

	/**
	 * 会话创建事件
	 *
	 * @param id
	 */
	void fireSessionCreated(long id) {
		callRemote(s -> s.onSessionCreated(id, getServer().getNodeId()));
	}
	
	/**
	 * 会话收取消息
	 *
	 * @param id
	 *            会话编号
	 * @param sessionMessage
	 *            消息对象
	 */
	public void fireSessionReceived(long id, SessionMessage sessionMessage) {
		callRemote(s -> s.onSessionReceived(id, sessionMessage, getServer()
				.getNodeId()));
	}

	/**
	 * 会话关闭事件
	 *
	 * @param id
	 *            会话编号
	 */
	void fireSessionClosed(long id) {
		callRemote(s -> s.onSessionClosed(id, getServer().getNodeId()));
	}

	/**
	 * 会话打开事件
	 *
	 * @param id
	 *            会话编号
	 */
	void fireSessionOpened(long id) {
		callRemote(s -> s.onSessionOpend(id, getServer().getNodeId()));
	}

	/**
	 * 会话空闲事件
	 *
	 * @param id
	 *            会话编号
	 * @param status
	 *            状态值
	 */
	void fireSessionIdle(long id, IdleStatus status) {
		callRemote(s -> s.onSessionIdle(id, SessionIdleType.parse(status),
				getServer().getNodeId()));
	}

	/**
	 * 调用远程对象方法
	 *
	 * @param action
	 *            函数体
	 */
	private void callRemote(RemoteObjectAction<BackendRMIServerInterface> action) {
		try {
			BackendRMIServerInterface serverInterface = getServer().getBackendServer();
			if (serverInterface != null) {
				action.run(serverInterface);
			}
		} catch (RemoteException e) {
			getServer().tryTestConnection();
		}
	}

	/**
	 * 写入数据
	 *
	 * @param sessionId
	 *            会话编号
	 * @param message
	 *            数据包
	 */
	public void write(long sessionId, SessionMessage message) {
		try {
			IoSession session = getServer().getAcceptor().getManagedSessions().get(sessionId);
			if (session != null) {
				session.write(message);
			}
		} catch (Exception e) {
			logger.error("[FrontendServer] 尝试会话发送数据失败：", e);
		}
	}

	public SocketAddress getAddress(long sessionId) {
		IoSession session = getServer().getAcceptor().getManagedSessions().get(sessionId);
		try {
			if (session != null) {
				return session.getRemoteAddress();
			}
		} catch (Exception e) {
			logger.error("[FrontendServer] 尝试获得客户端地址失败：", e);
		}
		return null;
	}
}
