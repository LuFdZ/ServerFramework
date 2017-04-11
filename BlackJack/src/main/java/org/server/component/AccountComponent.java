/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.component;

import static org.server.component.GamingComponent.ipPattern;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.json.JSONException;
import org.json.JSONObject;
import org.server.backend.component.GameComponent;
import org.server.backend.io.Transport;
import org.server.backend.io.TransportCallback;
import org.server.backend.session.GameSession;
import org.server.core.GameResource;
import org.server.core.UserCenter;
import org.server.core.http.HttpRequest;
import org.server.core.io.SessionMessage;
import org.server.core.model.custom.GameModel;
import org.server.core.model.custom.RoomModel;
import org.server.core.model.custom.UserInfoModel;
import org.server.core.share.TransferModel2.BnakDepositRequest;
import org.server.core.share.TransferModel2.BnakInfoRequest;
import org.server.core.share.TransferModel2.BnakInfoRespose;
import org.server.core.share.TransferModel2.BnakWithdrawalRequest;
import org.server.core.share.TransferModel2.BnakWithdrawalRespose;
import org.server.core.share.TransferModel2.PlayerInfo;
import org.server.core.share.TransferModel2.ReloginDate;
import org.server.core.share.TransferModel2.UserLoginRequest;
import org.server.core.share.TransferModel2.UserLoginRespose;
import org.server.core.share.TransferModel2.UserRoomLoginRequest;
import org.server.core.share.TransferModel2.UserRoomLoginRespose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tuiyu.shared.terrace.UserInfoUtils;

/**
 *
 * @author Administrator
 */
public class AccountComponent implements GameComponent {

	private static final Logger log = LoggerFactory
			.getLogger(AccountComponent.class);
	private TransportCallback[] _actionCallback;

	@Override
	public void load() {

		_actionCallback = new TransportCallback[] {
				Transport.registerModuleReceived((short) 0x03,
						this::userLoginAction, UserLoginRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x04,
						this::userGetRoomAction,
						UserRoomLoginRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x117,
						this::questBnakInfor, BnakInfoRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x118,
						this::questBnakWithdrawal,
						BnakWithdrawalRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x119,
						this::questBnakDeposit, BnakDepositRequest::parseFrom) };
	}

	@Override
	public void unload() {
		for (TransportCallback _actionCallback1 : _actionCallback) {
			Transport.unregisterMessageReceived(_actionCallback1);
		}
	}

	/**
	 * 用户房间请求
	 *
	 * @param session
	 *            游戏会话
	 * @param message
	 *            会话消息
	 * @param model
	 *            用户请求登录模块
	 */
	void userGetRoomAction(GameSession session, SessionMessage message,
			UserRoomLoginRequest model) {

		UserRoomLoginRespose.Builder builder = UserRoomLoginRespose
				.newBuilder();
		builder.setErrorTip("0");
		UserInfoModel user = UserCenter.getInstance().get("name",
				model.getUserName());

		if (user == null
				|| !UserCenter.getInstance().checkedNameAndPassWord(
						model.getUserName(), model.getUserPass())) {
			builder.setErrorTip("1,用户名或密码错误！\n错误代码：0001");
		} else {

			for (RoomModel room : GameResource.getInstance().getRoomList()) {
				builder.addRoomIdList(room.getRoomid());
				builder.addRoomDescList(room.getRoomName());
			}
		}
		// 推送模块
		Transport.write(session, builder.build());
	}

