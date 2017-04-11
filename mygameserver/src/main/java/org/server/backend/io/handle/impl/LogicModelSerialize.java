package org.server.backend.io.handle.impl;

import org.server.backend.io.handle.ILogicModelSerialize;
import org.server.backend.io.handle.serialize.ProtobufSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LogicModelSerialize implements ILogicModelSerialize{
	
	Protobuf {
		
		ProtobufSerialize serialize = new ProtobufSerialize();
		
		@Override
		public boolean isSupport(Class<?> type) {
			return serialize.isSupport(type);
		}

		@Override
		public byte[] serialize(Object obj) {
			return serialize.serialize(obj);
		}

		@Override
		public Object deserialization(Class<?> type, byte[] data) {
			return serialize.deserialization(type, data);
		}
	};
	
	static final Logger log = LoggerFactory.getLogger(LogicModelSerialize.class);
	
	@Override
	public boolean isSupport(Class<?> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] serialize(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object deserialization(Class<?> type, byte[] data) {
		throw new UnsupportedOperationException();
	}

}
