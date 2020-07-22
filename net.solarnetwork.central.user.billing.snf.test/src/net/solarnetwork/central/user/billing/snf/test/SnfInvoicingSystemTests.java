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
import static java.util.UUID.randomUUID;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import net.solarnetwork.central.user.billing.snf.SnfBillingSystem;
import net.solarnetwork.central.user.billing.snf.SnfInvoicingSystem;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceFilter;
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

	private SnfInvoiceDao invoiceDao;
	private ResourceBundleMessageSource messageSource;
	private SnfInvoicingSystem system;

	private Long userId;
	private LocalDate startDate;
	private LocalDate endDate;

	@Before
	public void setup() {
		invoiceDao = EasyMock.createMock(SnfInvoiceDao.class);
		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(SnfBillingSystem.class.getName());

		system = new SnfBillingSystem(invoiceDao, messageSource);

		userId = UUID.randomUUID().getMostSignificantBits();
		startDate = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).minusMonths(1)
				.toLocalDate();
		endDate = startDate.plusMonths(1);
	}

	private void replayAll() {
		EasyMock.replay(invoiceDao);
	}

	@After
	public void teardown() {
		EasyMock.verify(invoiceDao);
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
	public void generateInvoice_basic_dryRun() {
		// GIVEN

		// WHEN
		replayAll();
		SnfInvoice invoice = system.generateInvoice(userId, startDate, endDate, true);

		// THEN
		assertThat("Invoice created", invoice, notNullValue());
	}

}
