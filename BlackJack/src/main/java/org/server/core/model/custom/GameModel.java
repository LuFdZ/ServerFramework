/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.model.custom;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.server.core.GameResource;
import org.server.core.even.DoubleEven;
import org.server.core.even.GameEvenListener;
import org.server.core.even.GameResultEven;
import org.server.core.even.GetCardEven;
import org.server.core.even.InsuranceEven;
import org.server.core.even.PlayerChangeAndResult;
import org.server.core.even.SplitCardEven;
import org.server.core.even.StartGameEven;
import org.server.core.even.StopCardEven;
import org.server.core.even.SurrenderEven;

/**
 *
 * @author Administrator
 */
public class GameModel implements GameEvenListener {

	private int _userid;
	private int _brandid;
	private int _tableid;
	private String _gameTimeid;
	private Date _opTime;// 最近一次操作时间
	private Date _createDate;
	private int _cardIndex;// 当前发牌数
	private List<CardModel> _cards;
	private List<PlayerInfoModel> _playerList;// 玩家牌列表
	private PlayerInfoModel _aiPlayer;
	private double _chipCoin;// 玩家下注金额
	private int _canSplit;// 0 不可分牌 1 可分牌但没有请求 2分了牌且请求了
	private int _canInsurance;// 0 不可保险牌 1 可保险牌但没有请求 2可保险牌且请求了
	private boolean _isSplit;// 是否分牌
	private boolean _isInsurance;// 是否有保险
	private boolean _isGameOver;
	private PlayerChangeAndResult _playerChangeAndResultEven;// 玩家更换事件
	private StartGameEven _startGameEven;// 游戏开始事件
	private GameResultEven _gameResultEven;// 游戏结果产生事件
	private DoubleEven _doubleEven;// 双倍事件
	private InsuranceEven _insuranceEven;// 保险事件
	private GetCardEven _getCardEven;// 要牌事件
	private SplitCardEven _splitCardEven;// 分牌事件
	private StopCardEven _stopCardEven;// 停牌事件
	private SurrenderEven _surrenderEven;// 投降事件

	public GameModel(int userid, int roomid, double chipCoin, int tableid) {

		_tableid = tableid;
		_gameTimeid = String.valueOf(new Date().getTime());
		_userid = userid;
		_cards = new LinkedList<>();
		_chipCoin = chipCoin;
		_playerList = new LinkedList<>();
		_aiPlayer = new PlayerInfoModel(3, 0);// 初始化ai
		_playerList.add(new PlayerInfoModel(1, _chipCoin));// 初始化主牌玩家
		_opTime = new Date();
		_createDate = new Date();
		_brandid = 0;
		_tableid = 0;
		_cardIndex = 0;
		_isGameOver = false;
		_isInsurance = false;
		_isSplit = false;
		_canSplit = 0;
		_canInsurance = 0;
		initcard();
		shufflecard();
	}

	// 更新操作时间
	private void setOpTime() {
		_opTime = new Date();
	}

	public void changeRoom() {
		initcard();
		shufflecard();
	}

	public void init(double chipCoin) {
		getSurplus();
		_createDate = new Date();
		// _cards = new LinkedList<>();
		_gameTimeid = String.valueOf(new Date().getTime());
		_chipCoin = chipCoin;
		_playerList = new LinkedList<>();
		_aiPlayer = new PlayerInfoModel(3, 0);// 初始化ai
		_playerList.add(new PlayerInfoModel(1, _chipCoin));// 初始化主牌玩家
		// _cardIndex = 0;
		_opTime = new Date();
		_isGameOver = false;
		_isInsurance = false;
		_isSplit = false;
		_canSplit = 0;
		_canInsurance = 0;
	}

	public void getSurplus() {

		if (416 - _cardIndex < 20) {
			_cardIndex = 0;
			initcard();
			shufflecard();

		}
	}

