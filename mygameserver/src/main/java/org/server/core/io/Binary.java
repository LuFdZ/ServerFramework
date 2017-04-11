/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.io;

import org.apache.mina.core.buffer.IoBuffer;
import org.server.core.io.input.GenericLittleEndianAccessor;
import org.server.core.io.input.IoBufferByteInputStream;
import org.server.core.io.input.LittleEndianAccessor;
import org.server.core.io.output.GenericLittleEndianWriter;
import org.server.core.io.output.IoBufferByteOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 二进制处理工具
 *
 * @author Administrator
 */
public class Binary {
	
	static final Logger logger = LoggerFactory.getLogger(Binary.class);
	
	public static LittleEndianAccessor read(IoBuffer buffer) {
		return new GenericLittleEndianAccessor(new IoBufferByteInputStream(buffer));
	}
	
	public static GenericLittleEndianWriter write() {
		return new GenericLittleEndianWriter(new IoBufferByteOutputStream());
	}
	
	/**
	 * 建立二进制写入流
	 *
	 * @param size
	 *            缓冲区大小
	 * @return 二进制写入模块
	 */
	public static GenericLittleEndianWriter write(int size) {
		return new GenericLittleEndianWriter(new IoBufferByteOutputStream(size));
	}

	/**
	 * 尝试反序列化模块
	 *
	 * @param <T>
	 *            共享模块类型F
	 * @param createSharedModule
	 *            模块创建器
	 * @param message
	 *            会话消息数据
	 * @return 序列化成功返回模块
	 */
	public static <T extends com.google.protobuf.GeneratedMessage> T tryDeserialize(
			CreateSharedModule<T> createSharedModule, SessionMessage message) {
		try {
			return createSharedModule.Create(message.getData());
		} catch (InvalidProtocolBufferException e) {
			logger.debug("[GeneratedMessage] 尝试反序列化模块错误：", e);
		}
		return null;
	}

	/**
	 * 尝试序列化模块
	 *
	 * @param module
	 *            模块对象
	 * @return 序列化成功的数据
	 */
	public static byte[] trySerialize(
			com.google.protobuf.GeneratedMessage module) {
		
		// check args
		if (module == null)
			return null;
		
		
		// parse to bytes
		try {
			return module.toByteArray();
		} catch (Throwable e) {
			logger.error("[GeneratedMessage] 尝试序列化模块错误：", e);
		}
		
		return null;
	}
}
