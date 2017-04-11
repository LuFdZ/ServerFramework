package org.server.core.even;

/**
*用户更换并通知结果
* @author Administrator
*/
public interface PlayerChangeAndResult {

	/**
     * *
     * 回调函数
     *
     * @param userid
     * @param coin
     */
    void appay(String gametimeid,int userid,int userNowId, int userNextId,int result);
}
