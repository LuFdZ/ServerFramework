package org.server.backend.io;

import org.server.core.io.SessionMessage;

public interface IMessageProcess {
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
	public void fireMessageReceived(int nodeId, long sessionId,SessionMessage data);
}
