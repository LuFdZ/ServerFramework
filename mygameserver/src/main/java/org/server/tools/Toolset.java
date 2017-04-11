package org.server.tools;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.Consumer;

import jodd.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * 工具集
 */
public class Toolset {
	
	static final Logger log = LoggerFactory.getLogger(Toolset.class);
	
	/**
	 * 随机数发生器
	 */
	public static Random Random;
	
	/**
	 * 换行符字符
	 */
	public static String Separator = System.getProperty("line.separator");

	static {
		try {
			Random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			Random = new SecureRandom();
		}
	}
	
	/**
	 * 精确加法
	 * 
	 * @param a
	 *            数 a
	 * @param b
	 *            数 b
	 * @return 计算结果
	 */
	public static double add(double a, double b) {
		return BigDecimal.valueOf(a).add(BigDecimal.valueOf(b)).doubleValue();
	}
	
	/**
	 * 精确减法
	 * 
	 * @param a
	 *            数 a
	 * @param b
	 *            数 b
	 * @return 计算结果
	 */
	public static double subtract(double a, double b) {
		return BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)).doubleValue();
	}

	/**
	 * 精确乘法
	 * 
	 * @param a
	 *            数 a
	 * @param b
	 *            数 b
	 * @return 计算结果
	 */
	public static double multiply(double a, double b) {
		return BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b)).doubleValue();
	}

	/**
	 * 精确除法
	 * 
	 * @param a
	 *            数 a
	 * @param b
	 *            数 b
	 * @return 计算结果
	 */
	public static double divide(double a, double b) {
		return BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b)).doubleValue();
	}
	
	/**
	 * 安全调用任何函数
	 * 
	 * @param callback
	 *            回调接口
	 * @return 调用过程是否发生异常
	 */
	public static <T> boolean safeCallAny(T object, Consumer<T> callback) {
		return safeCallAny(object, callback, null);
	}
	
	/**
	 * 安全调用任何函数
	 * 
	 * @param callback
	 *            回调接口
	 * @return 调用过程是否发生异常
	 */
	public static <T> boolean safeCallAny(T object, Consumer<T> callback, Consumer<Throwable> throwableHandler) {

		/**
		 * 空对象直接返回
		 */
		if (object == null || callback == null)
			return false;

		try {
			callback.accept(object);
		} catch (Throwable e) {
			safeCallAnyThrowableHandler(e, throwableHandler);
			return false;
		}
		return true;
	}

	/**
	 * 安全调用任何函数
	 * 
	 * @param callback
	 *            回调接口
	 * @return 调用过程是否发生异常
	 */
	public static <T, R> R safeCallAnyFunction(T object, Function<T, R> callback,
			Consumer<Throwable> throwableHandler) {

		/**
		 * 空对象直接返回
		 */
		if (object == null || callback == null || throwableHandler == null)
			return null;

		try {
			return callback.apply(object);
		} catch (Throwable e) {
			safeCallAnyThrowableHandler(e, throwableHandler);
			return null;
		}
	}

	/**
	 * 安全调用接口 - 异常处理
	 * 
	 * @param e
	 *            异常
	 * @param throwableHandler
	 *            异常处理函数
	 */
	static void safeCallAnyThrowableHandler(Throwable e, Consumer<Throwable> throwableHandler) {
		try {
			if (throwableHandler != null)
				throwableHandler.accept(e);
		} catch (Throwable throwable) {
			safeCallAnyThrowableHandler(throwable, throwableHandler);
		}
	}

	/**
	 * 格式化输出
	 * 
	 * @param logger
	 *            日志输出
	 * 
	 * @param output
	 *            输出语句
	 * @param args
	 *            参数集合
	 */
	public static void prettyOutput(Logger logger, String output, Object... args) {
		StringBuffer finalOutput = new StringBuffer(Separator);
		finalOutput.append("\t");

		for (int i = 0; i < args.length; i++)
			output = StringUtil.replaceFirst(output, "{}", args[i].toString());

		output = output.replace("\n", "\n\t");
		output = output.replace(Separator, Separator + "\t");
		output = output.replace("{nl}", Separator + "\t");

		finalOutput.append(output);
		finalOutput.append(Separator);

		logger.info(finalOutput.toString());
	}

	/***
	 * delete CRLF; delete empty line ;delete blank lines
	 * 
	 * @param input
	 * @return
	 */
	public static String deleteCRLFOnce(String input) {
		return input.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
	}
}
