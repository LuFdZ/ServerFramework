package org.server.core.remote;

import java.rmi.RemoteException;

/**
 * 支持关机服务接口
 */
public interface ShutdownServiceInterface {
	
	/**
     * 关闭服务器
     *
     * @return 关闭是否成功
     * @throws RemoteException
     */
	public boolean shutdown() throws RemoteException;
}
