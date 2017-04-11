package org.server.frontend.jmx;

/**
 * 前台服务 Jmx MBean 服务接口
 */
public interface FrontendMXBean {
	/**
     * 获得服务名称
     *
     * @return 服务名称
     */
    public String getServerName();

    /**
     * 获得服务连接状态表
     *
     * @return 状态表
     */
    public FrontendConnectTable[] getConnectTable();
}
