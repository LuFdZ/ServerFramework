package org.server.core.even;

/**
*要牌事件
* @author Administrator
*/
public interface GetCardEven {

	/***
	 * 回调函数
	 * @param errorTip 失败提示
	 * @param player 1 主牌 2 副牌 3 ai
	 * @param newCard 新牌信息
	 */
	void appay(String gametimeid,int userid,String errorTip,int player, String newCard);
}
