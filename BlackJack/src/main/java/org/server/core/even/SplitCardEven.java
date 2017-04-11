package org.server.core.even;

import java.util.List;

public interface SplitCardEven {

	/***
	 * 回调函数
	 * @param mainCard
	 * @param subCard
	 */
	void appay(String gametimeid,int userid,String errorTip,List<String> mainCard,List<String> subCard);
}
