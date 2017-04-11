/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.component;

import java.util.Date;
import java.util.regex.Pattern;

import org.server.backend.component.GameComponent;
import org.server.backend.io.Transport;
import org.server.backend.io.TransportCallback;
import org.server.backend.session.GameSession;
import org.server.core.GameResource;
import org.server.core.UserCenter;
import org.server.core.io.SessionMessage;
import org.server.core.model.custom.RoomModel;
import org.server.core.model.custom.UserInfoModel;
import org.server.core.share.TransferModel2.ChangeTableRequest;
import org.server.core.share.TransferModel2.ChangeTableRespose;
import org.server.core.share.TransferModel2.DoubleRequest;
import org.server.core.share.TransferModel2.DoubleRespose;
import org.server.core.share.TransferModel2.GetCardRequest;
import org.server.core.share.TransferModel2.GetCardRespose;
import org.server.core.share.TransferModel2.HeartbeatPacket;
import org.server.core.share.TransferModel2.HeartbeatRespose;
import org.server.core.share.TransferModel2.InsuranceRequest;
import org.server.core.share.TransferModel2.InsuranceRespose;
import org.server.core.share.TransferModel2.QuitGameRequest;
import org.server.core.share.TransferModel2.SplitCardRequest;
import org.server.core.share.TransferModel2.SplitCardRespose;
import org.server.core.share.TransferModel2.StartGameRequest;
import org.server.core.share.TransferModel2.StartGameRespose;
import org.server.core.share.TransferModel2.StopRequest;
import org.server.core.share.TransferModel2.StopRespose;
import org.server.core.share.TransferModel2.SurrenderRequest;
import org.server.core.share.TransferModel2.SurrenderRespose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class GamingComponent implements GameComponent {

	private static final Logger Logger = LoggerFactory
			.getLogger(GameComponent.class);
	private TransportCallback[] _transportCallback;
	static final Pattern ipPattern = Pattern
			.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");

	public GamingComponent() {
	}

	@Override
	public void load() {
		_transportCallback = new TransportCallback[] {
				Transport.registerModuleReceived((short) 0x05,
						this::requestStartGame, StartGameRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x07,
						this::requestDealCard, GetCardRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x08,
						this::requestSplitCard, SplitCardRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x09,
						this::doubleRequest, DoubleRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x11,
						this::insuranceRequest, InsuranceRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x10,
						this::surrenderRequest, SurrenderRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x13,
						this::requestHeartbeat, HeartbeatPacket::parseFrom),
				Transport.registerModuleReceived((short) 0x14,
						this::requitGameRequest, QuitGameRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x06,
						this::requestStopCard, StopRequest::parseFrom),
				Transport.registerModuleReceived((short) 0x12,
						this::requestChangeTable, ChangeTableRequest::parseFrom) };
	}

	@Override
	public void unload() {
		for (TransportCallback transportCallback1 : _transportCallback) {
			Transport.unregisterMessageReceived(transportCallback1);
		}
	}

	/**
	 * 请求心跳检测
	 *
	 * @param session
	 *            游戏会话
	 * @param message
	 *            游戏消息
	 * @param model
	 *            消息模块
	 */
	void requestHeartbeat(GameSession session, SessionMessage message,
			HeartbeatPacket model) {
//		 System.out.println("请求心跳" + new Date());
		HeartbeatRespose.Builder builder = HeartbeatRespose.newBuilder()
				.setTextInfo(new Date().toString());
		Transport.write(session, builder.build());
//		 System.out.println("返回心跳" + new Date());
	}

	/**
	 * 请求开始一个新游戏
	 *
	 * @param session
	 *            游戏会话
	 * @param message
	 *            游戏消息
	 * @param model
	 *            消息模块
	 */
	void requestStartGame(GameSession session, SessionMessage message,
			UserInfoModel user, StartGameRequest model) {

		StartGameRespose.Builder respose = StartGameRespose.newBuilder();
		respose.setErrorTip("0");
		respose.setIsAiBlackjack(-1);
		respose.setIsSplit(-1);
		respose.setUserMoney("-1");
		respose.setAiBlackjack(-1);
		respose.setPlayerBlackjack(-1);
		respose.setGameId("-1");
		user.setOpTime();
		respose.setUserMoney(String.valueOf(user.getUserDemond()));// 更新玩家金额（含锁定金额）
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));

		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		
		
		if (room != null) {

			String checkedPT = UserCenter.getInstance().checkedPT(
					user.getUserid(), room.getRoomid());
			if (checkedPT.length() > 1) {
				System.out.println("开始："+checkedPT);
				respose.setErrorTip(checkedPT);
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
			
			if (!room.isRoomAndGameStatus()
					&& GameResource.getInstance().isMaintainStatus()) {
				respose.setErrorTip("0,平台正在维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
			} else {
				user.setStartGame(true);
				int result = room.startGame(user, model.getChip());
				if (result == -1) {
					respose.setErrorTip("1,金币未达到进入标准！\n你当前金币为:"
							+ String.format("%.4f", user.getUserDemond()));
					UserCenter.getInstance().userRequstPT(user);
					Transport.write(session, respose.build());
				} else if (result == -2) {
					respose.setErrorTip("0,玩家还有未结算游戏!");
					UserCenter.getInstance().userRequstPT(user);
					Transport.write(session, respose.build());
				}else if(result == -3){
					respose.setErrorTip("1,下注金额超过最大下注上限!");
					UserCenter.getInstance().userRequstPT(user);
					Transport.write(session, respose.build());
				}else if(result == 0){
					respose.setErrorTip("0,服务器繁忙，请稍后再试!\n错误代码：10100");
					UserCenter.getInstance().userRequstPT(user);
					Transport.write(session, respose.build());
				}else if(result == -4){
					respose.setErrorTip("0,押注金额必须大于0！");
					UserCenter.getInstance().userRequstPT(user);
					Transport.write(session, respose.build());
				}
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	/**
	 * 请求发牌
	 *
	 * @param session
	 *            游戏会话
	 * @param message
	 *            游戏消息
	 * @param model
	 *            消息模块
	 */
	void requestDealCard(GameSession session, SessionMessage message,
			UserInfoModel user, GetCardRequest model) {

		user.setOpTime();
		GetCardRespose.Builder respose = GetCardRespose.newBuilder();
		respose.setNewCard("-1");
		respose.setPlayer(-1);
		respose.setGameId("-1");
		if (GameResource.getInstance().isStopGame()
				&& GameResource.getInstance().isMaintainStatus()) {// 判断游戏是否在进行
																	// ||
																	// !GameManager.getInstance().isIsInGameTime()
			respose.setErrorTip("0,游戏关闭维护或暂停，详情见通知！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
			return;
		}
		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
		if (room != null) {
			if (!room.isRoomAndGameStatus()) {
				respose.setErrorTip("0,房间或游戏维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
//			String checkedPT = UserCenter.getInstance().checkedPT(
//					user.getUserid(), room.getRoomid());
//			System.out.println("发牌："+checkedPT);
//			if (checkedPT.length() > 1) {
//				respose.setErrorTip(checkedPT);
//				UserCenter.getInstance().userRequstPT(user);
//				Transport.write(session, respose.build());
//				return;
//			}
			int result = room.getCard(user,model.getRequestId());
			if (result == -1) {
				respose.setErrorTip("0,与用户关联的游戏不存在！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}else if (result == -2) {
				respose.setErrorTip("1,请求的游戏已过期！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	/**
	 * 请求停牌
	 *
	 * @param session
	 *            游戏会话
	 * @param message
	 *            游戏消息
	 * @param model
	 *            消息模块
	 */
	void requestStopCard(GameSession session, SessionMessage message,
			UserInfoModel user, StopRequest model) {

		user.setOpTime();
		StopRespose.Builder respose = StopRespose.newBuilder();
		respose.setErrorTip("0");
		respose.setResult2("1");
		respose.setGameId("-1");
		if (GameResource.getInstance().isStopGame()
				&& GameResource.getInstance().isMaintainStatus()) {// 判断游戏是否在进行
																	// ||
																	// !GameManager.getInstance().isIsInGameTime()
			respose.setErrorTip("0,游戏关闭维护或暂停，详情见通知！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
			return;
		}
		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
		if (room != null) {
			if (!room.isRoomAndGameStatus()) {
				respose.setErrorTip("0,房间或游戏维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
//			String checkedPT = UserCenter.getInstance().checkedPT(
//					user.getUserid(), room.getRoomid());
//			System.out.println("停牌："+checkedPT);
//			if (checkedPT.length() > 1) {
//				respose.setErrorTip(checkedPT);
//				UserCenter.getInstance().userRequstPT(user);
//				Transport.write(session, respose.build());
//				return;
//			}
			int result = room.setStop(user,model.getRequestId());
			if (result == -1) {
				respose.setErrorTip("0,与用户关联的游戏不存在！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}else if (result == -2) {
				respose.setErrorTip("1,请求的游戏已过期！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	/**
	 * 请求分牌
	 *
	 * @param session
	 *            游戏会话
	 * @param message
	 *            游戏消息
	 * @param model
	 *            消息模块
	 */
	void requestSplitCard(GameSession session, SessionMessage message,
			UserInfoModel user, SplitCardRequest model) {

		SplitCardRespose.Builder respose = SplitCardRespose.newBuilder();
		respose.setErrorTip("0");
		respose.setUserMoney("-1");
		respose.setGameId("-1");
		if (GameResource.getInstance().isStopGame()
				&& GameResource.getInstance().isMaintainStatus()) {// 判断游戏是否在进行
																	// ||
																	// !GameManager.getInstance().isIsInGameTime()
			respose.setErrorTip("0,游戏关闭维护或暂停，详情见通知！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
			return;
		}
		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
		if (room != null) {
			if (!room.isRoomAndGameStatus()) {
				respose.setErrorTip("0,房间或游戏维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
//			String checkedPT = UserCenter.getInstance().checkedPT(
//					user.getUserid(), room.getRoomid());
//			System.out.println("分牌："+checkedPT);
//			if (checkedPT.length() > 1) {
//				respose.setErrorTip(checkedPT);
//				UserCenter.getInstance().userRequstPT(user);
//				Transport.write(session, respose.build());
//				return;
//			}
			int result = room.setSplitCard(user,model.getRequestId());
			if (result == -1) {
				respose.setErrorTip("0,与用户关联的游戏不存在！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}else if (result == -2) {
				respose.setErrorTip("1,请求的游戏已过期！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	void doubleRequest(GameSession session, SessionMessage message,
			UserInfoModel user, DoubleRequest model) {

		DoubleRespose.Builder respose = DoubleRespose.newBuilder();
		respose.setErrorTip("0");
		respose.setNewCard("-1");
		respose.setPlayer(-1);
		respose.setUserMoney("-1");
		respose.setGameId("-1");
		if (GameResource.getInstance().isStopGame()
				&& GameResource.getInstance().isMaintainStatus()) {// 判断游戏是否在进行
																	// ||
																	// !GameManager.getInstance().isIsInGameTime()
			respose.setErrorTip("0,游戏关闭维护或暂停，详情见通知！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
			return;
		}
		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
		if (room != null) {
			if (!room.isRoomAndGameStatus()) {
				respose.setErrorTip("0,房间或游戏维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
//			String checkedPT = UserCenter.getInstance().checkedPT(
//					user.getUserid(), room.getRoomid());
//			System.out.println("双倍牌："+checkedPT);
//			if (checkedPT.length() > 1) {
//				respose.setErrorTip(checkedPT);
//				UserCenter.getInstance().userRequstPT(user);
//				Transport.write(session, respose.build());
//				return;
//			}
			int result = room.setDouble(user,model.getRequestId());
			if (result == -1) {
				respose.setErrorTip("0,与用户关联的游戏不存在！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}else if (result == -2) {
				respose.setErrorTip("1,请求的游戏已过期！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	void insuranceRequest(GameSession session, SessionMessage message,
			UserInfoModel user, InsuranceRequest model) {

		InsuranceRespose.Builder respose = InsuranceRespose.newBuilder();
		respose.setErrorTip("0");
		respose.setDemond("0");
		respose.setUserMoney("-1");
		respose.setGameId("-1");
		respose.setResult(-1);
		if (GameResource.getInstance().isStopGame()
				&& GameResource.getInstance().isMaintainStatus()) {// 判断游戏是否在进行
																	// ||
																	// !GameManager.getInstance().isIsInGameTime()
			respose.setErrorTip("0,游戏关闭维护或暂停，详情见通知！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
			return;
		}
		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
		if (room != null) {
			if (!room.isRoomAndGameStatus()) {
				respose.setErrorTip("0,房间或游戏维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
//			String checkedPT = UserCenter.getInstance().checkedPT(
//					user.getUserid(), room.getRoomid());
//			System.out.println("保险牌："+checkedPT);
//			if (checkedPT.length() > 1) {
//				respose.setErrorTip(checkedPT);
//				UserCenter.getInstance().userRequstPT(user);
//				Transport.write(session, respose.build());
//				return;
//			}
			System.out.println(user.getUserid()+"保险请求");
			int result = room.setInsurance(user, model.getHasInsurance(),model.getRequestId());
			if (result == -1) {
				respose.setErrorTip("0,与用户关联的游戏不存在！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}else if (result == -2) {
				respose.setErrorTip("1,请求的游戏已过期！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	void surrenderRequest(GameSession session, SessionMessage message,
			UserInfoModel user, SurrenderRequest model) {

		SurrenderRespose.Builder respose = SurrenderRespose.newBuilder();
		respose.setErrorTip("0");
		respose.setResult("0");
		respose.setGameId("-1");
		if (GameResource.getInstance().isStopGame()
				&& GameResource.getInstance().isMaintainStatus()) {// 判断游戏是否在进行
																	// ||
																	// !GameManager.getInstance().isIsInGameTime()
			respose.setErrorTip("0,游戏关闭维护或暂停，详情见通知！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
			return;
		}
		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
		if (room != null) {
			if (!room.isRoomAndGameStatus()) {
				respose.setErrorTip("0,房间或游戏维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
//			String checkedPT = UserCenter.getInstance().checkedPT(
//					user.getUserid(), room.getRoomid());
//			System.out.println("投降："+checkedPT);
//			if (checkedPT.length() > 1) {
//				respose.setErrorTip(checkedPT);
//				UserCenter.getInstance().userRequstPT(user);
//				Transport.write(session, respose.build());
//				return;
//			}
			int result = room.setSurrender(user,model.getRequestId());
			if (result == -1) {
				respose.setErrorTip("0,与用户关联的游戏不存在！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}else if (result == -2) {
				respose.setErrorTip("1,请求的游戏已过期！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	void requestChangeTable(GameSession session, SessionMessage message,
			UserInfoModel user, ChangeTableRequest model) {
		ChangeTableRespose.Builder respose = ChangeTableRespose.newBuilder();
		respose.setErrorTip("0");
		respose.setTableId(-1);
		respose.setGameId("-1");
		if (GameResource.getInstance().isStopGame()
				&& GameResource.getInstance().isMaintainStatus()) {// 判断游戏是否在进行
																	// ||
																	// !GameManager.getInstance().isIsInGameTime()
			respose.setErrorTip("0,游戏关闭维护或暂停，详情见通知！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
			return;
		}
		if (!UserCenter.getInstance().getUserStatus(user.getUserid())) {
			Transport.write(session, respose.setErrorTip("1,您现在不能下单，详情请咨询管理员。")
					.build());
			return;
		}
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
		if (room != null) {
			if (!room.isRoomAndGameStatus()) {
				respose.setErrorTip("0,房间或游戏维护中！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
				return;
			}
//			String checkedPT = UserCenter.getInstance().checkedPT(
//					user.getUserid(), room.getRoomid());
//			System.out.println("换桌："+checkedPT);
//			if (checkedPT.length() > 1) {
//				respose.setErrorTip(checkedPT);
//				UserCenter.getInstance().userRequstPT(user);
//				Transport.write(session, respose.build());
//				return;
//			}
			int result = room.setTableID(user, model.getTableId());
			if (result == -1) {
				respose.setErrorTip("1,与用户关联的游戏不存在,/n或还未开始一局游戏！");
//				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());

			} else if (result == -2) {
				respose.setErrorTip("1,游戏还在进行中不能换桌！");
				UserCenter.getInstance().userRequstPT(user);
				Transport.write(session, respose.build());
			} else {
				respose.setTableId(model.getTableId());
				Transport.write(session, respose.build());
			}
		} else {
			respose.setErrorTip("0,与用户关联的房间信息未找到，\n请重新登录！");
			UserCenter.getInstance().userRequstPT(user);
			Transport.write(session, respose.build());
		}

	}

	void requitGameRequest(GameSession session, SessionMessage message,
			UserInfoModel user, QuitGameRequest model) {
//		System.out.println("用户退出1111");
		RoomModel room = GameResource.getInstance().getRoom(
				GameResource.getInstance().getRoomid(user.getUserid()));
//		int ltype = user.getLtype();
//		user.setLtype(model.getLtype());
		if (room != null) {
//			System.out.println("用户退出2222");
			room.deleteUser(user.getUserid());
			GameResource.getInstance().deleteGame(user);
			
		}
//		user.setLtype(ltype);
	}

}