	/**
	 * 用户登录请求
	 *
	 * @param session
	 *            游戏会话
	 * @param message
	 *            会话消息
	 * @param model
	 *            用户请求登录模块
	 */
	void userLoginAction(GameSession session, SessionMessage message,
			UserLoginRequest model) {
		System.out.println("玩家登录:"+new Date());
		int state = -1;
		UserInfoModel user = null;
		String ip = "";
		Matcher ipMatcher = ipPattern.matcher(session.getRemoteAddress());
		if (ipMatcher.find()) {
			ip = ipMatcher.group();
		}
		int roomid = "-1".equals(model.getRoomId()) ? 1011 : Integer
				.valueOf(model.getRoomId());
		UserLoginRespose.Builder builder = UserLoginRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setGameCoin("-1");
		builder.setGameLivetime(-1);
		builder.setUserMoney("-1");
		builder.setHaveRelogin(-1);
		if (model.getLoginInfo().length() > 5) {
			List<String> list = UserCenter.getInstance().XMLparser(
					model.getLoginInfo());
			try {

				String result = HttpRequest.post(String.format(
						"http://%s:9911/VerifyOnline.do?uid=%s&ip=%s&sid=%s",
						GameResource.getInstance().getRequestIp(), list.get(1),
						ip, list.get(2)), "");
				state = Integer.valueOf(result.split(",")[0].split(":")[1]);// 121.11.83.246
																			// //192.168.1.13

				if (state == 0) {
					result = HttpRequest
							.post(String
									.format("http://%s:9911/ChangeUserRoom.do?userid=%s&etype=%s&rid=%s",
											GameResource.getInstance()
													.getRequestIp(), list
													.get(1), "r", -1), "");
					JSONObject dataJson = new JSONObject(result);
					try {
						state = dataJson.getInt("estatus");
					} catch (Exception e) {
						// 转int 失败既未找到用户
						builder.setErrorTip("0,平台中未找到用户，\n请重新登录平台！");
						Transport.write(session, builder.build());
						return;
					}
					if (state <= 0 || state == Integer.parseInt(list.get(2))) {
						user = UserCenter.getInstance().get("id",
								Integer.valueOf(list.get(1)));
						user.setLtype(0);
						if (user == null) {
							builder.setErrorTip("1,用户名或密码错误!\n错误代码：0024");
							Transport.write(session, builder.build());
							return;
						}
					} else {
						state = -2;// 别的房间中有游戏
					}
				}
				roomid = Integer.valueOf(list.get(0));
			} catch (JSONException | NumberFormatException e) {
				builder.setErrorTip("0,用户名或密码错误!\n错误代码：0003");
				Transport.write(session, builder.build());
				return;
			}
		} else {
			user = UserCenter.getInstance().get("name", model.getUserName());// ,
																				// UserCenter.getInstance().md5(model.getUserPass())
			if (user == null
					&& !UserCenter.getInstance().checkedNameAndPassWord(
							model.getUserName(), model.getUserPass())) {
				builder.setErrorTip("1,用户名或密码错误!\n错误代码：0003");
				Transport.write(session, builder.build());
				return;
			}
			if (user != null) {
				String result = "";
				try {

					String test = String
							.format("http://%s:9911/MbUserLogin.do?loginname=%s&loginpwd=%s&sip=%s",
									GameResource.getInstance().getRequestIp(),
									model.getUserName(), model.getUserPass(),
									ip);
					result = HttpRequest.post(test, "");
				} catch (Exception e) {
					builder.setErrorTip("0,平台服务器维护，详情见公告。");
					Transport.write(session, builder.build());
					return;
				}

				JSONObject dataJson = new JSONObject(result);
				state = -1;
				try {
					state = dataJson.getInt("state");
				} catch (Exception e) {
					// 转int 失败既未找到用户
					builder.setErrorTip("0,用户名或密码错误，\n请重新登录平台！\n错误代码：0013");
					Transport.write(session, builder.build());
					return;
				}
				if (state < 0) {
					builder.setErrorTip("0,用户名或密码错误，\n请重新登录平台！\n错误代码：0014");
					Transport.write(session, builder.build());
					return;
				}

				if (state == 1) {
					UserCenter.getInstance().upDateUserLoginInfo(
							user.getUserid(), ip);
					user.setLtype(1);
					state = 0;
				} else {
					state = -1;
				}
			}
		}
		if (state == -2) {
			builder.setErrorTip("0,检测到游戏客户端已在运行，请先关闭！");
			Transport.write(session, builder.build());
			return;
		}
		if (user == null) {

			builder.setErrorTip("1,用户名或密码错误!\n错误代码：0004");
			Transport.write(session, builder.build());
			return;
		}

		// }
		switch (state) {
		case 0:
			RoomModel roominfo = GameResource.getInstance().getRoom(roomid);
			if (roominfo == null) {
				builder.setErrorTip("0,未找到房间信息！");
			} else {
				if (!roominfo.isRoomAndGameStatus()
						|| !GameResource.getInstance().isMaintainStatus()) {
					builder.setErrorTip("0,平台正在维护中！");
				} else if (user.getUserDemond() < roominfo.getRequestCoin()) {
					builder.setErrorTip("1,金币未达到进入标准！\n你当前金币为:"
							+ String.format("%.4f", user.getUserDemond()));
				} else if (ip.isEmpty() || ip.length() < 4) {
					builder.setErrorTip("0,登录ip校验失败，请重新登录！");
				} else {

					String setRoomResult = roominfo.join(user);
					if (!"1".equals(setRoomResult)) {
						builder.setErrorTip("加入房间失败！");
					} else {

						HttpRequest
								.post(String
										.format("http://%s:9911/ChangeUserRoom.do?userid=%s&etype=%s&rid=%s",
												GameResource.getInstance()
														.getRequestIp(), user
														.getUserid(), "e",
												roomid), "");

						GameResource.getInstance().serUserRoomMap(
								user.getUserid(), roomid);
						String result = roominfo.join(user);
						int _hasGameModel = -1;
						GameModel gameModel = roominfo.getGameByUser(user.getUserid());
						if (gameModel!=null && !gameModel.isGameOver() && gameModel.getGameLiveTimeNow()!=0) {
							_hasGameModel = 1;
							ReloginDate.Builder reloginDate = ReloginDate.newBuilder();
							reloginDate.addAllAiCardList(gameModel.getAiPlayer().getPlayerCardStringList());
							reloginDate.setGameId(gameModel.getGameTimeid());
							int _isSplit = 1;
							if (gameModel.isCanSplit() == 2) {
								if (gameModel.isSplit()) {
									_isSplit = 2;
								}else{
									_isSplit = 3;
								}
							}else if(gameModel.isCanSplit() == 1){
								_isSplit = 1;
							}else {
								_isSplit = -1;
							}
							
							int _isInsurance = 1;
							if (gameModel.isCanInsurance() == 2) {
								if (gameModel.isSplit()) {
									_isInsurance = 2;
								}else{
									_isInsurance = 3;
								}
							}else if(gameModel.isCanInsurance() == 1){
								_isInsurance = 1;
							}else {
								_isInsurance = -1;
							}
							
							reloginDate.setIsSplit(_isSplit);
							reloginDate.setIsInsurance(_isInsurance);
							
							reloginDate.setGameLivetimeNow(gameModel.getGameLiveTimeNow());
//							System.out.println("时间返回："+gameModel.getGameLiveTimeNow());
							PlayerInfo.Builder player1 = PlayerInfo.newBuilder();
							player1.setPlayerRole(1);
							player1.addAllCardList(gameModel.getPlayerList().get(0).getPlayerCardStringList());
							player1.setChipCoin((int)gameModel.getChipCoin());
							player1.setIsDouble(gameModel.getPlayerList().get(0).isIsdouble()?1:-1);
							player1.setIsStop(gameModel.getPlayerList().get(0).isGameOver()?1:-1);
							reloginDate.setPlayerMain(player1.build());
							
							if (gameModel.isSplit()) {
								PlayerInfo.Builder player2 = PlayerInfo.newBuilder();
								player2.setPlayerRole(2);
								player2.addAllCardList(gameModel.getPlayerList().get(1).getPlayerCardStringList());
								player2.setChipCoin((int)gameModel.getChipCoin());
								player2.setIsDouble(gameModel.getPlayerList().get(1).isIsdouble()?1:-1);
								player2.setIsStop(gameModel.getPlayerList().get(1).isGameOver()?1:-1);
								reloginDate.setPlayerSub(player2.build());
							}
							builder.setRelogin(reloginDate.build());
						}
						user.setRoomid(roomid);
						user.setOpTime();
						user.setIpValue(ip);
						user.bindSession(session);
						user.setSepid(roominfo.getGameInfoId());
						String fcDetail = UserInfoUtils.generateTreeUserFc(user.getUserid())[0].toString();
                        user.setFcDetail(fcDetail);
						builder.setGameLivetime(GameResource.getInstance().getGameLiveTime());
						builder.setGameCoin(String.valueOf(roominfo
								.getGameCoin()));
						builder.setUserMoney(String.valueOf(user
								.getUserDemond()));
						builder.setHaveRelogin(_hasGameModel);
					}
				}
			}
			break;
		case 1:
			break;
		case -1:
			builder.setErrorTip("0,平台中未找到用户，请重新登录平台！");
			break;
		case -2:
			builder.setErrorTip("0,检测到游戏客户端已在运行，请先关闭！");
			break;
		default:
			break;
		}
		Transport.write(session, builder.build());
	}

