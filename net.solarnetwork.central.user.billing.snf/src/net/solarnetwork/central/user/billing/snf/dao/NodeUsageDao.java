/* ==================================================================
 * NodeUsageDao.java - 22/07/2020 10:01:17 AM
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

package net.solarnetwork.central.user.billing.snf.dao;

import java.time.LocalDate;
import java.util.List;
import net.solarnetwork.central.user.billing.snf.domain.EffectiveNodeUsageTiers;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;

/**
 * DAO API for billing usage data.
 * 
 * @author matt
 * @version 1.1
 */
public interface NodeUsageDao {

	/**
	 * Get the node usage tiers effective at a specific date.
	 * 
	 * @param date
	 *        the date to get the effective node usage tiers for
	 * @return the tiers, or {@literal null} if no tiers are available
	 */
	EffectiveNodeUsageTiers effectiveNodeUsageTiers(LocalDate date);

	/**
	 * Find all node usage for a given user and time range.
	 * 
	 * <p>
	 * This method applies all metered tier rates to the <b>node</b> level.
	 * </p>
	 * 
	 * <p>
	 * This method differs from
	 * {@link #findNodeUsageForAccount(Long, LocalDate, LocalDate)} in that rows
	 * with no associated cost are <b>excluded</b> from the output.
	 * </p>
	 * 
	 * @param userId
	 *        the user to get usage for
	 * @param startDate
	 *        the minimum date to get usage for (inclusive)
	 * @param endDate
	 *        the maximum date to get usage for (exclusive)
	 * @return the matching usage, never {@literal null}
	 */
	List<NodeUsage> findUsageForUser(Long userId, LocalDate startDate, LocalDate endDate);

	/**
	 * Find all usage for a given user and time range.
	 * 
	 * <p>
	 * This method applies all metered tier rates to the <b>account</b> level.
	 * </p>
	 * 
	 * @param userId
	 *        the user to get usage for
	 * @param startDate
	 *        the minimum date to get usage for (inclusive)
	 * @param endDate
	 *        the maximum date to get usage for (exclusive)
	 * @return the matching usage, never {@literal null}
	 * @since 1.1
	 */
	List<NodeUsage> findUsageForAccount(Long userId, LocalDate startDate, LocalDate endDate);

	/**
	 * Find all node usage for a given user and time range.
	 * 
	 * <p>
	 * This method differs from
	 * {@link #findUsageForUser(Long, LocalDate, LocalDate)} in that rows with
	 * no cost are <b>included</b> in the output. This is meant to support
	 * finding the total metered usage by node by tier, which can be combined
	 * with an account-level tier cost calculation provided by
	 * {@link #findUsageForAccount(Long, LocalDate, LocalDate)}.
	 * </p>
	 * 
	 * @param userId
	 *        the user to get usage for
	 * @param startDate
	 *        the minimum date to get usage for (inclusive)
	 * @param endDate
	 *        the maximum date to get usage for (exclusive)
	 * @return the matching usage, never {@literal null}
	 * @since 1.1
	 */
	List<NodeUsage> findNodeUsageForAccount(Long userId, LocalDate startDate, LocalDate endDate);

}
