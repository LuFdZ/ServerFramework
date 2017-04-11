/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.even;

/**
 * 游戏事件监听器
 *
 * @author Administrator
 */
public interface GameEvenListener {

    /***
     * 绑定双倍事件
     * @param event 
     */
    void bindDoubleEven(DoubleEven event);

    /***
     * 绑定游戏结算事件
     * @param event 
     */
    void bindGameResultEven(GameResultEven event);

    /***
     * 绑定要牌事件
     * @param event 
     */
    void bindGetCardEven(GetCardEven event);


    /***
     * 绑定保险事件
     * @param event 
     */
    void bindInsuranceEven(InsuranceEven event);
    
    /***
     * 绑定玩家角色转换事件
     * @param event 
     */
    void bindPlayerChangeAndResult(PlayerChangeAndResult event);
    
    /***
     * 绑定分牌事件
     * @param event 
     */
    void bindSplitCardEven(SplitCardEven event);
    
    /***
     * 绑定游戏开始事件
     * @param event 
     */
    void bindStartGameEven(StartGameEven event);
    
    /***
     * 绑定停牌事件
     * @param event 
     */
    void bindStopCardEven(StopCardEven event);
    
    /***
     * 绑定投降事件
     * @param event 
     */
    void bindSurrenderEven(SurrenderEven event);
}
