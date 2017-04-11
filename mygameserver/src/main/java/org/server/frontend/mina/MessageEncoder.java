package org.server.frontend.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.server.core.io.SessionMessage;

public class MessageEncoder implements ProtocolEncoder {

	@Override
	public void encode(IoSession is, Object o, ProtocolEncoderOutput peo) throws Exception {
		SessionMessage message = (SessionMessage) o;
        if (message != null) {
            peo.write(MessageCodec.writeMessage(is, message));
        }
		
	}

	@Override
	public void dispose(IoSession is) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
