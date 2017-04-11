package org.server.frontend.remote;

import java.net.SocketAddress;
import java.rmi.RemoteException;

import org.server.core.io.SessionMessage;
import org.server.core.remote.RMIServiceInterface;

/**
 * 前台后台交互对象
 *
 * @author Administrator
 */
public interface FrontendBackendInterface extends RMIServiceInterface{
	
	/**
     * 获得前台服务核心编号
     *
     * @return
     * @throws RemoteException
     */
    public int getId() throws RemoteException;
    
    /**
     * 设置前台服务核心编号
     *
     * @param id
     * @throws RemoteException
     */
    public void setId(int id) throws RemoteException;
	
    /**
     * 获得地址
     * @param sessionId 会话编号
     * @return 会话连接IP地址
     * @throws RemoteException 远程调用异常
     */
    public SocketAddress getAddress(long sessionId) throws RemoteException;

    /**
     * 写入数据
     * @param sessionId 会话编号
     * @param message 消息模块
     * @throws RemoteException 远程异常 
     */
    public void write(long sessionId, SessionMessage message) throws RemoteException;
}
