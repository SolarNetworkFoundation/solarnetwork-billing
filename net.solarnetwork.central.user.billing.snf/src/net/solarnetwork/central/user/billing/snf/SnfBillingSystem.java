/* ==================================================================
 * SnfBillingSystem.java - 20/07/2020 9:01:05 AM
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

package net.solarnetwork.central.user.billing.snf;

import static net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType.Usage;
import static net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem.newItem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import net.solarnetwork.central.domain.FilterResults;
import net.solarnetwork.central.domain.SortDescriptor;
import net.solarnetwork.central.security.AuthorizationException;
import net.solarnetwork.central.security.AuthorizationException.Reason;
import net.solarnetwork.central.user.billing.biz.BillingSystem;
import net.solarnetwork.central.user.billing.domain.BillingSystemInfo;
import net.solarnetwork.central.user.billing.domain.Invoice;
import net.solarnetwork.central.user.billing.domain.InvoiceFilter;
import net.solarnetwork.central.user.billing.domain.InvoiceMatch;
import net.solarnetwork.central.user.billing.snf.dao.AccountDao;
import net.solarnetwork.central.user.billing.snf.dao.NodeUsageDao;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceItemDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceFilter;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;
import net.solarnetwork.central.user.billing.support.BasicBillingSystemInfo;
import net.solarnetwork.central.user.domain.UserLongPK;

/**
 * {@link BillingSystem} implementation for SolarNetwork Foundation.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfBillingSystem implements BillingSystem, SnfInvoicingSystem {

	/** The {@literal accounting} billing data value for SNF. */
	public static final String ACCOUNTING_SYSTEM_KEY = "snf";

	/** The default {@code nodeUsagePropertiesInKey} property value. */
	public static final String DEFAULT_NODE_USAGE_PROPS_IN_KEY = "datum-props-in";
	public static final String DEFAULT_NODE_USAGE_DATUM_OUT_KEY = "datum-out";
	public static final String DEFAULT_NODE_USAGE_DATUM_DAYS_STORED_KEY = "datum-days-stored";

	private final AccountDao accountDao;
	private final SnfInvoiceDao invoiceDao;
	private final SnfInvoiceItemDao invoiceItemDao;
	private final NodeUsageDao usageDao;
	private final MessageSource messageSource;
	private String datumPropertiesInKey = DEFAULT_NODE_USAGE_PROPS_IN_KEY;
	private String datumOutKey = DEFAULT_NODE_USAGE_DATUM_OUT_KEY;
	private String datumDaysStoredKey = DEFAULT_NODE_USAGE_DATUM_DAYS_STORED_KEY;

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 * 
	 * @param accountDao
	 *        the account DAO
	 * @param invoiceDao
	 *        the invoice DAO
	 * @param invoiceItemDao
	 *        the invoice item DAO
	 * @param usageDao
	 *        the usage DAO
	 * @param messageSource
	 *        the message source
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SnfBillingSystem(AccountDao accountDao, SnfInvoiceDao invoiceDao,
			SnfInvoiceItemDao invoiceItemDao, NodeUsageDao usageDao, MessageSource messageSource) {
		super();
		if ( accountDao == null ) {
			throw new IllegalArgumentException("The accountDao argument must be provided.");
		}
		this.accountDao = accountDao;
		if ( invoiceDao == null ) {
			throw new IllegalArgumentException("The invoiceDao argument must be provided.");
		}
		this.invoiceDao = invoiceDao;
		if ( invoiceItemDao == null ) {
			throw new IllegalArgumentException("The invoiceItemDao argument must be provided.");
		}
		this.invoiceItemDao = invoiceItemDao;
		if ( usageDao == null ) {
			throw new IllegalArgumentException("The usageDao argument must be provided.");
		}
		this.usageDao = usageDao;
		if ( messageSource == null ) {
			throw new IllegalArgumentException("The messageSource argument must be provided.");
		}
		this.messageSource = messageSource;
	}

	@Override
	public String getAccountingSystemKey() {
		return ACCOUNTING_SYSTEM_KEY;
	}

	@Override
	public boolean supportsAccountingSystemKey(String key) {
		return ACCOUNTING_SYSTEM_KEY.equals(key);
	}

	@Override
	public BillingSystemInfo getInfo(Locale locale) {
		return new BasicBillingSystemInfo(getAccountingSystemKey());
	}

	@Override
	public FilterResults<InvoiceMatch> findFilteredInvoices(InvoiceFilter filter,
			List<SortDescriptor> sortDescriptors, Integer offset, Integer max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Invoice getInvoice(Long userId, String invoiceId, Locale locale) {
		final UserLongPK id = new UserLongPK(userId, Long.valueOf(invoiceId));
		final SnfInvoice invoice = invoiceDao.get(id);
		if ( invoice == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, invoiceId);
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Resource renderInvoice(Long userId, String invoiceId, MimeType outputType, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Account accountForUser(Long userId) {
		return accountDao.getForUser(userId);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public SnfInvoice findLatestInvoiceForAccount(UserLongPK accountId) {
		SnfInvoiceFilter filter = SnfInvoiceFilter.forAccount(accountId.getId());
		net.solarnetwork.dao.FilterResults<SnfInvoice, UserLongPK> results = invoiceDao
				.findFiltered(filter, SnfInvoiceDao.SORT_BY_INVOICE_DATE_DESCENDING, 0, 1);
		Iterator<SnfInvoice> itr = (results != null ? results.iterator() : null);
		return (itr != null && itr.hasNext() ? itr.next() : null);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public SnfInvoice generateInvoice(Long userId, LocalDate startDate, LocalDate endDate,
			boolean dryRun) {
		// get account
		Account account = accountDao.getForUser(userId);
		if ( account == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, userId);
		}

		// query for usage
		List<NodeUsage> usages = usageDao.findMonthlyUsageForUser(userId, startDate);
		if ( usages == null || usages.isEmpty() ) {
			// no invoice necessary
			return null;
		}

		// turn usage into invoice items
		SnfInvoice invoice = new SnfInvoice(account.getId().getId(), userId, Instant.now());
		invoice.setAddress(account.getAddress());
		invoice.setCurrencyCode(account.getCurrencyCode());
		invoice.setStartDate(startDate);
		invoice.setEndDate(endDate);
		final UserLongPK invoiceId = invoiceDao.save(invoice);

		List<SnfInvoiceItem> items = new ArrayList<>(usages.size());

		Collections.sort(usages, NodeUsage.SORT_BY_NODE_ID);
		for ( NodeUsage usage : usages ) {
			if ( usage.getTotalCost().compareTo(BigDecimal.ZERO) < 1 ) {
				// no cost for this node
				log.debug("No usage cost for node {} invoice date {}", usage.getId(), startDate);
				continue;
			}
			if ( usage.getDatumPropertiesIn().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumPropertiesInKey,
						new BigDecimal(usage.getDatumPropertiesIn()), usage.getDatumPropertiesInCost());
				invoiceItemDao.save(item);
				items.add(item);
			}
			if ( usage.getDatumOut().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumOutKey,
						new BigDecimal(usage.getDatumOut()), usage.getDatumOutCost());
				invoiceItemDao.save(item);
				items.add(item);
			}
			if ( usage.getDatumDaysStored().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumDaysStoredKey,
						new BigDecimal(usage.getDatumDaysStored()), usage.getDatumDaysStoredCost());
				invoiceItemDao.save(item);
				items.add(item);
			}
		}

		invoice.setItems(new LinkedHashSet<>(items));
		return invoice;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean deliverInvoice(UUID invoiceId) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Get the item key for datum properties input usage.
	 * 
	 * @return the key; defaults to {@link #DEFAULT_NODE_USAGE_PROPS_IN_KEY}
	 */
	public String getDatumPropertiesInKey() {
		return datumPropertiesInKey;
	}

	/**
	 * Set the item key for datum properties input usage.
	 * 
	 * @param datumPropertiesInKey
	 *        the key to set
	 * @throws IllegalArgumentException
	 *         if the argument is {@literal null}
	 */
	public void setDatumPropertiesInKey(String datumPropertiesInKey) {
		if ( datumPropertiesInKey == null ) {
			throw new IllegalArgumentException("The datumPropertiesInKey argumust must not be null.");
		}
		this.datumPropertiesInKey = datumPropertiesInKey;
	}

	/**
	 * Get the item key for datum output usage.
	 * 
	 * @return the key; defaults to {@link #DEFAULT_NODE_USAGE_DATUM_OUT_KEY}
	 */
	public String getDatumOutKey() {
		return datumOutKey;
	}

	/**
	 * Set the item key for datum output usage.
	 * 
	 * @param datumOutKey
	 *        the key to set
	 * @throws IllegalArgumentException
	 *         if the argument is {@literal null}
	 */
	public void setDatumOutKey(String datumOutKey) {
		if ( datumOutKey == null ) {
			throw new IllegalArgumentException("The datumOutKey argumust must not be null.");
		}
		this.datumOutKey = datumOutKey;
	}

	/**
	 * Get the item key for datum days stored usage.
	 * 
	 * @return the key; defaults to
	 *         {@link #DEFAULT_NODE_USAGE_DATUM_DAYS_STORED_KEY}
	 */
	public String getDatumDaysStoredKey() {
		return datumDaysStoredKey;
	}

	/**
	 * Set the item key for datum days stored usage.
	 * 
	 * @param datumDaysStoredKey
	 *        the key to set
	 * @throws IllegalArgumentException
	 *         if the argument is {@literal null}
	 */
	public void setDatumDaysStoredKey(String datumDaysStoredKey) {
		if ( datumDaysStoredKey == null ) {
			throw new IllegalArgumentException("The datumDaysStoredKey argumust must not be null.");
		}
		this.datumDaysStoredKey = datumDaysStoredKey;
	}

}
