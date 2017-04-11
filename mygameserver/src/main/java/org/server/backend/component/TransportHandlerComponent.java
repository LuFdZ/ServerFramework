package org.server.backend.component;

import java.util.ArrayList;

import org.server.backend.io.TransportCallback;

/**
 * 通讯处理组件
 *
 */
public abstract class TransportHandlerComponent implements GameComponent{
	
	ArrayList<TransportCallback<?, ?>> callbacks = new ArrayList<>();

	protected void registerCallback() {}
}
