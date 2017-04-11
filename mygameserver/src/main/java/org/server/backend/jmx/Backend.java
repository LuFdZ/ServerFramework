package org.server.backend.jmx;

import org.server.backend.session.GameSession;
import org.server.backend.session.GameSessionProvider;

/**
 * 后台管理实现
 */
public class Backend implements BackendMXBean {

	@Override
	public GameSession[] getGameSessions() {
		return GameSessionProvider.getInstance().getSessions().values().toArray(new GameSession[0]);
	}

}