	/**
	 * 请求银行信息
	 *
	 * @param session
	 * @param message
	 * @param model
	 */
	void questBnakInfor(GameSession session, SessionMessage message,
			BnakInfoRequest model) {
		UserInfoModel user = UserCenter.getInstance().get("name",
				model.getUserName());
		List<String> list = new LinkedList<>();
		BnakInfoRespose.Builder builder = BnakInfoRespose.newBuilder();
		builder.setErrorTip("0");
		builder.setBankCoin("-1");
		builder.setBankLockedCoin("-1");
		builder.setBankAvail("-1");
		builder.setGameBankCoin("-1");
		builder.setGameBankLockedCoin("-1");
		builder.setGameBankAvail("-1");
		if (user == null) {
			builder.setErrorTip("0,未找到用户，请重新登录！");
			Transport.write(session, builder.build());
			return;
		}
		if (!UserInfoUtils.checkUserPassword(model.getUserName(),null,  model.getSecPass())) {
        	builder.setErrorTip("1,安全密码不正确！");
            Transport.write(session, builder.build());
            return;
		}
		list = UserCenter.getInstance().getBankInfo(user.getUserid());

		builder.setBankCoin(list.get(0));
		builder.setBankLockedCoin(list.get(1));
		builder.setBankAvail(list.get(2));
		builder.setGameBankCoin(list.get(3));
		builder.setGameBankLockedCoin(list.get(4));
		builder.setGameBankAvail(list.get(5));
		Transport.write(session, builder.build());
	}

