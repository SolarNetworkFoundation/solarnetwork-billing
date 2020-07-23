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
import java.util.Comparator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.solarnetwork.dao.BasicLongEntity;

/**
 * Usage details for a single node.
 * 
 * <p>
 * The {@link #getId()} represents the node ID this usage relates to.
 * </p>
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

	/**
	 * Comparator that sorts {@link NodeUsage} objects by {@code id} in
	 * ascending order.
	 */
	public static final Comparator<NodeUsage> SORT_BY_NODE_ID = new NodeUsageNodeIdComparator();

	private BigInteger datumPropertiesIn;
	private BigInteger datumOut;
	private BigInteger datumDaysStored;
	private final NodeUsageCost costs;
	private BigDecimal totalCost;

	/**
	 * Compare {@link NodeUsageTier} instances by quantity in ascending order.
	 */
	public static final class NodeUsageNodeIdComparator implements Comparator<NodeUsage> {

		@Override
		public int compare(NodeUsage o1, NodeUsage o2) {
			return o1.compareTo(o2.getId());
		}

	}

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
		setDatumPropertiesIn(BigInteger.ZERO);
		setDatumOut(BigInteger.ZERO);
		setDatumDaysStored(BigInteger.ZERO);
		this.costs = new NodeUsageCost();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeUsage{");
		builder.append("nodeId=");
		builder.append(getId());
		builder.append(", datumPropertiesIn=");
		builder.append(datumPropertiesIn);
		builder.append(", datumOut=");
		builder.append(datumOut);
		builder.append(", datumDaysStored=");
		builder.append(datumDaysStored);
		builder.append(", datumDaysStoredCost=");
		builder.append(costs.getDatumDaysStoredCost());
		builder.append(", datumPropertiesInCost=");
		builder.append(costs.getDatumPropertiesInCost());
		builder.append(", totalCost=");
		builder.append(totalCost);
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the node usage costs.
	 * 
	 * @return the costs, never {@literal null}
	 */
	@JsonIgnore
	public NodeUsageCost getCosts() {
		return costs;
	}

	/**
	 * Get the count of datum properties added.
	 * 
	 * @return the count, never {@literal null}
	 */
	public BigInteger getDatumPropertiesIn() {
		return datumPropertiesIn;
	}

	/**
	 * Set the count of datum properties added.
	 * 
	 * @param datumPropertiesIn
	 *        the count to set; if {@literal null} then {@literal 0} will be
	 *        stored
	 */
	public void setDatumPropertiesIn(BigInteger datumPropertiesIn) {
		if ( datumPropertiesIn == null ) {
			datumPropertiesIn = BigInteger.ZERO;
		}
		this.datumPropertiesIn = datumPropertiesIn;
	}

	/**
	 * Get the cost of datum properties added.
	 * 
	 * @return the cost
	 */
	public BigDecimal getDatumPropertiesInCost() {
		return costs.getDatumPropertiesInCost();
	}

	/**
	 * Set the cost of datum properties added.
	 * 
	 * @param datumPropertiesInCost
	 *        the cost to set
	 */
	public void setDatumPropertiesInCost(BigDecimal datumPropertiesInCost) {
		costs.setDatumPropertiesInCost(datumPropertiesInCost);
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
	 *        the count to set; if {@literal null} then {@literal 0} will be
	 *        stored
	 */
	public void setDatumDaysStored(BigInteger datumDaysStored) {
		if ( datumDaysStored == null ) {
			datumDaysStored = BigInteger.ZERO;
		}
		this.datumDaysStored = datumDaysStored;
	}

	/**
	 * Get the cost of datum stored per day (accumulating).
	 * 
	 * @return the cost
	 */
	public BigDecimal getDatumDaysStoredCost() {
		return costs.getDatumDaysStoredCost();
	}

	/**
	 * Set the cost of datum stored per day (accumulating).
	 * 
	 * @param datumDaysStoredCost
	 *        the cost to set
	 */
	public void setDatumDaysStoredCost(BigDecimal datumDaysStoredCost) {
		costs.setDatumDaysStoredCost(datumDaysStoredCost);
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
	 *        the count to set; if {@literal null} then {@literal 0} will be
	 *        stored
	 */
	public void setDatumOut(BigInteger datumOut) {
		if ( datumOut == null ) {
			datumOut = BigInteger.ZERO;
		}
		this.datumOut = datumOut;
	}

	/**
	 * Get the cost of datum queried.
	 * 
	 * @return the cost
	 */
	public BigDecimal getDatumOutCost() {
		return costs.getDatumOutCost();
	}

	/**
	 * Set the cost of datum queried.
	 * 
	 * @param datumOutCost
	 *        the cost to set
	 */
	public void setDatumOutCost(BigDecimal datumOutCost) {
		costs.setDatumOutCost(datumOutCost);
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
