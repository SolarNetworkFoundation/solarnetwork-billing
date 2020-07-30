/* ==================================================================
 * NodeUsageTier.java - 22/07/2020 2:09:25 PM
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
import java.util.Comparator;
import java.util.Objects;

/**
 * Details about a node usage tier.
 * 
 * @author matt
 * @version 1.0
 */
public class NodeUsageTier {

	/**
	 * Comparator that sorts {@link NodeUsageTier} objects by {@code quantity}
	 * in ascending order.
	 */
	public static final Comparator<NodeUsageTier> SORT_BY_QUANTITY = new NodeUsageTierQuantityComparator();

	private final BigInteger quantity;
	private final NodeUsageCost costs;

	/**
	 * Compare {@link NodeUsageTier} instances by quantity in ascending order.
	 */
	public static final class NodeUsageTierQuantityComparator implements Comparator<NodeUsageTier> {

		@Override
		public int compare(NodeUsageTier o1, NodeUsageTier o2) {
			return o1.quantity.compareTo(o2.quantity);
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param quantity
	 *        the tier quantity
	 * @param costs
	 *        the costs associated with the tier
	 */
	public NodeUsageTier(long quantity, NodeUsageCost costs) {
		this(BigInteger.valueOf(quantity), costs);
	}

	/**
	 * Constructor.
	 * 
	 * @param quantity
	 *        the tier quantity
	 * @param datumPropertiesInCost
	 *        the properties in cost
	 * @param datumOutCost
	 *        the datum out cost
	 * @param datumDaysStoredCost
	 *        the days stored cost
	 */
	public NodeUsageTier(BigInteger quantity, BigDecimal datumPropertiesInCost, BigDecimal datumOutCost,
			BigDecimal datumDaysStoredCost) {
		this(quantity, new NodeUsageCost(datumPropertiesInCost, datumOutCost, datumDaysStoredCost));
	}

	/**
	 * Constructor.
	 * 
	 * @param quantity
	 *        the tier quantity
	 * @param costs
	 *        the costs associated with the tier
	 */
	public NodeUsageTier(BigInteger quantity, NodeUsageCost costs) {
		super();
		if ( quantity == null ) {
			throw new IllegalArgumentException("The quantity argument must be provided.");
		}
		this.quantity = quantity;
		if ( costs == null ) {
			throw new IllegalArgumentException("The costs argument must be provided.");
		}
		this.costs = costs;
	}

	@Override
	public int hashCode() {
		return Objects.hash(costs, quantity);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof NodeUsageTier) ) {
			return false;
		}
		NodeUsageTier other = (NodeUsageTier) obj;
		return Objects.equals(costs, other.costs) && Objects.equals(quantity, other.quantity);
	}

	/**
	 * Get the tier quantity.
	 * 
	 * <p>
	 * The {@code quantity} value can be interpreted in different ways,
	 * depending on the context it is used in. For example, when used to detail
	 * a tiered schedule, the {@code quantity} represents the starting (or
	 * minimum) value for the tier. When used to detail an invoice item cost,
	 * the {@code quantity} represents the count of usage units included in the
	 * tier.
	 * </p>
	 * 
	 * @return the quantity
	 */
	public BigInteger getQuantity() {
		return quantity;
	}

	/**
	 * Get the tier cost.
	 * 
	 * <p>
	 * The {@code quantity} value can be interpreted in different ways,
	 * depending on the context it is used in. For example, when used to detail
	 * a tiered schedule, the {@code costs} represents the cost per unit of
	 * usage for the tier. When used to detail an invoice item cost, the
	 * {@code costs} represents the cost of usage units within the tier.
	 * </p>
	 * 
	 * @return the costs
	 */
	public NodeUsageCost getCosts() {
		return costs;
	}

}
