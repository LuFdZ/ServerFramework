package org.server.core.even;

import java.util.List;

import org.server.core.model.custom.PlayerInfoModel;

/**
*游戏结算
* @author Administrator
*/
public interface GameResultEven {

	/***
	 * 回调函数
	 * @param result1 主牌结果
	 * @param result2 副牌结果
	 * @param resultAi ai结果
	 */
	void appay(String gametimeid,int userid,int result1, int result2,int resultAi,List<String> aiCardList,PlayerInfoModel player1,PlayerInfoModel player2,boolean isSplit,double insuranceEvenMoney,int tableid,String log);
}