	private void initcard()// 初始化
	{
		String num[] = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11", "12", "13" };// 1:A,11:J,12:Q,13:K
		String suit[] = { "1", "2", "3", "4" };// "4方块", "3梅花", "2红桃", "1黑桃"
		// _cards = new ConcurrentHashMap<>();AiCard：[2:11, 4:3, 2:11]
		int cardId = 0;

		int value;
		for (int i = 0; i < 8; i++) {// 8副牌
			for (int j = 0; j < 4; j++) {// 4个花色
				for (int n = 0; n < 13; n++) {// 13个面值
					if ("1".equals(num[n])) {
						value = 11;
					} else if ("11".equals(num[n]) || "12".equals(num[n])
							|| "13".equals(num[n])) {
						value = 10;
					} else {
						value = n + 1;
					}
					CardModel model = new CardModel(cardId, j + 1, n + 1, value);
					_cards.add(model);
					cardId++;
				}
			}
		}
	}

	private void shufflecard()// 洗牌
	{
		Random rd = new Random();
		try {
			for (int i = 0; i < 52 * 8; i++) {
				int j = rd.nextInt(52 * 8);// 生成随机数
				CardModel temp = _cards.get(i);// 交换
				_cards.set(i, _cards.get(j));
				_cards.set(j, temp);
			}
		} catch (Exception e) {

		}
	}

	public void setTableid(int tableid) {
		_tableid = tableid;
		initcard();
		shufflecard();
	}

	public List<PlayerInfoModel> getPlayer() {
		return _playerList;
	}

	public int brandOver() {
		int brandidNow = _brandid;
		_brandid++;
		return brandidNow;
	}

	public double getChipCoin() {
		return _chipCoin;
	}

	public boolean isGameOver() {
		return _isGameOver;
	}

	public String getGameTimeid() {
		return _gameTimeid;
	}

	public List<PlayerInfoModel> getPlayerList() {
		return _playerList;
	}

	public PlayerInfoModel getAiPlayer() {
		return _aiPlayer;
	}

	public boolean isSplit() {
		return _isSplit;
	}

	public boolean isInsurance() {
		return _isInsurance;
	}

	public int isCanSplit() {
		return _canSplit;
	}

	public int isCanInsurance() {
		return _canInsurance;
	}

	
//	int test = 0;
	// 发牌
	public List<String> dealCard() {
		setOpTime();
		// sss
//		if (test%2==0) {
//			_cardIndex = 0;
//			 _cards.add(0, new CardModel(1, 3, 10, 10));
//			 _cards.add(1, new CardModel(1, 2, 10,10));
//			 _cards.add(2, new CardModel(1, 1, 8,8));
//			 _cards.add(3, new CardModel(1, 4, 7,7));
//		}
//		test++;
		// _cards.add(4, new CardModel(1, 4, 1, 1));

//		_cards.add(0, new CardModel(1, 3, 4, 4));
//		_cards.add(1, new CardModel(1, 2, 6, 6));
//		_cards.add(2, new CardModel(1, 1, 3, 3));
//		_cards.add(3, new CardModel(1, 4, 7, 7));
//		_cards.add(4, new CardModel(1, 2, 1, 11));
//		_cards.add(5, new CardModel(1, 3, 1, 11));
//		_cards.add(6, new CardModel(1, 1, 11, 10));
//		_cards.add(7, new CardModel(1, 4, 11, 10));
//		_cards.add(8, new CardModel(1, 4, 9, 9));
//		_cards.add(9, new CardModel(1, 2, 7, 7));
		// xx
		List<String> dealcards = new LinkedList<>();
		int temp = _cardIndex + 4;
		int countIndex = 0;
		for (; _cardIndex < temp; _cardIndex++) {
			dealcards.add(_cards.get(_cardIndex).getTransferValues());
			if (countIndex % 2 == 0) {
				_playerList.get(0).setCard(_cards.get(_cardIndex));// 双数给玩家
			} else {
				_aiPlayer.setCard(_cards.get(_cardIndex));// 单数给AI
			}
			countIndex++;
		}
		getDealCardResult();
		return dealcards;
	}

