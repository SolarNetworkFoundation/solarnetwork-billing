/* ==================================================================
 * MyBatisPaymentDaoTests.java - 29/07/2020 7:30:11 AM
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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAccountDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAddressDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisPaymentDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.Payment;
import net.solarnetwork.central.user.billing.snf.domain.PaymentType;
import net.solarnetwork.central.user.domain.UserUuidPK;

/**
 * Test cases for the {@link MyBatisPaymentDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisPaymentDaoTests extends AbstractMyBatisDaoTestSupport {

	private static final String TEST_EXT_KEY = randomUUID().toString();

	private MyBatisAddressDao addressDao;
	private MyBatisAccountDao accountDao;
	private MyBatisPaymentDao dao;

	private Account account;
	private Payment last;

	@Before
	public void setUp() throws Exception {
		addressDao = new MyBatisAddressDao();
		addressDao.setSqlSessionTemplate(getSqlSessionTemplate());

		accountDao = new MyBatisAccountDao();
		accountDao.setSqlSessionTemplate(getSqlSessionTemplate());

		dao = new MyBatisPaymentDao();
		dao.setSqlSessionTemplate(getSqlSessionTemplate());

		last = null;

		Address address = addressDao.get(addressDao.save(createTestAddress()));
		account = accountDao.get(accountDao.save(createTestAccount(address)));
	}

	@Test
	public void insert() {
		Payment entity = new Payment(randomUUID(), account.getUserId(), account.getId().getId(), now());
		entity.setAmount(new BigDecimal("12345.67"));
		entity.setCurrencyCode("NZD");
		entity.setExternalKey(TEST_EXT_KEY);
		entity.setPaymentType(PaymentType.Payment);
		entity.setReference(UUID.randomUUID().toString());

		UserUuidPK pk = dao.save(entity);
		assertThat("PK preserved", pk, equalTo(entity.getId()));
		assertAccountBalance(entity.getAccountId(), BigDecimal.ZERO, entity.getAmount());
		last = entity;
	}

	@Test
	public void getByPK() {
		insert();
		Payment entity = dao.get(last.getId());

		assertThat("ID", entity.getId(), equalTo(last.getId()));
		assertThat("Created", entity.getCreated(), equalTo(last.getCreated()));
		assertThat("Entity sameness", entity.isSameAs(last), equalTo(true));
	}

}
