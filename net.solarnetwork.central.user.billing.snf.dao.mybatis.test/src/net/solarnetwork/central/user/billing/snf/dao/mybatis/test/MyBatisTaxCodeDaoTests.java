/* ==================================================================
 * MyBatisTaxCodeDaoTests.java - 24/07/2020 6:53:52 AM
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
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisTaxCodeDao;
import net.solarnetwork.central.user.billing.snf.domain.TaxCode;
import net.solarnetwork.central.user.billing.snf.domain.TaxCodeFilter;
import net.solarnetwork.dao.FilterResults;

/**
 * Test cases for the {@link MyBatisTaxCodeDao} class.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisTaxCodeDaoTests extends AbstractMyBatisDaoTestSupport {

	private static final String TEST_ITEM_KEY = "foo";

	private MyBatisTaxCodeDao dao;

	@Before
	public void setup() {
		dao = new MyBatisTaxCodeDao();
		dao.setSqlSessionTemplate(getSqlSessionTemplate());
	}

	private List<TaxCode> populateTestTaxData() {
		final List<TaxCode> codes = new ArrayList<>(5);
		codes.add(new TaxCode(UUID.randomUUID().getMostSignificantBits(), Instant.now(), "NZ",
				TEST_ITEM_KEY, "GST", new BigDecimal("0.10"),
				LocalDate.of(1986, 10, 1).atStartOfDay(ZoneId.of("Pacific/Auckland")).toInstant(),
				LocalDate.of(1989, 7, 1).atStartOfDay(ZoneId.of("Pacific/Auckland")).toInstant()));
		codes.add(new TaxCode(UUID.randomUUID().getMostSignificantBits(), Instant.now(), "NZ",
				TEST_ITEM_KEY, "GST", new BigDecimal("0.125"),
				LocalDate.of(1989, 7, 1).atStartOfDay(ZoneId.of("Pacific/Auckland")).toInstant(),
				LocalDate.of(2010, 10, 1).atStartOfDay(ZoneId.of("Pacific/Auckland")).toInstant()));
		codes.add(new TaxCode(UUID.randomUUID().getMostSignificantBits(), Instant.now(), "NZ",
				TEST_ITEM_KEY, "GST", new BigDecimal("0.125"),
				LocalDate.of(2010, 10, 1).atStartOfDay(ZoneId.of("Pacific/Auckland")).toInstant(),
				null));
		jdbcTemplate.batchUpdate(
				"insert into solarbill.bill_tax_code (id,created,tax_zone,item_key,tax_code,tax_rate,valid_from,valid_to)"
						+ " VALUES (?,?,?,?,?,?,?,?)",
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						TaxCode c = codes.get(i);
						int col = 0;
						ps.setLong(++col, c.getId());
						ps.setTimestamp(++col, new Timestamp(c.getCreated().toEpochMilli()));
						ps.setString(++col, c.getZone());
						ps.setString(++col, c.getItemKey());
						ps.setString(++col, c.getCode());
						ps.setBigDecimal(++col, c.getRate());
						ps.setTimestamp(++col, new Timestamp(c.getValidFrom().toEpochMilli()));
						if ( c.getValidTo() != null ) {
							ps.setTimestamp(++col, new Timestamp(c.getValidTo().toEpochMilli()));
						} else {
							ps.setNull(++col, Types.TIMESTAMP_WITH_TIMEZONE);
						}
					}

					@Override
					public int getBatchSize() {
						return codes.size();
					}
				});
		return codes;
	}

	@Test
	public void filter_zoneAndItemKey_noMatch() {
		// GIVEN
		populateTestTaxData();

		// WHEN
		TaxCodeFilter f = new TaxCodeFilter();
		f.setZone("US");
		f.setItemKey(TEST_ITEM_KEY);
		FilterResults<TaxCode, Long> results = dao.findFiltered(f, null, null, null);

		// THEN
		assertThat("Result not null", results, notNullValue());
		assertThat("No matches found", results.getReturnedResultCount(), equalTo(0));

	}

}
