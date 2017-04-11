package org.server.core;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class AbstractServerConfig {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractServerConfig.class);

	// 配置文件文档路径
	private final String _xmlFilePath;
	// 管理服务地址
	private String _masterAddress;
	// 管理服务端口
	private int _masterPort;
	// 服务钥匙
	private String _serverKey;
	// RMI服务地址
	private IPAddress _RMIAddress = new IPAddress();

	public AbstractServerConfig(String configPath) {
		_xmlFilePath = configPath;
	}

	/***
	 * 
	 * @return
	 */
	public String getMasterAddress() {
		return _masterAddress;
	}

	/**
	 * 设置管理服务器地址
	 *
	 * @param _masterAddress
	 *            the _masterAddress to set
	 */
	public void setMasterAddress(String _masterAddress) {
		this._masterAddress = _masterAddress;
	}

	/**
	 * 获得管理服务器端口
	 *
	 * @return the _masterPort
	 */
	public int getMasterPort() {
		return _masterPort;
	}

	/**
	 * 设置管理服务器端口
	 *
	 * @param _masterPort
	 *            the _masterPort to set
	 */
	public void setMasterPort(int _masterPort) {
		this._masterPort = _masterPort;
	}

	/**
	 * 获得当前服务钥匙
	 *
	 * @return
	 */
	public String getServerKey() {
		return _serverKey;
	}

	/**
	 * 设置当前服务钥匙
	 *
	 * @param _serverKey
	 */
	public void setServerKey(String _serverKey) {
		this._serverKey = _serverKey;
	}

	public IPAddress getRMIAddress() {
		return _RMIAddress;
	}

	public void setRMIAddress(IPAddress _RMIAddress) {
		this._RMIAddress = _RMIAddress;
	}

	/**
	 * 读取配置
	 */
	public void readConfig() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(_xmlFilePath); // 获取到xml文件
			readConfig0(doc);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			logger.error("读取配置错误：", e);
		}
	}

	/**
	 * 读取配置文件
	 *
	 * @param document
	 */
	public abstract void readConfig(Document document);

	/**
	 * 读取配置
	 *
	 * @param doc
	 *            配置文件文档
	 */
	private void readConfig0(Document doc) {
		Element docElement = doc.getDocumentElement();
		// 读取管理服务信息节点
		NodeList e = docElement.getElementsByTagName("MasterServer");
		if (e.getLength() > 0) {
			Element eSub = (Element) e.item(0);
			if (e.getLength() > 0) {
				Element ee = (Element) e.item(0);
				setMasterAddress(ee.getTextContent());
			}

			e = eSub.getElementsByTagName("Address");
			if (e.getLength() > 0) {
				Element ee = (Element) e.item(0);
				setMasterAddress(ee.getTextContent());
			}

			e = eSub.getElementsByTagName("Port");
			if (e.getLength() > 0) {
				Element ee = (Element) e.item(0);
				setMasterPort(Integer.parseInt(ee.getTextContent()));
			}
			// 结束读取管理服务信息节点

			// 读取当前服务钥匙
			// 读取服务器RMI地址信息
			e = docElement.getElementsByTagName("ServerRMIAddress");
			if (e.getLength() > 0) {
				Node node = e.item(0);
				if (node instanceof Element) {
					_RMIAddress.setIp(((Element) node).getTextContent());
					_RMIAddress.setPort(Integer.parseInt(((Element) node)
							.getAttribute("Port")));
				}
			}
			// 结束读取服务器RMI地址信息
			readConfig(doc);
		}
	}

}
