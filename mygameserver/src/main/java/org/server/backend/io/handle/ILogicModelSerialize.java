package org.server.backend.io.handle;

public interface ILogicModelSerialize {

	public boolean isSupport(Class<?> type);
	
	public byte[] serialize(Object obj);

	public Object deserialization(Class<?> type, byte[] data);
}
