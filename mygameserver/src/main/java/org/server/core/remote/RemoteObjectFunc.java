package org.server.core.remote;

import java.rmi.RemoteException;

/**
 * 远程对象操作接口
 *
 * @param <T> 远程接口类型F
 * @param <R> 返回参数类型
 */
@FunctionalInterface
public interface RemoteObjectFunc<T, R> {
	R run(T obj) throws RemoteException;
}
