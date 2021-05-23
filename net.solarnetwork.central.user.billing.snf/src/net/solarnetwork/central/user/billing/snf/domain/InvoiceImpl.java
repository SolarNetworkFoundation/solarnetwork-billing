/* ==================================================================
 * InvoiceImpl.java - 24/07/2020 3:14:15 PM
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import net.solarnetwork.central.domain.BaseStringEntity;
import net.solarnetwork.central.user.billing.domain.Invoice;
import net.solarnetwork.central.user.billing.domain.InvoiceItem;
import net.solarnetwork.central.user.billing.domain.InvoiceMatch;
import net.solarnetwork.central.user.billing.domain.InvoiceUsageRecord;
import net.solarnetwork.central.user.billing.snf.util.SnfBillingUtils;

/**
 * Wrap a {@link SnfInvoiceItem} as an
 * {@link net.solarnetwork.central.user.billing.domain.Invoice}.
 * 
 * @author matt
 * @version 1.1
 */
public class InvoiceImpl extends BaseStringEntity implements Invoice, InvoiceMatch {

	private static final long serialVersionUID = -8004601006737637111L;

	private final SnfInvoice invoice;
	private final List<InvoiceItem> items;

	/**
	 * Convert a Java {@link LocalDate} into a Joda instance.
	 * 
	 * @param date
	 *        the date to convert
	 * @return the date, or {@literal null} if {@code date} is {@literal null}
	 */
	public static org.joda.time.LocalDate jodaLocalDate(LocalDate date) {
		if ( date == null ) {
			return null;
		}
		return new org.joda.time.LocalDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
	}

	/**
	 * Constructor.
	 * 
	 * @param invoice
	 *        the invoice to wrap
	 * @throws IllegalArgumentException
	 *         if {@code invoice} is {@literal null}
	 */
	public InvoiceImpl(SnfInvoice invoice) {
		this(invoice, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param invoice
	 *        the invoice to wrap
	 * @param items
	 *        the items to use directly
	 * @throws IllegalArgumentException
	 *         if {@code invoice} is {@literal null}
	 */
	public InvoiceImpl(SnfInvoice invoice, List<InvoiceItem> items) {
		super();
		if ( invoice == null ) {
			throw new IllegalArgumentException("The invoice argument must not be null.");
		}
		this.invoice = invoice;
		this.items = items;
		setId(invoice.getId().getId().toString());
		setCreated(new DateTime(invoice.getCreated().toEpochMilli()));
	}

	@Override
	public String getTimeZoneId() {
		Address addr = invoice.getAddress();
		return (addr != null ? addr.getTimeZoneId() : null);
	}

	@Override
	public String getInvoiceNumber() {
		return SnfBillingUtils.invoiceNumForId(invoice.getId().getId());
	}

	@Override
	public BigDecimal getAmount() {
		return invoice.getTotalAmount();
	}

	@Override
	public BigDecimal getBalance() {
		return invoice.getTotalAmount();
	}

	@Override
	public BigDecimal getTaxAmount() {
		Set<SnfInvoiceItem> items = invoice.getItems();
		if ( items == null ) {
			items = Collections.emptySet();
		}
		return items.stream().filter(e -> InvoiceItemType.Tax.equals(e.getItemType()))
				.map(e -> e.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	public String getCurrencyCode() {
		return invoice.getCurrencyCode();
	}

	@Override
	public List<InvoiceItem> getInvoiceItems() {
		if ( items != null ) {
			return items;
		}
		Set<SnfInvoiceItem> items = invoice.getItems();
		if ( items == null ) {
			return Collections.emptyList();
		}
		return items.stream().map(e -> new InvoiceItemImpl(invoice, e)).collect(Collectors.toList());
	}

	@Override
	public List<InvoiceUsageRecord> getInvoiceUsageRecords() {
		if ( invoice == null ) {
			return Collections.emptyList();
		}
		Set<NodeUsage> usages = invoice.getUsages();
		if ( usages == null || usages.isEmpty() ) {
			return Collections.emptyList();
		}
		return new ArrayList<>(usages);
	}

}
