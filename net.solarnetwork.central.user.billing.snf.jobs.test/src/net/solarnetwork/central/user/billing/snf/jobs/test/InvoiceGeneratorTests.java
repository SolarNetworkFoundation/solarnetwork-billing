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

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.SnfInvoicingSystem;
import net.solarnetwork.central.user.billing.snf.dao.AccountDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.AccountTask;
import net.solarnetwork.central.user.billing.snf.domain.AccountTaskType;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.jobs.InvoiceGenerator;
import net.solarnetwork.central.user.domain.UserLongPK;

/**
 * Test cases for the {@link InvoiceGenerator} class.
 * 
 * @author matt
 * @version 1.0
 */
public class InvoiceGeneratorTests {

	private static final Long TEST_USER_ID = 1L;

	private AccountDao accountDao;
	private SnfInvoicingSystem invoicingSystem;
	private InvoiceGenerator generator;

	@Before
	public void setup() {
		accountDao = EasyMock.createMock(AccountDao.class);
		invoicingSystem = EasyMock.createMock(SnfInvoicingSystem.class);

		generator = new InvoiceGenerator(accountDao, invoicingSystem);
	}

	private void replayAll() {
		EasyMock.replay(accountDao, invoicingSystem);
	}

	@After
	public void teardown() {
		EasyMock.verify(accountDao, invoicingSystem);
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
		final LocalDate date = LocalDate.of(2019, 12, 1);

		// get account
		final Account account = createAccount(TEST_USER_ID, "en_NZ",
				createAddress("NZ", "Pacific/Auckland"));
		expect(accountDao.get(new UserLongPK(null, account.getId().getId()))).andReturn(account);

		// generate invoice for month ending on endDate
		SnfInvoice generatedInvoice = new SnfInvoice(UUID.randomUUID(), account.getUserId(),
				account.getId().getId(), Instant.now());
		expect(invoicingSystem.generateInvoice(TEST_USER_ID, date, date.plusMonths(1), false))
				.andReturn(generatedInvoice);

		// WHEN
		replayAll();
		boolean result = generator
				.handleTask(AccountTask.newTask(date.atStartOfDay(account.getTimeZone()).toInstant(),
						AccountTaskType.GenerateInvoice, account.getId().getId()));

		// THEN
		assertThat("Task handled", result, equalTo(true));
	}

	@Test
	public void generateInitialInvoices_oneAccount_us() {
		// GIVEN
		final LocalDate date = LocalDate.of(2019, 12, 1);

		// get account
		final Account account = createAccount(TEST_USER_ID, "en_US",
				createAddress("US", "America/Los_Angeles"));
		expect(accountDao.get(new UserLongPK(null, account.getId().getId()))).andReturn(account);

		// generate invoice for month ending on endDate
		SnfInvoice generatedInvoice = new SnfInvoice(UUID.randomUUID(), account.getUserId(),
				account.getId().getId(), Instant.now());
		expect(invoicingSystem.generateInvoice(TEST_USER_ID, date, date.plusMonths(1), false))
				.andReturn(generatedInvoice);

		// WHEN
		replayAll();
		boolean result = generator
				.handleTask(AccountTask.newTask(date.atStartOfDay(account.getTimeZone()).toInstant(),
						AccountTaskType.GenerateInvoice, account.getId().getId()));

		// THEN
		assertThat("Task handled", result, equalTo(true));
	}

}
