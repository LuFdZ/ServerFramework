package org.server.backend.io;

import org.server.backend.session.GameSession;
import org.server.core.io.SessionMessage;

/**
 * 回调异步包装
 */
public class TransportCallbackRunnable implements Runnable{
	
	TransportCallback<?, ?> _callback;
    GameSession _session;
    SessionMessage _data;
    
    public TransportCallbackRunnable(TransportCallback<?, ?> _callback, GameSession _session, SessionMessage _data) {
        this._callback = _callback;
        this._session = _session;
        this._data = _data;
    }
    
	@Override
	public void run() {
		Thread cur = Thread.currentThread();
        String name = cur.getName();
        cur.setName(String.format("MessageCallback Workthread:[%d]", cur.getId()));
        _callback.received(_session, _data);
        cur.setName(name);
	}

}
