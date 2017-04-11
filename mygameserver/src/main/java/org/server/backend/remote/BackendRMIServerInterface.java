package org.server.backend.remote;

import java.rmi.RemoteException;

import org.server.core.io.SessionIdleType;
import org.server.core.io.SessionMessage;
import org.server.core.remote.RMIServiceInterface;
import org.server.frontend.remote.FrontendBackendInterface;

/**
 * 后台服务接口
 *
 * @author Administrator
 */
public interface BackendRMIServerInterface extends RMIServiceInterface {

	 /**
     * 注册前台服务核心
     *
     * @param service 通信对象
     * @throws RemoteException
     */
    public void registerFrontendCore(FrontendBackendInterface service) throws RemoteException;
    
    /**
     * 会话创建事件发生
     *
     * @param id 会话编号
     * @param nodeId 前台服务节点编号
     * @throws RemoteException 远程异常
     */
    public void onSessionCreated(long id,int nodeId) throws RemoteException;

    /**
     * 会话打开事件发生
     *
     * @param id 会话编号
     * @param nodeId 前台服务节点编号
     * @throws RemoteException 远程异常
     */
    public void onSessionOpend(long id,int nodeId) throws RemoteException;

    /**
     * 会话关闭事件发生
     *
     * @param id 会话编号
     * @param nodeId 前台服务节点编号
     * @throws RemoteException 远程异常
     */
    public void onSessionClosed(long id,int nodeId) throws RemoteException;
    
    /**
     * 会话空闲事件发生
     *
     * @param id 会话编号
     * @param status 空闲状态
     * @param nodeId 前台服务节点编号
     * @throws RemoteException 远程异常
     */
    public void onSessionIdle(long id, SessionIdleType status,int nodeId) throws RemoteException;
    
    /**
     * 会话接收数据
     *
     * @param id 会话编号
     * @param data 数据内容
     * @param nodeId 前台服务节点编号
     * @throws RemoteException 远程异常
     */
    public void onSessionReceived(long id, SessionMessage data,int nodeId) throws RemoteException;
    
}
