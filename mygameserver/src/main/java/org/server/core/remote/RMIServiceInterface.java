package org.server.core.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.server.core.IPAddress;

/***
 * RMI服务器接口
 */
public interface RMIServiceInterface extends Remote {

	/**
     * 通信测试
     *
     * @return 延时测试是否成功
     * @throws RemoteException
     */
    boolean ping() throws RemoteException;
    
    /**
     * 获得RMI服务地址
     *
     * @return
     * @throws RemoteException
     */
    IPAddress getServerRMIAddress() throws RemoteException;

}
