package org.server.command;

/**
 * 本地命令回调函数
 *
 */
@FunctionalInterface
public interface LocalCommandCallback {
	
	void callback();
}
