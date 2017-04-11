package org.server.frontend.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.server.core.io.SessionMessage;

public class MessageDecoder extends CumulativeProtocolDecoder {

	@Override
	protected boolean doDecode(IoSession is, IoBuffer ib, ProtocolDecoderOutput pdo) throws Exception {
		synchronized (is) {
			final MessageCodec stats = getSessionCodec(is);
			// 消息
            SessionMessage message = null;
            // 消息不等于空，循环读取消息
            do {
            	message = stats.readMessage(is, ib);
                if (message != null) {
                    pdo.write(message);
                }
            } while (message != null);
            // 调试输出
            // System.out.println(ib);
            return message != null;
		}
	}
	
	private MessageCodec getSessionCodec(IoSession is) {
		MessageCodec stats = (MessageCodec) is.getAttribute(MessageCodec.KEY);
		if (stats == null) {
            stats = new MessageCodec();
            is.setAttribute(MessageCodec.KEY, stats);
        }
        return stats;
	}

}
