/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.model.custom;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.server.core.GameResource;
import org.server.core.UserCenter;
import org.server.core.data.DataSource;
import org.server.core.model.Roominfo;
import org.server.core.share.TransferModel2.DoubleRespose;
import org.server.core.share.TransferModel2.GameResult;
import org.server.core.share.TransferModel2.GetCardRespose;
import org.server.core.share.TransferModel2.InsuranceRespose;
import org.server.core.share.TransferModel2.PlayerChange;
import org.server.core.share.TransferModel2.SplitCardRespose;
import org.server.core.share.TransferModel2.StartGameRespose;
import org.server.core.share.TransferModel2.StopRespose;
import org.server.core.share.TransferModel2.SurrenderRespose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class RoomModel {

	private static final Logger _log = LoggerFactory.getLogger(RoomModel.class);

	private final int _roomid;

	private String _roomName;

	private double _gameCoin;//

	private double _requestCoin;// 用户进入房间所要求携带的金币最小量

	private int _userLimit;// 用户数量上限

	private double _maxLimit;// 用户下注上限

	private final CopyOnWriteArrayList<UserInfoModel> _userList;

	private ConcurrentHashMap<Integer, GameModel> _gameUserMap;

//	private List<UserInfoModel> _playerList;// 玩家列表

	private final int _gameInfoId;// 游戏实例id

	private final double _roomts;// 房间TS

	private boolean _roomAndGameStatus;

	private Object object = new Object();

	private int _roomGameStauts;// 游戏运行状态

	public RoomModel(int _roomid, String _roomName, double _gameCoin,
			double _requestCoin, int _userLimit, int _gameInfoId,
			double _roomts, double _maxLimit) {
		this._roomid = _roomid;
		this._roomName = _roomName;
		this._gameCoin = _gameCoin;
		this._requestCoin = _requestCoin;
		this._userLimit = _userLimit;
		this._gameInfoId = _gameInfoId;
		this._roomts = _roomts;
		this._maxLimit = _maxLimit;
		_gameUserMap = new ConcurrentHashMap<Integer, GameModel>();
		_userList = new CopyOnWriteArrayList<>();
		updateRoomAndGameStatus();
		_roomGameStauts = 0;
	}

	public int getRoomGameStauts() {
		return _roomGameStauts;
	}

	public int getRoomid() {
		return _roomid;
	}

	public String getRoomName() {
		return _roomName;
	}

	public double getGameCoin() {
		return _gameCoin;
	}

	public double getMaxLimit() {
		return _maxLimit;
	}

	public double getRequestCoin() {
		return _requestCoin;
	}

	public int getUserLimit() {
		return _userLimit;
	}

	public int getGameInfoId() {
		return _gameInfoId;
	}

	public double getRoomts() {
		return _roomts;
	}

	public List<UserInfoModel> getUserList() {
		return _userList;
	}

	public void setRoomName(String _roomName) {
		this._roomName = _roomName;
	}

	public void setGameCoin(double _gameCoin) {
		this._gameCoin = _gameCoin;
	}

	public void setRequestCoin(double _requestCoin) {
		this._requestCoin = _requestCoin;
	}

	public void setUserLimit(int _userLimit) {
		this._userLimit = _userLimit;
	}

	public String join(UserInfoModel user) {
		if (_userList.size() >= _userLimit) {
			return "0,房间用户已满！";
		}
		// if (user.getUserDemond() < _requestCoin) {
		// return "0,金币不足！";
		// }
		String result = "2";
		for (UserInfoModel usertemp : _userList) {
			if (usertemp.getUserid() == user.getUserid()) {
				usertemp.setSeqidUpdate(user.getSepid());
				usertemp.setOpTime();
				usertemp.setIpValue(user.getIpValue());
				usertemp.setIpValue(user.getIpValue());
				usertemp.setParentFc(user.getParentFc());
				result = "1";
			}
		}
		if (result.equals("2")) {
			_userList.add(user);
			result = "1";
		}

		return result;
	}

	// float a[] =new
	public int startGame(UserInfoModel user, int chip) {
		if (chip<=0) {
			return -4;
		}
		if (user.getUserDemond() < chip) {
			return -1;
		}
		if (_maxLimit < chip) {
			return -3;
		}
		int result = 0;
		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null && !gameModel.isGameOver()) {
			return -2;
		} else if (gameModel != null && gameModel.isGameOver()) {
			result = user.setUserOrder(chip, _roomid, _roomts, _gameCoin);
			if (result == 1) {
				gameModel.init(chip);
				gameModel.dealCard();
				user.setStartGame(true);
			}
		} else {
			result = user.setUserOrder(chip, _roomid, _roomts, _gameCoin);
			if (result == 1) {

				gameModel = new GameModel(user.getUserid(), _roomid, chip,user.getTableid());
				gameModel.bindDoubleEven(this::onDoubleEven);
				gameModel.bindGameResultEven(this::onGameResultEven);
				gameModel.bindGetCardEven(this::onGetCardEven);
				gameModel.bindInsuranceEven(this::onInsuranceEven);
				gameModel.bindPlayerChangeAndResult(this::onPlayerChangeAndResult);
				gameModel.bindSplitCardEven(this::onSplitCardEven);
				gameModel.bindStartGameEven(this::onStartGameEven);
				gameModel.bindStopCardEven(this::onStopCardEven);
				gameModel.bindSurrenderEven(this::onSurrenderEven);

				gameModel.dealCard();
				user.setStartGame(true);
			}

			_gameUserMap.put(user.getUserid(), gameModel);

		}
//		result = user.setUserOrder(chip, _roomid, _roomts, _gameCoin);
//		if (result == 1) {
//			gameModel.dealCard();
//			user.setStartGame(true);
//		}

		// double money, int roomid, double roomts, double gamecoin
		return result;

	}

	public int getCard(UserInfoModel user,String gameTimeid) {
		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null) {
			if (!gameModel.getGameTimeid().equals(gameTimeid)) {
				return -2;
			}
			gameModel.playerGetCard();
			return 1;
		} else {
			return -1;
		}
	}

	public GameModel getGameByUser(int userid) {
		return _gameUserMap.get(userid);
	}

	public int setDouble(UserInfoModel user,String gameTimeid) {

		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null) {
			if (!gameModel.getGameTimeid().equals(gameTimeid)) {
				return -2;
			}
			if (user.getUserDemond() < gameModel.getChipCoin()) {
				return -1;
			}
			user.updateUserOrder(gameModel.getChipCoin());
			gameModel.setDouble();
		}
		return 1;
	}

	public int setTableID(UserInfoModel user, int tableid) {
		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null) {
			if (!gameModel.isGameOver()) {
				return -2;
			}
			gameModel.setTableid(tableid);
		}else{
			user.setTableid(tableid);
		}
		return tableid;
	}

	public int setStop(UserInfoModel user,String gameTimeid) {
		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null) {
			if (!gameModel.getGameTimeid().equals(gameTimeid)) {
				return -2;
			}
			gameModel.setStopCard();
		} else {
			return -1;
		}
		return 1;
	}

	public int setInsurance(UserInfoModel user, boolean hasInsurance,String gameTimeid) {
		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null) {
			if (!gameModel.getGameTimeid().equals(gameTimeid)) {
				return -2;
			}
			if (hasInsurance && user.getUserDemond() < gameModel.getChipCoin() / 2) {
				return -1;
			}
			if (hasInsurance) {
				user.updateUserOrder(gameModel.getChipCoin() / 2);
			}
			gameModel.setInsuranceEven(hasInsurance);
		}
		return 1;
	}

	public int setSplitCard(UserInfoModel user,String gameTimeid) {
		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null) {
			if (!gameModel.getGameTimeid().equals(gameTimeid)) {
				return -2;
			}
			if (user.getUserDemond() < gameModel.getChipCoin()) {
				return -1;
			}
			user.updateUserOrder(gameModel.getChipCoin());
			gameModel.setSplitCard();
		}
		return 1;
	}

	public int setSurrender(UserInfoModel user,String gameTimeid) {
		GameModel gameModel = _gameUserMap.get(user.getUserid());
		if (gameModel != null) {
			if (!gameModel.getGameTimeid().equals(gameTimeid)) {
				return -2;
			}
			gameModel.setSurrender();
		}
		return 1;
	}

	public UserInfoModel getUser(int userid) {
		for (UserInfoModel user : _userList) {
			if (user.getUserid() == userid) {
				return user;
			}
		}
		return null;
	}

	public void cleanUser() {
		for (UserInfoModel user : _userList) {
//			System.out.println("用户计时"+user.getTimeSpan());
			if (user.getTimeSpan() > 60) {// && !user.isStartGame()//
											// GameResource.getInstance().getGameLiveTime()
		if (user.isStartGame()) {
					GameModel gameModel = _gameUserMap.get(user.getUserid());
//					System.out.println("游戏计时"+gameModel.getGameLiveTimeNow());
					if (gameModel != null
							&& gameModel.getGameLiveTimeNow() > GameResource
									.getInstance().getGameLiveTime()) {
						gameModel.autoSurrender();
						_gameUserMap.remove(user.getUserid());
						user.setStartGame(false);
						_userList.remove(user);
						GameResource.getInstance().deleteGame(user);
						_gameUserMap.remove(user.getUserid());
					}
				}else {
					_userList.remove(user);
					_gameUserMap.remove(user.getUserid());
					GameResource.getInstance().deleteGame(user);
				}
			}
		}
	}

	public boolean deleteUser(int userid) {
		for (UserInfoModel user : _userList) {
			if (user.getUserid() == userid) {
				GameModel gameModel = _gameUserMap.get(user.getUserid());
				if (gameModel != null && !gameModel.isGameOver()) {
					user.setStartGame(false);
					gameModel.autoSurrender();
					_gameUserMap.remove(user.getUserid());
				}
				_userList.remove(user);
				GameResource.getInstance().deleteGame(user);
				return true;
			}
			
		}
		
		return false;
	}

	public boolean existUser(int userid) {
		for (UserInfoModel user : _userList) {
			if (user.getUserid() == userid) {
				return true;
			}
		}
		return false;
	}

	public boolean isRoomAndGameStatus() {
		return _roomAndGameStatus;
	}

	public final void updateRoomAndGameStatus() {
		Session session = DataSource.openSession();
		int roomStatus = 0;
		int gameStatus = 0;
		try {
			roomStatus = (int) session.createCriteria(Roominfo.class)
					.setProjection(Projections.groupProperty("roomStatus"))
					.add(Restrictions.eq("roomId", _roomid)).uniqueResult();

			gameStatus = Integer.parseInt(GameResource.bjGameModel.readStringValue("gameStatus"));

		} catch (Exception e) {
			_log.error("roomAndGameStatus:" + e.getMessage());
		}
		session.close();

		if (roomStatus == 1 && gameStatus == 1) {
			_roomAndGameStatus = true;
			return;
		}
		_roomAndGameStatus = false;
	}

	private void onDoubleEven(String gameid,int userid, String errorTip, int player,
			String newCard) {

		DoubleRespose.Builder builder = DoubleRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setPlayer(0);
		builder.setNewCard("-1");
		builder.setUserMoney("-1");
		builder.setGameId(gameid);
		if (errorTip != null) {
			builder.setErrorTip(errorTip);
		} else {
			UserInfoModel user = getUser(userid);
			if (user == null) {
				builder.setErrorTip("0,未找到用户！错误代码：0011");
			} else {
				builder.setPlayer(player);
				builder.setNewCard(newCard);
				builder.setUserMoney(String.valueOf(user.getUserDemond()));
			}

		}
//		System.out.println(userid+"双倍事件："+builder.getUserMoney());
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onGameResultEven(String gameid,int userid, int result1, int result2,
			int resultAi, List<String> aiCardList, PlayerInfoModel player1,
			PlayerInfoModel player2, boolean isSplit, double hasInsuranceEven,
			int tableid,String log) {
		GameResult.Builder builder = GameResult.newBuilder();

		result1 = result1 == 0 ? 3 : result1;
		result2 = result2 == 0 ? 3 : result2;
		resultAi = resultAi == 0 ? 3 : resultAi;

		builder.setPlayerMainResult(result1);
		builder.setPlayerSubResult(result2);
		builder.setAiResult(resultAi);
		builder.setUserMoney("-1");
		builder.setResultMoney("-1");
		// 截取AI初始化之后新发的牌
		List<String> aiNewCardList = new LinkedList<>();
		for (int i = 2; i < aiCardList.size(); i++) {
			aiNewCardList.add(aiCardList.get(i));
		}
		builder.addAllAiCardList(aiNewCardList);
		double EChip = 0;//实际金额
		// 结算订单
		UserInfoModel user = getUser(userid);
		if (user != null) {
			double chip = player1.getChipCoin();
			double orderbargain = 0;
			double orderresult = 0;
			double orderbargain2 = 0;
			double orderresult2 = 0;
			boolean isTie1 = false;
			boolean isTie2 = false;
			if (hasInsuranceEven > 0) {
				orderbargain -= hasInsuranceEven;
			}
			if (result1 == 2) {
				orderbargain += chip;
			} else if (result1 == 3) {
				orderbargain = 0;
				orderbargain-= hasInsuranceEven;
				isTie1 = true;
			}else {
				orderbargain -= chip;
			}
			orderresult = orderbargain;
			if (orderbargain > 0) {
				orderresult = orderbargain * (1 - _roomts);
			}
			if (isTie1) {
				EChip += chip;
			}
			
			if (isSplit) {
				double chip2 = player2.getChipCoin();

				if (result2 == 2) {
					orderbargain2 += chip2;
				}else if (result2 == 3) {
					orderbargain2 = 0;
					isTie2 = true;
				} else {
					
					orderbargain2 -= chip2;
				}
				orderresult2 = orderbargain2;
				if (orderbargain2 > 0) {
					orderresult2 = orderbargain2 * (1 - _roomts);
				}
				if (isTie2) {
					EChip += chip2;
				}
			}
			
			double orderResult = user.overOrder(orderbargain + orderbargain2, orderresult
					+ orderresult2, tableid,EChip,log);
			user.setStartGame(false);
			builder.setResultMoney(String.valueOf(orderResult));
//			System.out.println(userid+"订单结算："+builder.getUserMoney());
//			System.out.println("orderbargain111：" + orderbargain
//					+ ",orderresult111:" + orderresult);
//			System.out.println("orderbargain222：" + orderbargain2
//					+ ",orderresult222:" + orderresult2);
			builder.setUserMoney(String.valueOf(user.getUserDemond()));

		}
		builder.setGameId(gameid);
		
//		System.out.println("玩家金币更新：" + builder.getUserMoney());
		// GameModel gameModel = _gameUserMap.get(user.getUserid());
		// _gameUserMap.remove(user.getUserid());
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onGetCardEven(String gameid,int userid, String errorTip, int player,
			String newCard) {
		GetCardRespose.Builder builder = GetCardRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setPlayer(-1);
		builder.setNewCard("-1");
		builder.setGameId(gameid);
		if (errorTip != null) {
			builder.setErrorTip(errorTip);
		} else {
			builder.setPlayer(player);
			builder.setNewCard(newCard);
		}
		builder.setGameId(gameid);
//		System.out.println(userid+"要牌事件发送");
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onInsuranceEven(String gameid,int userid, String errorTip, double diamond,
			int result) {
		InsuranceRespose.Builder builder = InsuranceRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setDemond("-1");
		builder.setResult(result);
		if (errorTip != null) {
			builder.setErrorTip(errorTip);
		} else {
			UserInfoModel user = getUser(userid);
			if (user == null) {
				builder.setErrorTip("0,未找到用户！错误代码：0011");
			} else {
				builder.setDemond(String.valueOf(diamond));
				builder.setUserMoney(String.valueOf(user.getUserDemond()));
			}
		}
		builder.setGameId(gameid);
//		System.out.println("保险事件发送"+builder.getUserMoney()+"游戏id："+gameid);
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onPlayerChangeAndResult(String gameid,int userid, int userNowId,
			int userNextId, int result) {
		PlayerChange.Builder builder = PlayerChange.newBuilder();
		builder.setErrorTip("0");
		builder.setPlayerNow(userNowId);
		builder.setPlayerNext(userNextId);
		builder.setPlayerNowResult(result);
		builder.setGameId(gameid);
//		System.out.println(userid+"角色更改事件发送");
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onSplitCardEven(String gameid,int userid, String errorTip,
			List<String> mainCard, List<String> subCard) {
		SplitCardRespose.Builder builder = SplitCardRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setUserMoney("0");
		if (errorTip != null) {
			builder.setErrorTip(errorTip);
		} else {
			UserInfoModel user = getUser(userid);
			if (user == null) {
				builder.setErrorTip("0,未找到用户！错误代码：0012");
			} else {
				builder.setUserMoney(String.valueOf(user.getUserDemond()));
				builder.addAllMainCard(mainCard);
				builder.addAllSubCard(subCard);
			}

		}
		builder.setGameId(gameid);
//		System.out.println(userid+"分牌事件发送"+builder.getUserMoney());
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onStartGameEven(String gameid,int userid, int _isAiBlackJack, int _isSplit,
			List<String> playerCardList, List<String> aiCardList,int aihasBlack,int playHasBlack) {
		StartGameRespose.Builder builder = StartGameRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setUserMoney("-1");
		builder.setIsAiBlackjack(_isAiBlackJack == 0 ? -1 : _isAiBlackJack);
		builder.setIsSplit(_isSplit == 0 ? -1 : _isSplit);
		builder.addAllAiCardList(aiCardList);
		builder.addAllPlayerMainCardList(playerCardList);
		builder.setAiBlackjack(aihasBlack);
		builder.setPlayerBlackjack(playHasBlack);
		// UserCenter.getInstance().sendMessage(userid, builder.build());

		UserInfoModel user = getUser(userid);
		if (user == null) {
			builder.setErrorTip("0,未找到用户！错误代码：0012");
		} else {
			builder.setUserMoney(String.valueOf(user.getUserDemond()));
		}
		builder.setGameId(gameid);
//		System.out.println(userid+"开始游戏事件发送"+builder.getUserMoney()+"游戏id："+gameid);
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onStopCardEven(String gameid,int userid, String errorTip) {
		StopRespose.Builder builder = StopRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setResult2("1");
		if (errorTip != null) {
			builder.setErrorTip(errorTip);
		}
		builder.setGameId(gameid);
//		System.out.println(userid+"停牌事件发送");
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

	private void onSurrenderEven(String gameid,int userid, String errorTip) {
		SurrenderRespose.Builder builder = SurrenderRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setResult("1");
		if (errorTip != null) {
			builder.setErrorTip(errorTip);
		}
		builder.setGameId(gameid);
//		System.out.println(userid+"投降事件发送");
		UserCenter.getInstance().sendMessage(userid, builder.build());
	}

}
