package org.server.core.filterchain;

/**
 * 过滤连处理
 */
public class FilterChainTask {
	AbstractFilterNode first = null;

	/**
	 * 添加到链尾
	 * 
	 * @param node
	 *            节点
	 */
	public void addToLast(AbstractFilterNode node) {

		AbstractFilterNode cur = first;

		while (true) {

			if (first == null) {
				first = node;
				break;
			}

			if (cur.next() == null) {
				cur.next(node);
				node.previous(cur);
				break;
			}

			cur = cur.next();

		}

	}

	/**
	 * 开始处理
	 * 
	 * @param object
	 *            参数
	 */
	public void fireTaskHandle(FilterTask object) {
		if (first != null && object != null) {
			first.run(object);
		}
	}
}
