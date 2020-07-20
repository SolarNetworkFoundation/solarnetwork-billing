/* ==================================================================
 * InvoiceGeneratorTests.java - 20/07/2020 3:14:22 PM
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

package net.solarnetwork.central.user.billing.snf.jobs.test;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.domain.FilterResults;
import net.solarnetwork.central.support.BasicFilterResults;
import net.solarnetwork.central.user.billing.domain.BillingDataConstants;
import net.solarnetwork.central.user.billing.snf.SnfBillingSystem;
import net.solarnetwork.central.user.billing.snf.SnfInvoicingSystem;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.jobs.InvoiceGenerator;
import net.solarnetwork.central.user.dao.UserDao;
import net.solarnetwork.central.user.domain.UserFilter;
import net.solarnetwork.central.user.domain.UserFilterMatch;
import net.solarnetwork.central.user.domain.UserMatch;

/**
 * Test cases for the {@link InvoiceGenerator} class.
 * 
 * @author matt
 * @version 1.0
 */
public class InvoiceGeneratorTests {

	private static final Long TEST_USER_ID = 1L;
	private static final String TEST_EMAIL = "test@localhost";

	private UserDao userDao;
	private SnfInvoicingSystem invoicingSystem;
	private InvoiceGenerator generator;

	@Before
	public void setup() {
		userDao = EasyMock.createMock(UserDao.class);
		invoicingSystem = EasyMock.createMock(SnfInvoicingSystem.class);

		generator = new InvoiceGenerator(userDao, invoicingSystem);
	}

	private void replayAll() {
		EasyMock.replay(userDao, invoicingSystem);
	}

	@After
	public void teardown() {
		EasyMock.verify(userDao, invoicingSystem);
	}

	private static Address createAddress(String country, String timeZoneId) {
		final Address addr = new Address(UUID.randomUUID().getMostSignificantBits(), Instant.now());
		addr.setCountry(country);
		addr.setTimeZoneId(timeZoneId);
		return addr;
	}

	private static Account createAccount(Long userId, String locale, Address address) {
		final Account account = new Account(UUID.randomUUID().getMostSignificantBits(), userId,
				Instant.now());
		account.setLocale(locale);
		account.setAddress(address);
		return account;
	}

	@Test
	public void generateInitialInvoices_oneAccount_nz() {
		// GIVEN
		final LocalDate endDate = LocalDate.of(2020, 1, 1);
		final Capture<UserFilter> userFilterCaptor = new Capture<>();
		final UserMatch user = new UserMatch(TEST_USER_ID, TEST_EMAIL);
		final FilterResults<UserFilterMatch> userMatches = new BasicFilterResults<>(asList(user));

		// find users configured with SNF billing
		expect(userDao.findFiltered(capture(userFilterCaptor), isNull(), eq(0),
				eq(InvoiceGenerator.DEFAULT_BATCH_SIZE))).andReturn(userMatches);

		// get Account for found user
		final Account account = createAccount(TEST_USER_ID, "en_NZ",
				createAddress("NZ", "Pacific/Auckland"));
		expect(invoicingSystem.accountForUser(TEST_USER_ID)).andReturn(account);

		// get latest invoice for account (there is none)
		expect(invoicingSystem.findLatestInvoiceForAccount(account.getId())).andReturn(null);

		// generate invoice for month ending on endDate
		SnfInvoice generatedInvoice = new SnfInvoice(account.getId().getId(), UUID.randomUUID(),
				account.getUserId(), Instant.now());
		expect(invoicingSystem.generateInvoice(TEST_USER_ID, endDate.minusMonths(1), endDate, false))
				.andReturn(generatedInvoice);

		// WHEN
		replayAll();
		generator.generateInvoices(endDate);

		// THEN
		UserFilter userFilter = userFilterCaptor.getValue();
		assertThat("User filter queried for SNF accounts", userFilter.getInternalData(), hasEntry(
				BillingDataConstants.ACCOUNTING_DATA_PROP, SnfBillingSystem.ACCOUNTING_SYSTEM_KEY));
	}

	@Test
	public void generateInitialInvoices_oneAccount_us() {
		// GIVEN
		final LocalDate endDate = LocalDate.of(2020, 1, 1);
		final Capture<UserFilter> userFilterCaptor = new Capture<>();
		final UserMatch user = new UserMatch(TEST_USER_ID, TEST_EMAIL);
		final FilterResults<UserFilterMatch> userMatches = new BasicFilterResults<>(asList(user));

		// find users configured with SNF billing
		expect(userDao.findFiltered(capture(userFilterCaptor), isNull(), eq(0),
				eq(InvoiceGenerator.DEFAULT_BATCH_SIZE))).andReturn(userMatches);

		// get Account for found user
		final Account account = createAccount(TEST_USER_ID, "en_US",
				createAddress("US", "America/Los_Angeles"));
		expect(invoicingSystem.accountForUser(TEST_USER_ID)).andReturn(account);

		// get latest invoice for account (there is none)
		expect(invoicingSystem.findLatestInvoiceForAccount(account.getId())).andReturn(null);

		// generate invoice for month ending on endDate
		SnfInvoice generatedInvoice = new SnfInvoice(account.getId().getId(), UUID.randomUUID(),
				account.getUserId(), Instant.now());
		expect(invoicingSystem.generateInvoice(TEST_USER_ID, endDate.minusMonths(1), endDate, false))
				.andReturn(generatedInvoice);

		// WHEN
		replayAll();
		generator.generateInvoices(endDate);

		// THEN
		UserFilter userFilter = userFilterCaptor.getValue();
		assertThat("User filter queried for SNF accounts", userFilter.getInternalData(), hasEntry(
				BillingDataConstants.ACCOUNTING_DATA_PROP, SnfBillingSystem.ACCOUNTING_SYSTEM_KEY));
	}

}