	/**
	 * 存款
	 *
	 * @param session
	 * @param message
	 * @param model
	 */
	void questBnakDeposit(GameSession session, SessionMessage message,
			BnakDepositRequest model) {
		UserInfoModel user = UserCenter.getInstance().get("name",
				model.getUserName());
		BnakWithdrawalRespose.Builder builder = BnakWithdrawalRespose
				.newBuilder();
		builder.setErrorTip("0");
		BnakInfoRespose.Builder builderSub = BnakInfoRespose.newBuilder();
		builderSub.setErrorTip("0");
		builderSub.setBankCoin("-1");
		builderSub.setBankLockedCoin("-1");
		builderSub.setBankAvail("-1");
		builderSub.setGameBankCoin("-1");
		builderSub.setGameBankLockedCoin("-1");
		builderSub.setGameBankAvail("-1");
		if (user == null
				|| !UserCenter.getInstance().checkedNameAndPassWord(
						model.getUserName(), model.getUserPsw())) {
			builder.setErrorTip("1,用户名密码错误！");
			Transport.write(session, builder.build());
			return;
		}
		
		if (!UserInfoUtils.checkUserPassword(model.getUserName(),model.getUserPsw(),  model.getSecPass())) {
        	builder.setErrorTip("1,用户密码或安全密码不正确！");
            Transport.write(session, builder.build());
            return;
		}

		int state = -1;
		String resultHttp = HttpRequest.post(String.format(
				"http://%s:9911/ChangeUserRoom.do?userid=%s&etype=%s&rid=%s",
				GameResource.getInstance().getRequestIp(), user.getUserid(),
				"r", -1), "");
		JSONObject dataJson = new JSONObject(resultHttp);
		try {
			state = dataJson.getInt("estatus");
			// System.out.println("sssssssssssssssssssssssssssssss:" + state);
		} catch (Exception e) {
		} finally {
			if (state > 0) {
				builder.setErrorTip("0,检测到客户端已在运行，\n请先关闭其他游戏！");
				Transport.write(session, builder.build());
				return;
			}
		}

		String ip = session.getRemoteAddress();
		int result = UserCenter.getInstance().updateUserMoney(user.getUserid(),
				Double.valueOf(model.getMoney()), ip);

		builder.setErrorTip("0");
		switch (result) {
		case 0:
			builder.setErrorTip("0");
			break;
		case -1:
			builder.setErrorTip("1,金币不足！\n");
			break;
		case -2:
			builder.setErrorTip("1,操作失败，请重新操作！");
			break;
		}
		List<String> list = new LinkedList<>();

		list = UserCenter.getInstance().getBankInfo(user.getUserid());

		builderSub.setBankCoin(list.get(0));
		builderSub.setBankLockedCoin(list.get(1));
		builderSub.setBankAvail(list.get(2));
		builderSub.setGameBankCoin(list.get(3));
		builderSub.setGameBankLockedCoin(list.get(4));
		builderSub.setGameBankAvail(list.get(5));
		builder.setUpdateInfo(builderSub);
		Transport.write(session, builder.build());
	}

