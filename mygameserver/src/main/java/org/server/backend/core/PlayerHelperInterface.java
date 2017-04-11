package org.server.backend.core;

/**
 * 玩家帮助接口
 * @param <?>基础模块类型
 * @param <P>玩家模块类型
 */
public interface PlayerHelperInterface<P extends Player<?>> {
	/**
     * 获得玩家
     *
     * @return 玩家实例
     */
    P getPlayer();
}
