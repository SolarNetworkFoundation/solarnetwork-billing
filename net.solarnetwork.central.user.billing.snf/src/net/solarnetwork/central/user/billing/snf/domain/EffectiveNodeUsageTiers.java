/* ==================================================================
 * EffectiveNodeUsageTiers.java - 22/07/2020 3:18:30 PM
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

import java.time.LocalDate;

/**
 * A set of tiers effective at a specific date.
 * 
 * @author matt
 * @version 1.0
 */
public class EffectiveNodeUsageTiers {

	private final LocalDate date;
	private final NodeUsageTiers tiers;

	/**
	 * Constructor.
	 * 
	 * @param date
	 *        the effective date
	 * @param tiers
	 *        the associated tiers
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public EffectiveNodeUsageTiers(LocalDate date, NodeUsageTiers tiers) {
		super();
		if ( date == null ) {
			throw new IllegalArgumentException("The date argument must be provided.");
		}
		this.date = date;
		if ( tiers == null ) {
			throw new IllegalArgumentException("The tiers argument must be provided.");
		}
		this.tiers = tiers;
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
	 * Get the tiers.
	 * 
	 * @return the tiers
	 */
	public NodeUsageTiers getTiers() {
		return tiers;
	}

}
