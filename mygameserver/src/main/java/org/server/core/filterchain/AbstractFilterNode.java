package org.server.core.filterchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象链式处理节点
 */
public abstract class AbstractFilterNode {
	public static interface AcceptCondition {

		public boolean accept(FilterTask task);

	}

	static final Logger logger = LoggerFactory
			.getLogger(AbstractFilterNode.class);

	AbstractFilterNode previous;
	AbstractFilterNode next;

	AcceptCondition acceptCondition;

	/**
	 * 获得上一个节点
	 * 
	 * @return 节点
	 */
	public AbstractFilterNode previous() {
		return previous;
	}

	/**
	 * 获得下一个节点
	 * 
	 * @return 节点
	 */
	public AbstractFilterNode next() {
		return next;
	}

	/**
	 * 设置上一个节点
	 * 
	 * @param previous
	 *            节点
	 */
	public void previous(AbstractFilterNode previous) {
		this.previous = previous;
	}

	/**
	 * 设置下一个节点
	 * 
	 * @param next
	 *            节点
	 */
	public void next(AbstractFilterNode next) {
		this.next = next;
	}

	/**
	 * 设置节点运行任务条件
	 * 
	 * @param acceptCondition
	 *            运行条件
	 */
	protected void acceptCondition(AcceptCondition acceptCondition) {
		this.acceptCondition = acceptCondition;
	}

	/**
	 * 接受指定 task 的任务
	 * 
	 * @param status
	 *            任务状态
	 */
	protected void acceptStatusTask(Integer status) {
		if (status != null)
			acceptCondition(x -> x.status() == status);
		else
			acceptCondition(null);
	}

	/**
	 * 处理参数
	 * 
	 * @param object
	 *            参数
	 */
	protected void run(FilterTask object) {

		FilterResult handleResult = FilterResult.Error;

		try {

			if (acceptCondition == null || acceptCondition.accept(object))
				handleResult = run0(object);
			else
				handleResult = FilterResult.OkAndCallNext;

		} catch (Throwable e) {
			logger.error("[AbstractFilterNode::handle0] throw expcetion ... ",
					e);
		}

		if (handleResult.equals(FilterResult.OkAndCallNext))
			callNextHandle(object);

	}

	/**
	 * 调用下一个处理链
	 * 
	 * @param object
	 *            处理参数
	 */
	protected void callNextHandle(FilterTask object) {
		if (next != null)
			next.run(object);
	}

	/**
	 * 处理
	 * 
	 * @param object
	 * @return 是否继续处理
	 */
	public abstract FilterResult run0(FilterTask object) throws Exception;
}
