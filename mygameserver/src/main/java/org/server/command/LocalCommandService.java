package org.server.command;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 本地服务命令
 * 
 * @author Administrator
 *
 */
public class LocalCommandService {

	static final Logger logger = LoggerFactory.getLogger(LocalCommandService.class);
	static LocalCommandService _instance;

	ConcurrentHashMap<String, LocalCommandCallback> _commands = new ConcurrentHashMap<String, LocalCommandCallback>();

	public static LocalCommandService getInstance() {
		if (_instance == null) {
			_instance = new LocalCommandService();
		}
		return _instance;
	}

	/***
	 * 注册命令
	 * 
	 * @param command
	 * @param consumer
	 */
	public void register(String command, LocalCommandCallback consumer) {
		_commands.put(command.toLowerCase(), consumer);
	}

	/**
	 * 获得命令
	 *
	 * @param command
	 *            命令字符串
	 * @return 回调函数对象
	 */
	public LocalCommandCallback getCommand(String command) {
		return _commands.get(command);
	}

	/***
	 * 执行命令
	 * @param input
	 * @return
	 */
	public boolean execute(String input) {
		LocalCommandCallback callback = _commands.get(input);
		if (callback != null) {
			logger.info(String.format("ExecuteCommand[%s]......", input));
			try {
				callback.callback();
			} catch (Exception e) {
				logger.error("LocalCommandService[execute] 执行回调命令错误：", e);
			}
			return true;
		}else{
			logger.error(String.format("Can't Not Find Command [%s]?!", input));
		}
		return false;
	}
}
