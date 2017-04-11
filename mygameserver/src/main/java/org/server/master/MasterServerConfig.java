package org.server.master;

import java.util.HashMap;
import java.util.Map;



import org.server.core.AbstractServerConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class MasterServerConfig extends AbstractServerConfig {

	private final Map<String, ManagedServerInformationNode> _managedServers = new HashMap<>();

	public MasterServerConfig() {
		super("config/master.xml");
	}

	@Override
	public void readConfig(Document document) {
		// 读取服务类型与钥匙
		NodeList list = document.getDocumentElement().getElementsByTagName("ServerInformation");
		if (list.getLength() > 0) {
			Element e = (Element)list.item(0);
			list = e.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node item = list.item(i);
				if (item instanceof Element) {
					e = (Element)item;
					String key = e.getAttribute("Key");
					_managedServers.put(key, new ManagedServerInformationNode(e.getTextContent(), key));
				}
			}
		}

	}

	/**
	 * 获得托管服务信息
	 *
	 * @return 信息集合
	 */
	public Map<String, ManagedServerInformationNode> getManagedServers() {
		return _managedServers;
	}
}
