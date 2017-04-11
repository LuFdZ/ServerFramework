package org.server.frontend.ace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ace 服务器核心
 */
public class AceCore {
	/**
     * 日志对象
     */
    static final Logger _log = LoggerFactory.getLogger(AceCore.class);

    static AceCore _instance;

    /**
     * 欢迎方法
     */
    native void sayHello();

    /**
     * 开始方法
     */
    native boolean start(int localPort);

    /**
     * 会话打开事件
     *
     * @param session 会话
     */
    protected void sessionOpened(AceSession session) {
        _log.info("会话开始：" + session.getAddress());
    }

    /**
     * 会话关闭事件
     *
     * @param session 会话对象
     */
    protected void sessionClosed(AceSession session) {
        _log.info("会话关闭：" + session.getAddress());
    }

    /**
     * 消息接收函数
     *
     * @param session 会话对象
     * @param message 消息数组
     * @param command 消息命令头值
     */
    protected void messageReceived(AceSession session, short command, byte[] message) {
        _log.info("收到数据：" + session.getAddress());
        session.write(command, message);
        session.close();
    }

    /**
     * 获得实例
     *
     * @return Ace实例
     */
    public static AceCore getInstance() {
        if (_instance == null) {
            _instance = new AceCore();
        }
        return _instance;
    }
}
