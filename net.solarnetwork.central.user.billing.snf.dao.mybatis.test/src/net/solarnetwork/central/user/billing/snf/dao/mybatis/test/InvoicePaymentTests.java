/* ==================================================================
 * InvoicePaymentTests.java - 30/07/2020 6:29:34 AM
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

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType.Fixed;
import static net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem.newItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAccountDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAddressDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisPaymentDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisSnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisSnfInvoiceItemDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.Payment;
import net.solarnetwork.central.user.billing.snf.domain.PaymentType;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;

/**
 * Test cases for invoice payment DB procedures.
 * 
 * @author matt
 * @version 1.0
 */
public class InvoicePaymentTests extends AbstractMyBatisDaoTestSupport {

	private MyBatisAddressDao addressDao;
	private MyBatisAccountDao accountDao;
	private MyBatisPaymentDao paymentDao;
	private MyBatisSnfInvoiceDao invoiceDao;
	private MyBatisSnfInvoiceItemDao itemDao;

	private Address address;
	private Account account;

	@Before
	public void setUp() throws Exception {
		addressDao = new MyBatisAddressDao();
		addressDao.setSqlSessionTemplate(getSqlSessionTemplate());

		accountDao = new MyBatisAccountDao();
		accountDao.setSqlSessionTemplate(getSqlSessionTemplate());

		paymentDao = new MyBatisPaymentDao();
		paymentDao.setSqlSessionTemplate(getSqlSessionTemplate());

		invoiceDao = new MyBatisSnfInvoiceDao();
		invoiceDao.setSqlSessionTemplate(getSqlSessionTemplate());

		itemDao = new MyBatisSnfInvoiceItemDao();
		itemDao.setSqlSessionTemplate(getSqlSessionTemplate());

		address = addressDao.get(addressDao.save(createTestAddress()));
		account = accountDao.get(accountDao.save(createTestAccount(address)));
	}

	private SnfInvoice createTestInvoice(Account account, Address address, LocalDate startDate) {
		SnfInvoice entity = new SnfInvoice(account.getId().getId(), account.getUserId(),
				Instant.ofEpochMilli(System.currentTimeMillis()));
		entity.setAddress(address);
		entity.setCurrencyCode(account.getCurrencyCode());
		entity.setStartDate(startDate);
		entity.setEndDate(startDate.plusMonths(1));
		return invoiceDao.get(invoiceDao.save(entity));
	}

	private SnfInvoice createTestInvoiceWithItems(Account account, Address address, LocalDate startDate,
			BigDecimal... amounts) {
		SnfInvoice invoice = createTestInvoice(account, address, startDate);
		for ( BigDecimal amount : amounts ) {
			SnfInvoiceItem item = newItem(invoice, Fixed, randomUUID().toString(), BigDecimal.ONE,
					amount);
			itemDao.save(item);
		}

		return invoiceDao.get(invoice.getId());
	}

	private SnfInvoice createTestInvoiceWithDefaultItems(Account account, Address address,
			LocalDate startDate) {
		return createTestInvoiceWithItems(account, address, startDate, new BigDecimal("1.23"),
				new BigDecimal("2.34"), new BigDecimal("3.45"));
	}

	private void addInvoicePayment(Long accountId, UUID paymentId, Long invoiceId, BigDecimal amount) {
		jdbcTemplate.update(
				"insert into solarbill.bill_invoice_payment (acct_id,pay_id,inv_id,amount) VALUES (?,?::uuid,?,?)",
				accountId, paymentId, invoiceId, amount);
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void addInvoicePayment_exceedPaymentAmount() {
		// create invoice
		final SnfInvoice invoice = createTestInvoiceWithDefaultItems(account, address,
				LocalDate.of(2020, 2, 1));

		// create payment
		Payment payment = new Payment(randomUUID(), account.getUserId(), account.getId().getId(), now());
		payment.setAmount(invoice.getTotalAmount());
		payment.setCurrencyCode(account.getCurrencyCode());
		payment.setExternalKey(randomUUID().toString());
		payment.setPaymentType(PaymentType.Payment);
		payment.setReference(randomUUID().toString());

		paymentDao.save(payment);
		getSqlSessionTemplate().flushStatements();

		// add one payment, full amount
		addInvoicePayment(invoice.getAccountId(), payment.getId().getId(), invoice.getId().getId(),
				invoice.getTotalAmount());
		assertAccountBalance(payment.getAccountId(), invoice.getTotalAmount(), payment.getAmount());

		// try to add another payment
		addInvoicePayment(invoice.getAccountId(), payment.getId().getId(), invoice.getId().getId(),
				new BigDecimal("0.01"));
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void updatePayment_underflowInvoicePaymentAmount() {
		// create invoice
		final SnfInvoice invoice = createTestInvoiceWithDefaultItems(account, address,
				LocalDate.of(2020, 2, 1));

		// create payment
		Payment payment = new Payment(randomUUID(), account.getUserId(), account.getId().getId(), now());
		payment.setAmount(invoice.getTotalAmount());
		payment.setCurrencyCode(account.getCurrencyCode());
		payment.setExternalKey(randomUUID().toString());
		payment.setPaymentType(PaymentType.Payment);
		payment.setReference(randomUUID().toString());

		paymentDao.save(payment);
		getSqlSessionTemplate().flushStatements();

		// add one payment, full amount
		addInvoicePayment(invoice.getAccountId(), payment.getId().getId(), invoice.getId().getId(),
				invoice.getTotalAmount());
		assertAccountBalance(payment.getAccountId(), invoice.getTotalAmount(), payment.getAmount());

		// try to decrease payment amount < invoice payments
		jdbcTemplate.update("update solarbill.bill_payment SET amount = ? WHERE id = ?::uuid",
				new BigDecimal("1.11"), payment.getId().getId());
	}

	@Test
	public void updatePayment_overflowInvoicePaymentAmount() {
		// create invoice
		final SnfInvoice invoice = createTestInvoiceWithDefaultItems(account, address,
				LocalDate.of(2020, 2, 1));

		// create payment
		Payment payment = new Payment(randomUUID(), account.getUserId(), account.getId().getId(), now());
		payment.setAmount(invoice.getTotalAmount());
		payment.setCurrencyCode(account.getCurrencyCode());
		payment.setExternalKey(randomUUID().toString());
		payment.setPaymentType(PaymentType.Payment);
		payment.setReference(randomUUID().toString());

		paymentDao.save(payment);
		getSqlSessionTemplate().flushStatements();

		// add one payment, full amount
		addInvoicePayment(invoice.getAccountId(), payment.getId().getId(), invoice.getId().getId(),
				invoice.getTotalAmount());
		assertAccountBalance(payment.getAccountId(), invoice.getTotalAmount(), payment.getAmount());

		// increase payment amount > invoice payments (this is OK)
		BigDecimal newPaymentAmount = new BigDecimal("100.10");
		jdbcTemplate.update("update solarbill.bill_payment SET amount = ? WHERE id = ?::uuid",
				newPaymentAmount, payment.getId().getId());
		assertAccountBalance(payment.getAccountId(), invoice.getTotalAmount(), newPaymentAmount);
	}
}
