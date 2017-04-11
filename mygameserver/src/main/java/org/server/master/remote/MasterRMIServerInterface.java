package org.server.master.remote;

import java.rmi.RemoteException;

import org.server.core.IPAddress;
import org.server.core.remote.RMIServiceInterface;

/***
 * 
 * @author Administrator
 *
 */
public interface MasterRMIServerInterface extends RMIServiceInterface {
	
	/***
	 * 注册服务
	 * @param key
	 * @param server
	 * @return
	 * @throws RemoteException
	 */
	public boolean registerServer(String key,RMIServiceInterface server) throws RemoteException;
	
	/***
	 * 获得RMI服务从服务钥匙检索
	 * @param key
	 * @return
	 * @throws RemoteException
	 */
	public IPAddress getRMIAddressByKey(String key)throws RemoteException;
}
