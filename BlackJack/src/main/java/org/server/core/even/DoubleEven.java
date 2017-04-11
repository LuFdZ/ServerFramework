package org.server.core.even;

/**
*双倍
* @author Administrator
*/
public interface DoubleEven {

	/***
	 * 回调函数
	 * @param errorTip 失败提示
	 * @param player 
	 * @param newCard 
	 */
	void appay(String gametimeid,int userid,String errorTip,int player, String newCard);
}
