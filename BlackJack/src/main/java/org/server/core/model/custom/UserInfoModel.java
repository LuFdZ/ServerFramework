/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core.model.custom;

import java.time.Instant;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.server.backend.component.GameComponent;
import org.server.backend.session.GameSession;
import org.server.core.GameAttributeResource;
import org.server.core.GameResource;
import org.server.core.data.DataSource;
import org.server.core.model.Userbasicinfo;
import org.server.core.share.TransferModel2.AuthenticationFail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tuiyu.shared.terrace.ServiceComponentUtils;
import com.tuiyu.shared.terrace.UserInfoUtils;

/**
 *
 * @author Administrator
 */
public class UserInfoModel {

	private static final Logger _Logger = LoggerFactory
			.getLogger(UserInfoModel.class);
	private final int _userid;
	private String _userName;
	private String _ipValue = "-1";
	private Date _opTime;
	private int _parentFc;
	private int _userStatus = 1;
	private long _sepid;
	private int _userLevel = 0;
	private int _ltype;// 登录类型 0平台 1手机
	private int _roomid;// 所在房间id
	private boolean _startGame;// 是否开始接受推送包
	private BlackjackOrderinfo orderModel;
	private int _tableid;//桌号id
	private String _fcDetail;//多层代理详细信息

	// 获得最近操作时间间隔
	public int getTimeSpan() {
		long between = 0;
		between = new Date().getTime() - _opTime.getTime();
		return (int) (between / 1000);
	}

	// 更新操作时间
	public void setOpTime() {
		synchronized (_opTime) {
			_opTime = new Date();
		}
	}

	public int setUserOrder(double money, int roomid, double roomts, double gamecoin) {//
		try {
			if (money > getUserDemond()) {
				return -1;
			}
			orderModel = new BlackjackOrderinfo();
			orderModel.setRoomId(roomid);
			orderModel.setRoomTs(roomts);
			orderModel.setGameCoin(gamecoin);
			orderModel.setGStatus(0);
			orderModel.setUserId(Integer.valueOf(_userid));
			orderModel.setUserIp(_ipValue);
			orderModel.setChipCoin(money);
			orderModel.setEffectChip(money);
			orderModel.setParentFc(_parentFc);
//			orderModel.setUpdateTime(orderModel.getCreateTime());
			orderModel.setGBargain(0.0);
//			orderModel.setGResult(0.0);
			orderModel.setSeqId(_sepid);
			orderModel.setFcDetail(_fcDetail);
			orderModel.setBRANDID(0);
			UserInfoUtils.createNewOrderAfter(orderModel,GameResource.bjGameModel);
			DataSource.insert(orderModel);
//			System.out.println("订单插入"+result+",id"+orderModel.getGId());
			return 1;
		} catch (HibernateException e) {
			_Logger.error("setUserOrder:" + _userid + "--" + e.getMessage());
			e.printStackTrace();
			return -5;
		}
	}

	public int updateUserOrder(double money) {
		if (orderModel != null) {
			orderModel.setChipCoin(orderModel.getChipCoin() + money);
			orderModel.setEffectChip(orderModel.getChipCoin());
			
			DataSource.update(orderModel);
		}
		return -1;
	}

	public int getUserid() {
		return _userid;
	}

	public int getLtype() {
		return _ltype;
	}

	public void setLtype(int _ltype) {
		this._ltype = _ltype;
	}

	public void setIpValue(String _ipValue) {
		this._ipValue = _ipValue;
	}

	public void setParentFc(int _parentFc) {
		this._parentFc = _parentFc;
	}

	public int getParentFc() {
		return _parentFc;
	}

	public String getIpValue() {
		return _ipValue;
	}

	public int getUserStatus() {
		return _userStatus;
	}

	public void setUserStatus(int _userStatus) {
		this._userStatus = _userStatus;
	}

	public long getSepid() {
		return _sepid;
	}

	public void setSeqidUpdate(long sepid) {
		this._sepid = sepid;
	}

	public void setSepid(int gameid) {
		this._sepid = nextval(gameid);
	}

