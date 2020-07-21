/* ==================================================================
 * MyBatisAccountDaoTests.java - 21/07/2020 7:55:24 AM
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAccountDao;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisAddressDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.domain.UserLongPK;

/**
 * Test cases for the {@link MyBatisAccountDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisAccountDaoTests extends AbstractMyBatisDaoTestSupport {

	private MyBatisAddressDao addressDao;
	private MyBatisAccountDao dao;

	private Address address;
	private Account last;

	@Before
	public void setUp() throws Exception {
		addressDao = new MyBatisAddressDao();
		addressDao.setSqlSessionTemplate(getSqlSessionTemplate());
		dao = new MyBatisAccountDao();
		dao.setSqlSessionTemplate(getSqlSessionTemplate());

		address = addressDao.get(addressDao.save(createTestAddress()));
		last = null;
	}

	@Test
	public void insert() {
		Account entity = createTestAccount(address);
		UserLongPK pk = dao.save(entity);
		assertThat("PK created", pk, notNullValue());
		assertThat("PK userId preserved", pk.getUserId(), equalTo(entity.getUserId()));
		last = entity;
		last.getId().setId(pk.getId());
	}

	@Test
	public void getByPK() {
		insert();
		Account entity = dao.get(last.getId());

		assertThat("ID", entity.getId(), equalTo(last.getId()));
		assertThat("Created", entity.getCreated(), equalTo(last.getCreated()));
		assertThat("Account", entity.isSameAs(last), equalTo(true));
	}

	@Test
	public void update() {
		insert();
		Account obj = dao.get(last.getId());
		obj.setCurrencyCode("USD");
		obj.setLocale("en_US");
		UserLongPK pk = dao.save(obj);
		assertThat("PK unchanged", pk, equalTo(obj.getId()));

		Account entity = dao.get(pk);
		assertThat("Entity updated", entity.isSameAs(obj), equalTo(true));
	}

	@Test
	public void delete() {
		insert();
		dao.delete(last);
		assertThat("No longer found", dao.get(last.getId()), nullValue());
	}

	@Test
	public void delete_noMatch() {
		insert();
		Account someAddr = createTestAccount(address);
		dao.delete(someAddr);

		Account entity = dao.get(last.getId());
		assertThat("Entity unchanged", entity.isSameAs(last), equalTo(true));
	}

}
