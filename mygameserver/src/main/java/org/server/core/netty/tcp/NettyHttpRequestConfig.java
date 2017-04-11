package org.server.core.netty.tcp;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder.ErrorDataEncoderException;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * netty http 请求组装器
 *
 */
public class NettyHttpRequestConfig {
	URI uri;
	HttpMethod method = HttpMethod.GET;
	HttpVersion version = HttpVersion.HTTP_1_1;
	String userAgent = "Dalvik/1.6.0 (Linux; U)";
	String content;
	Map<String, Object> form;

	/*
	 * 连接超时时间 , 请求超时时间 , 请求连接重试次数
	 */
	int connectTimeout = 3000, requestTimeout = 10000, retryConnectCount = 3;

	/**
	 * http 回调
	 */
	NettyHttpRequestCallback callback;

	private NettyHttpRequestConfig(URI uri) {
		this.uri = uri;
	}

	/**
	 * @return the method
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public NettyHttpRequestConfig setMethod(HttpMethod method) {
		this.method = method;
		return this;
	}

	/**
	 * @return the version
	 */
	public HttpVersion getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public NettyHttpRequestConfig setVersion(HttpVersion version) {
		this.version = version;
		return this;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public NettyHttpRequestConfig setContent(String content) {
		this.content = content;
		return this;
	}

	/**
	 * @return the form
	 */
	public Map<String, Object> getForm() {
		return form;
	}

	/**
	 * @param form
	 *            the form to set
	 */
	public NettyHttpRequestConfig setForm(Map<String, Object> form) {
		this.form = form;
		return this;
	}

	/**
	 * 
	 * 获得请求回调函数
	 * 
	 * @return the callback
	 */
	public NettyHttpRequestCallback getCallback() {
		return callback;
	}

	/**
	 * 
	 * 设置请求回调
	 * 
	 * @param callback
	 *            the callback to set
	 */
	public NettyHttpRequestConfig setCallback(NettyHttpRequestCallback callback) {
		this.callback = callback;
		return this;
	}

	/**
	 * 
	 * 获得 socket 连接超时时间
	 * 
	 * @return the connectTimeout
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * 
	 * 设置连接超时时间
	 * 
	 * @param connectTimeout
	 *            the connectTimeout to set
	 */
	public NettyHttpRequestConfig setConnectTimeout(int connectTimeout) {
		this.connectTimeout = Math.max(connectTimeout, 1);
		return this;
	}

	/**
	 * 
	 * 获得请求超时时间
	 * 
	 * @return the requestTimeout
	 */
	public int getRequestTimeout() {
		return requestTimeout;
	}

	/**
	 * 
	 * 设置请求时间
	 * 
	 * @param requestTimeout
	 *            the requestTimeout to set
	 */
	public NettyHttpRequestConfig setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
		return this;
	}

	/**
	 * 
	 * 获得 user agent 头
	 * 
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * 
	 * 设置 user agent 头
	 * 
	 * @param userAgent
	 *            the userAgent to set
	 */
	public NettyHttpRequestConfig setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	/**
	 * 
	 * 获得重试连接次数
	 * 
	 * @return the retryConnectCount
	 */
	public int getRetryConnectCount() {
		return retryConnectCount;
	}

	/**
	 * 
	 * 设置重试连接次数
	 * 
	 * @param retryConnectCount
	 *            the retryConnectCount to set
	 */
	public NettyHttpRequestConfig setRetryConnectCount(int retryConnectCount) {
		this.retryConnectCount = Math.max(retryConnectCount, 0);
		return this;
	}

	/**
	 * 获得链接的地址
	 * 
	 * @return 地址
	 */
	protected String getHostname() {
		return uri.getHost();
	}

	/**
	 * 获得端口
	 * 
	 * @return 端口
	 */
	protected int getPort() {
		return uri.getPort() == -1 ? 80 : uri.getPort();
	}

	/**
	 * 获得表单对象
	 * 
	 * @return
	 * @throws ErrorDataEncoderException
	 *             无法编译 post 数据
	 */
	HttpRequest getFormObject(HttpRequest request)
			throws ErrorDataEncoderException {

		if (form == null)
			return null;

		Object form_multipart = form.get("form_multipart");
		form.remove("form_multipart");

		// 解析值

		boolean multipart = false;
		if (form_multipart instanceof Boolean)
			multipart = (boolean) form_multipart;

		// 编码属性
		HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(request,
				multipart);

		// 处理属性
		for (Entry<String, Object> item : form.entrySet()) {

			// 根据值处理属性

			if (item.getValue() instanceof String) {
				encoder.addBodyAttribute(item.getKey(),
						(String) item.getValue());
			}

		}

		return encoder.finalizeRequest();
	}

	/**
	 * 获得 http 请求内容
	 * 
	 * @return 请求内容
	 * @throws ErrorDataEncoderException
	 *             无法编译 post 数据
	 */
	protected HttpObject[] getHttpObjects() throws ErrorDataEncoderException {

		// handler the uri

		String requestUri = null;
		String path = uri.getRawPath();
		String query = uri.getRawQuery();
		URI uri = this.uri;

		// concat uri

		if (query == null || query.isEmpty())
			requestUri = path;
		else
			requestUri = String.format("%s?%s", path, query);

		// create new path

		uri = URI.create(requestUri);

		// create request

		DefaultFullHttpRequest request = new DefaultFullHttpRequest(version,
				method, uri.toASCIIString());

		// config request header

		request.headers().set(HttpHeaders.Names.HOST, this.uri.getHost());

		// 添加压缩头

		request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING,
				"gzip, deflate");

		// added user agent

		request.headers().set(HttpHeaders.Names.USER_AGENT, userAgent);

		// encoder post
		HttpRequest finalRequest = getFormObject(request);

		// if encoder is null
		if (finalRequest == null) {
			finalRequest = request;
		}

		return new HttpObject[] { finalRequest };
	}

	/**
	 * 创建请求配置
	 * 
	 * @param uri
	 *            地址
	 * @return 配置
	 */
	public static NettyHttpRequestConfig create(URI uri) {
		return new NettyHttpRequestConfig(uri);
	}
}
