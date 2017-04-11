package org.server.core;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.server.command.LocalCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象标准服务器接口
 *
 * @author Administrator
 * @param <C> 服务器配置类型
 */
public abstract class AbstractStandardServer<C extends AbstractServerConfig> implements StandardServerInterface {

	private static final Logger logger = LoggerFactory.getLogger(AbstractStandardServer.class);
	
	private final C _serverConfig;
	
	/**
     * 构造函数
     *
     * @param _serverConfig 服务器配置
     */
    public AbstractStandardServer(C _serverConfig) {
        this._serverConfig = _serverConfig;
    }
    
    /**
     * @return the _serverConfig
     */
	public C getServerConfig() {
		return _serverConfig;
	}

	@Override
	public boolean executeCommand(String cmd) {
		return LocalCommandService.getInstance().execute(cmd);
	}

	@Override
	public void start() {
		try {
			_serverConfig.readConfig();
		} catch (Exception e) {
			logger.error("读取配置文件失败：", e);
		}
		logger.info(String.format("[%s] is Runing...", getClass().toString()));
		try {
			if (beforeStart()) {
				if (start0()) {
					if (afterStart()) {
						// 注册通用命令
						LocalCommandService.getInstance().register("exit", this::stop);
					}
				}
			}
		} catch (Exception e) {
			 logger.error("尝试启动服务失败：", e);
		}
	}

	@Override
	public void stop() {
		try {
			if (beforeStop()) {
				if (stop0()) {
					if (afterStop()) {
						
					}
				}
			}
		} catch (Exception e) {
			logger.error("尝试停止服务失败：", e);
		}
		logger.info("退出服务.");
        System.exit(0);
	}
	
    /**
     * 服务器启动开始之前
     *
     * @return 操作是否成功
     */
	protected boolean beforeStart(){
		return true;
	}
	
    /**
     * 服务器关闭之前
     *
     * @return 操作是否成功
     */
	protected boolean beforeStop(){
		return true;
	}
	
	/**
     * 服务启动成功
     *
     * @return 操作是否成功
     */
	protected boolean afterStart(){
		return true;
	}
	
	/**
     * 服务成功停止
     *
     * @return 操作是否成功
     */
	protected boolean afterStop(){
		return true;
	}
	
    /**
     * 服务开始执行主函数
     *
     * @return 操作是否成功
     */
    protected boolean start0() {
        return true;
    }

    /**
     * 服务停止执行主函数
     *
     * @return 操作是否成功
     */
    protected boolean stop0() {
        return true;
    }
    
    /**
     * 注册 jmx 管理
     *
     * @param name 管理名称
     * @param target 管理对象
     * @return 是否注册成功
     */
    protected boolean registerMBean(String name, Object target){
    	
    	try {
    		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    		mBeanServer.registerMBean(target, new ObjectName("hxms.server:name=" + name));
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
			logger.error("[Jmx] 注册失败，", e);
            return false;
		}
    	return true;
    }
}
