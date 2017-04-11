package org.server.frontend.mina;

import java.io.IOException;
import java.io.InputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.server.core.io.Binary;
import org.server.core.io.SessionMessage;
import org.server.core.io.output.GenericLittleEndianWriter;
import org.server.core.io.output.IoBufferByteOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息解码状态
 *
 * @author
 */
public class MessageCodec {

	/**
	 * 消息描述符
	 */
	public static class MessageDescription {
		
		private static final Logger logger = LoggerFactory.getLogger(MessageDescription.class);

		/**
		 * 头长度字节
		 */
		public final static int HEAD_LENGTH = 4;

		/**
		 * 命令字节长度
		 */
		public final static int COMMAND_LENGTH = 2;

		/**
		 * 检查长度字节
		 */
		public final static int CHECK_LENGTH = HEAD_LENGTH + COMMAND_LENGTH;

		/**
		 * 消息长度
		 */
		public Integer messageLength = null;

		/**
		 * 命令头值
		 */
		public Integer messageCommand = null;

		/**
		 * 消息体内容
		 */
		public byte[] message = null;

		/**
		 * 构造一个消息描述符
		 *
		 */
		public MessageDescription() {
		}
		
	    
	    
	    /**
         * 读取会话消息
         *
         * @param is 会话对象
         * @param ib 缓冲区对象
         * @return 会话消息模块s
         */
        SessionMessage readMessage(IoSession is, IoBuffer ib) {
        	if (messageLength == null) {
                tryReaderMessageLength(is, ib);
            }
        	if (messageLength != null && messageLength > 0 && messageCommand == null) {
                tryReaderMessageCommand(ib);
            }
        	if (messageLength != null && messageLength > 0 && messageCommand != null && message == null) {
                tryReaderMessage(is, ib);
            }
        	if (messageLength != null && messageLength > 0 && messageCommand != null && message != null) {
                return new SessionMessage(messageCommand.shortValue(), message);
            } else {
                return null;
            }
        }

		/**
		 * 尝试读取消息长度
		 *
		 * @param ib
		 *            缓冲区
		 */
		void tryReaderMessageLength(IoSession is, IoBuffer ib) {
			if (ib.remaining() > HEAD_LENGTH) {
				try {
					InputStream input = ib.asInputStream();
					int[] cache = new int[HEAD_LENGTH];
					cache[0] = input.read();
					cache[1] = input.read();
					cache[2] = input.read();
					cache[3] = input.read();
					// 读取消息头
					this.messageLength = (cache[3] << 24 | cache[2] << 16 | cache[1] << 8 | cache[0]);
				} catch (IOException ex) {
					logger.error("[tryReaderMessageLength] Error Read Length...", ex);
				}

				// 消息长度检查
				if (messageLength <= COMMAND_LENGTH) {
					logger.error(String.format("Session [%d] Received Error Message Length [%d] Buffer [%s]", is.getId(), messageLength, ib.toString()));
					is.close(false);
				}
			}
		}

		/**
		 * 尝试读取消息命令头值
		 *
		 * @param ib
		 *            缓冲区
		 */
		void tryReaderMessageCommand(IoBuffer ib) {
			if (ib.remaining() >= COMMAND_LENGTH) {
				try {
					InputStream is = ib.asInputStream();
					int[] cache = new int[COMMAND_LENGTH];
					cache[0] = is.read();
					cache[1] = is.read();
					this.messageCommand = cache[1] << 8 | cache[0];
				} catch (IOException ex) {
					logger.error("[tryReaderMessageCommand] Error Read Command...", ex);
				}
			}
		}

		/**
		 * 尝试读取消息
		 *
		 * @param ib
		 *            缓冲区
		 */
		void tryReaderMessage(IoSession is, IoBuffer ib) {
			// 消息长度包含2个字节命令头
			int ml = messageLength - COMMAND_LENGTH;
			if (ib.remaining() >= ml) {
                message = new byte[ml];
                ib.get(message);
            }
		}
		
		
        
        /**
         * 写入会话消息
         *
         * @param is 会话模块
         * @param message 会话消息
         * @return 消息缓冲区
         */
        public static IoBuffer writeMessage(IoSession is, SessionMessage message) {
        	GenericLittleEndianWriter writer = Binary.write(message.getData().length + MessageDescription.CHECK_LENGTH);
        	byte[] bytes = message.getData();
        	// 写入数据体
        	writer.writeInt(bytes.length + COMMAND_LENGTH);
        	writer.writeShort(message.getCommand());
        	writer.write(bytes);
        	
        	IoBufferByteOutputStream stream =  (IoBufferByteOutputStream) writer.getByteOutputStream();
            return stream.getBuffer().flip();
        }
        
        
	}
	
	/**
     * 消息解码器状态关键字
     */
    public final static String KEY = "MessageDecoderStats_Key";
    
	/**
     * 消息描述模块
     */
    private MessageDescription _description;
    
    /**
     * 读取消息
     *
     * @param is 会话模块
     * @param ib 数据缓冲区
     * @return
     */
    SessionMessage readMessage(IoSession is, IoBuffer ib) {
    	// 检查长度字节并构造消息包
    	if (_description == null) {
            _description = new MessageDescription();
        }
    	// 检查读取数据结构体信息 (命令头加消息体)
        // 读取消息
    	SessionMessage message = _description.readMessage(is, ib);
    	if (message != null) {
            _description = null;
        }
        return message;
    }

	/**
     * 写入会话消息
     *
     * @param is 会话模块
     * @param message 会话消息
     * @return 消息缓冲区
     */
    static IoBuffer writeMessage(IoSession is, SessionMessage message) {
    	return MessageDescription.writeMessage(is, message);
    }
}
