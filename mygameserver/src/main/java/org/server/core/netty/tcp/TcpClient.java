package org.server.core.netty.tcp;

import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 
 * 表示唯一会话 tcp 客户端
 *
 */
public class TcpClient extends AbstractTcpRetryConnector{
	/**
	 * @return the handler
	 */
	public TcpClientHandler getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 *            the handler to set
	 */
	public void setHandler(TcpClientHandler handler) {
		this.handler = handler;
	}

	/**
	 * 获得链接件唯一通信频道
	 * 
	 * @return 频道
	 */
	public NioSocketChannel getChannel() {
		return channel;
	}

	/**
	 * 客户端是否已经连接上
	 * 
	 * @return 是否连接成功
	 */
	public boolean isConnected() {
		return channel != null && channel.isActive();
	}

	/**
	 * 判断是否连接中
	 * 
	 * @return
	 */
	public boolean isConnecting() {
		return connectFuture != null && !connectFuture.isDone();
	}

	/**
	 * 断开连接
	 */
	public void disconnect() {
		closeChannel();
	}
}
