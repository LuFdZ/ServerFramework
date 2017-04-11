/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.model.custom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.server.core.even.PlayerChangeAndResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class PlayerInfoModel {

	private int _playerRole;// 1：主牌；2：副牌 3：AI牌
	private double _chipCoin;// 压注数
	private List<CardModel> _cardList;// 牌组信息
	private boolean _isdouble;// 是否双倍
//	private boolean _isStop;// 是否停牌
	private boolean _isSurrender;// 是否保险
	private boolean _isGameOver;// 此玩家是否已结算游戏
	private int _result;// 玩家游戏结果 -1 无； 0：平；1：已爆；2：已赢
	private int _score;//玩家分数
	private String _cardResultString;
//
//	private static final Logger _log = LoggerFactory
//			.getLogger(PlayerModel.class);
//	PlayerChangeAndResult _playerChangeAndResultEven;// 结果产生事件

	/***
	 * 
	 * @param playerType 玩家类型
	 * @param chipCoin 玩家成绩
	 */
	public PlayerInfoModel(int playerType, double chipCoin) {
		_playerRole = playerType;
		_chipCoin = chipCoin;
		_isdouble = false;
//		_isStop = false;
		_isSurrender = false;
		_isGameOver = false;
		_cardList = new ArrayList<>();
	}

	public int getPlayerRole() {
		return _playerRole;
	}

	public double getChipCoin() {
		return _chipCoin;
	}

	public List<CardModel> getCardList() {
		return _cardList;
	}

	public boolean isIsdouble() {
		return _isdouble;
	}

//	public boolean isIsStop() {
//		return _isStop;
//	}

	public int getResult() {
		return _result;
	}

	public void setDouble() {
		this._isdouble = true;
		this._chipCoin = _chipCoin*2;
	}

	public void setGameOver() {
		this._isGameOver = true;
	}

	public void setStop() {
		this._isGameOver = true;
		countResutl();
	}

	public boolean isSurrender() {
		return _isSurrender;
	}
	
	public int getScore() {
		return _score;
	}

	public void setResult(int _result) {
		this._result = _result;
	}

	public void setSurrender() {

		this._isSurrender = true;
		_isGameOver = true;
		_chipCoin =_chipCoin/2;
		countResutl();
	}

	public void setCard(CardModel card) {
		_cardList.add(card);
		countResutl();
	}
	
    public CardModel deleteCard(int i) {
        CardModel card = _cardList.remove(i);
        return card;
    }

	public boolean isGameOver() {
		return _isGameOver;
	}
	
	public String getResultString(){
		String list ="";
		for (CardModel cardModel : _cardList) {
			list +=cardModel.getTransferValues()+"-";
		}
		return list;
	}

	/***
	 * 分数计算
	 * 
	 * @return
	 */
	public int Score() {
		int r = 0;
		int Acard = 0;
		// 循环牌组计分
		for (CardModel c : _cardList) {
			r += c.getValues();
			if (c.getFaceValues() == 1) {
				Acard++;// 记录A的数量
			}
		}
		// 若分数>21且A的数量>0 A的分值一次由10变为1
		while (r > 21 && Acard > 0) {
			r -= 10;
			Acard--;
		}
		_score = r;
		return _score;
	}

	/**
	 *通用结果生成器
	 * @param aiscore
	 *            0：自检是否已爆 ;大于0：与ai牌比较分数;小于0 投降
	 * @return
	 */
	public int countResutl() {

		Score();
		if (_score == 21) {
			_result = 2;
			_isGameOver = true;
		} else if (_score < 21) {
			_result = -1;
		} else if (_score > 21) {
			_result = 1;
			_isGameOver = true;
		} 

		return _result;
	}
	
	/**
     * *
     * ai自动要牌模块
     *
     * @param compareScoreTop 最大分值牌组
     * @param compareScoreLow 最大分值牌组（只有一副牌的话为最大最小值相等）
     * @return
     */
    public boolean getCard(int compareScoreTop, int compareScoreLow) {
    	int _score = Score();
        if (_score >= 21) {
            return false;
        } else {
            if (_score > compareScoreTop) {//若ai分大于最高玩家牌分值
                if (_score <= 15) {//ai小于15就要牌
                    return true;
                }
            } else if (_score == compareScoreTop) {//若ai分等于最高玩家牌分值
                if (_score < 17) {//ai小于17就要牌
                    return true;
                }
            } else {//若ai分小于最高玩家牌分值
                if (_score > compareScoreLow) {//若ai分大于最低玩家牌分值
                    if (_score <= 16) {//ai小于16就要牌
                        return true;
                    }
                } else {//若ai分小于最低玩家牌分值 就要牌
                    if (_score == compareScoreLow) {//ai于最小玩家牌相等
                        if (_score < 17) {//ai小于17就要牌
                            return true;
                        }
                    } else if (_score < compareScoreLow && _score<20) {//ai小于玩家就要牌
                        return true;
                    }

                }
            }
        }
        return false;
    }
    
    public List<String> getPlayerCardStringList(){
    	List<String> reuslt = new ArrayList<>();
    	for (CardModel card : _cardList) {
    		reuslt.add(card.getTransferValues());
		}
    	return reuslt;
    }
    
    /***
     * 判断是否可分牌
     * @return
     */
    public int canSplitCard(){
    	if(_cardList.get(0).getValues() == _cardList.get(1).getValues()){
    		return 1;
    	}
    	return 0;
    }
    
    /***
     * 是否需要买保险
     * @return
     */
    public int hasInsurance(){
    	if(_cardList.get(0).getValues() == 11){
    		return 1;
    	}
    	return 0;
    	
    }

//	/**
//	 * * 触发结果事件
//	 */
//	private void getAutoFullManyCoinEven() {
//		if (_playerChangeAndResultEven != null) {
//			callBackTryCatch(code -> _playerChangeAndResultEven.appay(
//					_playerRole, ++_playerRole, _result));
//		}
//	}
//
//	private void callBackTryCatch(Consumer<Integer> consumer) {
//		try {
//			consumer.accept(0);
//		} catch (Exception e) {
//			_log.error("调用回调时异常！", e);
//		}
//	}
}
