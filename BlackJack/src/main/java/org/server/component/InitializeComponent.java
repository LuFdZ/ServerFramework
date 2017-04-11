/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.component;

import org.server.backend.component.GameComponent;
import org.server.backend.io.Transport;
import org.server.backend.session.GameSession;
import org.server.core.GameManager;
import org.server.core.GameResource;
import org.server.core.UserCenter;
import org.server.core.data.DataSource;
import org.server.core.model.custom.RoomModel;
import org.server.core.model.custom.UserInfoModel;
import org.server.core.share.TransferModel2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tuiyu.shared.DataSourceUtils;

/**
 * 服务端初始化组件
 *
 * @author Hxms
 */
@GameComponent.Setting(id = 0, loadOrder = -101, name = "Initialize")
public class InitializeComponent implements GameComponent {

    private static final Logger log = LoggerFactory.getLogger(InitializeComponent.class);

    @Override
    public void load() {
        log.info("正在配置 Hibernate ...");
        DataSourceUtils.attachTerraceMappings();
        DataSourceUtils.initializeDataSource();
        InitializeMessageConfig();
        GameResource.getInstance();
        GameManager.getInstance();
        
    }

    @Override
    public void unload() {
    }

    /**
     * 初始化消息命令头值
     */
    private void InitializeMessageConfig() {
        log.info("正在注册发送消息命令值...");

        Transport.registerMessageCommand(AuthenticationFail.class, 0x01);
        Transport.registerMessageCommand(UserLoginRespose.class, 0x103);
        Transport.registerMessageCommand(StartGameRespose.class, 0x105);
        Transport.registerMessageCommand(GetCardRespose.class, 0x107);
        Transport.registerMessageCommand(SplitCardRespose.class, 0x108);
        Transport.registerMessageCommand(DoubleRespose.class, 0x109);
        Transport.registerMessageCommand(InsuranceRespose.class, 0x111);
        Transport.registerMessageCommand(SurrenderRespose.class, 0x110);
        Transport.registerMessageCommand(StopRespose.class, 0x106);
        Transport.registerMessageCommand(HeartbeatRespose.class, 0x113);
        Transport.registerMessageCommand(UserRoomLoginRespose.class, 0x104);
        Transport.registerMessageCommand(ChangeTableRespose.class, 0x112);
        
        Transport.registerMessageCommand(PlayerChange.class, 0x120);
        Transport.registerMessageCommand(GameResult.class, 0x121);
        
        Transport.registerMessageCommand(BnakInfoRespose.class, 0x117);
        Transport.registerMessageCommand(BnakWithdrawalRespose.class, 0x118);
        Transport.registerMessageCommand(BnakDepositRespose.class, 0x119);
        log.info("注册认证函数.....");
        Transport.setAuthentication(this::authentication);
        Transport.setAuthenticationFail(this::authenticationFail);
    }

    /**
     * 游戏认证函数
     *
     * @param session 游戏会话
     * @return
     */
    private Object authentication(GameSession session) {
        Integer uid = (Integer) session.getAttribute("UID");
        if (uid != null) {
            if (session.getAttribute("Superseded") != null) {
                return null;
            }
//            uid
            return UserCenter.getInstance().get("id", uid);
        }
        return null;
    }

    /**
     * 会话认证失败
     *
     * @param session 会话
     */
    private void authenticationFail(GameSession session) {
        // 发送消息
        AuthenticationFail.Builder builder = AuthenticationFail
                .newBuilder()
                .setErrorCode(0);

        String errorTip = (String) session.getAttribute("Superseded");
        if (errorTip != null) {
            builder.setErrorTip("0,"+errorTip);
        }
        session.write(builder.build());
    }
}
