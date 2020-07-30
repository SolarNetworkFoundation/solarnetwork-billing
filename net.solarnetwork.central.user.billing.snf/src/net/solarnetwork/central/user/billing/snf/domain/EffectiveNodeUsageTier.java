/* ==================================================================
 * EffectiveNodeUsageTier.java - 22/07/2020 3:28:48 PM
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
import java.time.LocalDate;

/**
 * A tiers effective at a specific date.
 * 
 * @author matt
 * @version 1.0
 */
public class EffectiveNodeUsageTier {

	private final LocalDate date;
	private final NodeUsageTier tier;

	/**
	 * Constructor.
	 * 
	 * @param date
	 *        the effective date
	 * @param tier
	 *        the associated tier
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public EffectiveNodeUsageTier(LocalDate date, NodeUsageTier tier) {
		super();
		if ( date == null ) {
			throw new IllegalArgumentException("The date argument must be provided.");
		}
		this.date = date;
		if ( tier == null ) {
			throw new IllegalArgumentException("The tier argument must be provided.");
		}
		this.tier = tier;
	}

	/**
	 * Constructor.
	 * 
	 * @param date
	 *        the effective date
	 * @param quantity
	 *        the tier quantity
	 * @param datumPropertiesInCost
	 *        the properties in cost
	 * @param datumOutCost
	 *        the datum out cost
	 * @param datumDaysStoredCost
	 *        the days stored cost
	 */
	public EffectiveNodeUsageTier(LocalDate date, BigInteger quantity, BigDecimal datumPropertiesInCost,
			BigDecimal datumOutCost, BigDecimal datumDaysStoredCost) {
		this(date,
				new NodeUsageTier(quantity, datumPropertiesInCost, datumOutCost, datumDaysStoredCost));
	}

	/**
	 * Get the effective date.
	 * 
	 * @return the date
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * Get the tier.
	 * 
	 * @return the tier
	 */
	public NodeUsageTier getTier() {
		return tier;
	}

}
