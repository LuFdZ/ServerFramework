package org.server.core.io;

import java.io.Serializable;

/**
 * 会话消息模块
 *
 * @author 
 */
public class SessionMessage implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1344636526677325402L;
	
	private short command;
	
	private byte[] data;
	
	private long timestamp;
	
	public SessionMessage(short command, byte[] data) {
		this.command = command;
		this.data = data;
	}

	public short getCommand() {
		return command;
	}

	public void setCommand(short command) {
		this.command = command;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return String.format("命令值：[0x%s] 消息长度：[%d]",
				Integer.toHexString(getCommand()), getData().length);
	}

}
