/* ==================================================================
 * TaxCodeFilter.java - 24/07/2020 6:29:23 AM
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

import java.time.Instant;
import net.solarnetwork.domain.SimplePagination;

/**
 * Query filter for {@code TaxCode} entities.
 * 
 * @author matt
 * @version 1.0
 */
public class TaxCodeFilter extends SimplePagination {

	private String[] zones;
	private String itemKey;
	private String code;
	private Instant date;

	/**
	 * Get the tax zones.
	 * 
	 * @return the zones
	 */
	public String[] getZones() {
		return zones;
	}

	/**
	 * Set the tax zones.
	 * 
	 * @param zones
	 *        the zones to set
	 */
	public void setZones(String[] zones) {
		this.zones = zones;
	}

	/**
	 * Get the tax zone.
	 * 
	 * <p>
	 * This returns the first-available value from the {@code zones} array.
	 * </p>
	 * 
	 * @return the zone
	 */
	public String getZone() {
		String[] zones = getZones();
		return zones != null && zones.length > 0 ? zones[0] : null;
	}

	/**
	 * Set the tax zone.
	 * 
	 * <p>
	 * This replaces the configured {@code zones} array with a single-element
	 * array if {@code zone} is not {@literal null}, otherwise sets
	 * {@code zones} to {@literal null}.
	 * </p>
	 * 
	 * @param zone
	 *        the zone to set
	 */
	public void setZone(String zone) {
		setZones(zone != null ? new String[] { zone } : null);
	}

	/**
	 * Get the item key.
	 * 
	 * @return the item key
	 */
	public String getItemKey() {
		return itemKey;
	}

	/**
	 * Set the item key.
	 * 
	 * @param itemKey
	 *        the item key to set
	 */
	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
	}

	/**
	 * Get the tax code.
	 * 
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Set the tax code.
	 * 
	 * @param code
	 *        the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Get the effective date.
	 * 
	 * @return the date
	 */
	public Instant getDate() {
		return date;
	}

	/**
	 * Set the effective date.
	 * 
	 * @param date
	 *        the date to set
	 */
	public void setDate(Instant date) {
		this.date = date;
	}

}
