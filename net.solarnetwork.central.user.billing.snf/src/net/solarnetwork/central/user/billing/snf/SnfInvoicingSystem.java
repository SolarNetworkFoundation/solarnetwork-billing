/* ==================================================================
 * SnfInvoicingSystem.java - 20/07/2020 9:26:30 AM
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

package net.solarnetwork.central.user.billing.snf;

import java.time.LocalDate;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.domain.UserLongPK;

/**
 * API for generating invoices for the {@link SnfBillingSystem}.
 * 
 * @author matt
 * @version 1.0
 */
public interface SnfInvoicingSystem {

	/**
	 * Get the billing account for a given user.
	 * 
	 * @param userId
	 *        the ID of the user to get the account for
	 * @return the account, or {@literal null} if not available
	 */
	Account accountForUser(Long userId);

	/**
	 * Get the latest invoice for a given account.
	 * 
	 * @param accountId
	 *        the ID of the account to get the latest invoice for
	 * @return the latest available invoice, or {@literal null} if none
	 *         available
	 */
	SnfInvoice findLatestInvoiceForAccount(UserLongPK accountId);

	/**
	 * Generate a new invoice.
	 * 
	 * @param userId
	 *        the user ID to generate an invoice for
	 * @param startDate
	 *        the desired invoice period starting date; will be interpreted in
	 *        the user's account's time zone
	 * @param endDate
	 *        the desired invoice period ending date; will be interpreted in the
	 *        user's account's time zone
	 * @param dryRun
	 *        {@literal true} to generate an ephemeral invoice that is not
	 *        persisted nor delivered to the account holder, {@literal false} to
	 *        generate a persistent invoice that is also delivered to the
	 *        account holder if appropriate
	 * @return the generated invoice
	 */
	SnfInvoice generateInvoice(Long userId, LocalDate startDate, LocalDate endDate, boolean dryRun);

}
