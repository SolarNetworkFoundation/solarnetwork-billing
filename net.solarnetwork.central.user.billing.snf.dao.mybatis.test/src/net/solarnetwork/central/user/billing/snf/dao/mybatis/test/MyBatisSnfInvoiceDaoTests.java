/* ==================================================================
 * MyBatisSnfInvoiceDaoTests.java - 21/07/2020 3:28:34 PM
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

package net.solarnetwork.central.user.billing.snf.dao.mybatis.test;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType.Fixed;
import static net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem.newItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAccountDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAddressDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisSnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisSnfInvoiceItemDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceFilter;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;
import net.solarnetwork.central.user.domain.UserLongPK;
import net.solarnetwork.dao.FilterResults;

/**
 * Test cases for the {@link MyBatisSnfInvoiceDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisSnfInvoiceDaoTests extends AbstractMyBatisDaoTestSupport {

	private static final String TEST_PROD_KEY = UUID.randomUUID().toString();

	private MyBatisAddressDao addressDao;
	private MyBatisAccountDao accountDao;
	private MyBatisSnfInvoiceItemDao itemDao;
	private MyBatisSnfInvoiceDao dao;

	private SnfInvoice last;

	@Before
	public void setUp() throws Exception {
		addressDao = new MyBatisAddressDao();
		addressDao.setSqlSessionTemplate(getSqlSessionTemplate());

		accountDao = new MyBatisAccountDao();
		accountDao.setSqlSessionTemplate(getSqlSessionTemplate());

		itemDao = new MyBatisSnfInvoiceItemDao();
		itemDao.setSqlSessionTemplate(getSqlSessionTemplate());

		dao = new MyBatisSnfInvoiceDao();
		dao.setSqlSessionTemplate(getSqlSessionTemplate());
		last = null;
	}

	@Test
	public void insert() {
		Address address = addressDao.get(addressDao.save(createTestAddress()));
		Account account = accountDao.get(accountDao.save(createTestAccount(address)));
		SnfInvoice entity = new SnfInvoice(account.getId().getId(), account.getUserId(),
				Instant.ofEpochMilli(System.currentTimeMillis()));
		entity.setAddress(address);
		entity.setCurrencyCode("NZD");
		entity.setStartDate(LocalDate.of(2019, 12, 1));
		entity.setEndDate(LocalDate.of(2020, 1, 1));
		UserLongPK pk = dao.save(entity);
		assertThat("PK created", pk.getId(), notNullValue());
		last = entity;
	}

	@Test
	public void getByPK() {
		insert();
		SnfInvoice entity = dao.get(last.getId());

		assertThat("ID", entity.getId(), equalTo(last.getId()));
		assertThat("Created", entity.getCreated(), equalTo(last.getCreated()));
		assertThat("InvoiceImpl sameness", entity.isSameAs(last), equalTo(true));
	}

	@Test
	public void getByPK_withItems() {
		// GIVEN
		insert();
		final SnfInvoice invoice = dao.get(last.getId());

		SnfInvoiceItem item1 = newItem(invoice, Fixed, TEST_PROD_KEY, BigDecimal.ONE,
				new BigDecimal("1.23"));
		item1.setMetadata(Collections.singletonMap("just", "testing"));
		SnfInvoiceItem item2 = newItem(invoice, Fixed, TEST_PROD_KEY, BigDecimal.ONE,
				new BigDecimal("2.34"));
		SnfInvoiceItem item3 = newItem(invoice, Fixed, TEST_PROD_KEY, BigDecimal.ONE,
				new BigDecimal("3.45"));
		final List<SnfInvoiceItem> items = Arrays.asList(item1, item2, item3);
		for ( SnfInvoiceItem item : items ) {
			itemDao.save(item);
		}

		// WHEN
		final SnfInvoice entity = dao.get(invoice.getId());

		// THEN
		assertThat("Items returned", entity.getItems(), hasSize(3));
		assertThat("Items count matches returned size", entity.getItemCount(), equalTo(3));
		Map<UUID, SnfInvoiceItem> itemMap = entity.itemMap();
		for ( SnfInvoiceItem item : items ) {
			SnfInvoiceItem other = itemMap.remove(item.getId());
			assertThat("Returned item same as saved", other.isSameAs(item), equalTo(true));
		}
		assertThat("Expected items returned", itemMap.keySet(), hasSize(0));
		assertAccountBalance(invoice.getAccountId(),
				item1.getAmount().add(item2.getAmount()).add(item3.getAmount()), BigDecimal.ZERO);
	}

	private List<SnfInvoice> createMonthlyInvoices(Account account, Address address, String currencyCode,
			LocalDate start, int count) {
		List<SnfInvoice> result = new ArrayList<>(count);
		for ( int i = 0; i < count; i++ ) {
			SnfInvoice invoice = new SnfInvoice(account.getId().getId(), account.getUserId(),
					Instant.ofEpochMilli(System.currentTimeMillis()));
			invoice.setAddress(address);
			invoice.setCurrencyCode(currencyCode);
			invoice.setStartDate(start.plusMonths(i));
			invoice.setEndDate(start.plusMonths(i + 1));
			UserLongPK invoiceId = dao.save(invoice);

			SnfInvoiceItem item1 = newItem(invoice, Fixed, TEST_PROD_KEY, BigDecimal.ONE,
					new BigDecimal("1.23"));
			item1.setMetadata(Collections.singletonMap("just", "testing"));
			SnfInvoiceItem item2 = newItem(invoice, Fixed, TEST_PROD_KEY, BigDecimal.ONE,
					new BigDecimal("2.34"));
			SnfInvoiceItem item3 = newItem(invoice, Fixed, TEST_PROD_KEY, BigDecimal.ONE,
					new BigDecimal("3.45"));
			for ( SnfInvoiceItem item : asList(item1, item2, item3) ) {
				itemDao.save(item);
			}

			result.add(dao.get(invoiceId));
		}
		return result;
	}

	@Test
	public void filterForUser_sortDefault() {
		// GIVEN
		insert();
		List<SnfInvoice> others = createMonthlyInvoices(
				accountDao.get(new UserLongPK(last.getUserId(), last.getAccountId())), last.getAddress(),
				"NZD", last.getStartDate().plusMonths(1), 3);

		// WHEN
		SnfInvoiceFilter filter = SnfInvoiceFilter.forUser(last.getUserId());
		final FilterResults<SnfInvoice, UserLongPK> result = dao.findFiltered(filter, null, null, null);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Returned result count", result.getReturnedResultCount(), equalTo(4));
		assertThat("Total results unknown", result.getTotalResults(), nullValue());

		List<SnfInvoice> expectedInvoices = Stream
				.concat(Collections.singleton(last).stream(), others.stream())
				.sorted(Collections.reverseOrder(SnfInvoice.SORT_BY_DATE)).collect(Collectors.toList());

		List<SnfInvoice> invoices = stream(result.spliterator(), false).collect(toList());
		assertThat("Returned results", invoices, hasSize(4));
		for ( int i = 0; i < 4; i++ ) {
			SnfInvoice invoice = invoices.get(i);
			SnfInvoice expected = expectedInvoices.get(i);
			assertThat(format("InvoiceImpl %d returned in order", i), invoice, equalTo(expected));
			assertThat(format("InvoiceImpl %d data preserved", i), invoice.isSameAs(expected),
					equalTo(true));
		}
	}

	@Test
	public void filterForUser_sortDefault_paged() {
		// GIVEN
		insert();
		List<SnfInvoice> others = createMonthlyInvoices(
				accountDao.get(new UserLongPK(last.getUserId(), last.getAccountId())), last.getAddress(),
				"NZD", last.getStartDate().plusMonths(1), 3);

		final List<SnfInvoice> expectedInvoices = Stream
				.concat(Collections.singleton(last).stream(), others.stream())
				.sorted(Collections.reverseOrder(SnfInvoice.SORT_BY_DATE)).collect(Collectors.toList());

		// WHEN
		SnfInvoiceFilter filter = SnfInvoiceFilter.forUser(last.getUserId());

		for ( int offset = 0; offset < 6; offset += 2 ) {
			final FilterResults<SnfInvoice, UserLongPK> result = dao.findFiltered(filter, null, offset,
					2);

			// THEN
			final int expectedCount = (offset < 4 ? 2 : 0);
			assertThat("Result returned", result, notNullValue());
			assertThat("Returned result page count", result.getReturnedResultCount(),
					equalTo(expectedCount));
			assertThat("Total results unknown", result.getTotalResults(), nullValue());

			List<SnfInvoice> invoices = stream(result.spliterator(), false).collect(toList());
			assertThat("Returned page results", invoices, hasSize(expectedCount));
			for ( int i = 0; i < expectedCount; i++ ) {
				SnfInvoice invoice = invoices.get(i);
				SnfInvoice expected = expectedInvoices.get(offset + i);
				assertThat(format("InvoiceImpl %d returned in order", i), invoice, equalTo(expected));
				assertThat(format("InvoiceImpl %d data preserved", i), invoice.isSameAs(expected),
						equalTo(true));
			}
		}
	}

	@Test
	public void filterForAccount_sortDefault() {
		// GIVEN
		insert();
		List<SnfInvoice> others = createMonthlyInvoices(
				accountDao.get(new UserLongPK(last.getUserId(), last.getAccountId())), last.getAddress(),
				"NZD", last.getStartDate().plusMonths(1), 3);

		// WHEN
		SnfInvoiceFilter filter = SnfInvoiceFilter.forAccount(last.getAccountId());
		final FilterResults<SnfInvoice, UserLongPK> result = dao.findFiltered(filter, null, null, null);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Returned result count", result.getReturnedResultCount(), equalTo(4));
		assertThat("Total results unknown", result.getTotalResults(), nullValue());

		List<SnfInvoice> expectedInvoices = Stream
				.concat(Collections.singleton(last).stream(), others.stream())
				.sorted(Collections.reverseOrder(SnfInvoice.SORT_BY_DATE)).collect(Collectors.toList());

		List<SnfInvoice> invoices = stream(result.spliterator(), false).collect(toList());
		assertThat("Returned results", invoices, hasSize(4));
		for ( int i = 0; i < 4; i++ ) {
			SnfInvoice invoice = invoices.get(i);
			SnfInvoice expected = expectedInvoices.get(i);
			assertThat(format("InvoiceImpl %d returned in order", i), invoice, equalTo(expected));
			assertThat(format("InvoiceImpl %d data preserved", i), invoice.isSameAs(expected),
					equalTo(true));
		}
	}

	@Test
	public void filterForAccount_sortDefault_paged() {
		// GIVEN
		insert();
		List<SnfInvoice> others = createMonthlyInvoices(
				accountDao.get(new UserLongPK(last.getUserId(), last.getAccountId())), last.getAddress(),
				"NZD", last.getStartDate().plusMonths(1), 3);

		final List<SnfInvoice> expectedInvoices = Stream
				.concat(Collections.singleton(last).stream(), others.stream())
				.sorted(Collections.reverseOrder(SnfInvoice.SORT_BY_DATE)).collect(Collectors.toList());

		// WHEN
		SnfInvoiceFilter filter = SnfInvoiceFilter.forAccount(last.getAccountId());

		for ( int offset = 0; offset < 6; offset += 2 ) {
			final FilterResults<SnfInvoice, UserLongPK> result = dao.findFiltered(filter, null, offset,
					2);

			// THEN
			final int expectedCount = (offset < 4 ? 2 : 0);
			assertThat("Result returned", result, notNullValue());
			assertThat("Returned result page count", result.getReturnedResultCount(),
					equalTo(expectedCount));
			assertThat("Total results unknown", result.getTotalResults(), nullValue());

			List<SnfInvoice> invoices = stream(result.spliterator(), false).collect(toList());
			assertThat("Returned page results", invoices, hasSize(expectedCount));
			for ( int i = 0; i < expectedCount; i++ ) {
				SnfInvoice invoice = invoices.get(i);
				SnfInvoice expected = expectedInvoices.get(offset + i);
				assertThat(format("InvoiceImpl %d returned in order", i), invoice, equalTo(expected));
				assertThat(format("InvoiceImpl %d data preserved", i), invoice.isSameAs(expected),
						equalTo(true));
			}
		}
	}

	@Test
	public void findLatestAccount() {
		// GIVEN
		insert();
		List<SnfInvoice> others = createMonthlyInvoices(
				accountDao.get(new UserLongPK(last.getUserId(), last.getAccountId())), last.getAddress(),
				"NZD", last.getStartDate().plusMonths(1), 3);

		final List<SnfInvoice> expectedInvoices = Stream
				.concat(Collections.singleton(last).stream(), others.stream())
				.sorted(Collections.reverseOrder(SnfInvoice.SORT_BY_DATE)).collect(Collectors.toList());

		// WHEN
		SnfInvoiceFilter filter = SnfInvoiceFilter.forAccount(last.getAccountId());
		final FilterResults<SnfInvoice, UserLongPK> result = dao.findFiltered(filter,
				SnfInvoiceDao.SORT_BY_INVOICE_DATE_DESCENDING, 0, 1);

		// THEN
		assertThat("Result returned", result, notNullValue());
		assertThat("Returned result page count", result.getReturnedResultCount(), equalTo(1));
		assertThat("Total results unknown", result.getTotalResults(), nullValue());

		List<SnfInvoice> invoices = stream(result.spliterator(), false).collect(toList());
		assertThat("Returned page results", invoices, hasSize(1));
		SnfInvoice invoice = invoices.get(0);
		SnfInvoice expected = expectedInvoices.get(0);
		assertThat("InvoiceImpl returned in order", invoice, equalTo(expected));
		assertThat("InvoiceImpl data preserved", invoice.isSameAs(expected), equalTo(true));
	}

}
