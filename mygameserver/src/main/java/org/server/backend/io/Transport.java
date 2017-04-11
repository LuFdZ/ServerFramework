package org.server.backend.io;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.server.backend.BackendServer;
import org.server.backend.session.BackendSession;
import org.server.backend.session.GameSession;
import org.server.backend.session.GameSessionProvider;
import org.server.core.io.Binary;
import org.server.core.io.CreateSharedModule;
import org.server.core.io.SessionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

/**
 * 传输调度类
 */
public class Transport {

	static final Logger log = LoggerFactory.getLogger(Transport.class);
	static ExecutorService _callbackExecutor;
	static final List<TransportCallback<?, ?>> _callbacks = new CopyOnWriteArrayList<>();
	static final Map<Integer, Integer> _commandValueCaches = new ConcurrentHashMap<>();
	static SessionAuthentication _authentication;
	static SessionAuthenticationFail _authenticationFail;
	
	static {
		int callbackExecutorCount = Runtime.getRuntime().availableProcessors();
		log.info("[BackendServer::Transport] callback executor pool using thread count : {}.",callbackExecutorCount);
		_callbackExecutor = Executors.newWorkStealingPool(callbackExecutorCount);
	}
	
	/**
	 * 注册模块接收处理回调接口
	 *
	 * @param <T>
	 *            处理函数共享模块类型
	 * @param command
	 *            命令头值
	 * @param callback
	 *            回调函数
	 * @param createSharedModule
	 *            创建模块构造函数
	 * @return 注册回调模块
	 */
	public static <T extends GeneratedMessage> TransportCallback<T, ?> registerModuleReceived(short command, MessageCallback<T> callback,CreateSharedModule<T> createSharedModule) {
		TransportCallback<T, ?> result = new TransportCallback<>(command,callback, createSharedModule);
		_callbacks.add(result);
		return result;
	}
	
	/**
	 * 注册模块接收处理回调接口
	 *
	 * @param <T>
	 *            处理函数共享模块类型
	 * @param <U>
	 *            用户类型
	 * @param command
	 *            命令头值
	 * @param callback
	 *            回调函数
	 * @param createSharedModule
	 *            创建模块构造函数
	 * @return 注册回调模块
	 */
	public static <T extends GeneratedMessage, U> TransportCallback<T, U> registerModuleReceived(short command, VerifyMessageCallback<T, U> callback,CreateSharedModule<T> createSharedModule) {
		TransportCallback<T, U> result = new TransportCallback<>(command,createSharedModule, callback);
		_callbacks.add(result);
		return result;
	}
	
	/**
	 * 卸载消息接收处理回调接口
	 *
	 * @param callback
	 *            回调接口
	 */
	public static void unregisterMessageReceived(
			TransportCallback<?, ?> callback) {
		_callbacks.remove(callback);
	}
	
	/**
	 * 发送消息
	 *
	 * @param session
	 *            会话对象
	 * @param module
	 *            消息对象
	 * @return 发送是否成功
	 */
	public static boolean write(GameSession session, GeneratedMessage module) {
		int hash = module.getClass().hashCode();
		if (_commandValueCaches.containsKey(hash)) {
			write(session, _commandValueCaches.get(hash), module);
			return true;
		} else {
			log.error(String
					.format("BackendServer -> Transport[write]: Try Send Module But Unregister Module Command. Module:[%s].",
							module.getClass().toString()));
		}
		return false;
	}
	
	/**
	 * 发送消息
	 *
	 * @param session
	 *            会话对象
	 * @param message
	 *            消息对象
	 */
	public static void write(GameSession session, SessionMessage message) {
		// 增加流量
		session.addOutputBytes(message.getData().length + 6);

		BackendServer.getInstance().getBackendRMIServerInterface().write(session.getBackendSession(), message);
	}
	
	/**
	 * 获得会话认证函数
	 *
	 * @return
	 */
	public static SessionAuthentication getAuthentication() {
		return _authentication;
	}
	
	/**
	 * 设置会话认证函数
	 *
	 * @param _authentication
	 */
	public static void setAuthentication(SessionAuthentication _authentication) {
		Transport._authentication = _authentication;
	}
	
	/**
	 * 获得会话认证失败 Callback
	 *
	 * @return 回调函数
	 */
	public static SessionAuthenticationFail getAuthenticationFail() {
		return _authenticationFail;
	}
	
	/**
	 * 设置会话认证失败 Callback
	 *
	 * @param _authenticationFail
	 *            回调函数
	 */
	public static void setAuthenticationFail(SessionAuthenticationFail _authenticationFail) {
		Transport._authenticationFail = _authenticationFail;
	}
	
	/**
	 * 发送消息
	 *
	 * @param session
	 *            会话对象
	 * @param command
	 *            命令头值
	 * @param module
	 *            发送的数据模块
	 */
	private static void write(GameSession session, int command,GeneratedMessage module) {
		byte[] bytes = Binary.trySerialize(module);
		if (bytes != null) {
			write(session, new SessionMessage((short) command, bytes));
		}
	}
	
	/**
	 * 注册消息头值
	 *
	 * @param <T>
	 *            消息类型
	 * @param cls
	 *            消息类型 Class 对象
	 * @param command
	 *            头值数
	 */
	public static <T extends GeneratedMessage> void registerMessageCommand(Class<T> cls, Integer command) {
		_commandValueCaches.put(cls.hashCode(), command);
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
	public static void fireMessageReceived(int nodeId, long sessionId,
			SessionMessage data) {
		GameSession session = GameSessionProvider.getInstance().getGameSession(
				new BackendSession(nodeId, sessionId));

		// 回调对应函数
		boolean caller = false;
		for (TransportCallback<?, ?> cb : _callbacks) {
			if (cb.getCommand() == data.getCommand()) {
				// 设置输入流量
				session.addInputBytes(data.getData().length + 6);
				_callbackExecutor.execute(new TransportCallbackRunnable(cb,session, data));
				caller = true;
			}
		}
		if (!caller) {
			log.error(String
					.format("BackendServer -> Transport[fireMessageReceived]: Recvived Unregister Command Message Header :[0x%s].",
							Integer.toHexString(data.getCommand())));
		}
	}
}
