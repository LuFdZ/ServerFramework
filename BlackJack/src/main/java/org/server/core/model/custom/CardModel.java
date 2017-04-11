/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.model.custom;

/**
 *
 * @author Administrator
 */
public class CardModel {
    
    private final int _cardid;//牌ID
    private final int _suit;//"1方块", "2梅花", "3红桃", "4黑桃"
    private final int _faceValues;//牌面值 1:A,11:J,12:Q,13:K
    private final int _values;//计算值
    private final String _transferValues;//传输字符串
    
    public CardModel(int cardid,int suit,int faceValues,int values){
        _cardid = cardid;
        _suit = suit;
        _faceValues = faceValues;
        _values = values;
        _transferValues = suit+":"+faceValues;
    }
    
    public int getFaceValues(){
        return _faceValues;
    }
    
    public int getValues(){
        return _values;
    }
    
    public String getTransferValues() {
        return _transferValues;
    }
}
