/* ==================================================================
 * NamedCost.java - 23/07/2020 4:49:16 PM
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
import java.util.Objects;
import net.solarnetwork.domain.Differentiable;

/**
 * A named resource with associated cost.
 * 
 * @author matt
 * @version 1.0
 */
public class NamedCost implements Differentiable<NamedCost> {

	private final String name;
	private final BigInteger quantity;
	private final BigDecimal cost;

	/**
	 * Get a "tier" cost, using string number values.
	 * 
	 * @param i
	 *        the tier number
	 * @param quantity
	 *        the quantity; will be stored as {@literal 0} if {@literal null}
	 * @param cost
	 *        the cost; will be stored as {@literal 0} if {@literal null}
	 * @return the new instance
	 */
	public static NamedCost forTier(int i, String quantity, String cost) {
		return forTier(i, new BigInteger(quantity), new BigDecimal(cost));
	}

	/**
	 * Get a "tier" cost.
	 * 
	 * @param i
	 *        the tier number
	 * @param quantity
	 *        the quantity; will be stored as {@literal 0} if {@literal null}
	 * @param cost
	 *        the cost; will be stored as {@literal 0} if {@literal null}
	 * @return the new instance
	 */
	public static NamedCost forTier(int i, BigInteger quantity, BigDecimal cost) {
		return of(String.format("Tier %d", i), quantity, cost);
	}

	/**
	 * Create a new named cost instance.
	 * 
	 * @param name
	 *        the name
	 * @param quantity
	 *        the quantity; will be stored as {@literal 0} if {@literal null}
	 * @param cost
	 *        the cost; will be stored as {@literal 0} if {@literal null}
	 * @return the new instance
	 * @throws IllegalArgumentException
	 *         if {@code name} is {@literal null}
	 */
	public static NamedCost of(String name, BigInteger quantity, BigDecimal cost) {
		return new NamedCost(name, quantity, cost);
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        the name
	 * @param quantity
	 *        the quantity; will be stored as {@literal 0} if {@literal null}
	 * @param cost
	 *        the cost; will be stored as {@literal 0} if {@literal null}
	 * @throws IllegalArgumentException
	 *         if {@code name} is {@literal null}
	 */
	public NamedCost(String name, BigInteger quantity, BigDecimal cost) {
		super();
		if ( name == null ) {
			throw new IllegalArgumentException("The name argument must be provided.");
		}
		this.name = name;
		this.quantity = quantity != null ? quantity : BigInteger.ZERO;
		this.cost = cost = cost != null ? cost : BigDecimal.ZERO;
	}

	/**
	 * Test if the properties of another entity are the same as in this
	 * instance.
	 * 
	 * @param other
	 *        the other entity to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(NamedCost other) {
		return equals(other);
	}

	@Override
	public boolean differsFrom(NamedCost other) {
		return !isSameAs(other);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NamedCost{name=");
		builder.append(name);
		builder.append(", quantity=");
		builder.append(quantity);
		builder.append(", cost=");
		builder.append(cost);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(cost, name, quantity);
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !(obj instanceof NamedCost) ) {
			return false;
		}
		NamedCost other = (NamedCost) obj;
		return Objects.equals(name, other.name) && Objects.equals(quantity, other.quantity)
				&& (cost == other.cost) || (cost != null && cost.compareTo(other.cost) == 0);
	}

	/**
	 * Get the resource name.
	 * 
	 * @return the name, never {@literal null}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the resource quantity.
	 * 
	 * @return the quantity, never {@literal null}
	 */
	public BigInteger getQuantity() {
		return quantity;
	}

	/**
	 * Get the cost.
	 * 
	 * @return the cost, never {@literal null}
	 */
	public BigDecimal getCost() {
		return cost;
	}

	/**
	 * Get the effective rate, derived from the quantity and cost.
	 * 
	 * @return the effective rate
	 */
	public BigDecimal getEffectiveRate() {
		if ( BigInteger.ZERO.compareTo(quantity) == 0 ) {
			return BigDecimal.ZERO;
		}
		return cost.divide(new BigDecimal(quantity));
	}

}
