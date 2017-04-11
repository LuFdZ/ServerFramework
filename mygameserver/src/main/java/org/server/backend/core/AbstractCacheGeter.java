package org.server.backend.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存读取器
 * @param <T>
 *            用户类型
 */
public abstract class AbstractCacheGeter<T> extends AbstractGeter<T> {
	protected final ConcurrentHashMap<Object, T> _cache = new ConcurrentHashMap<>();
	protected boolean _enable = true;
	
	/**
	 * 读取
	 * 
	 * @param type
	 *            类型
	 * @param key
	 *            键
	 * @param ignoreCache
	 *            是否忽略缓存
	 * @return 要读取的类
	 */
	public synchronized T get(String type, Object key) {
		return get(type, key, false);
	}
	
	/**
	 * 读取
	 * 
	 * @param type
	 *            类型
	 * @param key
	 *            键
	 * @param ignoreCache
	 *            是否忽略缓存
	 * @return 要读取的类
	 */
	public synchronized T get(String type, Object key, boolean ignoreCache) {

		T result = null;

		if (!ignoreCache) {

			result = getFromCache(key);

		}

		if (result == null) {

			result = super.get(type, key);

			// auto register object to cache

			if (_enable && result != null) {
				registerCache(result);
			}

		}

		return result;

	}
	
	/**
	 * 从缓存信息读取模块
	 * 
	 * @param key
	 *            关键字
	 * @return 模块
	 */
	protected T getFromCache(Object key) {
		if (_enable && _cache.containsKey(key)) {
			return _cache.get(key);
		}
		return null;
	}

	/**
	 * 清理缓存
	 */
	public void clearCache() {
		synchronized (this) {
			_cache.clear();
		}
	}

	/**
	 * 启用缓存
	 */
	public void enableCache() {
		this._enable = true;
	}

	/**
	 * 禁用缓存
	 */
	public void disabledCache() {
		this._enable = false;
	}

	/**
	 * 注册缓存
	 *
	 * @param result
	 *            用户对象
	 */
	protected void registerCache(T result) {
		if (!_enable) {
			return;
		}
		try {
			synchronized (this) {
				initialize0(result);
				registerCache0(result);
			}
		} catch (Exception e) {
			_log.error("[AbstractUserCenter] : 尝试注册缓存异常：", e);
		}
	}

	/**
	 * 注册缓存
	 *
	 * @param model
	 *            用户对象模块
	 */
	protected abstract void registerCache0(T model);

	/**
	 * 初始化数据模块
	 *
	 * @param model
	 *            用户数据模块
	 */
	protected abstract void initialize0(T model);
}
