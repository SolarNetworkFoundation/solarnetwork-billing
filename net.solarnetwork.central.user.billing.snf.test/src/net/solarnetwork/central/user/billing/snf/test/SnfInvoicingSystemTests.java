/* ==================================================================
 * SnfInvoicingSystemTests.java - 22/07/2020 9:19:57 AM
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

package net.solarnetwork.central.user.billing.snf.test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import net.solarnetwork.central.user.billing.snf.SnfBillingSystem;
import net.solarnetwork.central.user.billing.snf.SnfInvoicingSystem;
import net.solarnetwork.central.user.billing.snf.dao.AccountDao;
import net.solarnetwork.central.user.billing.snf.dao.NodeUsageDao;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceItemDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceFilter;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;
import net.solarnetwork.central.user.domain.UserLongPK;
import net.solarnetwork.dao.BasicFilterResults;

/**
 * Test cases for the {@link SnfBillingSystem} implementation of
 * {@link SnfInvoicingSystem}.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfInvoicingSystemTests {

	private AccountDao accountDao;
	private SnfInvoiceDao invoiceDao;
	private SnfInvoiceItemDao invoiceItemDao;
	private NodeUsageDao usageDao;
	private ResourceBundleMessageSource messageSource;
	private SnfInvoicingSystem system;

	private Long userId;
	private LocalDate startDate;
	private LocalDate endDate;

	@Before
	public void setup() {
		accountDao = EasyMock.createMock(AccountDao.class);
		invoiceDao = EasyMock.createMock(SnfInvoiceDao.class);
		invoiceItemDao = EasyMock.createMock(SnfInvoiceItemDao.class);
		usageDao = EasyMock.createMock(NodeUsageDao.class);
		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(SnfBillingSystem.class.getName());

		system = new SnfBillingSystem(accountDao, invoiceDao, invoiceItemDao, usageDao, messageSource);

		userId = UUID.randomUUID().getMostSignificantBits();
		startDate = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).minusMonths(1)
				.toLocalDate();
		endDate = startDate.plusMonths(1);
	}

	private void replayAll() {
		EasyMock.replay(accountDao, invoiceDao, usageDao);
	}

	@After
	public void teardown() {
		EasyMock.verify(accountDao, invoiceDao, usageDao);
	}

	@Test
	public void findLatestInvoice_none() {
		// GIVEN
		UserLongPK pk = new UserLongPK(randomUUID().getMostSignificantBits(),
				randomUUID().getMostSignificantBits());
		Capture<SnfInvoiceFilter> filterCaptor = new Capture<>();
		expect(invoiceDao.findFiltered(capture(filterCaptor),
				same(SnfInvoiceDao.SORT_BY_INVOICE_DATE_DESCENDING), eq(0), eq(1)))
						.andReturn(new BasicFilterResults<>(emptyList()));

		// WHEN
		replayAll();
		SnfInvoice invoice = system.findLatestInvoiceForAccount(pk);

		// THEN
		assertThat("Invoice not found.", invoice, nullValue());
		SnfInvoiceFilter filter = filterCaptor.getValue();
		assertThat("Query filter was by account ID", filter.getAccountId(), equalTo(pk.getId()));
	}

	@Test
	public void findLatestInvoice_found() {
		// GIVEN
		UserLongPK pk = new UserLongPK(randomUUID().getMostSignificantBits(),
				randomUUID().getMostSignificantBits());
		Capture<SnfInvoiceFilter> filterCaptor = new Capture<>();
		SnfInvoice inv = new SnfInvoice(pk.getId());
		expect(invoiceDao.findFiltered(capture(filterCaptor),
				same(SnfInvoiceDao.SORT_BY_INVOICE_DATE_DESCENDING), eq(0), eq(1)))
						.andReturn(new BasicFilterResults<>(singleton(inv)));

		// WHEN
		replayAll();
		SnfInvoice invoice = system.findLatestInvoiceForAccount(pk);

		// THEN
		assertThat("Invoice returned from DAO.", invoice, sameInstance(inv));
		SnfInvoiceFilter filter = filterCaptor.getValue();
		assertThat("Query filter was by account ID", filter.getAccountId(), equalTo(pk.getId()));
	}

	@Test
	public void accountForUser_none() {
		// GIVEN
		final Long userId = randomUUID().getMostSignificantBits();
		expect(accountDao.getForUser(userId)).andReturn(null);

		// WHEN
		replayAll();
		Account result = system.accountForUser(userId);

		// THEN
		assertThat("Account not found.", result, nullValue());
	}

	@Test
	public void accountForUser_found() {
		// GIVEN
		final Account account = new Account(userId, Instant.now());
		expect(accountDao.getForUser(userId)).andReturn(account);

		// WHEN
		replayAll();
		Account result = system.accountForUser(userId);

		// THEN
		assertThat("DAO result returned.", result, sameInstance(account));
	}

	private static void assertUsageItem(SnfInvoice invoice, SnfInvoiceItem item, BigInteger quantity,
			BigDecimal amount) {
		assertThat(item.getKey() + "Item ID generated", item.getId(), notNullValue());
		assertThat(item.getKey() + "Item invoice ID", item.getInvoiceId(),
				equalTo(invoice.getId().getId()));
		assertThat(item.getKey() + "Item type", item.getItemType(), equalTo(InvoiceItemType.Usage));
		assertThat(item.getKey() + "Item quantity", item.getQuantity(),
				equalTo(new BigDecimal(quantity)));
		assertThat(item.getKey() + "Item amount", item.getAmount(), equalTo(amount));
	}

	@Test
	public void generateInvoice_basic_dryRun() {
		// GIVEN
		final Account account = new Account(randomUUID().getMostSignificantBits(), userId,
				Instant.now());
		expect(accountDao.getForUser(userId)).andReturn(account);

		final NodeUsage usage = new NodeUsage(randomUUID().getMostSignificantBits());
		usage.setDatumPropertiesIn(new BigInteger("123"));
		usage.setDatumPropertiesInCost(new BigDecimal("1.23"));
		usage.setDatumOut(new BigInteger("234"));
		usage.setDatumOutCost(new BigDecimal("2.34"));
		usage.setDatumDaysStored(new BigInteger("345"));
		usage.setDatumDaysStoredCost(new BigDecimal("3.45"));
		usage.setTotalCost(new BigDecimal("7.02"));

		expect(usageDao.findUsageForUser(userId, startDate, endDate)).andReturn(singletonList(usage));

		// WHEN
		replayAll();
		SnfInvoice invoice = system.generateInvoice(userId, startDate, endDate, true);

		// THEN
		assertThat("Invoice created", invoice, notNullValue());
		assertThat("Invoice items created for all usage", invoice.getItems(), hasSize(3));

		Map<String, SnfInvoiceItem> itemMap = invoice.getItemsByKey();
		assertThat("Invoice item mapping contains all items", itemMap.keySet(), contains(
				NodeUsage.DATUM_PROPS_IN_KEY, NodeUsage.DATUM_OUT_KEY, NodeUsage.DATUM_DAYS_STORED_KEY));

		SnfInvoiceItem item;
		item = itemMap.get(NodeUsage.DATUM_PROPS_IN_KEY);
		assertUsageItem(invoice, item, usage.getDatumPropertiesIn(), usage.getDatumPropertiesInCost());
		item = itemMap.get(NodeUsage.DATUM_OUT_KEY);
		assertUsageItem(invoice, item, usage.getDatumOut(), usage.getDatumOutCost());
		item = itemMap.get(NodeUsage.DATUM_DAYS_STORED_KEY);
		assertUsageItem(invoice, item, usage.getDatumDaysStored(), usage.getDatumDaysStoredCost());
	}

}
