package org.server.backend.io.handle.serialize;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.server.backend.io.handle.ILogicModelSerialize;
import org.server.backend.io.handle.impl.MethodCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

public class ProtobufSerialize implements ILogicModelSerialize{
	
	static final Logger log = LoggerFactory.getLogger(ProtobufSerialize.class);
	
	final Class<MessageLite> messageBaseClass = MessageLite.class;
	final Map<Object, MethodCall> protoDeserializationAccess = new HashMap<>();
	MethodCall protoSerializeCall;
	
	void registerSerializeAccess() {
		if (protoSerializeCall == null) {
			try {
				Method method = messageBaseClass
						.getDeclaredMethod("toByteArray");
				protoSerializeCall = new MethodCall(null, messageBaseClass,
						method);
			} catch (NoSuchMethodException | SecurityException e) {
				log.error(
						"[LogicModelSerialize] cannot find toByteArray method [{}].",
						messageBaseClass.getSimpleName());
			}
		}
	}
	
	void registerAccess(Class<?> type) {
		registerSerializeAccess();
		if (protoDeserializationAccess.containsKey(type))
			return;
		try {
			Method method = type.getDeclaredMethod("parseFrom", byte[].class);
			MethodCall methodCall = new MethodCall(null, type, method);
			protoDeserializationAccess.put(type, methodCall);
		} catch (NoSuchMethodException | SecurityException e) {
			log.error(
					"[LogicModelSerialize] cannot find parseFrom method [{}].",
					type.getSimpleName());
		}
	}
	
	@Override
	public boolean isSupport(Class<?> type) {
		boolean result = messageBaseClass.isAssignableFrom(type);
		if (result)
			registerAccess(type);
		return result;
	}

	@Override
	public byte[] serialize(Object obj) {
		return (byte[]) protoSerializeCall.invokeWithContext(obj);
	}

	@Override
	public Object deserialization(Class<?> type, byte[] data) {
		if (!protoDeserializationAccess.containsKey(type))
			return null;
		return protoDeserializationAccess.get(type).invoke(data);
	}

}
