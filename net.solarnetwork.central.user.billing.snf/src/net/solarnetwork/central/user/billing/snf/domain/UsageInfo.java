/* ==================================================================
 * UsageInfo.java - 22/07/2020 8:48:28 AM
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
import java.util.Map;
import net.solarnetwork.central.user.billing.domain.InvoiceItemUsageRecord;

/**
 * Information about resource usage.
 * 
 * @author matt
 * @version 1.0
 */
public class UsageInfo implements InvoiceItemUsageRecord {

	private final String unitType;
	private final BigDecimal cost;
	private final BigDecimal amount;

	/**
	 * Get an instance out of a usage Map.
	 * 
	 * @param usage
	 *        the usage Map, whose keys match the properties of this class
	 * @return the usage, or {@literal null} if {@code usage} is {@literal null}
	 *         or does not contain valid property values
	 */
	public static UsageInfo of(Map<String, ?> usage) {
		if ( usage == null ) {
			return null;
		}
		Object unitType = usage.get("unitType");
		Object amount = usage.get("amount");
		Object cost = usage.get("cost");
		if ( unitType != null ) {
			try {
				return new UsageInfo(unitType.toString(),
						amount != null ? new BigDecimal(amount.toString()) : null,
						cost != null ? new BigDecimal(cost.toString()) : null);
			} catch ( IllegalArgumentException e ) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * Constructor.
	 * 
	 * @param unitType
	 *        the usage unit type
	 * @param amount
	 *        the usage amount; will be stored as {@literal 0} if
	 *        {@literal null}
	 * @param cost
	 *        the usage cost, in the currency of the account or invoice this
	 *        usage is associated with; will be stored as {@literal 0} if
	 *        {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code unitType} is {@literal null}
	 */
	public UsageInfo(String unitType, BigDecimal amount, BigDecimal cost) {
		super();
		if ( unitType == null ) {
			throw new IllegalArgumentException("The unitType argument must be provided.");
		}
		this.unitType = unitType;
		this.amount = amount != null ? amount : BigDecimal.ZERO;
		this.cost = cost != null ? cost : BigDecimal.ZERO;
	}

	/**
	 * Get the unit type.
	 * 
	 * @return the unit type, never {@literal null}
	 */
	@Override
	public String getUnitType() {
		return unitType;
	}

	/**
	 * Get the amount.
	 * 
	 * @return the amount, never {@literal null}
	 */
	@Override
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * Get the cost.
	 * 
	 * @return the cost, never {@literal null}
	 */
	public BigDecimal getCost() {
		return cost;
	}

}
