package org.server.core.netty.tcp;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * 
 * 抽象 tcp 连接件
 */
public class AbstractTcpConnector extends AbstractTcpClient {
	/**
	 * 连接操作返回对象
	 */
	ChannelFuture connectFuture;

	/**
	 * 连接操作检查
	 * 
	 * @return 连接操作
	 */
	protected ChannelFuture connectFutureCheck() {
		if (connectFuture == null)
			return null;

		ChannelFuture future = connectFuture;
		connectFuture = null;

		return future;
	}

	/**
	 * 连接操作完成事件
	 */
	protected void connectFutureFinish() {
		ChannelFuture future = connectFutureCheck();

		if (future != null) {
			connectFinalCallback(future, 0);
		}
	}

	/**
	 * 
	 * 调用连接完成回调
	 * 
	 * @param future
	 *            操作事件
	 * 
	 * @param retryCount
	 *            重试次数
	 */
	protected void connectFinalCallback(ChannelFuture future, int retryCount) {

		closeChannel();

		channel = (NioSocketChannel) future.channel();

		safeCallHandle(h -> h.onConnectFinish(future.isSuccess(), channel,
				future.cause(), retryCount));
	}

	/**
	 * 开始连接
	 * 
	 * @param hostname
	 *            域名
	 * @param port
	 *            端口
	 */
	public boolean connect(String hostname, int port) {
		return connect(new InetSocketAddress(hostname, port));
	}

	/**
	 * 开始连接函数
	 * 
	 * @param socketAddress
	 *            连接地址
	 * @return 连接操作是否已经开始
	 */
	public boolean connect(InetSocketAddress socketAddress) {
		if (connectFuture != null)
			return false;

		connectFuture = bootstrap.connect(socketAddress);
		connectFuture.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				connectFutureFinish();
			}
		});

		return true;
	}

	/**
	 * 等待连接完成
	 * 
	 * @throws InterruptedException
	 */
	public void waitConnect() throws InterruptedException {
		if (connectFuture != null)
			connectFuture.await();
	}
}
