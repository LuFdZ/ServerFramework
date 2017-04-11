package org.server.core.netty.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.function.Consumer;

import org.server.core.netty.NettyCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Netty Tcp 连接件
 */
public abstract class AbstractTcpClient {
	/**
	 * 连接件处理适配器
	 * 
	 * @author Hxms
	 *
	 */
	class ConnectorHandlerAdapter extends ChannelHandlerAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * io.netty.channel.ChannelHandlerAdapter#exceptionCaught(io.netty.channel
		 * .ChannelHandlerContext, java.lang.Throwable)
		 */
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			safeCallHandle(h -> h.onException(cause));
		}

	}

	static final Logger _log = LoggerFactory.getLogger(AbstractTcpClient.class);

	/**
	 * Bootstrap
	 */
	Bootstrap bootstrap;

	/**
	 * 表示链接件唯一连接
	 */
	NioSocketChannel channel;

	/**
	 * 事件处理对象
	 */
	TcpClientHandler handler;

	/**
	 * 创建 tcp 链接件
	 */
	public AbstractTcpClient() {
		createBootstrap();
	}

	/**
	 * 创建 Bootstrap
	 */
	void createBootstrap() {
		// create a new bootstrap
		bootstrap = NettyCenter.getInstance().newNioTcpBootstrap();
		// config bootstrap
		safeCallHandle(h -> h.configBootstrap(bootstrap));
		// coifig initialize handler
		bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {

			@Override
			protected void initChannel(NioSocketChannel ch) throws Exception {
				configNewChannel(ch);
			}
		});
	}

	/**
	 * 配置 nio socket channel
	 * 
	 * @param channel
	 *            通信频道
	 */
	protected void configNewChannel(NioSocketChannel channel) {
		// 获得 pipeline
		ChannelPipeline pipeline = channel.pipeline();
		// 添加标准处理
		pipeline.addLast(new ConnectorHandlerAdapter());
		// 通知事件处理
		safeCallHandle(h -> h.configNewChannel(channel));
	}

	/**
	 * 设置连接超时时间
	 * 
	 * @param millis
	 *            毫秒数
	 */
	public void setConnectTimeoutMillis(int millis) {
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, millis);
	}

	/**
	 * 关闭之前频道
	 */
	protected void closeChannel() {
		if (channel != null)
			channel.close();
		channel = null;
	}

	/**
	 * 安全调用 handler
	 * 
	 * @param consumer
	 *            调用函数
	 */
	protected void safeCallHandle(Consumer<TcpClientHandler> consumer) {
		if (handler != null) {
			try {
				consumer.accept(handler);
			} catch (Exception e) {
				_log.error("AbstractTcpClient::Callback . ", e);
			}
		}
	}
}
