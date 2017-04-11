package org.server.backend.io;

import org.server.backend.session.GameSession;
import org.server.core.io.Binary;
import org.server.core.io.CreateSharedModule;
import org.server.core.io.SessionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

/**
 * 回调接口类
 *
 * @param <M>
 *            处理函数共享模块类型
 * @param <U>
 *            用户模块类型
 */
public class TransportCallback<M extends GeneratedMessage, U> {
	
	private static final Logger logger = LoggerFactory.getLogger(TransportCallback.class);
	private final short _command;
	private MessageCallback<M> _callback;
	private final CreateSharedModule<M> _createSharedModule;
	private VerifyMessageCallback<M, U> _verifyMessageCallback;
	
	public TransportCallback(short command, MessageCallback<M> callback,CreateSharedModule<M> createSharedModule) {
		this._command = command;
		this._callback = callback;
		this._createSharedModule = createSharedModule;
	}

	public TransportCallback(short _command,CreateSharedModule<M> _createSharedModule,VerifyMessageCallback<M, U> _verifyMessageCallback) {
		this._command = _command;
		this._createSharedModule = _createSharedModule;
		this._verifyMessageCallback = _verifyMessageCallback;
	}
	
	public short getCommand() {
		return _command;
	}
	
	@SuppressWarnings("unchecked")
	public void received(GameSession session, SessionMessage data) {
		M obj = null;
		if (_createSharedModule != null) {
			obj = Binary.tryDeserialize(_createSharedModule, data);
		}
		boolean messageModelVerify = (_createSharedModule == null || (_createSharedModule != null && obj != null));
		
		if (_callback != null && messageModelVerify) {
			try {
				_callback.callback(session, data, obj);
			} catch (Exception e) {
				logger.error("Transport MessageCallback Error:", e);
			}
		}
		Object _ObjectuserModel = null;
		boolean verify = _verifyMessageCallback != null && Transport.getAuthentication() != null;
		
		if (verify && messageModelVerify) {
			_ObjectuserModel = Transport.getAuthentication().authentication(
					session);
			if (_ObjectuserModel != null) {
				try {
					_verifyMessageCallback.callback(session, data,
							(U) _ObjectuserModel, obj);
				} catch (Exception e) {
					logger.error("Transport MessageCallback Error:", e);
				}
			}
		}
		
		// 认证失败
		if (_verifyMessageCallback != null
				&& Transport.getAuthenticationFail() != null
				&& messageModelVerify && _ObjectuserModel == null // 无验证对应用户模块
		) {
			Transport.getAuthenticationFail().authenticationFail(session);
		}
		
	}
	
	
}
