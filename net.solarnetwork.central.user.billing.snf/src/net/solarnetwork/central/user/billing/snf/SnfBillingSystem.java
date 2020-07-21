/* ==================================================================
 * SnfBillingSystem.java - 20/07/2020 9:01:05 AM
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;
import net.solarnetwork.central.domain.FilterResults;
import net.solarnetwork.central.domain.SortDescriptor;
import net.solarnetwork.central.user.billing.biz.BillingSystem;
import net.solarnetwork.central.user.billing.domain.BillingSystemInfo;
import net.solarnetwork.central.user.billing.domain.Invoice;
import net.solarnetwork.central.user.billing.domain.InvoiceFilter;
import net.solarnetwork.central.user.billing.domain.InvoiceMatch;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.support.BasicBillingSystemInfo;
import net.solarnetwork.central.user.domain.UserLongPK;

/**
 * {@link BillingSystem} implementation for SolarNetwork Foundation.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfBillingSystem implements BillingSystem, SnfInvoicingSystem {

	/** The {@literal accounting} billing data value for SNF. */
	public static final String ACCOUNTING_SYSTEM_KEY = "snf";

	@Override
	public String getAccountingSystemKey() {
		return ACCOUNTING_SYSTEM_KEY;
	}

	@Override
	public boolean supportsAccountingSystemKey(String key) {
		return ACCOUNTING_SYSTEM_KEY.equals(key);
	}

	@Override
	public BillingSystemInfo getInfo(Locale locale) {
		return new BasicBillingSystemInfo(getAccountingSystemKey());
	}

	@Override
	public FilterResults<InvoiceMatch> findFilteredInvoices(InvoiceFilter filter,
			List<SortDescriptor> sortDescriptors, Integer offset, Integer max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Invoice getInvoice(Long userId, String invoiceId, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource renderInvoice(Long userId, String invoiceId, MimeType outputType, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Account accountForUser(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SnfInvoice findLatestInvoiceForAccount(UserLongPK accountId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SnfInvoice generateInvoice(Long userId, LocalDate startDate, LocalDate endDate,
			boolean dryRun) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deliverInvoice(UUID invoiceId) {
		// TODO Auto-generated method stub
		return false;
	}

}
