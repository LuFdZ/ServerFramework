package org.server.core;

/**
 * 关机服务
 */
public class ShutdownService implements Runnable {
	
	private final AbstractStandardServer<?> _server;
    private Thread _thread;
    
    public ShutdownService(AbstractStandardServer<?> _server) {
        this._server = _server;
    }
    
    public void shutdown() {
        if (_thread == null) {
            _thread = new Thread(this);
            _thread.start();
        }
    }
    
	@Override
	public void run() {
		_server.stop();
	}

}
