package org.server.backend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.server.core.AbstractServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class BackendServerConfig extends AbstractServerConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(BackendServerConfig.class);
	
	// 后台自定义配置项
	BackendServerCustomConfig _customConfig;
		
	// 后台自定义 json 配置文件
	Map<String, JSONObject> _configs = new HashMap<String, JSONObject>();
		
	public BackendServerConfig() {
		super("config/backend.xml");
	}
	
	@Override
	public void readConfig(Document document) {
		_customConfig = new BackendServerCustomConfig(document);
		tryToReadJsonConfigs();
	}
	
	public BackendServerCustomConfig getCustomConfig() {
		return _customConfig;
	}
	
	/**
	 * 尝试读取 json 配置
	 */
	void tryToReadJsonConfigs() {
		Path configPath = Paths.get("config");
		File configFile = configPath.toFile();

		if (!configFile.exists() || !configFile.isDirectory())
			return;

		File[] jsonFiles = configFile.listFiles((f, p) -> p.contains(".json"));

		for (File file : jsonFiles) {

			byte[] fileData = null;

			try {
				fileData = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			} catch (IOException ignore) {
				continue;
				// ignore
			}

			JSONObject config = null;

			if ((config = tryToJson(fileData, StandardCharsets.UTF_8)) != null)
				_configs.put(file.getName().replace(".json", ""), config);

			else if ((config = tryToJson(fileData, Charset.forName("GBK"))) != null)
				_configs.put(file.getName().replace(".json", ""), config);
		}
	}
	
	JSONObject tryToJson(byte[] fileData, Charset charset) {
		try {
			return new JSONObject(new String(fileData, charset));
		} catch (Throwable ignore) {
			logger.error("try to parse json get error message : {}", ignore.getMessage());
		}
		return null;
	}
	
}
