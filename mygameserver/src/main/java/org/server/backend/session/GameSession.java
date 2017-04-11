package org.server.backend.session;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.server.backend.io.Transport;
import org.server.tools.Toolset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

/**
 * 游戏会话类
 */
public class GameSession implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	static final Logger logger = LoggerFactory.getLogger(GameSession.class);
	
	public GameSession(BackendSession _backendSession) {
		this._backendSession = _backendSession;
	}
	
	@FunctionalInterface
	public static interface GameSessionCloseListenter {

		/**
		 * 会话关闭
		 * 
		 * @param session
		 *            会话
		 */
		public void sessionClose(GameSession session);

	}
	
	/**
	 * 会话创建时间标记
	 */
	public static final String SESSION_CREATE = "SESSION_CREATE";

	/**
	 * 会话 Id 标记
	 */
	public static final String SESSION_IDENTITY = "SESSION_IDENTITY";

	/**
	 * 会话 连接地址 标记
	 */
	protected static final String SESSION_ADDRESS = "SESSION_ADDRESS";

	/**
	 * 后台 关联 session
	 */
	final BackendSession _backendSession;

	/**
	 * 会话关联属性值
	 */
	final Map<String, Object> _attribute = new HashMap<>();

	/**
	 * 会话 是否已经关闭
	 */
	boolean _dispose = false;
	
	/**
	 * 会话 关闭事件
	 */
	GameSessionCloseListenter sessionCloseListenter;
	
	// 统计数据
	volatile int _inputBytes = 0;
	volatile int _outputBytes = 0;
	
	public BackendSession getBackendSession() {
		return _backendSession;
	}

	public int getInputBytes() {
		return _inputBytes;
	}

	public void addInputBytes(int _inputBytes) {
		this._inputBytes += _inputBytes;
	}

	public int getOutputBytes() {
		return _outputBytes;
	}

	public void addOutputBytes(int _outputBytes) {
		this._outputBytes += _outputBytes;
	}

	/**
	 * 触发会话关闭事件
	 */
	protected void fireSessionClose() {

		this._dispose = true;

		if (sessionCloseListenter != null) {
			try {

				sessionCloseListenter.sessionClose(this);

			} catch (Throwable throwable) {

				logger.error(
						"[GameSession::fireSessionClose event exception] ",
						throwable);

			}
		}

	}
	
	/**
	 * 设置关闭会话监听
	 * 
	 * @param listenter
	 *            关闭会话监听事件
	 */
	public void setCloseListenter(GameSessionCloseListenter listenter) {
		this.sessionCloseListenter = listenter;
	}
	
	/**
	 * 获得属性
	 *
	 * @param key
	 *            键值
	 * @return 返回值
	 */
	public Object getAttribute(String key) {
		return _attribute.get(key);
	}

	/**
	 * 设置属性
	 *
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void setAttribute(String key, Object value) {
		_attribute.put(key, value);
	}

	/**
	 * 移除属性
	 *
	 * @param key
	 *            键
	 * @return 值
	 */
	public Object removeAttribute(String key) {
		return _attribute.remove(key);
	}

	/**
	 * 设置身份验证信息
	 * 
	 * @param identity
	 */
	public void setIdentity(Object identity) {
		_attribute.put(SESSION_IDENTITY, identity);
	}

	/**
	 * 获得远程客户端地址
	 * 
	 * @return 客户端地址
	 */
	public String getRemoteAddress() {

		InetSocketAddress address = (InetSocketAddress) _attribute
				.get("SESSION_ADDRESS");

		if (address != null)
			return address.getAddress().getHostAddress();

		return "<None>";

	}
	
	/**
	 * 获得远程客户端长整数地址
	 * 
	 * @return 客户端地址
	 */
	public long getRemoteAddressLong() {

		InetSocketAddress address = (InetSocketAddress) _attribute
				.get(SESSION_ADDRESS);

		if (address != null) {

			InetAddress inetAddress = address.getAddress();

			byte[] ip = inetAddress.getAddress();

			return ((ip[0] & 0xffL) << 24) + ((ip[1] & 0xffL) << 16)
					+ ((ip[2] & 0xffL) << 8) + (ip[3] & 0xffL);
		}

		return 0;
	}
	
	/**
	 * 获得远程客户端端口
	 * 
	 * @return 客户端端口
	 */
	public int getRemoteAddressPort() {

		InetSocketAddress address = (InetSocketAddress) _attribute.get(SESSION_ADDRESS);
		if (address != null)
			return address.getPort();

		return -1;

	}

	/**
	 * 获得会话是否注销 - [前台 Socket 事件注销]
	 *
	 * @return
	 */
	public boolean isDispose() {
		return _dispose;
	}

	/**
	 * 写入消息
	 *
	 * @param obj
	 *            消息对象
	 */
	public void write(GeneratedMessage obj) {
		Transport.write(this, obj);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		String separator = Toolset.Separator;

		sb.append("GameSession::[").append(getBackendSession())
				.append(separator).append(" , 输入流量：")
				.append(getInputBytes() / 1000.0).append(" KB.")
				.append(separator).append(" , 输出流量：")
				.append(getOutputBytes() / 1000.0).append(" KB]");

		return sb.toString();

	}
}
