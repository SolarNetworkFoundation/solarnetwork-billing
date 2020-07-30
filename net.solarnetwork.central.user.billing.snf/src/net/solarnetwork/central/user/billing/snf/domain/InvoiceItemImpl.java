/* ==================================================================
 * InvoiceItemImpl.java - 24/07/2020 2:08:15 PM
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import net.solarnetwork.central.domain.BaseStringEntity;
import net.solarnetwork.central.user.billing.domain.InvoiceItem;
import net.solarnetwork.central.user.billing.domain.InvoiceItemUsageRecord;

/**
 * Wrap a {@link SnfInvoiceItem} as an
 * {@link net.solarnetwork.central.user.billing.domain.InvoiceItem}.
 * 
 * @author matt
 * @version 1.0
 */
public class InvoiceItemImpl extends BaseStringEntity implements InvoiceItem {

	private static final long serialVersionUID = 4459693712467984755L;

	private final SnfInvoice invoice;
	private final SnfInvoiceItem item;
	private final List<InvoiceItemUsageRecord> itemUsageRecords;
	private final org.joda.time.LocalDate startDate;
	private final org.joda.time.LocalDate endDate;

	/**
	 * Constructor.
	 * 
	 * @param invoice
	 *        the invoice to wrap
	 * @param item
	 *        the item to wrap
	 * @throws IllegalArgumentException
	 *         if {@code invoice} or {@code item} are {@literal null}
	 */
	public InvoiceItemImpl(SnfInvoice invoice, SnfInvoiceItem item) {
		this(invoice, item, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param invoice
	 *        the invoice to wrap
	 * @param item
	 *        the item to wrap
	 * @param itemUsageRecords
	 *        the explicit usage records to use
	 * @throws IllegalArgumentException
	 *         if {@code invoice} or {@code item} are {@literal null}
	 */
	public InvoiceItemImpl(SnfInvoice invoice, SnfInvoiceItem item,
			List<InvoiceItemUsageRecord> itemUsageRecords) {
		super();
		if ( invoice == null ) {
			throw new IllegalArgumentException("The invoice argument must not be null.");
		}
		this.invoice = invoice;
		if ( item == null ) {
			throw new IllegalArgumentException("The item argument must not be null.");
		}
		this.item = item;
		this.itemUsageRecords = itemUsageRecords;
		this.startDate = (invoice.getStartDate() != null
				? InvoiceImpl.jodaLocalDate(invoice.getStartDate())
				: null);
		this.endDate = (invoice.getEndDate() != null ? InvoiceImpl.jodaLocalDate(invoice.getEndDate())
				: null);
		if ( item.getId() != null ) {
			setId(item.getId().toString());
		}
		if ( item.getCreated() != null ) {
			setCreated(new DateTime(item.getCreated().toEpochMilli()));
		}

	}

	@Override
	public Map<String, Object> getMetadata() {
		return item.getMetadata();
	}

	@Override
	public String getTimeZoneId() {
		Address addr = invoice.getAddress();
		return (addr != null ? addr.getTimeZoneId() : null);
	}

	@Override
	public String getPlanName() {
		return item.getKey();
	}

	@Override
	public String getItemType() {
		InvoiceItemType type = item.getItemType();
		return (type != null ? type.toString().toUpperCase() : null);
	}

	@Override
	public String getDescription() {
		return getPlanName();
	}

	@Override
	public org.joda.time.LocalDate getStartDate() {
		return startDate;
	}

	@Override
	public org.joda.time.LocalDate getEndDate() {
		return endDate;
	}

	@Override
	public BigDecimal getAmount() {
		return item.getAmount();
	}

	@Override
	public String getCurrencyCode() {
		return invoice.getCurrencyCode();
	}

	@Override
	public DateTime getEnded() {
		return null;
	}

	@Override
	public List<InvoiceItemUsageRecord> getItemUsageRecords() {
		if ( itemUsageRecords != null ) {
			return itemUsageRecords;
		}
		Map<String, Object> metadata = item.getMetadata();
		if ( metadata != null && (metadata.get(SnfInvoiceItem.META_USAGE) instanceof Map) ) {
			@SuppressWarnings("unchecked")
			UsageInfo usage = UsageInfo.of((Map<String, ?>) metadata.get(SnfInvoiceItem.META_USAGE));
			if ( usage != null ) {
				return Collections.singletonList(usage);
			}
		}
		return null;
	}

}
