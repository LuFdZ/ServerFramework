package org.server.core.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 
 * nettytools 工具集
 */
public class NettyCenter {
	/**
	 * 工具类实例
	 */
	static NettyCenter instance = new NettyCenter();
	
	/**
	 * 事件循环组
	 */
	EventLoopGroup eventLoopGroup;

	/**
	 * 定时器
	 */
	HashedWheelTimer hashedWheelTimer;

	/**
	 * 私有构造函数
	 */
	private NettyCenter() {

		/**
		 * 构造事件循环组
		 */
		eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime()
				.availableProcessors(), new DefaultThreadFactory(
				"NettyNioLoopGroup"));

		/**
		 * 构造定时器
		 */
		hashedWheelTimer = new HashedWheelTimer(new DefaultThreadFactory(
				"NettyHashedWheelTimer"));

	}

	/**
	 * 获得事件循环组
	 * 
	 * @return 事件循环组
	 */
	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	/**
	 * 获得定时器对象
	 * 
	 * @return 定时器对象
	 */
	public HashedWheelTimer geTimer() {
		return hashedWheelTimer;
	}

	/**
	 * 创建新的 Bootstrap
	 * 
	 * @return
	 */
	public Bootstrap newNioTcpBootstrap() {

		Bootstrap bootstrap = new Bootstrap();

		// config event loop group

		bootstrap.group(getEventLoopGroup());

		// config channel

		bootstrap.channel(NioSocketChannel.class);

		// set tcp flag

		bootstrap.option(ChannelOption.TCP_NODELAY, true);

		return bootstrap;
	}

	/**
	 * 获得 nettytools 实例
	 * 
	 * @return 获得实例
	 */
	public static NettyCenter getInstance() {
		return instance;
	}
}
