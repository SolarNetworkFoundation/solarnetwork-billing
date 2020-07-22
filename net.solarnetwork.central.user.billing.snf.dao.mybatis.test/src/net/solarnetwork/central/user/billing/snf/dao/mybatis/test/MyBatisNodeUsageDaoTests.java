/* ==================================================================
 * MyBatisNodeUsageDaoTests.java - 22/07/2020 10:40:26 AM
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisNodeUsageDao;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;

/**
 * Test cases for the {@link MyBatisNodeUsageDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisNodeUsageDaoTests extends AbstractMyBatisDaoTestSupport {

	private static final String TEST_TZ = "UTC";
	private static final ZoneId TEST_ZONE = ZoneId.of(TEST_TZ);

	private MyBatisNodeUsageDao dao;
	private Long userId;
	private Long locId;
	private Long nodeId;

	@Before
	public void setup() {
		dao = new MyBatisNodeUsageDao();
		dao.setSqlSessionTemplate(getSqlSessionTemplate());

		userId = UUID.randomUUID().getMostSignificantBits();
		setupTestUser(userId);

		locId = UUID.randomUUID().getMostSignificantBits();
		setupTestLocation(locId, TEST_TZ);

		nodeId = createTestNode(locId, userId);
	}

	private Long createTestNode(Long locId, Long userId) {
		Long nodeId = UUID.randomUUID().getMostSignificantBits();
		setupTestNode(nodeId, locId);
		setupTestUserNode(userId, nodeId);
		return nodeId;
	}

	@Test
	public void usageForUser_none() {
		// GIVEN
		final LocalDate month = LocalDate.of(2020, 1, 1);

		// WHEN
		List<NodeUsage> results = dao.findMonthlyUsageForUser(userId, month);

		// THEN
		assertThat("Results non-null but empty", results, hasSize(0));
	}

	private void addAuditDatumMonthly(Long nodeId, String sourceId, Instant date, long propCount,
			long datumQueryCount, long datumCount, long datumHourlyCount, long datumDailyCount,
			boolean monthPresent) {
		jdbcTemplate.update("insert into solaragg.aud_datum_monthly "
				+ "(ts_start,node_id,source_id,prop_count,datum_q_count,datum_count,datum_hourly_count,datum_daily_count,datum_monthly_pres)"
				+ "VALUES (?,?,?,?,?,?,?,?,?)", new Timestamp(date.toEpochMilli()), nodeId, sourceId,
				propCount, datumQueryCount, datumCount, datumHourlyCount, datumDailyCount, monthPresent);
	}

	private void addAuditAccumulatingDatumDaily(Long nodeId, String sourceId, Instant date,
			long datumCount, long datumHourlyCount, long datumDailyCount, long datumMonthlyCount) {
		jdbcTemplate.update("insert into solaragg.aud_acc_datum_daily "
				+ "(ts_start,node_id,source_id,datum_count,datum_hourly_count,datum_daily_count,datum_monthly_count)"
				+ "VALUES (?,?,?,?,?,?,?)", new Timestamp(date.toEpochMilli()), nodeId, sourceId,
				datumCount, datumHourlyCount, datumDailyCount, datumMonthlyCount);
	}

	@Test
	public void usageForUser_oneNodeOneSource() {
		// GIVEN
		final LocalDate month = LocalDate.of(2020, 1, 1);
		final String sourceId = "S1";

		// add 10 days worth of audit data
		for ( int dayOffset = 0; dayOffset < 10; dayOffset++ ) {
			Instant day = month.plusDays(dayOffset).atStartOfDay(TEST_ZONE).toInstant();
			addAuditAccumulatingDatumDaily(nodeId, sourceId, day, 1000, 2000, 3000, 4000);
			addAuditDatumMonthly(nodeId, sourceId, day, 100, 200, 300, 400, 500, true);
		}

		// WHEN
		List<NodeUsage> results = dao.findMonthlyUsageForUser(userId, month);

		// THEN
		assertThat("Results non-null with single result", results, hasSize(1));
	}

}
