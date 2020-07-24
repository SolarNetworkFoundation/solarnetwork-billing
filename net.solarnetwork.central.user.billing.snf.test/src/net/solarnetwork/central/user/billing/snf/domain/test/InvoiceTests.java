/* ==================================================================
 * InvoiceTests.java - 24/07/2020 3:30:53 PM
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

package net.solarnetwork.central.user.billing.snf.domain.test;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Test;
import net.solarnetwork.central.user.billing.domain.InvoiceItem;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.Invoice;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;

/**
 * Test cases for the {@link Invoice} class.
 * 
 * @author matt
 * @version 1.0
 */
public class InvoiceTests {

	private void assertInvoiceItem(String prefix, SnfInvoiceItem expected, InvoiceItem item) {
		assertThat(prefix + " ID", item.getId(), equalTo(expected.getId().toString()));
		assertThat(prefix + " type", item.getItemType(), equalTo(expected.getItemType().toString()));
		assertThat(prefix + " amount", item.getAmount(), equalTo(expected.getAmount()));
	}

	@Test
	public void properties() {
		// GIVEN
		Address addr = new Address();
		addr.setCountry("NZ");
		addr.setTimeZoneId("Pacific/Auckland");
		SnfInvoice inv = new SnfInvoice(randomUUID().getMostSignificantBits(),
				randomUUID().getMostSignificantBits(), randomUUID().getMostSignificantBits(),
				Instant.now());
		inv.setAddress(addr);
		inv.setStartDate(LocalDate.of(2020, 1, 1));
		inv.setEndDate(LocalDate.of(2020, 2, 1));

		SnfInvoiceItem itm1 = SnfInvoiceItem.newItem(inv, InvoiceItemType.Usage, NodeUsage.DATUM_OUT_KEY,
				new BigDecimal("1234567890"), new BigDecimal("123456.78"));
		SnfInvoiceItem itm2 = SnfInvoiceItem.newItem(inv, InvoiceItemType.Usage, NodeUsage.DATUM_OUT_KEY,
				new BigDecimal("2345678901"), new BigDecimal("234567.89"));
		SnfInvoiceItem tax1 = SnfInvoiceItem.newItem(inv, InvoiceItemType.Tax, "GST", BigDecimal.ONE,
				new BigDecimal("345.67"));
		SnfInvoiceItem tax2 = SnfInvoiceItem.newItem(inv, InvoiceItemType.Tax, "GIMMIE", BigDecimal.ONE,
				new BigDecimal("89.01"));

		inv.setItems(new LinkedHashSet<>(asList(itm1, itm2, tax1, tax2)));

		// WHEN
		Invoice invoice = new Invoice(inv);

		// THEN
		assertThat("ID is Long string", invoice.getId(), equalTo(inv.getId().getId().toString()));
		assertThat("Creation same", invoice.getCreated(),
				equalTo(new DateTime(inv.getCreated().toEpochMilli())));
		assertThat("Amount same as total amount", invoice.getAmount(), equalTo(inv.getTotalAmount()));
		assertThat("Balance same as total amount", invoice.getBalance(), equalTo(inv.getTotalAmount()));
		assertThat("Currency same", invoice.getCurrencyCode(), equalTo(inv.getCurrencyCode()));
		assertThat("Invoice number is upper-case base-16 string of ID", invoice.getInvoiceNumber(),
				equalTo(Long.toHexString(inv.getId().getId()).toUpperCase()));
		assertThat("Tax is added up", invoice.getTaxAmount(),
				equalTo(tax1.getAmount().add(tax2.getAmount())));
		assertThat("Time zone same as invoice address", invoice.getTimeZoneId(),
				equalTo(addr.getTimeZoneId()));

		List<InvoiceItem> items = invoice.getInvoiceItems();
		assertThat("Same number of items as original", items, hasSize(4));
		assertInvoiceItem("Item 1", itm1, items.get(0));
		assertInvoiceItem("Item 2", itm2, items.get(1));
		assertInvoiceItem("Item 3", tax1, items.get(2));
		assertInvoiceItem("Item 4", tax2, items.get(3));
	}

}
