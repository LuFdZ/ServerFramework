package org.server.core.data;

import java.io.InputStream;
import java.io.StringReader;

import jodd.io.StreamUtil;
import jodd.jerry.Jerry;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.server.tools.Toolset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxoolHelper {
	static final Logger log = LoggerFactory.getLogger(ProxoolHelper.class);
	
	public static final String ProxoolFilename = "proxool_config.xml";
	public static final String ProxoolAliasname = "datasource";
	public static final String ConnectionProviderClass = "org.hibernate.connection.ProxoolConnectionProvider";

	public static String ProxoolConfigTemplate = null;

	public static void addProxoolConfig(StandardServiceRegistryBuilder registryBuilder) {
		registryBuilder.applySetting("hibernate.connection.provider_class", ConnectionProviderClass);
		registryBuilder.applySetting("hibernate.proxool.pool_alias", ProxoolAliasname);
		registryBuilder.applySetting("hibernate.proxool.existing_pool", "true");

		log.info("[ProxoolHelper] using [{}] .", ConnectionProviderClass);
		log.info("[ProxoolHelper] using alias [{}] .", ProxoolAliasname);
	}
	
	public static void configProxoolAlias(String jdbcUsername, String jdbcPassword, String jdbcUrl,
			String jdbcDriverClassname, int fixedConnectioinSize) {
		try {
			StringBuffer htmlText = new StringBuffer();

			if (ProxoolConfigTemplate != null && !ProxoolConfigTemplate.isEmpty()) {
				htmlText.append(ProxoolConfigTemplate);
			} else {
				try (InputStream resource = ClassLoader.getSystemResourceAsStream(ProxoolFilename)) {
					byte[] resourceConfigDatas = StreamUtil.readBytes(resource);
					htmlText.append(new String(resourceConfigDatas, java.nio.charset.StandardCharsets.UTF_8));
				}
			}

			Jerry doc = Jerry.jerry(htmlText.toString());

			for (Jerry node : doc.$("proxool")) {
				if (node.$("alias").text().equals(ProxoolAliasname)) {

					node.$("driver-url").text(jdbcUrl);
					node.$("driver-class").text(jdbcDriverClassname);

					Jerry driverProperties = node.$("driver-properties property");
					for (Jerry prop : driverProperties) {
						if (prop.attr("name").equals("user"))
							prop.attr("value", jdbcUsername);
						else if (prop.attr("name").equals("password"))
							prop.attr("value", jdbcPassword);
					}

					if (fixedConnectioinSize != 0) {
						node.$("maximum-connection-count").text(String.valueOf(fixedConnectioinSize));
						node.$("minimum-connection-count").text(String.valueOf(fixedConnectioinSize));
					}
					break;
				}
			}
			
			String configXml = doc.html();
			configXml = Toolset.deleteCRLFOnce(configXml);
			
			Toolset.prettyOutput(log,
					"ProxoolHelper Config {nl}{}",
					configXml);
			try (StringReader stringReader = new StringReader(configXml)) {
				JAXPConfigurator.configure(stringReader, false);
			}

		} catch (Throwable e) {
			log.info("[ProxoolHelper] initialize proxoool exceptioin !!!.", e);
		}
	}
}
