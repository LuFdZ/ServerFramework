package org.server.core.message.callback;

/**
 * 单参数消息回调
 * @param <T1> 自定义类型
 */
public interface SingeMessageCallback<T1> extends MessageCallback {
	/**
     * 回调接口
     *
     * @param argT1 参数 T1
     */
    void callback(T1 argT1);

    @SuppressWarnings("unchecked")
    @Override
    default Object callback(Object... args) {
        try {
            if (args.length >= 1) {
                callback((T1) args[0]);
            }
        } catch (ClassCastException cce) {
            // nothing 
        } catch (Exception e) {
            MessageCallback.getLogger().error("Error Callback Runtime Expcetion:", e);
        }
        return null;
    }
}
