package org.server.core.netty.tcp;

import io.netty.handler.codec.http.HttpResponse;

/**
 * 
 * nehttp 回调函数
 *
 */
@FunctionalInterface
public interface NettyHttpRequestCallback {
	/**
	 * 回调函数
	 * 
	 * @param errorCode
	 *            错误代码
	 * @param response
	 *            响应
	 * @param content
	 *            响应内容
	 */
	public void callback(int errorCode, HttpResponse response, String content) throws Exception;
}
