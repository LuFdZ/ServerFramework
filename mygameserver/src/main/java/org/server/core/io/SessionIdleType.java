package org.server.core.io;

import java.io.Serializable;

import org.apache.mina.core.session.IdleStatus;

public enum SessionIdleType implements Serializable {

	READER_IDLE,
    WRITER_IDLE,
    BOTH_IDLE;
	
	/**
     * 转换会话空闲状态类型
     *
     * @param status Mina 会话空闲状态类型
     * @return
     */
    public static SessionIdleType parse(IdleStatus status) {
        if (IdleStatus.BOTH_IDLE == status) {
            return BOTH_IDLE;
        } else if (IdleStatus.READER_IDLE == status) {
            return READER_IDLE;
        } else if (IdleStatus.WRITER_IDLE == status) {
            return WRITER_IDLE;
        }
        return null;
    }
}
