package org.server.core.message;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.server.core.message.callback.MessageCallback;
import org.server.core.message.callback.SingeMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messager {
	static final Logger _log = LoggerFactory.getLogger(Messager.class);
    static final Messager _INSTANCE = new Messager();

    final List<WeakReference<MessageReceiver<?>>> _receiver = new ArrayList<>();
    final ReferenceQueue<MessageReceiver<?>> _referenceQueue = new ReferenceQueue<>();
    /**
     * 获得消息实例
     *
     * @return
     */
    public static Messager getInstance() {
        return _INSTANCE;
    }

    /**
     * 私有构造函数
     */
    private Messager() {
    }

    /**
     * 注册消息接收器
     *
     * @param <T> 键类型
     * @param keyClass 键类
     * @param filter 过滤器
     * @param callback 回调函数
     * @return 消息接收器
     */
    <T> MessageReceiver<T> register0(
            Class<T> keyClass,
            Function<T, Boolean> filter,
            MessageCallback callback) {
        MessageReceiver<T> result = new MessageReceiver<>(keyClass, filter, callback);
        synchronized (_receiver) {
            _receiver.add(new WeakReference<>(result, _referenceQueue));
        }
        return result;
    }

    /**
     * 注册消息接收器
     *
     * @param <T> 键类型
     * @param <R> 回调参数类型
     * @param keyClass 键类
     * @param filter 过滤器
     * @param callback 回调函数
     * @return 消息接收器
     */
    public <T, R> MessageReceiver<T> register(
            Class<T> keyClass,
            Function<T, Boolean> filter,
            SingeMessageCallback<R> callback) {
        return register0(keyClass, filter, callback);
    }

    /**
     * 发送消息
     *
     * @param key 键
     * @param values 消息值
     */
    public void sendMessage(Object key, Object... values) {
        synchronized (_receiver) {
            for (Iterator<WeakReference<MessageReceiver<?>>> iterator = _receiver.iterator(); iterator.hasNext();) {
                WeakReference<MessageReceiver<?>> next = iterator.next();
                MessageReceiver<?> receiver = next.get();
                if (receiver != null && receiver.isActive()) {
                    receiver.invoke(key, values);
                } else {
                    iterator.remove();
                }
            }
        }
    }
}
