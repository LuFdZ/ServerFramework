package org.server.backend.io.handle.impl;

import java.lang.reflect.Method;

import com.esotericsoftware.reflectasm.MethodAccess;

public class MethodCall {
	Object context;

	MethodAccess methodAccess;
	int methodIdx = -1;

	Class<?>[] requireParams;
	
	public MethodCall(Object context, Class<?> contextClass, Method method) {
		this.context = context;
		this.requireParams = MethodCallUtils.getMethodParamsArray(method);

		methodAccess = MethodAccess.get(contextClass);
		methodIdx = methodAccess.getIndex(method.getName(), requireParams);
	}
	
	public Object[] orderParams(Object... actualParams) {
		if (requireParams.length > actualParams.length)
			return null;

		boolean valid = true;

		Object[] orderParams = new Object[this.requireParams.length];

		for (int i = 0; i < orderParams.length; i++) {
			Class<?> requireType = requireParams[i];

			for (int j = 0; j < actualParams.length; j++) {
				Object actualParam = actualParams[j];
				if (actualParam == null)
					continue;
				if (requireType.isAssignableFrom(actualParam.getClass())) {
					orderParams[i] = actualParam;
					break;
				}
			}

			valid = orderParams[i] != null;

			if (!valid)
				break;
		}

		return valid ? orderParams : null;
	}

	public Object invokeWithContext(Object context, Object... params) {
		return methodAccess.invoke(context != null ? context : this.context,
				methodIdx, params);
	}

	public Object invoke(Object... params) {
		return invokeWithContext(null, params);
	}
}
