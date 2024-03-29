/* ==================================================================
 * SnfInvoiceNodeUsageDao.java - 28/05/2021 7:21:22 AM
 * 
 * Copyright 2021 SolarNetwork.net Dev Team
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

package net.solarnetwork.central.user.billing.snf.dao;

import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceNodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceRelatedPK;
import net.solarnetwork.dao.GenericDao;

/**
 * DAO API for {@link SnfInvoiceNodeUsage} entities.
 * 
 * @author matt
 * @version 1.0
 * @since 2.0
 */
public interface SnfInvoiceNodeUsageDao extends GenericDao<SnfInvoiceNodeUsage, SnfInvoiceRelatedPK> {

}