	// 计算发牌结果
	public void getDealCardResult() {
		setOpTime();

		if (_playerList.get(0).getScore() == 21 && _aiPlayer.getScore() < 21) {// 玩家21点
			_playerList.get(0).setResult(2);
			_isGameOver = true;
			getStartGameEven(0, 0, -1, 1);
			getGameResultEven(2, -1, 1, _aiPlayer.getPlayerCardStringList());
		} else if (_playerList.get(0).getScore() == 21
				&& _aiPlayer.getScore() == 21) {
			_playerList.get(0).setResult(0);
			_isGameOver = true;
			getStartGameEven(0, 0, 1, 1);
			getGameResultEven(0, -1, 0, _aiPlayer.getPlayerCardStringList());
		} else {

			getStartGameEven(_aiPlayer.hasInsurance(), _playerList.get(0)
					.canSplitCard(), _aiPlayer.getScore() == 21 ? 1 : -1, -1);
			// getStartGameEven(0, 0);
			_playerList.get(0).setResult(-1);

		}
		// else if (_playerList.get(0).getScore() < 21
		// && (_aiPlayer.getScore() == 21 && _aiPlayer.hasInsurance() == 1)) {
		// _playerList.get(0).setResult(1);
		// _isGameOver = true;
		// getStartGameEven(0, 0,1,-1);
		// getGameResultEven(1, -1, 2, _aiPlayer.getPlayerCardStringList());
		// }
		_canSplit = _playerList.get(0).canSplitCard();// ==1?1:0;
		_canInsurance = _aiPlayer.hasInsurance();// ==1?1:0;
	}

