package org.server.frontend;

import org.server.core.AbstractServerConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 前台服务器配置表
 *
 * @author Administrator
 */
public class FrontendServerConfig extends AbstractServerConfig {

	private String _backendServerAddress;

	private int _backendServerPort;

	private int _frontendPort;

	private String _minaHandlerClass;

	private int _testConnectionTimer;

	public FrontendServerConfig() {
		super("config/frontend.xml");
		readConfig();
	}

	public String getBackendServerAddress() {
		return _backendServerAddress;
	}

	public void setBackendServerAddress(String _backendServerAddress) {
		this._backendServerAddress = _backendServerAddress;
	}

	public int getBackendServerPort() {
		return _backendServerPort;
	}

	public void setBackendServerPort(int _backendServerPort) {
		this._backendServerPort = _backendServerPort;
	}

	public int getFrontendPort() {
		return _frontendPort;
	}

	public void setFrontendPort(int _frontendPort) {
		this._frontendPort = _frontendPort;
	}

	public String getMinaHandlerClass() {
		return _minaHandlerClass;
	}

	public void setMinaHandlerClass(String _minaHandlerClass) {
		this._minaHandlerClass = _minaHandlerClass;
	}

	public int getTestConnectionTimer() {
		return _testConnectionTimer;
	}

	public void SetTestConnectionTimer(int _testConnectionTimer) {
		this._testConnectionTimer = _testConnectionTimer;
	}

	@Override
	public void readConfig(Document document) {
		Element docElement = document.getDocumentElement();
		// 读取后台服务器连接地址
		NodeList e = docElement.getElementsByTagName("BackendServerAddress");
		if (e.getLength() > 0) {
			Element element = (Element)e.item(0);
			setBackendServerAddress(element.getTextContent());
			setBackendServerPort(Integer.parseInt(element.getAttribute("Port")));
		}
		
		// 读取服务Socket 监听端口
		e = docElement.getElementsByTagName("FrontendPort");
        if (e.getLength() > 0) {
            Element element = (Element) e.item(0);
            setFrontendPort(Integer.parseInt(element.getTextContent()));
        }
        
        // 读取 Mina 处理类型
        e = docElement.getElementsByTagName("MinaHandler");
        if (e.getLength() > 0) {
            Element element = (Element) e.item(0);
            setMinaHandlerClass(element.getTextContent());
        }
        
        // 读取 后台链接测试循环时间 处理类型
        e = docElement.getElementsByTagName("TestConnectionTimer");
        if (e.getLength() > 0) {
            Element element = (Element) e.item(0);
            SetTestConnectionTimer(Integer.parseInt(element.getTextContent()));
        }
	}

}
