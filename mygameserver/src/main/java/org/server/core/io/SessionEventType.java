package org.server.core.io;

/**
 * 会话事件类型
 */
public enum SessionEventType implements java.io.Serializable {
	SessionCreate,
    SessionOpen,
    SessionIdle,
    SessionClosed
}
