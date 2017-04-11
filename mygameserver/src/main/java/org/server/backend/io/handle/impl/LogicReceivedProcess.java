package org.server.backend.io.handle.impl;

import org.server.backend.io.handle.ILogicDeadMessageProcess;
import org.server.backend.session.GameSession;
import org.server.core.io.SessionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicReceivedProcess implements Runnable {
	
	static final Logger log = LoggerFactory.getLogger(LogicReceivedProcess.class);
	
	static final int fixedLen = 3;
	
	ILogicDeadMessageProcess deadMessageProcess;
	GameSession session;
	CallbackDefine define;
	SessionMessage message;
	
	public LogicReceivedProcess(ILogicDeadMessageProcess deadMessageProcess,
			GameSession session, CallbackDefine define, SessionMessage message) {
		super();
		this.deadMessageProcess = deadMessageProcess;
		this.session = session;
		this.define = define;
		this.message = message;
	}
	
	Object[] readyParams() {
		Object[] params = new Object[fixedLen
				+ define.getRequireAuthAccess().length];
		params[0] = session;
		params[1] = message;
		return params;
	}
	
	@Override
	public void run() {
		Object[] params = readyParams();
		Object modelObject = define.deserialization(session, message);
		boolean deadMessage = true;

		params[2] = modelObject;

		try {
			if (modelObject != null)
				deadMessage = !define.invoke(session, message, modelObject,
						params, fixedLen);
		} catch (Throwable e) {
			log.error("[LogicReceivedProcess] invoke inline error !", e);
		}

		if (deadMessage)
			deadMessageProcess.processDeadMessage(params);
	}

}
