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

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType.Usage;
import static net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem.newItem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import net.solarnetwork.central.user.billing.domain.InvoiceItem;
import net.solarnetwork.central.user.billing.domain.InvoiceMatch;
import net.solarnetwork.central.user.billing.snf.dao.AccountDao;
import net.solarnetwork.central.user.billing.snf.dao.NodeUsageDao;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceDao;
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceItemDao;
import net.solarnetwork.central.user.billing.snf.dao.TaxCodeDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceImpl;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceItemImpl;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType;
import net.solarnetwork.central.user.billing.snf.domain.NamedCost;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceFilter;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;
import net.solarnetwork.central.user.billing.snf.domain.TaxCode;
import net.solarnetwork.central.user.billing.snf.domain.TaxCodeFilter;
import net.solarnetwork.central.user.billing.snf.domain.UsageInfo;
import net.solarnetwork.central.user.billing.support.BasicBillingSystemInfo;
import net.solarnetwork.central.user.billing.support.LocalizedInvoiceItemUsageRecord;
import net.solarnetwork.central.user.domain.UserLongPK;
import net.solarnetwork.util.OptionalService;

/**
 * {@link BillingSystem} implementation for SolarNetwork Foundation.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfBillingSystem implements BillingSystem, SnfInvoicingSystem, SnfTaxCodeResolver {

	/** The {@literal accounting} billing data value for SNF. */
	public static final String ACCOUNTING_SYSTEM_KEY = "snf";

	private final AccountDao accountDao;
	private final SnfInvoiceDao invoiceDao;
	private final SnfInvoiceItemDao invoiceItemDao;
	private final NodeUsageDao usageDao;
	private final MessageSource messageSource;
	private final TaxCodeDao taxCodeDao;
	private OptionalService<SnfTaxCodeResolver> taxCodeResolver;
	private String datumPropertiesInKey = NodeUsage.DATUM_PROPS_IN_KEY;
	private String datumOutKey = NodeUsage.DATUM_OUT_KEY;
	private String datumDaysStoredKey = NodeUsage.DATUM_DAYS_STORED_KEY;

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
	 * @param taxCodeDAO
	 *        the tax code DAO
	 * @param messageSource
	 *        the message source
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SnfBillingSystem(AccountDao accountDao, SnfInvoiceDao invoiceDao,
			SnfInvoiceItemDao invoiceItemDao, NodeUsageDao usageDao, TaxCodeDao taxCodeDao,
			MessageSource messageSource) {
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
		if ( taxCodeDao == null ) {
			throw new IllegalArgumentException("The taxCodeDao argument must be provided.");
		}
		this.taxCodeDao = taxCodeDao;
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

		List<InvoiceItem> invoiceItems = null;
		if ( locale != null ) {
			invoiceItems = invoice.getItems().stream().map(e -> {
				String desc = messageSource.getMessage(e.getKey(), null, null, locale);
				UsageInfo usageInfo = e.getUsageInfo();
				String unitTypeDesc = messageSource.getMessage(usageInfo.getUnitType(), null, null,
						locale);
				LocalizedInvoiceItemUsageRecord locInfo = new LocalizedInvoiceItemUsageRecord(usageInfo,
						locale, unitTypeDesc);
				return new net.solarnetwork.central.user.billing.support.LocalizedInvoiceItem(
						new InvoiceItemImpl(invoice, e, singletonList(locInfo)), locale, desc);

			}).collect(toList());
		} else {
			invoiceItems = invoice.getItems().stream().map(e -> {
				return new InvoiceItemImpl(invoice, e);
			}).collect(toList());
		}

		InvoiceImpl result = new InvoiceImpl(invoice, invoiceItems);

		// TODO Auto-generated method stub
		return result;
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

	/**
	 * Resolve tax codes for a given invoice.
	 * 
	 * <p>
	 * This implementation creates tax zone names out of the invoice's address,
	 * with the following patterns:
	 * </p>
	 * 
	 * <ol>
	 * <li><code>country</code> via {@link Address#getCountry()} (required)</li>
	 * <li><code>country.state</code> via {@link Address#getCountry()} and
	 * {@link Address#getStateOrProvince()} (only if state available)</li>
	 * </ol>
	 * 
	 * <p>
	 * The tax date is resolved as the invoice's start date, or the current date
	 * if that is not available.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public TaxCodeFilter taxCodeFilterForInvoice(SnfInvoice invoice) {
		SnfTaxCodeResolver service = OptionalService.service(taxCodeResolver);
		if ( service != null ) {
			return service.taxCodeFilterForInvoice(invoice);
		}
		if ( invoice == null ) {
			throw new IllegalArgumentException("The invoice argument must be provided.");
		}
		Address addr = invoice.getAddress();
		if ( addr == null ) {
			throw new IllegalArgumentException("The invoice must provide an address.");
		}
		if ( addr.getCountry() == null || addr.getCountry().trim().isEmpty() ) {
			throw new IllegalArgumentException("The address must provide a country.");
		}
		List<String> zones = new ArrayList<>(2);
		zones.add(addr.getCountry());
		if ( addr.getStateOrProvince() != null && !addr.getStateOrProvince().trim().isEmpty() ) {
			zones.add(String.format("%s.%s", addr.getCountry(), addr.getStateOrProvince()));
		}

		ZoneId tz = invoice.getTimeZone();
		if ( tz == null ) {
			throw new IllegalArgumentException("The invoice must provide a time zone.");
		}

		TaxCodeFilter filter = new TaxCodeFilter();
		filter.setZones(zones.toArray(new String[zones.size()]));

		LocalDate date = invoice.getStartDate();
		if ( date == null ) {
			date = LocalDate.now();
		}
		filter.setDate(date.atStartOfDay(tz).toInstant());
		return filter;
	}

	public static Map<String, Object> usageMetadata(Long nodeId, Map<String, UsageInfo> usageData,
			Map<String, List<NamedCost>> tierData, String tierKey) {
		Map<String, Object> result = new LinkedHashMap<>(2);
		result.put(SnfInvoiceItem.META_NODE_ID, nodeId);
		if ( usageData.containsKey(tierKey) ) {
			result.put(SnfInvoiceItem.META_USAGE, usageData.get(tierKey).toMetadata());
		}
		if ( tierData.containsKey(tierKey) ) {
			List<Map<String, Object>> tierMeta = tierData.get(tierKey).stream().map(e -> e.toMetadata())
					.collect(toList());
			result.put(SnfInvoiceItem.META_TIER_BREAKDOWN, tierMeta);
		}
		return result;
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
		List<NodeUsage> usages = usageDao.findUsageForUser(userId, startDate, endDate);
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

		// for dryRun support, we generate a negative invoice ID based on current time
		final UserLongPK invoiceId = (dryRun ? new UserLongPK(userId, -System.currentTimeMillis())
				: invoiceDao.save(invoice));
		invoice.getId().setId(invoiceId.getId()); // for return

		List<SnfInvoiceItem> items = new ArrayList<>(usages.size());

		Collections.sort(usages, NodeUsage.SORT_BY_NODE_ID);
		for ( NodeUsage usage : usages ) {
			if ( usage.getTotalCost().compareTo(BigDecimal.ZERO) < 1 ) {
				// no cost for this node
				log.debug("No usage cost for node {} invoice date {}", usage.getId(), startDate);
				continue;
			}
			final Map<String, List<NamedCost>> tiersBreakdown = usage.getTiersCostBreakdown();
			final Map<String, UsageInfo> usageInfo = usage.getUsageInfo();
			if ( usage.getDatumPropertiesIn().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumPropertiesInKey,
						new BigDecimal(usage.getDatumPropertiesIn()), usage.getDatumPropertiesInCost());
				item.setMetadata(usageMetadata(usage.getId(), usageInfo, tiersBreakdown,
						NodeUsage.DATUM_PROPS_IN_KEY));
				if ( !dryRun ) {
					invoiceItemDao.save(item);
				}
				items.add(item);
			}
			if ( usage.getDatumOut().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumOutKey,
						new BigDecimal(usage.getDatumOut()), usage.getDatumOutCost());
				item.setMetadata(usageMetadata(usage.getId(), usageInfo, tiersBreakdown,
						NodeUsage.DATUM_OUT_KEY));
				if ( !dryRun ) {
					invoiceItemDao.save(item);
				}
				items.add(item);
			}
			if ( usage.getDatumDaysStored().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumDaysStoredKey,
						new BigDecimal(usage.getDatumDaysStored()), usage.getDatumDaysStoredCost());
				item.setMetadata(usageMetadata(usage.getId(), usageInfo, tiersBreakdown,
						NodeUsage.DATUM_DAYS_STORED_KEY));
				if ( !dryRun ) {
					invoiceItemDao.save(item);
				}
				items.add(item);
			}
		}

		invoice.setItems(new LinkedHashSet<>(items));

		List<SnfInvoiceItem> taxItems = computeInvoiceTaxItems(invoice);
		for ( SnfInvoiceItem taxItem : taxItems ) {
			if ( !dryRun ) {
				invoiceItemDao.save(taxItem);
			}
			invoice.getItems().add(taxItem);
		}

		log.info("Generated invoice for user {} for date {}: {}", userId, startDate, invoice);

		return invoice;
	}

	/**
	 * Compute the set of tax items for a given invoice.
	 * 
	 * <p>
	 * Existing tax items are ignored in the given invoice. The invoice is not
	 * mutated in any way.
	 * </p>
	 * 
	 * @param invoice
	 *        the invoice to compute tax items for
	 * @return the list of tax items, never {@literal null}
	 */
	public List<SnfInvoiceItem> computeInvoiceTaxItems(SnfInvoice invoice) {
		SnfTaxCodeResolver taxResolver = OptionalService.service(taxCodeResolver, this);
		TaxCodeFilter taxFilter = taxResolver.taxCodeFilterForInvoice(invoice);
		List<SnfInvoiceItem> taxItems = new ArrayList<>(8);
		if ( taxFilter != null ) {
			net.solarnetwork.dao.FilterResults<TaxCode, Long> taxes = taxCodeDao.findFiltered(taxFilter,
					null, null, null);
			if ( taxes != null && taxes.getReturnedResultCount() > 0 ) {
				Map<String, BigDecimal> taxAmounts = new LinkedHashMap<>(taxes.getReturnedResultCount());
				for ( SnfInvoiceItem item : invoice.getItems() ) {
					final InvoiceItemType itemType = item.getItemType();
					if ( itemType == InvoiceItemType.Tax ) {
						continue;
					}
					final String itemKey = item.getKey();
					final BigDecimal itemAmount = item.getAmount();
					if ( itemKey == null || itemAmount == null ) {
						continue;
					}
					for ( TaxCode tax : taxes ) {
						final String taxCode = tax.getCode();
						final BigDecimal taxRate = tax.getRate();
						if ( taxCode == null || taxRate == null ) {
							continue;
						}
						if ( itemKey.equalsIgnoreCase(tax.getItemKey()) ) {
							taxAmounts.merge(taxCode, taxRate.multiply(itemAmount), BigDecimal::add);
						}
					}
				}
				for ( Map.Entry<String, BigDecimal> me : taxAmounts.entrySet() ) {

					SnfInvoiceItem taxItem = newItem(invoice, InvoiceItemType.Tax, me.getKey(),
							BigDecimal.ONE, me.getValue().setScale(2, RoundingMode.HALF_UP));
					taxItems.add(taxItem);
				}
			}
		}
		return taxItems;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean deliverInvoice(final UserLongPK invoiceId) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Get the item key for datum properties input usage.
	 * 
	 * @return the key; defaults to {@link NodeUsage#DATUM_PROPS_IN_KEY}
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
	 * @return the key; defaults to {@link NodeUsage#DATUM_OUT_KEY}
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
	 * @return the key; defaults to {@link NodeUsage#DATUM_DAYS_STORED_KEY}
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

	/**
	 * Get the optional tax code resolver service to use.
	 * 
	 * <p>
	 * If this property is not configured or the service is not available at
	 * runtime, this class will act as its own resolver.
	 * </p>
	 * 
	 * @return the service
	 */
	public OptionalService<SnfTaxCodeResolver> getTaxCodeResolver() {
		return taxCodeResolver;
	}

	/**
	 * Set the optional tax code resolver service to use.
	 * 
	 * @param taxCodeResolver
	 *        the service to set
	 */
	public void setTaxCodeResolver(OptionalService<SnfTaxCodeResolver> taxCodeResolver) {
		this.taxCodeResolver = taxCodeResolver;
	}

}
