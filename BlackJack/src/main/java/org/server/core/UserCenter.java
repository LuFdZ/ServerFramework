/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;
import org.server.backend.core.AbstractCacheGeter;
import org.server.backend.io.Transport;
import org.server.backend.session.GameSession;
import org.server.core.data.DataSource;
import org.server.core.http.HttpRequest;
import org.server.core.model.Userbasicinfo;
import org.server.core.model.custom.UserInfoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;
import com.tuiyu.shared.terrace.ServiceComponentUtils;
import com.tuiyu.shared.terrace.UserInfoUtils;

/**
 *
 * @author Administrator
 */
public class UserCenter extends AbstractCacheGeter<UserInfoModel> {

	private static final Logger _log = LoggerFactory
			.getLogger(UserCenter.class);

	private static final UserCenter _instance = new UserCenter();
	static final Pattern _pattern = Pattern
			.compile("(\\[|<)(\\S+)(\\]|>)(\\S+)\\1/\\2\\3");

	/**
	 * 获得默认实例
	 *
	 * @return 默认用户中心实例
	 */
	public static UserCenter getInstance() {
		return _instance;
	}
	
	/**
     * 注册缓存
     *
     */
    @Override
    protected void registerCache0(UserInfoModel model) {
        _cache.put(model.getUserid(), model);
        _cache.put(model.getUserName(), model);
    }
	
	public UserCenter() {
        registerGeter("id", this::getById);
        registerGeter("name", this::getByName);
    }

	public UserInfoModel getByName(Object name) {
        if (name instanceof String) {
            return queryUser(session -> (Userbasicinfo) session.createCriteria(Userbasicinfo.class)
                    .add(Restrictions.eq("userName", name))
                    .setMaxResults(1)
                    .uniqueResult());
        }

        return null;
    }

    public UserInfoModel getById(Object id) {
        if (id instanceof Integer) {
            Integer targetId = (Integer) id;
            return queryUser(session -> (Userbasicinfo) session.get(Userbasicinfo.class, targetId));
        }
        return null;
    }

    private UserInfoModel queryUser(Function<Session, Userbasicinfo> action) {
        Session session = DataSource.openSession();
        try {
            Userbasicinfo user = action.apply(session);
            if (user != null) {
                int user_fc = (int)UserInfoUtils.generateTreeUserFc(user.getUserId())[1];//((Userbasicinfo) session.get(Userbasicinfo.class, user.getParentId())).getUserFc();
                UserInfoModel result = new UserInfoModel(user.getUserId(), user.getUserName(), Integer.valueOf(user.getStatus()), user_fc, Integer.valueOf(user.getUserLevel()));
                registerCache(result);
                return result;
            }
        } catch (Exception ex) {
            _log.error("queryUser:" + ex);
        } finally {
            session.close();
        }
        return null;
    }

	public List<String> XMLparser(String xmlString) {

		List<String> xmlList = new LinkedList<>();
		HashMap<String, String> informationMap = new HashMap<>();
		Matcher matcher = _pattern.matcher(xmlString);
		while (matcher.find()) {
			informationMap.put(matcher.group(2), matcher.group(4));
			if ("rid".equals(matcher.group(2))) {
				xmlList.add(0, matcher.group(4));
			} else if ("uid".equals(matcher.group(2))) {
				xmlList.add(1, matcher.group(4));
			} else if ("sid".equals(matcher.group(2))) {
				xmlList.add(2, matcher.group(4));
			}
		}
		return xmlList;
	}

	public double getUserDemond(int userid) {
		return UserInfoUtils.getUnlockedAmount(userid);

	}
	
	 /**
     * 广播数据包
     *
     * @param model 数据模块
     */
    public void broadcastMessage(GeneratedMessage model) {
        broadcastMessage(null, model);
    }

    public void broadcastMessage(List<UserInfoModel> playerList, GeneratedMessage model) {
        for (UserInfoModel user : playerList) {
            if (user != null && user.isStartGame()) {//
//                System.out.println("broadcastMessage:" + user.getUserid());
                sendMessage(user.getUserid(), model);
            }
        }
    }

    public void sendMessage(int userId, GeneratedMessage model) {
        // 读取用户会话
        GameSession session = (GameSession) GameAttributeResource.
                getInstance().
                get(GameSession.class, userId);
        if (session != null) {
//            System.out.println(model+"session:"+session.getBackendSession().getSessionId());
            session.write(model);
        } else {
            _log.error("用户session未找到！");
            Exception e = new Exception("推送信息异常！");
            e.printStackTrace();
        }
    }