	/**
	 * 取款
	 *
	 * @param session
	 * @param message
	 * @param model
	 */
	void questBnakWithdrawal(GameSession session, SessionMessage message,
			BnakWithdrawalRequest model) {
		UserInfoModel user = UserCenter.getInstance().get("name",
				model.getUserName());
		BnakWithdrawalRespose.Builder builder = BnakWithdrawalRespose
				.newBuilder();
		builder.setErrorTip("0");
		BnakInfoRespose.Builder builderSub = BnakInfoRespose.newBuilder();
		builderSub.setErrorTip("0");
		builderSub.setBankCoin("-1");
		builderSub.setBankLockedCoin("-1");
		builderSub.setBankAvail("-1");
		builderSub.setGameBankCoin("-1");
		builderSub.setGameBankLockedCoin("-1");
		builderSub.setGameBankAvail("-1");
		if (user == null
				|| !UserCenter.getInstance().checkedNameAndPassWord(
						model.getUserName(), model.getUserPsw())) {
			builder.setErrorTip("1,用户名密码错误！");
			Transport.write(session, builder.build());
			return;
		}
		
		if (!UserInfoUtils.checkUserPassword(model.getUserName(),model.getUserPsw(),  model.getSecPass())) {
        	builder.setErrorTip("1,用户密码或安全密码不正确！");
            Transport.write(session, builder.build());
            return;
		}

		int state = -1;
		String resultHttp = HttpRequest.post(String.format(
				"http://%s:9911/ChangeUserRoom.do?userid=%s&etype=%s&rid=%s",
				GameResource.getInstance().getRequestIp(), user.getUserid(),
				"r", -1), "");
		JSONObject dataJson = new JSONObject(resultHttp);
		try {
			state = dataJson.getInt("estatus");
		} catch (Exception e) {
		} finally {
			if (state > 0) {
				builder.setErrorTip("0,检测到客户端已在运行，\n请先关闭其他游戏！");
				Transport.write(session, builder.build());
				return;
			}
		}

		String ip = session.getRemoteAddress();
		int result = UserCenter.getInstance().updateUserMoney(user.getUserid(),
				-Double.valueOf(model.getMoney()), ip);

		builder.setErrorTip("0");
		switch (result) {
		case 0:
			builder.setErrorTip("0");
			break;
		case -1:
			builder.setErrorTip("1,金币不足！");
			break;
		case -2:
			builder.setErrorTip("1,操作失败，请重新操作！");
			break;
		}

		List<String> list = new LinkedList<>();

		list = UserCenter.getInstance().getBankInfo(user.getUserid());

		builderSub.setBankCoin(list.get(0));
		builderSub.setBankLockedCoin(list.get(1));
		builderSub.setBankAvail(list.get(2));
		builderSub.setGameBankCoin(list.get(3));
		builderSub.setGameBankLockedCoin(list.get(4));
		builderSub.setGameBankAvail(list.get(5));
		builder.setUpdateInfo(builderSub);
		Transport.write(session, builder.build());
	}
}
