package org.server.frontend.ace;

/**
 * Ace 服务器核心消息会话
 */
public class AceSession {
	int mAcceptorId;
    long mSessionId;
    String mAddress;
    
    private AceSession() {
    }
    
    /**
     * 获得消息会话编号
     *
     * @return 编号
     */
    public long getId() {
        return mSessionId;
    }

    /**
     * 获得客户端地址
     *
     * @return 客户端地址
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * 写入数据
     *
     * @param command 命令头值
     * @param data 数据数组
     * @return 写入是否成功
     */
    private native boolean write0(short command, byte[] data);

    /**
     * 关闭会话
     *
     * @return 是否关闭成功
     */
    private native boolean close0();

    /**
     * 发送消息函数
     *
     * @param message 消息数组
     * @param command 消息命令头值
     */
    public void write(short command, byte[] message) {
        write0(command, message);
    }

    /**
     * 关闭会话
     * @return 关闭是否成功
     */
    public boolean close() {
        return close0();
    }
}
