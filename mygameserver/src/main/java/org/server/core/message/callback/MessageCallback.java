package org.server.core.message.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息回调接口
 */
public interface MessageCallback {
	static final Logger _log = LoggerFactory.getLogger(MessageCallback.class);

    /**
     * 获得日志记录器
     * @return 日志记录器
     */
    public static Logger getLogger() {
        return _log;
    }

    default Object callback(Object... args) {
        return null;
    }
}
