package org.server.frontend.mina;

import java.io.IOException;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.server.core.io.SessionMessage;
import org.server.frontend.FrontendServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 前台服务 Mina 处理链
 */
public class FrontendServerHandler extends IoHandlerAdapter{

	private static final Logger logger = LoggerFactory.getLogger(FrontendServerHandler.class);
	private final FrontendServer _server;
	public FrontendServerHandler(FrontendServer _server) {
        this._server = _server;
    }
	
	@Override
	public void messageReceived(IoSession session, Object message)throws Exception{
		logger.debug("Received:" + message.toString());
		_server.getFrontendSocketEventHandler().fireSessionReceived(session.getId(), (SessionMessage) message);
	}
	
	@Override
    public void messageSent(IoSession session, Object message) throws Exception {
        logger.debug("Sent:" + message.toString());
        super.messageSent(session, message); 
    }
	
	@Override
    public void sessionCreated(IoSession session) throws Exception {
        _server.getFrontendSocketEventHandler().fireSessionCreated(session.getId());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        _server.getFrontendSocketEventHandler().fireSessionClosed(session.getId());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        _server.getFrontendSocketEventHandler().fireSessionOpened(session.getId());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        _server.getFrontendSocketEventHandler().fireSessionIdle(session.getId(), status);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            logger.warn(session.getId() + ":" + cause.getMessage());
        } else {
            logger.error("Seesion Error:", cause);
        }
    }
}
