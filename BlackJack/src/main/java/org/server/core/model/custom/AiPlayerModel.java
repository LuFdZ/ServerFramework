/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.model.custom;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class AiPlayerModel {

    private List<CardModel> _cardList;
    private int _score = 0;

    public AiPlayerModel() {
        _cardList = new LinkedList<>();
    }

    public int getScore() {
        return _score;
    }

    public List<CardModel> getCardList() {
        return _cardList;
    }

    public void setCard(CardModel card) {
        _cardList.add(card);
        _score = Score();
    }

    public int Score() {
        int r = 0;
//        for (CardModel c : _cardList) {
//            if (c.getFaceValues() != 1) {
//                r += c.getValues();
//            }
//        }
//
//        for (CardModel c : _cardList) {
//            if (c.getFaceValues() == 1) {
//                r += r + c.getValues() > 21 ? 1 : c.getValues();
////                if (r > 21) {
////                    for (CardModel d : _cardList) {
////                        if (d.getFaceValues() == 1 && d != c) {
////                            r -= 10;
////                        }
////                    }
////                }
//            }
//        }
        int Acard = 0;
        for (CardModel c : _cardList) {
            r += c.getValues();
            if (c.getFaceValues() == 1) {
                Acard++;
            }
        }
        while (r>21 && Acard>0) {            
            r-=10;
            Acard--;
        }
        return r;
    }

//    private void countScore(int score) {
//        _score += score;
//        if (_score>21) {
//            int countA = 0;
//            _score = 0;
//            for (CardModel model : _cardList) {
//                if (model.getFaceValues() == 1) {
//                    countA++;
//                }
//                _score += model.getValues();
//            }
//            while (_score>21 && countA>0) {                
//                _score -= 10;
//                countA--;
//            }
//        }
////        _score = 0;
////        _cardList.stream().forEach((CardModel model) -> {
////            if (_score > 21) {
////                if (model.getFaceValues() == 1) {
////                    _score += 1;
////                } else {
////                    _score += model.getValues();
////                }
////            } else {
////                _score += model.getValues();
////            }
////        });
//    }
    /**
     * *
     * ai是否要牌
     *
     * @param compareScoreTop 最大分值牌组
     * @param compareScoreLow 最大分值牌组（只有一副牌的话为最大最小值相等）
     * @return
     */
    public boolean getCard(int compareScoreTop, int compareScoreLow) {
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
}