    /**
     * *
     * 用户推出平台
     *
     * @param user
     */
    public void userRequstPT(UserInfoModel user) {
        if (user.getLtype() == 0) {
            HttpRequest.post(String.format("http://%s:9911/ChangeUserRoom.do?userid=%s&etype=%s&rid=%s", GameResource.getInstance().getRequestIp(), user.getUserid(), "q", user.getRoomid()), "");
        } else {
            HttpRequest.post(String.format("http://%s:9911/MbUserLogout.do?userid=%s", GameResource.getInstance().getRequestIp(), user.getUserid()), "");
        }
        Exception e = new Exception("用户平台退出");

        _log.info("用户平台退出", e);
    }

	/**
	 * 利用MD5进行加密
	 *
	 * @param data
	 * @return 加密后的字符串
	 */
	public String EncoderByMd5(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data.getBytes());
			StringBuilder buf = new StringBuilder();
			byte[] bits = md.digest();
			for (int i = 0; i < bits.length; i++) {
				int a = bits[i];
				if (a < 0) {
					a += 256;
				}
				if (a < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(a));
			}
			return buf.toString().toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	public long nextval(int gameid) {
		return ServiceComponentUtils.getNextSequenceId(gameid);
	}

	public boolean getUserStatus(int userid) {
		Session session = DataSource.openSession();

		Userbasicinfo result = (Userbasicinfo) session
				.createCriteria(Userbasicinfo.class)
				.add(Restrictions.eq("userId", userid)).uniqueResult();
		session.close();
		return result != null && "1".equals(result.getStatus())
				&& "9".equals(result.getUserLevel());
	}

	/**
	 * * 查询锁定金额
	 *
	 * @param userid
	 *            用户id
	 * @param type
	 *            查询类型 0：用户锁定金额；1：银行锁定金额
	 * @return
	 */
	public double getUserLockedMoney(int userid, int type) {
		return UserInfoUtils.getUserLockedMoney(userid, type);
	}

	public boolean updateUserCoin(int userid, double money, String ip) {
		return UserInfoUtils.updateUserCoin(userid, money, ip);
	}

	public List<String> getBankInfo(int userid) {

		Session session = DataSource.openSession();
		Userbasicinfo user = (Userbasicinfo) session.get(Userbasicinfo.class,
				userid);
		if (user == null) {
			return null;
		}
		List<String> result = new LinkedList<>();
		double coin = user.getBankCoin();
		double lockedMoney = getUserLockedMoney(userid, 1);
		result.add(String.valueOf(coin));
		result.add(String.valueOf(lockedMoney));
		result.add(String.valueOf(coin - lockedMoney));

		coin = user.getUserMoney();
		lockedMoney = getUserLockedMoney(userid, 0);
		result.add(String.valueOf(coin));
		result.add(String.valueOf(lockedMoney));
		result.add(String.valueOf(coin - lockedMoney));
		session.close();
		return result;
	}

	public int updateUserMoney(int userid, double money, String ip) {
		Session session = DataSource.openSession();
		Userbasicinfo user = (Userbasicinfo) session.get(Userbasicinfo.class,
				userid);
		session.close();

		if (money > 0 && user.getUserMoney() < Math.abs(money)) {
			return -1;
		}
		if (money < 0 && user.getBankCoin() < Math.abs(money)) {
			return -1;
		}
		if (updateUserCoin(userid, money, ip)) {
			return 0;
		}
		return -2;
	}

	public void upDateUserLoginInfo(int userid, String Login_ip) {
		UserInfoUtils.upDateUserLoginInfo(userid, Login_ip, null);
	}

	@Override
	protected void initialize0(UserInfoModel model) {
		// TODO Auto-generated method stub
		
	}

	public boolean checkedNameAndPassWord(String name, String password) {
        StatelessSession statelessSession = DataSource.openStatelessSession();
        Userbasicinfo uniqueResult = (Userbasicinfo) statelessSession.
                createQuery("from Userbasicinfo where userName=:name and userPass=md5(:pwd)").
                setString("name", name).
                setString("pwd", password).
                setMaxResults(1).
                uniqueResult();
        statelessSession.close();
        return uniqueResult != null;
    }
	
	public String checkedPT(int userid,int roomid){
		int state = 0;
		String resultString = "0";
        String resultPT = HttpRequest.post(String.format("http://%s:9911/ChangeUserRoom.do?userid=%s&etype=%s&rid=%s", GameResource.getInstance().getRequestIp(), userid, "r", -1), "");
        JSONObject dataJson = new JSONObject(resultPT);
        try {
            state = dataJson.getInt("estatus");
        } catch (Exception e) {
            //转int 失败既未找到用户
        	return "0,平台中未找到用户信息，\n请重新登录平台！";
            
        }
        if (state != roomid) {
        	return  "0,平台中用户登录信息以改变，\n请重新登录平台！\n用户状态代码："+state;
        }
        return resultString;
	}
}