	public int getUserLevel() {
		return _userLevel;
	}

	public int getRoomid() {
		return _roomid;
	}

	public void setRoomid(int _roomid) {
		this._roomid = _roomid;
	}

	public boolean isStartGame() {
		return _startGame;
	}

	public void setStartGame(boolean _startGame) {
		this._startGame = _startGame;
	}

	public String getUserName() {
		return _userName;
	}
	
	

	public int getTableid() {
		return _tableid;
	}

	public void setTableid(int tableid) {
		this._tableid = tableid;
	}
	

	public void setFcDetail(String _fcDetail) {
		this._fcDetail = _fcDetail;
	}

	public UserInfoModel(int _userid, String _userName, int _userStatus,
			int _parentFc, int _userLevel) {
		this._userid = _userid;
		this._parentFc = _parentFc;
		this._userName = _userName;
		_opTime = new Date();
		this._userStatus = _userStatus;
		this._userLevel = _userLevel;
		_startGame = false;
		orderModel = new BlackjackOrderinfo();
		_tableid = 17;
//		System.out.println("新建了订单111111");
	}
	
	public double overOrder(double bargain,double result,int tableid,double isTie,String log){
		double orderresult = 0;
		if (orderModel!=null) {
			orderModel.setGBargain(bargain);
			orderModel.setGResult(result);
//			orderModel.setUpdateTime(Instant.now());
//			orderModel.setGStatus(3);
			orderModel.setGTableid(tableid);
			orderModel.setDetails(log);
			
			if (isTie>0) {
				orderModel.setEffectChip(orderModel.getChipCoin() - isTie);
			}
			UserInfoUtils.finishingOrder(orderModel,GameResource.bjGameModel);
			DataSource.update(orderModel);
			orderresult = orderModel.getGResult();
			orderModel = null;
//			System.out.println("清空了订单");
		}
		return orderresult;
	}

//	public void cleanOrder() {
//		orderModel = null;
//	}
//	
	public BlackjackOrderinfo getOrderModel() {
		return orderModel;
	}

	public double getUserDemond() {
		setOpTime();
		return UserInfoUtils.getUnlockedAmount(_userid);
	}

	public long nextval(int gameid) {
		setOpTime();
		return ServiceComponentUtils.getNextSequenceId(gameid);
	}

	public void updateUserStatus() {
		Session session = DataSource.openSession();

		Userbasicinfo result = (Userbasicinfo) session
				.createCriteria(Userbasicinfo.class)
				.add(Restrictions.eq("userId",
						Integer.valueOf(this.getUserid()))).uniqueResult();
		this._userStatus = Integer.valueOf(result.getStatus());
		this._userLevel = Integer.valueOf(result.getUserLevel());
		session.close();
	}

	/**
	 * 绑定会话
	 *
	 * @param session
	 *            会话对象
	 */
	public void bindSession(GameSession session) {
		GameSession previous = get(GameSession.class);
		if (previous != null && previous != session) {
			previous.setAttribute("Superseded", "此次登录已失效！");
			
			previous.write(AuthenticationFail.newBuilder().setErrorCode(-1)
					.setErrorTip("0,此次登录已失效 \n请重新登录！").build());
		}

		// 关联对象
		session.setAttribute("UID", getUserid());
		session.removeAttribute("Superseded");
		set(GameSession.class, session);
	}

	/**
	 * 获得关联属性
	 *
	 * @param <T>
	 *            结果类型
	 * @param key
	 *            键值
	 * @return 关联值
	 */
	public <T> T get(Object key) {
		try {
			return (T) GameAttributeResource.getInstance()
					.get(key, getUserid());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 设置关联属性
	 *
	 * @param key
	 *            键值
	 * @param val
	 *            关联值
	 */
	public void set(Object key, Object val) {
		GameAttributeResource.getInstance().set(key, getUserid(), val);
	}

	/**
	 * 删除关联属性
	 *
	 * @param key
	 *            键值
	 * @return 删除是否成功
	 */
	public boolean remove(Object key) {
		return GameAttributeResource.getInstance().remove(key, getUserid()) != null;
	}
}
