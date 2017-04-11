package org.server.backend.core;

import java.util.HashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象读取器
 * @param <T> 用户类型
 */
public abstract class AbstractGeter<T> {

	protected static final Logger _log = LoggerFactory.getLogger(AbstractGeter.class);
	
	final HashMap<String, Function<Object, T>> _registerGetFunction = new HashMap<>();
	
	/**
     * 读取模块
     *
     * @param type 键类型
     * @param key 键值
     * @return 模块值
     */
    public synchronized T get(String type, Object key) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        } else if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (_registerGetFunction.containsKey(type)) {
            Function<Object, T> geter = _registerGetFunction.get(type);
            return geter.apply(key);
        } else {
            _log.error("[AbstractUserCenter] 无法根据类型找到对应读取器.");
            return null;
        }
    }
    
    /**
     * 注册读取器
     *
     * @param type 读取器值
     * @param geter 读取器
     */
    protected void registerGeter(String type, Function<Object, T> geter) {
        _registerGetFunction.put(type, geter);
    }
}
