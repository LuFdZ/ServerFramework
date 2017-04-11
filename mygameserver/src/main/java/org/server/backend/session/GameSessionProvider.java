package org.server.backend.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.server.backend.BackendServer;

/**
 * 游戏会话提供类
 */
public class GameSessionProvider {
	
	static final GameSessionProvider _instance = new GameSessionProvider();
	
	final Map<BackendSession, GameSession> _sessions = new ConcurrentHashMap<>();
	
	/**
	 * 读取游戏会话对象
	 *
	 * @param session
	 *            后台服务会话标识
	 * @return 游戏会话对象
	 */
	public GameSession getGameSession(BackendSession session) {

		GameSession result = _sessions.get(session);

		if (result == null) {

			// 创建会话
			result = new GameSession(session);
			result.setAttribute(GameSession.SESSION_CREATE,System.currentTimeMillis());
			result.setAttribute(GameSession.SESSION_ADDRESS,BackendServer.getInstance().getBackendRMIServerInterface().getAddress(session));

			// 放入缓存
			_sessions.put(session, result);

		}
		return result;
	}
	
	/**
	 * 根据身份验证信息获得 session
	 * 
	 * @param identity
	 *            身份验证信息
	 * @return session
	 */
	public GameSession getGameSessionForIdentity(Object identity) {

		if (identity == null)
			return null;

		for (GameSession session : _sessions.values()) {
			if (identity.equals(session.getAttribute(GameSession.SESSION_IDENTITY)))
				return session;
		}
		return null;
	}
	
	/**
	 * 销毁游戏会话对象
	 *
	 * @param session
	 *            后台服务会话标识
	 */
	public void dispose(BackendSession session) {
		GameSession result = _sessions.get(session);
		if (result != null) {
			// 删除引用
			_sessions.remove(session);
			result.fireSessionClose();
		}
	}
	
	public Map<BackendSession, GameSession> getSessions() {
		return _sessions;
	}
	
	/**
	 * 获得游戏会话对象实例
	 *
	 * @return 管理器实例
	 */
	public static GameSessionProvider getInstance() {
		return _instance;
	}
}
