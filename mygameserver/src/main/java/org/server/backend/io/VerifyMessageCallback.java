package org.server.backend.io;

import org.server.backend.session.GameSession;
import org.server.core.io.SessionMessage;

import com.google.protobuf.GeneratedMessage;

/**
 * 验证消息回调
 * @param <M> 消息模块类型
 * @param <U> 用户模块类型
 */
@FunctionalInterface
public interface VerifyMessageCallback<M extends GeneratedMessage, U> {
	void callback(GameSession session, SessionMessage message, U userModel, M messageModel);
}
