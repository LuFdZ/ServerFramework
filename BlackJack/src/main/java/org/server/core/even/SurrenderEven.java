package org.server.core.even;

/***
 * 投降响应事件
 * @author Administrator
 *
 */
public interface SurrenderEven {

	/***
	 * 回调函数
	 * @param errorTip 错误提示
	 */
	void appay(String gametimeid,int userid,String errorTip);
}
