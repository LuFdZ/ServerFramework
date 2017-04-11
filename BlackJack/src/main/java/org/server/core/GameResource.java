/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;
import org.server.command.LocalCommandService;
import org.server.core.data.DataSource;
import org.server.core.http.HttpRequest;
import org.server.core.model.Roominfo;
import org.server.core.model.custom.GameModel;
import org.server.core.model.custom.RoomModel;
import org.server.core.model.custom.UserInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tuiyu.shared.terrace.database.GameEnvironment;

/**
 *
 * @author Administrator
 */
public class GameResource {

	private static final Logger _log = LoggerFactory
			.getLogger(GameResource.class);
	private static GameResource _instance;
	List<Roominfo> _roomInfoList;
	private static boolean _stopGame = false;
	ConcurrentHashMap<Integer, Integer> _userRoomMap;
	List<RoomModel> _roomList;
//	private int _gameLiveTime = 0;
//	public String _requestIp;
	private boolean _roomAndGameStatus;
//	private int _gameStatus;
	private static int _gameid = 101;
//	public int _maintainStuatus = 0;// 平台维护状态 0正常；1维护
	
	public static GameEnvironment bjGameModel;// 游戏信息实例

	static{
		bjGameModel = new GameEnvironment(_gameid);
		_instance = new GameResource();
	}

	public static GameResource getInstance() {
		return _instance;
	}

	public GameResource() {
		_roomInfoList = new LinkedList<>();
		_roomList = new LinkedList<>();
		_userRoomMap = new ConcurrentHashMap<>();
		Session session = DataSource.openSession();
		_roomInfoList = session.createCriteria(Roominfo.class)
				.add(Restrictions.eq("gameId", _gameid)).list();
//		_gameInfo = (Gameinfo) session.createCriteria(Gameinfo.class)
//				.add(Restrictions.eq("gameId", _gameid)).list().get(0);
//		_gameStatus = _gameInfo.getGameStatus();
//		_gameLiveTime = Integer.valueOf(((Syscodebook) session
//				.createCriteria(Syscodebook.class)
//				.add(Restrictions.eq("id", new SyscodebookId("BLACKJACKTIME",
//						"GAME"))).list().get(0)).getCodeLabel());
//
//		_requestIp = String
//				.valueOf(((Syscodebook) session
//						.createCriteria(Syscodebook.class)
//						.add(Restrictions.eq("id", new SyscodebookId("PTIP",
//								"SYSTEM"))).list().get(0)).getCodeLabel());

		session.close();
		//int _roomid, String _roomName, double _gameCoin,double _requestCoin, int _userLimit,int _gameInfoId, double _roomts
		for (Roominfo _room : _roomInfoList) {
			RoomModel room = new RoomModel(_room.getRoomId(),_room.getRoomName(),_room.getGameCoin(),_room.getMinRequire(),200,_gameid,_room.getRoomTs(),_room.getMaxLimit());
			_roomList.add(room);
		}
//		getMaintainStatus();
		// 注册重新加载命令
		LocalCommandService.getInstance().register("reloadResources",
				GameResource::clearInstance);
		LocalCommandService.getInstance().register("stopGame",
				GameResource::stopGame);
	}

	/**
	 * 清理实例
	 */
	static void clearInstance() {
		_instance = null;
		getInstance();
	}


	public void deleteGame(UserInfoModel user) {
		if (user.getLtype() == 0) {
			HttpRequest
					.post(String
							.format("http://%s:9911/ChangeUserRoom.do?userid=%s&etype=%s&rid=%s",
									GameResource.getInstance().getRequestIp(),
									user.getUserid(), "q", user.getRoomid()),
							"");
			
		} else {
			HttpRequest.post(String.format(
					"http://%s:9911/MbUserLogout.do?userid=%s", GameResource
							.getInstance().getRequestIp(), user.getUserid()),
					"");
		}
		_userRoomMap.remove(user.getUserid());
//		Exception e = new Exception("用户平台退出"+user.getLtype());
//		_log.info("用户平台退出", e);
	}

	public List<Roominfo> getRoomInfoList() {
		return _roomInfoList;
	}

	public int getGameLiveTime() {
		return Integer.parseInt(bjGameModel.readStringValue("BLACKJACKTIME_GAME"));
	}

	public String getRequestIp() {
		return bjGameModel.readStringValue("PTIP_SYSTEM");
	}

//	public Gameinfo getGameInfo() {
//		return _gameInfo;
//	}
	
	public List<RoomModel> getRoomList() {
		return _roomList;
	}
	
	public RoomModel getRoom(int roomid){
		for (RoomModel room : _roomList) {
			if (room.getRoomid() == roomid) {
				return room;
			}
		}
		return null;
	}

	public void serUserRoomMap(int userid, int roomid) {
		synchronized (_userRoomMap) {
			_userRoomMap.put(userid, roomid);
		}
	}
	
	public int getRoomid(int userid){
		
		try {
			return _userRoomMap.get(userid);
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean isStopGame() {
		return _stopGame;
	}

	private static void stopGame() {
		_stopGame = true;
	}

	public boolean isRoomAndGameStatus() {
		return _roomAndGameStatus;
	}

	public GameEnvironment getGame() {
		return bjGameModel;
	}
	
//	public int getMaintainStatus() {
//		Session session = DataSource.openSession();
//		_maintainStuatus = Integer.valueOf(((Syscodebook) session
//				.createCriteria(Syscodebook.class)
//				.add(Restrictions.eq("id", new SyscodebookId("MAINTAINSTATUS",
//						"SYSTEM"))).list().get(0)).getCodeLabel());
//		session.close();
//		return _maintainStuatus;
//	}

	/**
	 * * 服务器是否在运行状态
	 *
	 * @return
	 */
	public boolean isMaintainStatus() {
		return bjGameModel.readStringValue("MAINTAINSTATUS_SYSTEM").equals("0");
	}

}
