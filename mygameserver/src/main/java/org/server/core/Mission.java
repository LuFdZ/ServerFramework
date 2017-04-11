package org.server.core;

import java.util.concurrent.Phaser;

/**
 * 
 * 任务同步器
 *
 */
public class Mission {
	Phaser phaser = new Phaser(1);
	int phaserCount = -1;
	
	/**
	 * 创建新任务
	 * 
	 * @return 任务代码
	 */
	public int newMission() {
		phaserCount = phaser.getPhase();
		return phaserCount;
	}

	/**
	 * 发送任务结束，导致等待继续
	 */
	public boolean missionFinish() {
		if (phaser.getPhase() <= phaserCount) {
			phaser.arrive();
			return true;
		}
		return false;
	}

	/**
	 * 获得当前任务代码
	 * 
	 * @return 当前任务代码
	 */
	public int getMissionCode() {
		return phaserCount;
	}

	/**
	 * 等待任务完成
	 */
	public void awaitMission() {
		phaser.awaitAdvance(getMissionCode());
	}
}
