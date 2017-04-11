package org.server.core.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.Timeout;

import java.util.concurrent.TimeUnit;

import org.server.core.Mission;
import org.server.core.netty.NettyCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * netty http 异步客户端
 */
public class NettyHttpRequest {
	static final Logger _log = LoggerFactory.getLogger(NettyHttpRequest.class);

	/**
	 * http 请求成功
	 */
	public static final int NE_OK = 0;

	/**
	 * http 请求连接失败
	 */
	public static final int NE_CONNECT_FAIL = 1;

	/**
	 * http 请求连接超时
	 */
	public static final int NE_CONNECT_TIMEOUT = 2;

	/**
	 * http 请求超时
	 */
	public static final int NE_REQUEST_TIMEOUT = 3;

	/**
	 * http 请求失去连接
	 */
	public static final int NE_REQUEST_LOST_CONNECT = 4;

	/**
	 * tcp 客户端
	 */
	TcpClient client = new TcpClient();

	/**
	 * 请求配置表
	 */
	NettyHttpRequestConfig requestConfig;

	/**
	 * 请求任务
	 */
	Mission requestMission = new Mission();

	/**
	 * 请求超时计时器
	 */
	Timeout requestTimeout;

	/**
	 * 表示请求是否已经释放
	 */
	volatile boolean release = false;

	/**
	 * 创建新请求对象
	 * 
	 * @param requestConfig
	 *            请求配置文件
	 */
	public NettyHttpRequest(NettyHttpRequestConfig requestConfig) {
		super();
		this.requestConfig = requestConfig;
	}

	/**
	 * 开始请求
	 * 
	 * @return 操作是否成功
	 */
	public boolean request() {
		int missionCode = requestMission.getMissionCode();
		int newCode = requestMission.newMission();

		if (missionCode == newCode) {
			return false;
		}

		// config handler
		client.setHandler(new NettyHttpClientHandler());

		// config option
		client.setConnectTimeoutMillis(requestConfig.getConnectTimeout());

		// enable retry connect
		if (requestConfig.getRetryConnectCount() > 0)
			client.enableRetryConnect(requestConfig.getRetryConnectCount());

		// begin connect
		client.connect(requestConfig.getHostname(), requestConfig.getPort());

		return true;
	}

	/**
	 * 等待请求完成
	 */
	public void waitRequest() {
		requestMission.awaitMission();
	}

	/**
	 * 发送 http 请求
	 */
	void sendRequest() {

		HttpObject[] httpObjects = {};

		try {
			httpObjects = requestConfig.getHttpObjects();
		} catch (Exception ex) {
			_log.error("NettyHttpRequest :: error of encoder post data !", ex);
		}

		// 发送请求
		for (Object httpObject : httpObjects) {
			client.getChannel().writeAndFlush(httpObject);
		}

		// 设定超时
		requestTimeout = NettyCenter
				.getInstance()
				.geTimer()
				.newTimeout(this::onRequestTimeout,
						requestConfig.getRequestTimeout(),
						TimeUnit.MILLISECONDS);
	}

	/**
	 * 请求超时事件
	 * 
	 * @param timeout
	 *            超时对象
	 */
	void onRequestTimeout(Timeout timeout) {
		finalCallback(NE_REQUEST_TIMEOUT, null, null);
	}

	/**
	 * 最终回调
	 * 
	 * @param callback
	 *            回调执行
	 */
	void finalCallback(int errorCode, HttpResponse response, String content) {

		NettyHttpRequestCallback cb = requestConfig.getCallback();

		if (clearResource() && cb != null) {

			try {
				cb.callback(errorCode, response, content);
			} catch (Throwable e) {
				_log.error("NettyHttpRequest::Callback. ", e);
			}

			requestMission.missionFinish();
		}
	}

	/**
	 * 清理资源
	 */
	boolean clearResource() {
		if (!release) {
			release = true;

			// 关闭客户端
			client.disconnect();

			// 取消定时器
			if (requestTimeout != null)
				requestTimeout.cancel();

			return true;
		}

		return false;
	}

	class NettyHttpClientHandler extends TcpClientHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.hxms.wx.client.netty.tcp.TcpClientHandler#configNewChannel(io
		 * .netty .channel.socket.nio.NioSocketChannel)
		 */
		@Override
		public void configNewChannel(NioSocketChannel channel) {
			super.configNewChannel(channel);

			ChannelPipeline pipeline = channel.pipeline();

			// 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
			pipeline.addLast("decoder", new HttpResponseDecoder());

			// 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
			pipeline.addLast("encoder", new HttpRequestEncoder());

			// 接收的请求累计器
			pipeline.addLast("aggegator", new HttpObjectAggregator(0x30000));

			// mime 类型写出
			pipeline.addLast("streamew", new ChunkedWriteHandler());

			// 添加解压器
			pipeline.addLast("decompressor", new HttpContentDecompressor());

			// add new handler
			pipeline.addLast("handler", new NettyHttpRequestChannelHandler());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.hxms.wx.client.netty.tcp.TcpClientHandler#onConnectFinish(boolean
		 * , io.netty.channel.socket.nio.NioSocketChannel, java.lang.Throwable,
		 * int)
		 */
		@Override
		public void onConnectFinish(boolean success, NioSocketChannel channel,
				Throwable LastThrowable, int retryCount) {
			// 连接不成功回调
			if (!success) {
				finalCallback(NE_CONNECT_FAIL, null, null);
			}
		}
	}

	/**
	 * http request channel handler class
	 * 
	 * @author Hxms
	 *
	 */
	class NettyHttpRequestChannelHandler extends
			SimpleChannelInboundHandler<HttpObject> {

		HttpResponse response;
		String content;

		NettyHttpRequestChannelHandler() {
			super(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty
		 * .channel.ChannelHandlerContext)
		 */
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			sendRequest();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * io.netty.channel.ChannelInboundHandlerAdapter#channelInactive(io.
		 * netty.channel.ChannelHandlerContext)
		 */
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			finalCallback(NE_REQUEST_LOST_CONNECT, null, null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty
		 * .channel.ChannelHandlerContext, java.lang.Object)
		 */
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
				throws Exception {
			boolean finish = false;

			// block http message
			if (msg instanceof HttpResponse) {
				response = (HttpResponse) msg;
				// fill http message
				if (response instanceof FullHttpResponse) {
					FullHttpResponse rsp = (FullHttpResponse) msg;
					content = decodeHtmlString(rsp.content());
					finish = true;
				}
			} else if (msg instanceof HttpContent) {
				// final block
				if (msg instanceof LastHttpContent) {
					finish = true;
				} else {
					// content block
					HttpContent content = (HttpContent) msg;
					this.content = decodeHtmlString(content.content());
				}
			}

			// 完成请求
			if (finish) {
				finalCallback(NE_OK, response, this.content);
			}
		}

		/**
		 * 解码 http 请求
		 * 
		 * @param buf
		 *            缓冲区
		 * @return 结果字符串
		 */
		String decodeHtmlString(ByteBuf buf) {
			try {
				return buf.toString(CharsetUtil.UTF_8);
			} catch (Exception e) {
				_log.error("decodeHtmlString ::  " + e);
			} finally {
				buf.release();
			}
			return null;
		}

	}
}
