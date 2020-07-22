/* ==================================================================
 * NodeUsage.java - 22/07/2020 10:05:31 AM
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.central.user.billing.snf.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import net.solarnetwork.dao.BasicLongEntity;

/**
 * Usage details for a single node.
 * 
 * <p>
 * The usage date range is defined in whatever associated entity refers to this
 * usage. For example for an {@link SnfInvoice} the date range would be that of
 * the invoice. The costs are shown in the system's <i>normalized</i> currency,
 * and must be translated into real currencies elsewhere.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class NodeUsage extends BasicLongEntity {

	private BigInteger datumPropertiesIn;
	private BigDecimal datumPropertiesInCost;
	private BigInteger datumDaysStored;
	private BigDecimal datumDaysStoredCost;
	private BigInteger datumOut;
	private BigDecimal datumOutCost;
	private BigDecimal totalCost;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the node ID
	 */
	public NodeUsage(Long id) {
		this(id, Instant.now());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the node ID
	 * @param created
	 *        the creation date
	 */
	public NodeUsage(Long id, Instant created) {
		super(id, created);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeUsage{");
		builder.append("nodeId=");
		builder.append(getId());
		builder.append(", datumPropertiesIn=");
		builder.append(datumPropertiesIn);
		builder.append(", datumPropertiesInCost=");
		builder.append(datumPropertiesInCost);
		builder.append(", datumDaysStored=");
		builder.append(datumDaysStored);
		builder.append(", datumDaysStoredCost=");
		builder.append(datumDaysStoredCost);
		builder.append(", datumOut=");
		builder.append(datumOut);
		builder.append(", datumOutCost=");
		builder.append(datumOutCost);
		builder.append(", totalCost=");
		builder.append(totalCost);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the count of datum properties added.
	 * 
	 * @return the count
	 */
	public BigInteger getDatumPropertiesIn() {
		return datumPropertiesIn;
	}

	/**
	 * Set the count of datum properties added.
	 * 
	 * @param datumPropertiesIn
	 *        the count to set
	 */
	public void setDatumPropertiesIn(BigInteger datumPropertiesIn) {
		this.datumPropertiesIn = datumPropertiesIn;
	}

	/**
	 * Get the cost of datum properties added.
	 * 
	 * @return the cost
	 */
	public BigDecimal getDatumPropertiesInCost() {
		return datumPropertiesInCost;
	}

	/**
	 * Set the cost of datum properties added.
	 * 
	 * @param datumPropertiesInCost
	 *        the cost to set
	 */
	public void setDatumPropertiesInCost(BigDecimal datumPropertiesInCost) {
		this.datumPropertiesInCost = datumPropertiesInCost;
	}

	/**
	 * Get the count of datum stored per day (accumulating).
	 * 
	 * @return the count
	 */
	public BigInteger getDatumDaysStored() {
		return datumDaysStored;
	}

	/**
	 * Set the count of datum stored per day (accumulating).
	 * 
	 * @param datumDaysStored
	 *        the count to set
	 */
	public void setDatumDaysStored(BigInteger datumDaysStored) {
		this.datumDaysStored = datumDaysStored;
	}

	/**
	 * Get the cost of datum stored per day (accumulating).
	 * 
	 * @return the cost
	 */
	public BigDecimal getDatumDaysStoredCost() {
		return datumDaysStoredCost;
	}

	/**
	 * Set the cost of datum stored per day (accumulating).
	 * 
	 * @param datumDaysStoredCost
	 *        the cost to set
	 */
	public void setDatumDaysStoredCost(BigDecimal datumDaysStoredCost) {
		this.datumDaysStoredCost = datumDaysStoredCost;
	}

	/**
	 * Get the count of datum queried.
	 * 
	 * @return the count
	 */
	public BigInteger getDatumOut() {
		return datumOut;
	}

	/**
	 * Set the count of datum queried.
	 * 
	 * @param datumOut
	 *        the count to set
	 */
	public void setDatumOut(BigInteger datumOut) {
		this.datumOut = datumOut;
	}

	/**
	 * Get the cost of datum queried.
	 * 
	 * @return the cost
	 */
	public BigDecimal getDatumOutCost() {
		return datumOutCost;
	}

	/**
	 * Set the cost of datum queried.
	 * 
	 * @param datumOutCost
	 *        the cost to set
	 */
	public void setDatumOutCost(BigDecimal datumOutCost) {
		this.datumOutCost = datumOutCost;
	}

	/**
	 * Get the overall cost.
	 * 
	 * @return the cost
	 */
	public BigDecimal getTotalCost() {
		return totalCost;
	}

	/**
	 * Set the overall cost.
	 * 
	 * @param totalCost
	 *        the cost to set
	 */
	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

}
