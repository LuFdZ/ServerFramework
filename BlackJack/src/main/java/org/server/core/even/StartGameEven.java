package org.server.core.even;

import java.util.List;

/**
*开始游戏事件
* @author Administrator
*/
public interface StartGameEven {

	/***
	 * 回调函数
	 * @param _isAiBlackJack 是否可买保险
	 * @param _isSplit 是否可分牌
	 * @param playerCardList 玩家牌
	 * @param aiCardList ai牌
	 * @param aihasBlack  ai是否黑杰克
	 * @param playHasBlack 玩家是否黑杰克
	 */
	void appay(String gametimeid,int userid,int _isAiBlackJack, int _isSplit,List<String> playerCardList,List<String> aiCardList,int aihasBlack,int playHasBlack);
}
