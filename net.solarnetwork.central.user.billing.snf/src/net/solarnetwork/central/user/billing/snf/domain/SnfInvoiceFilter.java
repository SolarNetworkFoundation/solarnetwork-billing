/* ==================================================================
 * SnfInvoiceFilter.java - 23/07/2020 6:45:43 AM
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
import net.solarnetwork.domain.SimplePagination;

/**
 * Query filter for {@link SnfInvoice} entities.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfInvoiceFilter extends SimplePagination {

	private Long userId;
	private LocalDate startDate;
	private LocalDate endDate;

	/**
	 * Create a new filter with a user ID.
	 * 
	 * @param userId
	 *        the user ID to set
	 * @return the filter, never {@literal null}
	 */
	public static SnfInvoiceFilter forUser(Long userId) {
		SnfInvoiceFilter f = new SnfInvoiceFilter();
		f.setUserId(userId);
		return f;
	}

	@Override
	public SnfInvoiceFilter clone() {
		return (SnfInvoiceFilter) super.clone();
	}

	/**
	 * Get the user ID.
	 * 
	 * @return the user ID
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * Set the user ID.
	 * 
	 * @param userId
	 *        the user ID to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * Get the minimum date.
	 * 
	 * @return the starting date (inclusive)
	 */
	public LocalDate getStartDate() {
		return startDate;
	}

	/**
	 * Set the minimum date.
	 * 
	 * @param startDate
	 *        the date to set (inclusive)
	 */
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	/**
	 * Get the maximum date.
	 * 
	 * @return the date (exclusive)
	 */
	public LocalDate getEndDate() {
		return endDate;
	}

	/**
	 * Set the maximum date.
	 * 
	 * @param endDate
	 *        the date to set (exclusive)
	 */
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

}
