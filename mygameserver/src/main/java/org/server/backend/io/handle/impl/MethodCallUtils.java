package org.server.backend.io.handle.impl;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public class MethodCallUtils {
	public static Stream<Class<?>> getMethodParams(Method method) {
		return Stream.of(method.getParameters()).map(x -> (Class<?>) x.getParameterizedType());
	}

	public static Class<?>[] getMethodParamsArray(Method method) {
		return getMethodParams(method).toArray(Class<?>[]::new);
	}
}
