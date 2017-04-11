package org.server.backend.jmx;

import org.server.backend.session.GameSession;

/**
 * 后台管理 bean
 */
public interface BackendMXBean {
	/**
     * 获得游戏会话集合
     *
     * @return 会话集合
     */
    GameSession[] getGameSessions();
}
