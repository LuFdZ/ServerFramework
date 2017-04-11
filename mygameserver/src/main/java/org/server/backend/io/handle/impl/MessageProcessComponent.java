package org.server.backend.io.handle.impl;

import org.server.backend.BackendServer;
import org.server.backend.component.GameComponentManagement;
import org.server.backend.io.IMessageProcess;
import org.server.backend.io.handle.ILogicDeadMessageProcess;
import org.server.backend.session.BackendSession;
import org.server.backend.session.GameSession;
import org.server.backend.session.GameSessionProvider;
import org.server.core.io.SessionMessage;
import org.server.tools.ExecuteFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息处理调度
 */
public class MessageProcessComponent implements IMessageProcess,ILogicDeadMessageProcess {

	static final Logger log = LoggerFactory.getLogger(MessageProcessComponent.class);
	ExecuteFactory executeFactory;
	LogicHandleFinder logicHandleFinder;

	public MessageProcessComponent() {
	}
	
	public void load() {
		this.executeFactory = new ExecuteFactory("MessageHandleCenter");
		this.logicHandleFinder = new LogicHandleFinder(GameSession.class,
				SessionMessage.class);

		this.executeFactory.usingWorkStealingPool();
		this.logicHandleFinder.searchDefine(GameComponentManagement.orderdComponents().toArray(Object[]::new));

		BackendServer.getInstance().getBackendRMIServerInterface()
				.setHandleComponent(this);
	}

	public void unload() {
		this.executeFactory = null;
		this.logicHandleFinder = null;

		BackendServer.getInstance().getBackendRMIServerInterface()
				.setHandleComponent(null);
	}
	
	/**
	 * 触发消息接收事件
	 *
	 * @param nodeId
	 *            节点编号
	 * @param sessionId
	 *            会话编号
	 * @param data
	 *            会话消息
	 */
	public void fireMessageReceived(int nodeId, long sessionId,SessionMessage data) {
		GameSession session = GameSessionProvider.getInstance().getGameSession(
				new BackendSession(nodeId, sessionId));

		CallbackDefine define = logicHandleFinder.getDefineByCommand(data
				.getCommand());
		if (define != null) {
			session.addInputBytes(data.getData().length + 6);
			data.setTimestamp(System.currentTimeMillis());
			fireModelReceived(session, define, data);
		} else {
			log.error(String
					.format("BackendServer -> fireMessageReceived: Recvived Unregister Command Message Header :[0x%s].",
							Integer.toHexString(data.getCommand())));
		}
	}

	void fireModelReceived(GameSession session, CallbackDefine define,SessionMessage message) {
		LogicReceivedProcess process = new LogicReceivedProcess(this, session,
				define, message);
		this.executeFactory.doWorker(process);
	}
	
	@Override
	public void processDeadMessage(Object[] params) {
		LogicDeadMessageProcess process = new LogicDeadMessageProcess(this,
				params, logicHandleFinder.getDeadMessageHandle());
		this.executeFactory.doWorker(process);
	}
}
