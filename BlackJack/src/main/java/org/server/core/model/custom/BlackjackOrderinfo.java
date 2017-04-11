package org.server.core.model.custom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.tuiyu.shared.terrace.database.GlobalOrderInfo;

@Entity
@Table(name = "BLACKJACKORDERINFO")
public class BlackjackOrderinfo  extends GlobalOrderInfo implements java.io.Serializable{

	private static final long serialVersionUID = -2459596500056601343L;
	
	/**
	 * 订单描述信息
	 */
	private String GDetails; 
	
	/**
	 * 游戏桌号
	 */
	private int GTableid; 
	
	/**
	 * 游戏桌号
	 */
	private int GBRANDID; 
	
	@Id
	@SequenceGenerator(name = "sequenceGenerator", sequenceName = "s_203_SEQ", initialValue = 100000, allocationSize = 1)
	@GeneratedValue(generator = "sequenceGenerator", strategy = GenerationType.SEQUENCE)
	@Column(name = "g_id", unique = true, nullable = false)
	public long getGId() {
		return this.GId;
	}

	public void setGId(long GId) {
		this.GId = GId;
	}

	/**
	 * 获得订单描述信息
	 * 
	 * @return the gDetail
	 */
	@Column(name = "G_DETAIL", nullable = true, length = 1000)
	public String getDetails() {
		return GDetails;
	}

	/**
	 * 设置订单描述信息
	 * 
	 * @param detail
	 *            the gDetail to set
	 */
	public void setDetails(String detail) {
		this.GDetails = detail;
	}
	
	/**
	 * 获得订单描述信息
	 * 
	 * @return the gDetail
	 */
	@Column(name = "G_TABLEID", nullable = true, length = 1000)
	public int getGTableid() {
		return GTableid;
	}

	/**
	 * 设置订单描述信息
	 * 
	 * @param detail
	 *            the gDetail to set
	 */
	public void setGTableid(int tableid) {
		this.GTableid = tableid;
	}
	
	/**
	 * 获得订单描述信息
	 * 
	 * @return the gDetail
	 */
	@Column(name = "G_BRANDID", nullable = true, length = 1000)
	public int getBRANDID() {
		return GBRANDID;
	}

	/**
	 * 设置订单描述信息
	 * 
	 * @param detail
	 *            the gDetail to set
	 */
	public void setBRANDID(int GBRANDID) {
		this.GBRANDID = GBRANDID;
	}

}
