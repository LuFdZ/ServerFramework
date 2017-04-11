package org.server.core.message;

import java.lang.ref.WeakReference;
import java.util.function.Function;

import org.server.core.message.callback.MessageCallback;

/**
 * 消息接收者
 * @param <T> 键数据类型
 */
public class MessageReceiver<T> {
	Class<T> _cls;
    Function<T, Boolean> _filter;
    WeakReference<MessageCallback> _callback;

    public MessageReceiver(Class<T> _cls, Function<T, Boolean> _filter, MessageCallback _callback) {
        this._cls = _cls;
        this._filter = _filter;
        this._callback = new WeakReference<>(_callback);
    }

    public Class<T> getCls() {
        return _cls;
    }

    public Function<T, Boolean> getFilter() {
        return _filter;
    }

    public boolean isActive() {
        return _callback.get() != null;
    }

    @SuppressWarnings("unchecked")
    public boolean invoke(Object key, Object[] values) {
        boolean result = false;
        // 比对参数
        if (key.getClass().equals(getCls())
                && getFilter().apply((T) key)) {
            MessageCallback callback = _callback.get();
            if (callback != null) {
                callback.callback(values);
                result = true;
            }
        }
        return result;
    }
}