	public void setStopCard() {
		setOpTime();
		// 获得当前活动玩家
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			_playerList.get(1).getResult();
			_playerList.get(1).setGameOver();
			getStopCardEven(null);
			int playerOldResult = _playerList.get(1).getResult() == 2 ? -1
					: _playerList.get(1).getResult();// 副牌停牌 游戏未结束 副牌21点做待续处理

			if (!_playerList.get(0).isGameOver()) {
				getPlayerChangeEven(2, 1, playerOldResult);
			} else {
				aiPlayerGetCard();
			}

		} else if (!_playerList.get(0).isGameOver()) {
			_playerList.get(0).getResult();
			_playerList.get(0).setGameOver();
			getStopCardEven(null);
			aiPlayerGetCard();
		} else {
			getStopCardEven("1,当前玩家无法停牌!");
		}
	}

	public void setInsuranceEven(boolean hasInsurance) {
		setOpTime();

		_canInsurance = 2;
		if (hasInsurance) {
			// 是否可购买保险
			if (_playerList.get(0).isGameOver()
					|| _aiPlayer.hasInsurance() != 1) {
				getInsuranceEven("1,当前玩家无法买保险", 0, -1);
				_canInsurance = _aiPlayer.hasInsurance();
				return;
			} else {

				_isInsurance = true;
				if (_aiPlayer.getResult() == 2) {
					getInsuranceEven(null, _chipCoin / 2, 1);
					this._isGameOver = true;
					getGameResultEven(2, -1, 1,
							_aiPlayer.getPlayerCardStringList());
					return;
				} else {
					getInsuranceEven(null, _chipCoin / 2, -1);
					return;
				}
			}
		} else if (_aiPlayer.getResult() == 2) {
			this._isGameOver = true;
			getInsuranceEven(null, 0, -1);
			getGameResultEven(1, -1, 2, _aiPlayer.getPlayerCardStringList());
			return;
		}
		getInsuranceEven(null, 0, -1);
	}

	public void setSplitCard() {
		setOpTime();
		if (_playerList.size() == 2 && _playerList.get(0).canSplitCard() == 0) {
			getSplitCardEven("1,当前玩家不能分牌！", null, null);
			return;
		}
		_canSplit = 2;
		_isSplit = true;
		_playerList.add(new PlayerInfoModel(2, _chipCoin));// 添加玩家
		CardModel card = _playerList.get(0).deleteCard(1);
		_playerList.get(1).setCard(card);

		int temp = _cardIndex + 2;
		int countIndex = 0;
		for (; _cardIndex < temp; _cardIndex++) {
			if (countIndex % 2 == 0) {
				_playerList.get(1).setCard(_cards.get(_cardIndex));

			} else {
				_playerList.get(0).setCard(_cards.get(_cardIndex));
			}
			countIndex++;
		}
		_isSplit = true;
		getSplitCardEven(null, _playerList.get(0).getPlayerCardStringList(),
				_playerList.get(1).getPlayerCardStringList());
		int playerStop = 0;
		for (PlayerInfoModel player : _playerList) {
			if (player.getResult() != -1) {
				player.setStop();
				playerStop++;
			}
		}

		if (playerStop == 2) {
			aiPlayerGetCard();
		} else if (_playerList.get(1).isGameOver()) {
			int playerOldResult = _playerList.get(1).getResult() == 2 ? -1
					: _playerList.get(1).getResult();
			getPlayerChangeEven(2, 1, playerOldResult);
		}

	}

	public void setDouble() {
		setOpTime();
		PlayerInfoModel playerOld = null;
		// 获得当前活动玩家
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			playerOld = _playerList.get(1);
		} else if (!_playerList.get(0).isGameOver()) {
			playerOld = _playerList.get(0);
		}

		if (playerOld == null) {
			getDoubleEvenEven("1,当前玩家不能加倍！", -1, null);
			return;
		}

		String cardString = getCard();
		if (cardString.length() <= 1) {
			getDoubleEvenEven("1,当前玩家不能加倍！", -1, null);
			return;
		}
		playerOld.setDouble();
		getDoubleEvenEven(null, playerOld.getPlayerRole(), cardString);
		// getGetCardEven(null, playerOld.getPlayerRole(), cardString);
		// 获得更换玩家
		PlayerInfoModel playerNext = _playerList.get(0);
		// if (_isSplit && !_playerList.get(1).isGameOver()) {
		// playerNext = _playerList.get(0);
		// }
		// else {
		// playerNext = _playerList.get(1);
		// }

		// 判断玩家是否有变动
		if (playerOld.getPlayerRole() == 2) {
			int playerOldResult = playerOld.getResult() == 2 ? -1 : playerOld
					.getResult();// 游戏未结束，可能和局 21点作为继续标记
			getPlayerChangeEven(playerOld.getPlayerRole(),
					playerNext.getPlayerRole(), playerOldResult);
		}
		playerOld.setGameOver();
		// 判断是否停牌
		if (playerNext.getPlayerRole() == 1 && playerNext.isGameOver()) {
			aiPlayerGetCard();
		}
	}

	public void setSurrender() {
		setOpTime();
		PlayerInfoModel playerOld = null;
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			playerOld = _playerList.get(1);
		} else if (!_playerList.get(0).isGameOver()) {
			playerOld = _playerList.get(0);
		}

		if (playerOld == null) {
			getSurrenderEven("1,当前玩家不能投降！");
			return;
		}
		playerOld.setSurrender();
		playerOld.setResult(1);
		getSurrenderEven(null);
		PlayerInfoModel playerNext = null;
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			playerNext = _playerList.get(1);
		} else {
			playerNext = _playerList.get(0);
		}

		// 判断玩家是否有变动
		if (playerNext.getPlayerRole() == 2 && playerNext.getPlayerRole() == 1) {
			int playerOldResult = playerOld.getResult() == 2 ? -1 : playerOld
					.getResult();// 游戏未结束，可能和局 21点作为继续标记
			getPlayerChangeEven(playerOld.getPlayerRole(),
					playerNext.getPlayerRole(), playerOldResult);
		}

		playerOld.setGameOver();
		// 判断是否停牌
		if (playerNext.getPlayerRole() == 1 && playerNext.isGameOver()) {
			// aiPlayerGetCard();
			int play1 = _playerList.get(0).getResult();
			int play2 = -1;
			if (_isSplit) {
				play2 = _playerList.get(2).getResult();
			}
			this._isGameOver = true;
			getGameResultEven(play1, play2, 2,
					_aiPlayer.getPlayerCardStringList());
		}

	}

	public void playerGetCard() {
		setOpTime();
		PlayerInfoModel playerOld = null;
		// 获得当前活动玩家
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			playerOld = _playerList.get(1);
		} else if (!_playerList.get(0).isGameOver()) {
			playerOld = _playerList.get(0);
		}

		if (playerOld == null) {
			getGetCardEven("1,当前玩家不能要牌！", -1, null);
			return;
		}

		String cardString = getCard();
		if (cardString.length() <= 1) {
			getGetCardEven("1,当前玩家不能要牌！", -1, null);
			return;
		}
		getGetCardEven(null, playerOld.getPlayerRole(), cardString);
		// 获得更换玩家
		PlayerInfoModel playerNext = null;
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			playerNext = _playerList.get(1);
		} else {
			playerNext = _playerList.get(0);
		}

		// 判断玩家是否有变动
		if (playerOld.getPlayerRole() == 2 && playerNext.getPlayerRole() == 1) {
			int playerOldResult = playerOld.getResult() == 2 ? -1 : playerOld
					.getResult();// 游戏未结束，可能和局 21点作为继续标记
			getPlayerChangeEven(playerOld.getPlayerRole(),
					playerNext.getPlayerRole(), playerOldResult);
		}

		// 判断是否停牌
		if (playerNext.getPlayerRole() == 1 && playerNext.isGameOver()) {
			aiPlayerGetCard();
		}

	}

	private String getCard() {
		setOpTime();
		String result = "0";
		CardModel card = _cards.get(_cardIndex);
		_cardIndex++;
		result = card.getTransferValues();
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			_playerList.get(1).setCard(card);
		} else if (!_playerList.get(0).isGameOver()) {
			_playerList.get(0).setCard(card);
		} else if (!_aiPlayer.isGameOver()) {
			_aiPlayer.setCard(card);
		}
		return result;
	}

	/***
	 * 获得玩家输赢
	 */
	public void getGameResult() {

		if (_aiPlayer.getResult() == 2) {
			if (_playerList.get(0).getResult() == 2) {
				_playerList.get(0).setResult(0);
			} else {
				_playerList.get(0).setResult(1);
			}
		} else if (_aiPlayer.getResult() == 1) {
			if (_playerList.get(0).getResult() == 1) {
				_playerList.get(0).setResult(0);
			} else {
				_playerList.get(0).setResult(2);
			}
		} else if (_aiPlayer.getResult() == -1) {
			if (_playerList.get(0).getResult() == 1) {
				_playerList.get(0).setResult(1);
			} else if (_playerList.get(0).getResult() == 2) {
				_playerList.get(0).setResult(2);
			} else {
				if (_playerList.get(0).getScore() > _aiPlayer.getScore()) {
					_playerList.get(0).setResult(2);
				} else if (_playerList.get(0).getScore() == _aiPlayer
						.getScore()) {
					_playerList.get(0).setResult(0);
				} else {
					_playerList.get(0).setResult(1);
				}
			}
		}
		if (_isSplit) {
			if (_aiPlayer.getResult() == 2) {
				if (_playerList.get(1).getResult() == 2) {
					_playerList.get(1).setResult(0);
				} else {
					_playerList.get(1).setResult(1);
				}
			} else if (_aiPlayer.getResult() == 1) {
				if (_playerList.get(1).getResult() == 1) {
					_playerList.get(1).setResult(0);
				} else {
					_playerList.get(1).setResult(2);
				}
			} else if (_aiPlayer.getResult() == -1) {
				if (_playerList.get(1).getResult() == 1) {
					_playerList.get(1).setResult(1);
				} else if (_playerList.get(1).getResult() == 2) {
					_playerList.get(1).setResult(2);
				} else {
					if (_playerList.get(1).getScore() > _aiPlayer.getScore()) {
						_playerList.get(1).setResult(2);
					} else if (_playerList.get(1).getScore() == _aiPlayer
							.getScore()) {
						_playerList.get(1).setResult(0);
					} else {
						_playerList.get(1).setResult(1);
					}
				}
			}
		}
	}

	public void aiPlayerGetCard() {
		int player1score = _playerList.get(0).getScore();
		int player2score2 = 0;
		if (_isSplit) {
			player2score2 = _playerList.get(1).getScore();
		}
		boolean gomeOver = false;
		if (_isSplit) {
			if ((player1score > 21 && player2score2 > 21)) {// || (_playerList.get(0).isGameOver() && _playerList.get(1).isGameOver())
				gomeOver = true;
			}
		} else if (player1score > 21) {// || _playerList.get(0).isGameOver()
			gomeOver = true;
		}

		player1score = player1score > 21 ? 0 : player1score;
		player2score2 = player2score2 > 21 ? 0 : player2score2;
		int compareScoreTop = player1score > player2score2 ? player1score
				: player2score2;
		int compareScoreLow = player1score < player2score2 ? player1score
				: player2score2;
		if (!_isSplit) {
			compareScoreLow = compareScoreTop;
		}

		while (!gomeOver && _aiPlayer.getCard(compareScoreTop, compareScoreLow)) {
			getCard();
		}
		getGameResult();
		if (_isSplit) {
//			System.out.println("游戏结算：玩家1-" + _playerList.get(0).getResult()
//					+ "玩家2-" + _playerList.get(1).getResult() + "ai-"
//					+ _aiPlayer.getResult());
			getGameResultEven(_playerList.get(0).getResult(), _playerList
					.get(1).getResult(), _aiPlayer.getResult(),
					_aiPlayer.getPlayerCardStringList());
		} else {
			getGameResultEven(_playerList.get(0).getResult(), -1,
					_aiPlayer.getResult(), _aiPlayer.getPlayerCardStringList());
		}
		_isGameOver = true;

	}

	public void autoSurrender() {

		int result = -1;
		if (_isSplit && !_playerList.get(1).isGameOver()) {
			_playerList.get(1).setSurrender();
			_playerList.get(1).setResult(1);
			result = 1;
		}
		if (!_playerList.get(0).isGameOver()) {
			_playerList.get(0).setSurrender();
			_playerList.get(0).setResult(1);

		}
		this._isGameOver = true;
		_aiPlayer.setResult(2);
		getGameResultEven(_playerList.get(0).getResult(), result,
				_aiPlayer.getResult(), _aiPlayer.getPlayerCardStringList());
	}

	// 获得最近操作时间间隔
	public int getGameLiveTime() {
		long between = 0;
		between = new Date().getTime() - _opTime.getTime();
		return (int) (between / 1000);
	}

	// 获得游戏已持续时间
	public int getGameLiveTimeNow() {
		long between = 0;
		between = new Date().getTime() - _createDate.getTime();
		between = GameResource.getInstance().getGameLiveTime() - between / 1000;
		// System.out.println("现在时间："+new
		// Date().getTime()+"，本轮时间："+_createDate.getTime());
		if (between < 0) {
			_isGameOver = true;
			autoSurrender();
		}
		return (int) (between);
	}

	/**
	 * * 触发玩家变更事件
	 */
	private void getPlayerChangeEven(int playerNow, int playerNext, int result) {
		if (_playerChangeAndResultEven != null) {
			callBackTryCatch(code -> _playerChangeAndResultEven.appay(
					_gameTimeid, _userid, playerNow, playerNext, result));
		}
	}

	/**
	 * * 触发开始游戏事件
	 */
	private void getStartGameEven(int isAiBlackJack, int isSplit,
			int aihasblack, int playerhasblack) {
		if (_startGameEven != null) {
			callBackTryCatch(code -> _startGameEven.appay(_gameTimeid, _userid,
					isAiBlackJack, isSplit, _playerList.get(0)
							.getPlayerCardStringList(), _aiPlayer
							.getPlayerCardStringList(), aihasblack,
					playerhasblack));
		}
	}

	/**
	 * * 触发结果事件
	 */
	private void getGameResultEven(int player1, int player2, int ai,
			List<String> aiCardList) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("AI牌：[" + _aiPlayer.getResultString() + "]"
				+ "详细：[是否保险" + _isInsurance + "，是否分牌：" + _isSplit + "] ");
		stringBuffer.append("主牌：[" + _playerList.get(0).getResultString()
				+ "]详细：" + "[双倍：" + _playerList.get(0).isIsdouble() + "投降："
				+ _playerList.get(0).isSurrender() + "]");
		if (_isSplit) {
			stringBuffer.append("副牌：[" + _playerList.get(1).getResultString()
					+ "]详细：" + "[双倍：" + _playerList.get(1).isIsdouble() + "投降："
					+ _playerList.get(1).isSurrender() + "]");
		}
		// stringBuffer.append(b);
		if (_gameResultEven != null) {
			callBackTryCatch(code -> _gameResultEven.appay(_gameTimeid,
					_userid, player1, player2, ai, aiCardList,
					_playerList.get(0), _isSplit ? _playerList.get(1) : null,
					_isSplit, _isInsurance ? _chipCoin / 2 : 0, _tableid,
					stringBuffer.toString()));
		}
	}

	/**
	 * * 触发双倍事件
	 */
	private void getDoubleEvenEven(String errorTip, int player, String card) {
		if (_doubleEven != null) {
			callBackTryCatch(code -> _doubleEven.appay(_gameTimeid, _userid,
					errorTip, player, card));
		}
	}

	/**
	 * * 触发要牌事件
	 */
	private void getGetCardEven(String errorTip, int player, String card) {
		if (_getCardEven != null) {
			callBackTryCatch(code -> _getCardEven.appay(_gameTimeid, _userid,
					errorTip, player, card));
		}
	}

	/**
	 * * 触发保险事件
	 */
	private void getInsuranceEven(String errorTip, double insuranceCoin,
			int result) {
		if (_insuranceEven != null) {
			callBackTryCatch(code -> _insuranceEven.appay(_gameTimeid, _userid,
					errorTip, insuranceCoin, result));
		}
	}

	/**
	 * * 触发分牌事件
	 */
	private void getSplitCardEven(String errorTip, List<String> mainCard,
			List<String> subCard) {
		if (_splitCardEven != null) {
			callBackTryCatch(code -> _splitCardEven.appay(_gameTimeid, _userid,
					errorTip, mainCard, subCard));
		}
	}

	/**
	 * * 触发停牌事件
	 */
	private void getStopCardEven(String errorTip) {
		if (_stopCardEven != null) {
			callBackTryCatch(code -> _stopCardEven.appay(_gameTimeid, _userid,
					errorTip));
		}
	}

	/**
	 * * 触发停牌事件
	 */
	private void getSurrenderEven(String errorTip) {
		if (_surrenderEven != null) {
			callBackTryCatch(code -> _surrenderEven.appay(_gameTimeid, _userid,
					errorTip));
		}
	}

	private void callBackTryCatch(Consumer<Integer> consumer) {
		try {
			consumer.accept(0);
		} catch (Exception e) {
			e.printStackTrace();
			// _log.error("调用回调时异常！", e);
		}
	}

	@Override
	public void bindDoubleEven(DoubleEven event) {
		this._doubleEven = event;
	}

	@Override
	public void bindGameResultEven(GameResultEven event) {
		this._gameResultEven = event;
	}

	@Override
	public void bindGetCardEven(GetCardEven event) {
		this._getCardEven = event;
	}

	@Override
	public void bindInsuranceEven(InsuranceEven event) {
		this._insuranceEven = event;
	}

	@Override
	public void bindPlayerChangeAndResult(PlayerChangeAndResult event) {
		this._playerChangeAndResultEven = event;
	}

	@Override
	public void bindSplitCardEven(SplitCardEven event) {
		this._splitCardEven = event;
	}

	@Override
	public void bindStartGameEven(StartGameEven event) {
		this._startGameEven = event;
	}

	@Override
	public void bindStopCardEven(StopCardEven event) {
		this._stopCardEven = event;
	}

	@Override
	public void bindSurrenderEven(SurrenderEven event) {
		this._surrenderEven = event;
	}

}
