package org.server.backend.io.handle.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.server.backend.io.handle.ILogicModelSerialize;
import org.server.backend.io.handle.annotation.LogicHandle;
import org.server.backend.io.handle.annotation.LogicModelMeta;
import org.server.backend.session.GameSession;
import org.server.core.io.SessionMessage;

/**
 * 消息回调定义
 */
public class CallbackDefine {

	Method defineMethod;
	MethodCall methodCall;

	LogicHandle logicHandle;
	LogicModelMeta logicModelMeta;

	List<Class<?>> requireAuthTypes;
	MethodCall[] requireAuthAccess;
	
	public CallbackDefine(Method defineMethod, MethodCall methodCall,
			LogicHandle logicHandle, LogicModelMeta logicModelMeta,
			List<Class<?>> requireAuthTypes) {
		super();
		this.defineMethod = defineMethod;
		this.methodCall = methodCall;
		this.logicHandle = logicHandle;
		this.logicModelMeta = logicModelMeta;
		this.requireAuthTypes = requireAuthTypes;
	}
	
	public Method getDefineMethod() {
		return defineMethod;
	}

	public LogicHandle getLogicHandle() {
		return logicHandle;
	}

	public LogicModelMeta getLogicModelMeta() {
		return logicModelMeta;
	}

	public Class<?> getLogicModelType() {
		return logicModelMeta.owner();
	}

	public List<Class<?>> getRequireAuthTypes() {
		return requireAuthTypes;
	}

	public MethodCall[] getRequireAuthAccess() {
		return requireAuthAccess;
	}

	public void setRequireAuthAccess(MethodCall[] requireAuthAccess) {
		this.requireAuthAccess = requireAuthAccess;
	}
	
	public Object deserialization(GameSession session, SessionMessage data) {
		Object result = null;
		try {
			ILogicModelSerialize serialize = logicModelMeta.serialize();
			result = serialize.deserialization(logicModelMeta.owner(),
					data.getData());
		} catch (Throwable ignore) {
			// ignore
		}
		return result;
	}
	
	public boolean invoke(GameSession session, SessionMessage data,
			Object modelObject, Object[] params, int fixedLen) {

		for (int i = 0; i < requireAuthAccess.length; i++) {
			MethodCall authCall = requireAuthAccess[i];
			try {
				Object[] authParamsObjects = authCall.orderParams(session,
						data, modelObject);

				if (authParamsObjects == null)
					return false;

				Object authResultObject = authCall.invoke(authParamsObjects);

				// autuorize fail
				if (authResultObject == null)
					return false;

				params[fixedLen + i] = authResultObject;
			} catch (Throwable e) {
				throw new RuntimeException("Call Auth Function Exception !", e);
			}
		}

		try {
			Object[] logicParamsObjects = methodCall.orderParams(params);
			methodCall.invoke(logicParamsObjects);
			return true;
		} catch (Throwable e) {
			throw new RuntimeException("Call Logic Function Exception !", e);
		}
	}
}
