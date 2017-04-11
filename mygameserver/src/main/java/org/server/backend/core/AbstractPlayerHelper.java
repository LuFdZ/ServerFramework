package org.server.backend.core;

/**
 * 玩家帮助抽象类
 * @param <T> 玩家抽象类型
 */
public abstract class AbstractPlayerHelper<T extends AbstractPlayer<?>> implements PlayerHelperInterface<T> {
	
	T _player;// 保存玩家
	
	/**
     * 初始化函数
     */
    public abstract void initialize();

    /**
     * 读取玩家
     *
     * @return 读取玩家对象
     */
    @Override
    public T getPlayer() {
        return _player;
    }

    /**
     * 设置玩家对象
     *
     * @param _player 玩家对象
     */
    protected void setPlayer(T _player) {
        this._player = _player;
    }
}
