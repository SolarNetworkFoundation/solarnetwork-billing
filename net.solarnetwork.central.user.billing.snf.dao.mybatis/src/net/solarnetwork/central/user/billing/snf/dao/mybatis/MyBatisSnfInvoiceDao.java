/* ==================================================================
 * MyBatisSnfInvoiceDao.java - 20/07/2020 9:47:42 AM
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

import java.util.List;
import net.solarnetwork.central.dao.mybatis.support.BaseMyBatisGenericDaoSupport;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceFilter;
import net.solarnetwork.central.user.domain.UserLongPK;
import net.solarnetwork.dao.BasicFilterResults;
import net.solarnetwork.dao.FilterResults;
import net.solarnetwork.domain.SortDescriptor;

/**
 * MyBatis implementation of {@link SnfInvoiceDao}.
 * 
 * @author matt
 * @version 1.0
 */
public class MyBatisSnfInvoiceDao extends BaseMyBatisGenericDaoSupport<SnfInvoice, UserLongPK>
		implements SnfInvoiceDao {

	/** Query name enumeration. */
	public enum QueryName {

		FindFiltered("find-SnfInvoice-for-filter");

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
	public MyBatisSnfInvoiceDao() {
		super(SnfInvoice.class, UserLongPK.class);
	}

	@Override
	public FilterResults<SnfInvoice, UserLongPK> findFiltered(SnfInvoiceFilter filter,
			List<SortDescriptor> sorts, Integer offset, Integer max) {
		List<SnfInvoice> results = selectList(QueryName.FindFiltered.getQueryName(), filter, offset,
				max);
		return new BasicFilterResults<>(results, null, offset, results.size());
	}

}
