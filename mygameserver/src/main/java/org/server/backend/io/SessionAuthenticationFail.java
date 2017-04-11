package org.server.backend.io;

import org.server.backend.session.GameSession;

/**
 * 会话认证失败函数
 */
@FunctionalInterface
public interface SessionAuthenticationFail {
	/**
     * 会话认证失败
     *
     * @param session 会话消息
     */
    void authenticationFail(GameSession session);
}
