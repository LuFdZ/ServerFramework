package org.server.core.even;

/***
 * 停牌响应事件
 * @author Administrator
 *
 */
public interface StopCardEven {

	/***
	 * 回调函数
	 * @param errorTip 错误提示
	 */
	void appay(String gametimeid,int userid,String errorTip);
}
