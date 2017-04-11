package org.server.core.netty.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * tcp 连接处理
 */
public class TcpClientHandler {
	static final Logger _log = LoggerFactory.getLogger(TcpClientHandler.class);

	/**
	 * 配置 bootstrap
	 * 
	 * @param bootstrap
	 */
	public void configBootstrap(Bootstrap bootstrap) {
	}

	/**
	 * 配置 nio socket channel
	 * 
	 * @param channel
	 *            通信频道
	 */
	public void configNewChannel(NioSocketChannel channel) {
	}

	/**
	 * 连接操作完成事件
	 * 
	 * @param success
	 *            操作是否成功
	 * @param channel
	 *            频道
	 * @param LastThrowable
	 *            最后一次发生的异常
	 * @param retryCount
	 *            重试次数
	 */
	public void onConnectFinish(boolean success, NioSocketChannel channel,
			Throwable LastThrowable, int retryCount) {
	}

	/**
	 * 异常处理函数
	 * 
	 * @param exception
	 *            异常
	 */
	public void onException(Throwable exception) {
		_log.warn("connector onException :: {} , {}", exception.getClass(),
				exception.getMessage());
	}
}
