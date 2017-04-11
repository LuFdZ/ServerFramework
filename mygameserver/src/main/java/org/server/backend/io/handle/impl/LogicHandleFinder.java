package org.server.backend.io.handle.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.server.backend.io.handle.ILogicDefineFinder;
import org.server.backend.io.handle.annotation.LogicAuthorize;
import org.server.backend.io.handle.annotation.LogicDeadMessageHandle;
import org.server.backend.io.handle.annotation.LogicHandle;
import org.server.backend.io.handle.annotation.LogicModelMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息处理寻找器
 */
public class LogicHandleFinder implements ILogicDefineFinder  {

	static final Logger logger = LoggerFactory.getLogger(LogicHandleFinder.class);
	
	List<CallbackDefine> defines = new ArrayList<CallbackDefine>();
	Map<Object, CallbackDefine> defineMap = new HashMap<Object, CallbackDefine>();

	List<MethodCall> deadMessageHandle = new ArrayList<MethodCall>();

	Map<Object, MethodCall> authorizes = new HashMap<>();
	List<Class<?>> returnTypes = new ArrayList<Class<?>>();

	Class<?>[] supportParamsClasses;
	List<Class<?>> authReturnTypes;
	List<Class<?>> allSupportParamsClasses;
	
	public LogicHandleFinder(Class<?>... supportParamsClasses) {
		super();
		this.supportParamsClasses = supportParamsClasses;
	}

	public List<CallbackDefine> getDefines() {
		return defines;
	}

	public List<MethodCall> getDeadMessageHandle() {
		return deadMessageHandle;
	}

	public List<Class<?>> getAuthReturnTypes() {
		return returnTypes;
	}

	public MethodCall getAuthorizeFunction(Class<?> returnType) {
		return authorizes.get(returnType);
	}

	@Override
	public void setMethodSupportTypes(List<Class<?>> types) {
		this.authReturnTypes = types;
		this.allSupportParamsClasses = new ArrayList<Class<?>>(types);
		for (Class<?> supportType : supportParamsClasses)
			if (!allSupportParamsClasses.contains(supportType))
				allSupportParamsClasses.add(supportType);
		
	}
	
	public void searchDefine(Object[] objects) {
		if (objects == null)
			return;

		for (Object obj : objects)
			searchAuthorizeDefine(obj);
		
		setMethodSupportTypes(getAuthReturnTypes());

		for (Object obj : objects)
			searchHandleDefine(obj);

		generateHandleDefineinfo();
	}

	void searchAuthorizeDefine(Object object) {
		if (object == null)
			return;

		Class<?> objectClass = object.getClass();

		for (Method method : objectClass.getDeclaredMethods()) {
			LogicAuthorize authorize = method
					.getAnnotation(LogicAuthorize.class);

			if (authorize != null)
				registerAuthorize(object, objectClass, method, authorize);
		}

	}

	void searchHandleDefine(Object object) {
		if (object == null)
			return;

		Class<?> objectClass = object.getClass();

		for (Method method : objectClass.getDeclaredMethods()) {
			LogicHandle handleDefine = method.getAnnotation(LogicHandle.class);
			LogicDeadMessageHandle deadMessageHandle = method
					.getAnnotation(LogicDeadMessageHandle.class);

			if (handleDefine != null)
				registerHandle(objectClass, object, method, handleDefine);

			if (deadMessageHandle != null)
				registerDeadMessageHandle(objectClass, object, method,
						deadMessageHandle);
		}
	}

	void generateHandleDefineinfo() {
		for (CallbackDefine define : getDefines()) {
			List<Class<?>> requireAuthTypes = define.getRequireAuthTypes();
			MethodCall[] calls = new MethodCall[requireAuthTypes.size()];
			for (int i = 0; i < requireAuthTypes.size(); i++)
				calls[i] = getAuthorizeFunction(requireAuthTypes.get(i));
			define.setRequireAuthAccess(calls);
		}
	}

	public CallbackDefine getDefineByCommand(int command) {
		return defineMap.get(command);
	}

	void registerAuthorize(Object object, Class<?> objectClass, Method method,
			LogicAuthorize authorize) {

		if (authorizes.containsKey(authorize.id())) {
			logger.error(
					"[MethodFinder] register authorize function id must be unique : [{}_{}] .",
					method, authorize.id());
			return;
		}

		Class<?> returnType = method.getReturnType();

		if (returnType.getName().equalsIgnoreCase("void")) {
			logger.error(
					"[MethodFinder] register authorize function must have a return value : [{}_{}] .",
					method, authorize.id());
			return;
		}

		if (returnTypes.contains(returnType)) {
			logger.error(
					"[MethodFinder] authorize function return value must be different : [{}_{}] .",
					method, authorize.id());
			return;
		}

		MethodCall methodCall = new MethodCall(object, objectClass, method);

		authorizes.put(returnType, methodCall);
		authorizes.put(authorize.id(), methodCall);
		returnTypes.add(returnType);
	}

	void registerHandle(Class<?> objectClass, Object target, Method method,
			LogicHandle annotation) {

		LogicModelMeta meta = annotation.model();

		if (defineMap.containsKey(meta.owner())) {
			logger.error(
					"[MessageMetaFinder] find a duplicate modelmeta info :: [{}-{}]",
					objectClass.getSimpleName(), method.getName());
			return;
		}

		List<Class<?>> methodParams = MethodCallUtils.getMethodParams(method)
				.collect(Collectors.toList());

		boolean hasModelParam = methodParams.remove(meta.owner());

		if (!hasModelParam) {
			logger.error(
					"[MessageMetaFinder] method must have model param ! :: [{}-{}]",
					objectClass.getSimpleName(), method.getName());
			return;
		}

		List<Class<?>> testRemoveParamsClasses = new ArrayList<Class<?>>(
				methodParams);
		testRemoveParamsClasses.removeAll(allSupportParamsClasses);
		if (!testRemoveParamsClasses.isEmpty()) {
			logger.error(
					"[MessageMetaFinder] method have not support param types :: [{}-{}-{}]",
					objectClass.getSimpleName(), method.getName(),
					testRemoveParamsClasses);
			return;
		}

		if (!meta.serialize().isSupport(meta.owner())) {
			logger.error(
					"[MessageMetaFinder] serialize is not support the type :: [{}-{}-{}-{}]",
					objectClass.getSimpleName(), method.getName(), meta.owner()
							.getSimpleName(), meta.serialize());
			return;
		}

		List<Class<?>> methodNeedAuthReturnTypes = new ArrayList<Class<?>>();
		for (Class<?> param : methodParams)
			if (authReturnTypes.contains(param))
				methodNeedAuthReturnTypes.add(param);

		MethodCall call = new MethodCall(target, objectClass, method);
		CallbackDefine callbackDefine = new CallbackDefine(method, call,
				annotation, meta, methodNeedAuthReturnTypes);

		defines.add(callbackDefine);
		defineMap.put(meta.owner(), callbackDefine);
		defineMap.put(meta.command(), callbackDefine);
	}

	void registerDeadMessageHandle(Class<?> objectClass, Object object,
			Method method, LogicDeadMessageHandle handleDefine) {
		deadMessageHandle.add(new MethodCall(object, objectClass, method));
	}

}
