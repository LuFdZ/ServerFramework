package org.server.backend.io.handle.impl;

import java.util.List;

import org.server.backend.io.IMessageProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicDeadMessageProcess implements Runnable {
	
	static final Logger log = LoggerFactory.getLogger(LogicDeadMessageProcess.class);
	
	IMessageProcess process;
	Object[] params;

	List<MethodCall> calls;
	
	public LogicDeadMessageProcess(IMessageProcess process, Object[] params,
			List<MethodCall> calls) {
		super();
		this.process = process;
		this.params = params;
		this.calls = calls;
	}
	
	@Override
	public void run() {
		try {
			for (MethodCall methodCall : calls) {
				Object[] params = methodCall.orderParams(this.params);
				if (params != null)
					methodCall.invoke(params);
			}
		} catch (Throwable e) {
			log.error("[LogicDeadMessageProcess] invoke inline error !", e);
		}
	}

}
