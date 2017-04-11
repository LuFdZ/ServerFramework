package org.server.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏属性资源管理器
 */
public class GameAttributeResource {
	private static GameAttributeResource _instance;

    public static GameAttributeResource getInstance() {
        if (_instance == null) {
            _instance = new GameAttributeResource();
        }
        return _instance;
    }

    private final ConcurrentHashMap<SimpleEntry<Object, Object>, Object> _map;

    private GameAttributeResource() {
        this._map = new ConcurrentHashMap<>();
    }

    /**
     * 获得实例
     *
     * @param key1 键值1
     * @param key2 键值2
     * @return 实例对象
     */
    public Object get(Object key1, Object key2) {
        return _map.get(new SimpleEntry<>(key1, key2));
    }

    /**
     * 设置实例
     *
     * @param key1 键值1
     * @param key2 键值2
     * @param value 实例对象
     */
    public void set(Object key1, Object key2, Object value) {
        _map.put(new SimpleEntry<>(key1, key2), value);
    }

    /**
     * 删除实例
     *
     * @param key1 键值1
     * @param key2 键值2
     * @return 删除的值
     */
    public Object remove(Object key1, Object key2) {
        return _map.remove(new SimpleEntry<>(key1, key2));
    }
}
