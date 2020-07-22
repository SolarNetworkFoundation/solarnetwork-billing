/* ==================================================================
 * MyBatisNodeUsageDao.java - 22/07/2020 10:34:03 AM
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

package net.solarnetwork.central.user.billing.snf.dao.mybatis;

import static java.util.stream.Collectors.toList;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.solarnetwork.central.dao.mybatis.support.BaseMyBatisGenericDaoSupport;
import net.solarnetwork.central.user.billing.snf.dao.NodeUsageDao;
import net.solarnetwork.central.user.billing.snf.domain.EffectiveNodeUsageTier;
import net.solarnetwork.central.user.billing.snf.domain.EffectiveNodeUsageTiers;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageTier;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsageTiers;

/**
 * MyBatis implementation of {@link NodeUsageDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisNodeUsageDao extends BaseMyBatisGenericDaoSupport<NodeUsage, Long>
		implements NodeUsageDao {

	/** Query name enumeration. */
	public enum QueryName {

		FindEffectiveUsageTierForDate("find-EffectiveNodeUsageTier-for-date"),

		/** Find all available usage for a given user and month. */
		FindMonthlyUsageForUser("find-NodeUsage-for-user-month");

		private final String queryName;

		private QueryName(String queryName) {
			this.queryName = queryName;
		}

		/**
		 * Get the query name.
		 * 
		 * @return the query name
		 */
		public String getQueryName() {
			return queryName;
		}
	}

	/**
	 * Constructor.
	 */
	public MyBatisNodeUsageDao() {
		super(NodeUsage.class, Long.class);
	}

	@Override
	public EffectiveNodeUsageTiers effectiveNodeUsageTiers(LocalDate date) {
		if ( date == null ) {
			date = LocalDate.now();
		}
		List<EffectiveNodeUsageTier> results = selectList(
				QueryName.FindEffectiveUsageTierForDate.getQueryName(), date, null, null);
		if ( results == null ) {
			return null;
		}
		List<NodeUsageTier> tierList = results.stream().map(e -> e.getTier()).collect(toList());
		return new EffectiveNodeUsageTiers(results.get(0).getDate(), new NodeUsageTiers(tierList));
	}

	@Override
	public List<NodeUsage> findMonthlyUsageForUser(Long userId, LocalDate month) {
		if ( userId == null ) {
			throw new IllegalArgumentException("The userId argument must be provided.");
		}
		if ( month == null ) {
			throw new IllegalArgumentException("The month argument must be provided.");
		}
		Map<String, Object> params = new LinkedHashMap<>(2);
		params.put("userId", userId);
		params.put("startDate", month);
		params.put("endDate", month.plusMonths(1));
		return selectList(QueryName.FindMonthlyUsageForUser.getQueryName(), params, null, null);
	}

}
