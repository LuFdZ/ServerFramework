package org.server.core.even;

public interface InsuranceEven {

	/***
	 * 回调函数
	 * @param errorTip 失败提示
	 * @param diamond 保险费用
	 */
	void appay(String gametimeid,int userid,String errorTip,double diamond,int result);
}
