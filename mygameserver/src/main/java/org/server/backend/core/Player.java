package org.server.backend.core;

/**
 * 玩家接口类
 * @param <T> 基础模块类型
 */
public interface Player<T> {
	/**
     * 获得基础模块
     *
     * @return 基础模块实例
     */
    T getBasicModel();
}
