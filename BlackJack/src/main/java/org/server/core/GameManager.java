/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core;

import java.time.Duration;
import java.time.LocalDateTime;

import org.server.backend.core.Schedule;
import org.server.core.model.custom.RoomModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class GameManager {

	static final Logger _log = LoggerFactory.getLogger(GameManager.class);
	static GameManager _instance;

	/**
	 * 获得游戏单例
	 *
	 * @return
	 */
	public static GameManager getInstance() {
		if (_instance == null) {
			_instance = new GameManager();
		}
		return _instance;
	}

	public GameManager() {
		Schedule.scheduleWithFixedDelay(() -> cleanGame(), LocalDateTime.now(),
				Duration.ofSeconds(15));
	}

	private void cleanGame() {

		for (RoomModel room : GameResource.getInstance().getRoomList()) {
			room.cleanUser();
			room.updateRoomAndGameStatus();
//			GameResource.getInstance().getMaintainStatus();
		}

	}

}
