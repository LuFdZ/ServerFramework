package org.server.core.remote;

import java.rmi.RemoteException;

/**
 * 远程对象操作接口
 * @param <T> 远程接口类型F
 */
@FunctionalInterface
public interface RemoteObjectAction<T> {
	void run(T obj) throws RemoteException;
}
