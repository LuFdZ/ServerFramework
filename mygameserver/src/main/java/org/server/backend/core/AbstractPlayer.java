package org.server.backend.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

/**
 * 抽象玩家类
 * @param <T> 玩家基础类型
 */
public abstract class AbstractPlayer<T> implements Player<T> {
	
	protected static final org.slf4j.Logger _log = LoggerFactory.getLogger(AbstractPlayer.class);
	
	/**
     * 玩家帮助类
     */
    Map<Class<? extends AbstractPlayerHelper<?>>, Object> _helps = new HashMap<>();
    
    /**
     * 获得帮助类对象
     *
     * @param <H> 帮助类类型
     * @param cls 帮助类 Class 对象
     * @return 帮助类对象
     */
    @SuppressWarnings("unchecked")
	public <H extends AbstractPlayerHelper<AbstractPlayer<?>>> H getHelp(Class<H> cls) {
    	if (!_helps.containsKey(cls)) {
            try {
                H result = cls.newInstance();
                result.setPlayer(this);
                result.initialize();
                _helps.put(cls, result);
                return result;
            } catch (IllegalAccessException | InstantiationException ex) {
                _log.error("Can't Instance Help Class Object...", ex);
            }
        }
        return (H) _helps.get(cls);
    }
}
