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

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import net.solarnetwork.central.user.billing.snf.dao.mybatis.MyBatisNodeUsageDao;
import net.solarnetwork.central.user.billing.snf.domain.EffectiveNodeUsageTiers;
import net.solarnetwork.central.user.billing.snf.domain.NamedCost;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageCost;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageTier;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageTiers;

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
	public void tiersForDate_wayBack() {
		// GIVEN
		final LocalDate date = LocalDate.of(2010, 1, 1);

		// WHEN
		EffectiveNodeUsageTiers result = dao.effectiveNodeUsageTiers(date);

		assertThat("Effective tiers returned", result, notNullValue());
		assertThat("Effective date", result.getDate(), equalTo(LocalDate.of(2008, 1, 1)));
		NodeUsageTiers tiers = result.getTiers();
		assertThat("Single tier available", tiers.getTiers(), hasSize(1));
		assertThat("Tier 1", tiers.getTiers().get(0),
				equalTo(new NodeUsageTier(0, new NodeUsageCost("0.000009", "0.000002", "0.000000006"))));
	}

	@Test
	public void tiersForDate_moreRecent() {
		// GIVEN
		final LocalDate date = LocalDate.of(2020, 7, 1);

		// WHEN
		EffectiveNodeUsageTiers result = dao.effectiveNodeUsageTiers(date);

		assertThat("Effective tiers returned", result, notNullValue());
		assertThat("Effective date", result.getDate(), equalTo(LocalDate.of(2020, 6, 1)));
		NodeUsageTiers tiers = result.getTiers();
		assertThat("4 tiers available", tiers.getTiers(), hasSize(4));
		assertThat("Tier 1", tiers.getTiers().get(0),
				equalTo(new NodeUsageTier(0, new NodeUsageCost("0.000009", "0.000002", "0.0000004"))));
		assertThat("Tier 2", tiers.getTiers().get(1), equalTo(
				new NodeUsageTier(50000, new NodeUsageCost("0.000006", "0.000001", "0.0000002"))));
		assertThat("Tier 3", tiers.getTiers().get(2), equalTo(
				new NodeUsageTier(400000, new NodeUsageCost("0.000004", "0.0000005", "0.00000005"))));
		assertThat("Tier 4", tiers.getTiers().get(3), equalTo(
				new NodeUsageTier(1000000, new NodeUsageCost("0.000002", "0.0000002", "0.000000006"))));
	}

	@Test
	public void usageForUser_none() {
		// GIVEN
		final LocalDate month = LocalDate.of(2020, 1, 1);

		// WHEN
		List<NodeUsage> results = dao.findUsageForUser(userId, month, null);

		// THEN
		assertThat("Results non-null but empty", results, hasSize(0));
	}

	@Test
	public void usageForUser_oneNodeOneSource_wayBack() {
		// GIVEN
		final LocalDate month = LocalDate.of(2010, 1, 1);
		final String sourceId = "S1";

		// add 10 days worth of audit data
		final int numDays = 10;
		for ( int dayOffset = 0; dayOffset < numDays; dayOffset++ ) {
			Instant day = month.plusDays(dayOffset).atStartOfDay(TEST_ZONE).toInstant();
			addAuditAccumulatingDatumDaily(nodeId, sourceId, day, 1000, 2000, 3000, 4000);
			addAuditDatumMonthly(nodeId, sourceId, day, 100, 200, 300, (short) 400, (short) 500, true);
		}

		debugRows("solardatm.aud_acc_datm_daily", "ts_start");
		debugQuery(format(
				"select * from solarbill.billing_tier_details(%d, '2010-01-01'::timestamp, '2010-02-01'::timestamp, '2010-01-01'::date)",
				userId));

		// WHEN
		List<NodeUsage> results = dao.findUsageForUser(userId, month, month.plusMonths(1));

		// THEN
		assertThat("Results non-null with single result", results, hasSize(1));
		NodeUsage usage = results.get(0);
		assertThat("Properties in count aggregated", usage.getDatumPropertiesIn(),
				equalTo(BigInteger.valueOf(100L * numDays)));
		assertThat("Datum out count aggregated", usage.getDatumOut(),
				equalTo(BigInteger.valueOf(200L * numDays)));
		assertThat("Datum stored count aggregated", usage.getDatumDaysStored(),
				equalTo(BigInteger.valueOf((1000L + 2000L + 3000L + 4000L) * numDays)));

		// see {@link #tiersForDate_wayBack()}
		EffectiveNodeUsageTiers tiers = dao.effectiveNodeUsageTiers(month);
		NodeUsageCost cost = tiers.getTiers().getTiers().get(0).getCosts();

		assertThat("Properties in cost", usage.getDatumPropertiesInCost(), equalTo(
				new BigDecimal(usage.getDatumPropertiesIn()).multiply(cost.getDatumPropertiesInCost())));
		assertThat("Datum out cost", usage.getDatumOutCost().setScale(3), equalTo(
				new BigDecimal(usage.getDatumOut()).multiply(cost.getDatumOutCost()).setScale(3)));
		assertThat("Datum stored cost", usage.getDatumDaysStoredCost(), equalTo(
				new BigDecimal(usage.getDatumDaysStored()).multiply(cost.getDatumDaysStoredCost())));
	}

	@Test
	public void usageForUser_oneNodeOneSource_moreRecent() {
		// GIVEN
		final LocalDate month = LocalDate.of(2020, 7, 1);
		final String sourceId = "S1";

		// add 10 days worth of audit data
		final int numDays = 10;
		for ( int dayOffset = 0; dayOffset < numDays; dayOffset++ ) {
			Instant day = month.plusDays(dayOffset).atStartOfDay(TEST_ZONE).toInstant();
			addAuditAccumulatingDatumDaily(nodeId, sourceId, day, 1000000, 2000000, 3000000, 4000000);
			addAuditDatumMonthly(nodeId, sourceId, day, 100000, 200000, 300000, (short) 400000,
					(short) 500000, true);
		}

		debugRows("solardatm.aud_acc_datm_daily", "ts_start");
		debugQuery(format(
				"select * from solarbill.billing_tier_details(%d, '2020-07-01'::timestamp, '2020-08-01'::timestamp, '2020-07-01'::date)",
				userId));

		// WHEN
		List<NodeUsage> r1 = dao.findUsageForUser(userId, month, month.plusMonths(1));
		List<NodeUsage> r2 = dao.findUsageForAccount(userId, month, month.plusMonths(1));

		// THEN
		int i = 0;
		for ( List<NodeUsage> results : Arrays.asList(r1, r2) ) {
			assertThat("Results non-null with single result", results, hasSize(1));
			NodeUsage usage = results.get(0);
			if ( i == 0 ) {
				assertThat("Node ID present for node-level usage", usage.getId(), equalTo(nodeId));
			} else {
				assertThat("No node ID for account-level usage", usage.getId(), nullValue());
			}
			assertThat("Properties in count aggregated", usage.getDatumPropertiesIn(),
					equalTo(BigInteger.valueOf(100000L * numDays)));
			assertThat("Datum out count aggregated", usage.getDatumOut(),
					equalTo(BigInteger.valueOf(200000L * numDays)));
			assertThat("Datum stored count aggregated", usage.getDatumDaysStored(),
					equalTo(BigInteger.valueOf((1000000L + 2000000L + 3000000L + 4000000L) * numDays)));

			// see {@link #tiersForDate_wayBack()}
			EffectiveNodeUsageTiers tiers = dao.effectiveNodeUsageTiers(month);
			NodeUsageCost t1Costs = tiers.getTiers().getTiers().get(0).getCosts();
			NodeUsageCost t2Costs = tiers.getTiers().getTiers().get(1).getCosts();
			NodeUsageCost t3Costs = tiers.getTiers().getTiers().get(2).getCosts();
			NodeUsageCost t4Costs = tiers.getTiers().getTiers().get(3).getCosts();

			Map<String, List<NamedCost>> tiersBreakdown = usage.getTiersCostBreakdown();
			List<NamedCost> propsInTiersCost = tiersBreakdown.get(NodeUsage.DATUM_PROPS_IN_KEY);
			assertThat("Properties in cost tier count", propsInTiersCost, hasSize(3));
			List<NamedCost> datumOutTiersCost = tiersBreakdown.get(NodeUsage.DATUM_OUT_KEY);
			assertThat("Datum out cost tier count", datumOutTiersCost, hasSize(4));
			List<NamedCost> datumStoredTiersCost = tiersBreakdown.get(NodeUsage.DATUM_DAYS_STORED_KEY);
			assertThat("Datum stored cost tier count", datumStoredTiersCost, hasSize(4));

			/*-
			min=0		tier_prop_in=50000	cost_prop_in=0.000009	prop_in_cost=0.450000	tier_datum_stored=50000		cost_datum_stored=4E-7	datum_stored_cost=0.0200000		tier_datum_out=50000	cost_datum_out=0.000002	datum_out_cost=0.100000		total_cost=0.57}
			min=50000	tier_prop_in=350000	cost_prop_in=0.000006	prop_in_cost=2.100000	tier_datum_stored=350000	cost_datum_stored=2E-7	datum_stored_cost=0.0700000		tier_datum_out=350000	cost_datum_out=0.000001	datum_out_cost=0.350000		total_cost=2.52}
			min=400000	tier_prop_in=600000	cost_prop_in=0.000004	prop_in_cost=2.400000	tier_datum_stored=600000	cost_datum_stored=5E-8	datum_stored_cost=0.03000000	tier_datum_out=600000	cost_datum_out=5E-7		datum_out_cost=0.3000000	total_cost=2.73}
			min=1000000	tier_prop_in=0		cost_prop_in=0.000002	prop_in_cost=0.000000	tier_datum_stored=99000000	cost_datum_stored=6E-9	datum_stored_cost=0.594000000	tier_datum_out=1000000	cost_datum_out=2E-7		datum_out_cost=0.2000000	total_cost=0.79}
			*/

			// @formatter:off
			assertThat("Properties in cost", usage.getDatumPropertiesInCost().setScale(3), equalTo(
							new BigDecimal("50000").multiply(		t1Costs.getDatumPropertiesInCost())
					.add(	new BigDecimal("350000").multiply(		t2Costs.getDatumPropertiesInCost()))
					.add(	new BigDecimal("600000").multiply(		t3Costs.getDatumPropertiesInCost()))
					.setScale(3)
					));
			assertThat("Properties in cost tiers", propsInTiersCost, contains(
					NamedCost.forTier(1, "50000", 		new BigDecimal("50000").multiply(t1Costs.getDatumPropertiesInCost()).toString()),
					NamedCost.forTier(2, "350000", 		new BigDecimal("350000").multiply(t2Costs.getDatumPropertiesInCost()).toString()),
					NamedCost.forTier(3, "600000", 		new BigDecimal("600000").multiply(t3Costs.getDatumPropertiesInCost()).toString())));
			
			assertThat("Datum out cost", usage.getDatumOutCost().setScale(3), equalTo(
							new BigDecimal("50000").multiply(		t1Costs.getDatumOutCost())
					.add(	new BigDecimal("350000").multiply(		t2Costs.getDatumOutCost()))
					.add(	new BigDecimal("600000").multiply(		t3Costs.getDatumOutCost()))
					.add(	new BigDecimal("1000000").multiply(		t4Costs.getDatumOutCost()))
					.setScale(3)
					));
			assertThat("Datum out cost tiers", datumOutTiersCost, contains(
					NamedCost.forTier(1, "50000", 		new BigDecimal("50000").multiply(t1Costs.getDatumOutCost()).toString()),
					NamedCost.forTier(2, "350000", 		new BigDecimal("350000").multiply(t2Costs.getDatumOutCost()).toString()),
					NamedCost.forTier(3, "600000", 		new BigDecimal("600000").multiply(t3Costs.getDatumOutCost()).toString()),
					NamedCost.forTier(4, "1000000", 	new BigDecimal("1000000").multiply(t4Costs.getDatumOutCost()).toString())));
			
			assertThat("Datum stored cost", usage.getDatumDaysStoredCost().setScale(3), equalTo(
							new BigDecimal("50000").multiply(		t1Costs.getDatumDaysStoredCost())
					.add(	new BigDecimal("350000").multiply(		t2Costs.getDatumDaysStoredCost()))
					.add(	new BigDecimal("600000").multiply(		t3Costs.getDatumDaysStoredCost()))
					.add(	new BigDecimal("99000000").multiply(	t4Costs.getDatumDaysStoredCost()))
					.setScale(3)
					));
			assertThat("Datum stored cost tiers", datumStoredTiersCost, contains(
					NamedCost.forTier(1, "50000", 		new BigDecimal("50000").multiply(t1Costs.getDatumDaysStoredCost()).toString()),
					NamedCost.forTier(2, "350000", 		new BigDecimal("350000").multiply(t2Costs.getDatumDaysStoredCost()).toString()),
					NamedCost.forTier(3, "600000", 		new BigDecimal("600000").multiply(t3Costs.getDatumDaysStoredCost()).toString()),
					NamedCost.forTier(4, "99000000",	new BigDecimal("99000000").multiply(t4Costs.getDatumDaysStoredCost()).toString())));
			// @formatter:on
			i++;
		}

	}

	@Test
	public void usageForAccount_twoNodesOneSource_moreRecent() {
		// GIVEN
		Long nodeId2 = createTestNode(locId, userId);
		final LocalDate month = LocalDate.of(2020, 7, 1);
		final String sourceId = "S1";

		// add 10 days worth of audit data
		final int numDays = 10;
		for ( int dayOffset = 0; dayOffset < numDays; dayOffset++ ) {
			Instant day = month.plusDays(dayOffset).atStartOfDay(TEST_ZONE).toInstant();
			addAuditAccumulatingDatumDaily(nodeId, sourceId, day, 1_000_000, 2_000_000, 3_000_000,
					4_000_000);
			addAuditAccumulatingDatumDaily(nodeId2, sourceId, day, 500_000, 1_500_000, 2_500_000,
					3_500_000);

			addAuditDatumMonthly(nodeId, sourceId, day, 100_000, 200_000, 300_000, (short) 400_000,
					(short) 500_000, true);
			addAuditDatumMonthly(nodeId2, sourceId, day, 50_000, 150_000, 250_000, (short) 350_000,
					(short) 450_000, true);
		}

		debugRows("solardatm.aud_acc_datm_daily", "ts_start");
		debugQuery(format(
				"select * from solarbill.billing_usage_tier_details(%d, '2020-07-01'::timestamp, '2020-08-01'::timestamp, '2020-07-01'::date)",
				userId));

		// WHEN
		List<NodeUsage> results = dao.findUsageForAccount(userId, month, month.plusMonths(1));

		// THEN
		assertThat("Results non-null with single result", results, hasSize(1));
		NodeUsage usage = results.get(0);
		assertThat("No node ID for account-level usage", usage.getId(), nullValue());
		assertThat("Properties in count aggregated", usage.getDatumPropertiesIn(),
				equalTo(BigInteger.valueOf(150_000L * numDays)));
		assertThat("Datum out count aggregated", usage.getDatumOut(),
				equalTo(BigInteger.valueOf(350_000L * numDays)));
		assertThat("Datum stored count aggregated", usage.getDatumDaysStored(), equalTo(
				BigInteger.valueOf((1_500_000L + 3_500_000L + 5_500_000L + 7_500_000L) * numDays)));

		// see {@link #tiersForDate_wayBack()}
		EffectiveNodeUsageTiers tiers = dao.effectiveNodeUsageTiers(month);
		NodeUsageCost t1Costs = tiers.getTiers().getTiers().get(0).getCosts();
		NodeUsageCost t2Costs = tiers.getTiers().getTiers().get(1).getCosts();
		NodeUsageCost t3Costs = tiers.getTiers().getTiers().get(2).getCosts();
		NodeUsageCost t4Costs = tiers.getTiers().getTiers().get(3).getCosts();

		Map<String, List<NamedCost>> tiersBreakdown = usage.getTiersCostBreakdown();
		List<NamedCost> propsInTiersCost = tiersBreakdown.get(NodeUsage.DATUM_PROPS_IN_KEY);
		assertThat("Properties in cost tier count", propsInTiersCost, hasSize(4));
		List<NamedCost> datumOutTiersCost = tiersBreakdown.get(NodeUsage.DATUM_OUT_KEY);
		assertThat("Datum out cost tier count", datumOutTiersCost, hasSize(4));
		List<NamedCost> datumStoredTiersCost = tiersBreakdown.get(NodeUsage.DATUM_DAYS_STORED_KEY);
		assertThat("Datum stored cost tier count", datumStoredTiersCost, hasSize(4));

		/*-
		min=0,       prop_in=1500000, tier_prop_in=50000,  cost_prop_in=0.000009, prop_in_cost=0.450000, datum_stored=180000000, tier_datum_stored=50000, cost_datum_stored=4E-7, datum_stored_cost=0.0200000, datum_out=3500000, tier_datum_out=50000, cost_datum_out=0.000002, datum_out_cost=0.100000, total_cost=0.57}
		min=50000,   prop_in=1500000, tier_prop_in=350000, cost_prop_in=0.000006, prop_in_cost=2.100000, datum_stored=180000000, tier_datum_stored=350000, cost_datum_stored=2E-7, datum_stored_cost=0.0700000, datum_out=3500000, tier_datum_out=350000, cost_datum_out=0.000001, datum_out_cost=0.350000, total_cost=2.52
		min=400000,  prop_in=1500000, tier_prop_in=600000, cost_prop_in=0.000004, prop_in_cost=2.400000, datum_stored=180000000, tier_datum_stored=600000, cost_datum_stored=5E-8, datum_stored_cost=0.03000000, datum_out=3500000, tier_datum_out=600000, cost_datum_out=5E-7, datum_out_cost=0.3000000, total_cost=2.73
		min=1000000, prop_in=1500000, tier_prop_in=500000, cost_prop_in=0.000002, prop_in_cost=1.000000, datum_stored=180000000, tier_datum_stored=179000000, cost_datum_stored=6E-9, datum_stored_cost=1.074000000, datum_out=3500000, tier_datum_out=2500000, cost_datum_out=2E-7, datum_out_cost=0.5000000, total_cost=2.57
		*/

		// @formatter:off
		assertThat("Properties in cost", usage.getDatumPropertiesInCost().setScale(3), equalTo(
						new BigDecimal("50000").multiply(		t1Costs.getDatumPropertiesInCost())
				.add(	new BigDecimal("350000").multiply(		t2Costs.getDatumPropertiesInCost()))
				.add(	new BigDecimal("600000").multiply(		t3Costs.getDatumPropertiesInCost()))
				.add(	new BigDecimal("500000").multiply(		t4Costs.getDatumPropertiesInCost()))
				.setScale(3)
				));
		assertThat("Properties in cost tiers", propsInTiersCost, contains(
				NamedCost.forTier(1, "50000", 		new BigDecimal("50000").multiply(t1Costs.getDatumPropertiesInCost()).toString()),
				NamedCost.forTier(2, "350000", 		new BigDecimal("350000").multiply(t2Costs.getDatumPropertiesInCost()).toString()),
				NamedCost.forTier(3, "600000", 		new BigDecimal("600000").multiply(t3Costs.getDatumPropertiesInCost()).toString()),
				NamedCost.forTier(4, "500000", 		new BigDecimal("500000").multiply(t4Costs.getDatumPropertiesInCost()).toString())
				));
		
		assertThat("Datum out cost", usage.getDatumOutCost().setScale(3), equalTo(
						new BigDecimal("50000").multiply(		t1Costs.getDatumOutCost())
				.add(	new BigDecimal("350000").multiply(		t2Costs.getDatumOutCost()))
				.add(	new BigDecimal("600000").multiply(		t3Costs.getDatumOutCost()))
				.add(	new BigDecimal("2500000").multiply(		t4Costs.getDatumOutCost()))
				.setScale(3)
				));
		assertThat("Datum out cost tiers", datumOutTiersCost, contains(
				NamedCost.forTier(1, "50000", 		new BigDecimal("50000").multiply(t1Costs.getDatumOutCost()).toString()),
				NamedCost.forTier(2, "350000", 		new BigDecimal("350000").multiply(t2Costs.getDatumOutCost()).toString()),
				NamedCost.forTier(3, "600000", 		new BigDecimal("600000").multiply(t3Costs.getDatumOutCost()).toString()),
				NamedCost.forTier(4, "2500000", 	new BigDecimal("2500000").multiply(t4Costs.getDatumOutCost()).toString())
				));
		
		assertThat("Datum stored cost", usage.getDatumDaysStoredCost().setScale(3), equalTo(
						new BigDecimal("50000").multiply(		t1Costs.getDatumDaysStoredCost())
				.add(	new BigDecimal("350000").multiply(		t2Costs.getDatumDaysStoredCost()))
				.add(	new BigDecimal("600000").multiply(		t3Costs.getDatumDaysStoredCost()))
				.add(	new BigDecimal("179000000").multiply(	t4Costs.getDatumDaysStoredCost()))
				.setScale(3)
				));
		assertThat("Datum stored cost tiers", datumStoredTiersCost, contains(
				NamedCost.forTier(1, "50000", 		new BigDecimal("50000").multiply(t1Costs.getDatumDaysStoredCost()).toString()),
				NamedCost.forTier(2, "350000", 		new BigDecimal("350000").multiply(t2Costs.getDatumDaysStoredCost()).toString()),
				NamedCost.forTier(3, "600000", 		new BigDecimal("600000").multiply(t3Costs.getDatumDaysStoredCost()).toString()),
				NamedCost.forTier(4, "179000000",	new BigDecimal("179000000").multiply(t4Costs.getDatumDaysStoredCost()).toString())
				));
		// @formatter:on
	}

}
