/* ==================================================================
 * NodeUsageTiers.java - 22/07/2020 2:34:58 PM
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A collection of ordered {@link NodeUsageTier} objects.
 * 
 * @author matt
 * @version 1.0
 */
public class NodeUsageTiers {

	private final List<NodeUsageTier> tiers;

	/**
	 * Constructor.
	 * 
	 * <p>
	 * The tiers will be sorted by {@code quantity}.
	 * </p>
	 * 
	 * @param tiers
	 *        the tiers; the list will be copied
	 * @throws IllegalArgumentException
	 *         if {@code tiers} is {@literal null}
	 */
	public NodeUsageTiers(List<NodeUsageTier> tiers) {
		this(tiers, NodeUsageTier.SORT_BY_QUANTITY);
	}

	/**
	 * Constructor.
	 * 
	 * @param tiers
	 *        the tiers; the list will be copied
	 * @param comparator
	 *        an optional comparator to sort the tiers with, or {@literal null}
	 *        to not sort
	 * @throws IllegalArgumentException
	 *         if {@code tiers} is {@literal null}
	 */
	public NodeUsageTiers(List<NodeUsageTier> tiers, Comparator<NodeUsageTier> comparator) {
		super();
		if ( tiers == null ) {
			throw new IllegalArgumentException("The tiers argumnet must be provided.");
		}
		if ( comparator != null ) {
			List<NodeUsageTier> sorted = new ArrayList<>(tiers);
			sorted.sort(NodeUsageTier.SORT_BY_QUANTITY);
			tiers = sorted;
		}
		this.tiers = Collections.unmodifiableList(tiers);
	}

	/**
	 * Get the tiers.
	 * 
	 * @return the tiers (unmodifiable)
	 */
	public List<NodeUsageTier> getTiers() {
		return tiers;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("| %9s | %-12s | %-12s | %-12s |\n", "Quantity", "PropIn", "DatumOut",
				"DatumStored"));
		buf.append("|-----------|--------------|--------------|--------------|");
		final String row = "| %,9d | %0,9.10f | %0,9.10f | %0,9.10f |";
		for ( NodeUsageTier tier : tiers ) {
			buf.append("\n");
			buf.append(String.format(row, tier.getQuantity(), tier.getCosts().getDatumPropertiesInCost(),
					tier.getCosts().getDatumOutCost(), tier.getCosts().getDatumDaysStoredCost()));
		}
		return buf.toString();
	}

}
