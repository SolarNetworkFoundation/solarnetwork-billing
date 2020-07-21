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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAccountDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAddressDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisSnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.domain.UserUuidPK;

/**
 * Test cases for the {@link MyBatisSnfInvoiceDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisSnfInvoiceDaoTests extends AbstractMyBatisDaoTestSupport {

	private MyBatisAddressDao addressDao;
	private MyBatisAccountDao accountDao;
	private MyBatisSnfInvoiceDao dao;

	private SnfInvoice last;

	@Before
	public void setUp() throws Exception {
		addressDao = new MyBatisAddressDao();
		addressDao.setSqlSessionTemplate(getSqlSessionTemplate());

		accountDao = new MyBatisAccountDao();
		accountDao.setSqlSessionTemplate(getSqlSessionTemplate());

		dao = new MyBatisSnfInvoiceDao();
		dao.setSqlSessionTemplate(getSqlSessionTemplate());
		last = null;
	}

	@Test
	public void insert() {
		Address address = addressDao.get(addressDao.save(createTestAddress()));
		Account account = accountDao.get(accountDao.save(createTestAccount(address)));
		SnfInvoice entity = new SnfInvoice(UUID.randomUUID(), account.getUserId(),
				account.getId().getId(), Instant.ofEpochMilli(System.currentTimeMillis()));
		entity.setAddress(address);
		entity.setCurrencyCode("NZD");
		entity.setStartDate(LocalDate.of(2019, 12, 1));
		entity.setEndDate(LocalDate.of(2020, 1, 1));
		UserUuidPK pk = dao.save(entity);
		assertThat("PK preserved", pk, equalTo(entity.getId()));
		last = entity;
	}

	@Test
	public void getByPK() {
		insert();
		SnfInvoice entity = dao.get(last.getId());

		assertThat("ID", entity.getId(), equalTo(last.getId()));
		assertThat("Created", entity.getCreated(), equalTo(last.getCreated()));
		assertThat("Invoice sameness", entity.isSameAs(last), equalTo(true));
	}
}
