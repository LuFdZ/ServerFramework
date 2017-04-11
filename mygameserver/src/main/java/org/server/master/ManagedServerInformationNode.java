package org.server.master;

import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import org.server.core.IPAddress;
import org.server.core.remote.RMIServiceInterface;
import org.server.core.remote.ShutdownServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 托管服务器节点信息
 *
 * @author Administrator
 */
public class ManagedServerInformationNode extends TimerTask {

	// 日志记录程序
	private static final Logger logger = LoggerFactory.getLogger(ManagedServerInformationNode.class);
	
	// 最大超时次数
    private static final int MAX_TIMEOUT_COUNT = 3;
    
    // 定时监测器检测时间
    private static final int _timerDuration = 10000;
    
    //定时器
    private final Timer _timer;
    
    // 服务器类型名称
    private String _serverType;
    
    // 服务器钥匙
    private String _serverKey;
    
    // 远程服务器RMI对象引用
    private RMIServiceInterface _remoteService;
    
    // 远程服务器RMI地址
    private IPAddress _serverRMIAddress;

    // 远程服务器无响应次数
    private int _timeOutCount = 0;

    public ManagedServerInformationNode(String _serverType, String _serverKey){
    	this._serverType = _serverType;
        this._serverKey = _serverKey;
        this._timer = new Timer();
        _timer.schedule(this, _timerDuration, _timerDuration);
    }

    public String getServerType() {
        return _serverType;
    }

    public void setServerType(String _serverType) {
        this._serverType = _serverType;
    }

    public String getServerKey() {
        return _serverKey;
    }

    public void setServerKey(String _serverKey) {
        this._serverKey = _serverKey;
    }

    public RMIServiceInterface getRemoteService() {
        return _remoteService;
    }

    public void setRemoteService(RMIServiceInterface _remoteService) {
        clearService();
        this._remoteService = _remoteService;
    }

    public IPAddress getServerRMIAddress() {
        return _serverRMIAddress;
    }

    public void setServerRMIAddress(IPAddress _serverRMIAddress) {
        this._serverRMIAddress = _serverRMIAddress;
    }
    
    /**
     * 定时器检查服务active状态。
     */
	@Override
	public void run() {
		if (_remoteService != null) {
			try {
				_remoteService.ping();
				_timeOutCount = 0;
				
			} catch (RemoteException e) {
				logger.error(String.format("远程服务节点 [%s(%s)] 超时：%s", _serverType, _serverKey, e.getMessage()));
                _timeOutCount++;
			} finally {
                if (_timeOutCount >= MAX_TIMEOUT_COUNT) {
                    _remoteService = null;
                    logger.error(String.format("远程服务节点 [%s] 离线,节点钥匙 [%s]...", _serverType, _serverKey));
                }
            }
		}
		
	}
	
	/**
     * 清理已存在的服务。
     */
    public void clearService() {
    	if (_remoteService != null) {
			try {
				_remoteService.ping();
				ShutdownServiceInterface shutdownServiceInterface = (ShutdownServiceInterface)_remoteService;
				shutdownServiceInterface.shutdown();
			} catch (RemoteException e) {
				logger.error("尝试断开上一个连接服务失败：" + e.getMessage());
			}
		}
    }

}
