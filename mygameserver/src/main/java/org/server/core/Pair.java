package org.server.core;

import java.util.Objects;

/**
 * 二元组类型
 * 
 * @author Hxms
 *
 * @param <F>
 *            第一个物品类型
 * @param <S>
 *            第二个物品类型
 */
public class Pair<F, S> {
	F o1;
	S o2;

	public Pair(F o1, S o2) {
		this.o1 = o1;
		this.o2 = o2;
	}

	/**
	 * 创建二元组
	 * 
	 * @param item1
	 *            A item
	 * @param item2
	 *            B item
	 * @return 二元组对象
	 */
	public static <T, R> Pair<T, R> create(T item1, R item2) {
		return new Pair<T, R>(item1, item2);
	}

	public static boolean same(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	/**
	 * 获得第一个对象
	 * 
	 * @return 第一个对象
	 */
	public F getFirst() {
		return o1;
	}

	/**
	 * 获得第二个对象
	 * 
	 * @return 第二个对象
	 */
	public S getSecond() {
		return o2;
	}

	/**
	 * 设置第一个对象
	 * 
	 * @param o
	 *            第一个对象
	 */
	public void setFirst(F o) {
		o1 = o;
	}

	/**
	 * 设置第二个对象
	 * 
	 * @param o
	 *            第二个对象
	 */
	public void setSecond(S o) {
		o2 = o;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair)) {
			return false;
		}
		Pair<?, ?> p = (Pair<?, ?>) obj;
		return same(p.o1, this.o1) && same(p.o2, this.o2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + Objects.hashCode(this.o1);
		hash = 59 * hash + Objects.hashCode(this.o2);
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Pair{" + o1 + ", " + o2 + "}";
	}
}
