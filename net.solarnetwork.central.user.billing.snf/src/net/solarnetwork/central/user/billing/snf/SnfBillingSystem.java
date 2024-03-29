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

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType.Usage;
import static net.solarnetwork.central.user.billing.snf.domain.NodeUsages.DATUM_DAYS_STORED_KEY;
import static net.solarnetwork.central.user.billing.snf.domain.NodeUsages.DATUM_OUT_KEY;
import static net.solarnetwork.central.user.billing.snf.domain.NodeUsages.DATUM_PROPS_IN_KEY;
import static net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem.DEFAULT_ITEM_ORDER;
import static net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem.META_AVAILABLE_CREDIT;
import static net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem.newItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;
import javax.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import net.solarnetwork.central.RepeatableTaskException;
import net.solarnetwork.central.dao.VersionedMessageDao;
import net.solarnetwork.central.dao.VersionedMessageDao.VersionedMessages;
import net.solarnetwork.central.domain.FilterResults;
import net.solarnetwork.central.domain.SortDescriptor;
import net.solarnetwork.central.security.AuthorizationException;
import net.solarnetwork.central.security.AuthorizationException.Reason;
import net.solarnetwork.central.support.BasicFilterResults;
import net.solarnetwork.central.support.VersionedMessageDaoMessageSource;
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
import net.solarnetwork.central.user.billing.snf.dao.SnfInvoiceNodeUsageDao;
import net.solarnetwork.central.user.billing.snf.dao.TaxCodeDao;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.AccountBalance;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceImpl;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceItemImpl;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType;
import net.solarnetwork.central.user.billing.snf.domain.NamedCost;
import net.solarnetwork.central.user.billing.snf.domain.NodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceFilter;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceNodeUsage;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoicingOptions;
import net.solarnetwork.central.user.billing.snf.domain.TaxCode;
import net.solarnetwork.central.user.billing.snf.domain.TaxCodeFilter;
import net.solarnetwork.central.user.billing.snf.domain.UsageInfo;
import net.solarnetwork.central.user.billing.snf.util.SnfBillingUtils;
import net.solarnetwork.central.user.billing.support.BasicBillingSystemInfo;
import net.solarnetwork.central.user.billing.support.LocalizedInvoice;
import net.solarnetwork.central.user.billing.support.LocalizedInvoiceItemUsageRecord;
import net.solarnetwork.central.user.domain.UserLongPK;
import net.solarnetwork.domain.Result;
import net.solarnetwork.javax.money.MoneyUtils;
import net.solarnetwork.support.TemplateRenderer;
import net.solarnetwork.util.OptionalService;
import net.solarnetwork.util.OptionalServiceCollection;

/**
 * {@link BillingSystem} implementation for SolarNetwork Foundation.
 * 
 * @author matt
 * @version 1.1
 */
public class SnfBillingSystem implements BillingSystem, SnfInvoicingSystem, SnfTaxCodeResolver {

	/** The {@literal accounting} billing data value for SNF. */
	public static final String ACCOUNTING_SYSTEM_KEY = "snf";

	/** The message bundle name to use for versioned messages. */
	public static final String MESSAGE_BUNDLE_NAME = "snf.billing";

	/** The message bundle name to use for global versioned messages. */
	public static final String GLOBAL_MESSAGE_BUNDLE_NAME = "snf.global";

	/** The default {@code deliveryTimeoutSecs} property value. */
	public static final int DEFAULT_DELIVERY_TIMEOUT = 60;

	/** The invoice ID used for dry-run (draft) invoice generation. */
	public static final Long DRAFT_INVOICE_ID = Long.valueOf(Invoice.DRAFT_INVOICE_ID);

	/** The invoice number used for dry-run (draft) invoice generation. */
	public static final String DRAFT_INVOICE_NUMBER = SnfBillingUtils.invoiceNumForId(DRAFT_INVOICE_ID);

	private static final String[] MESSAGE_BUNDLE_NAMES = new String[] { GLOBAL_MESSAGE_BUNDLE_NAME,
			MESSAGE_BUNDLE_NAME };

	private final AccountDao accountDao;
	private final SnfInvoiceDao invoiceDao;
	private final SnfInvoiceItemDao invoiceItemDao;
	private final SnfInvoiceNodeUsageDao invoiceNodeUsageDao;
	private final NodeUsageDao usageDao;
	private final TaxCodeDao taxCodeDao;
	private final VersionedMessageDao messageDao;
	private Cache<String, VersionedMessages> messageCache;
	private OptionalService<SnfTaxCodeResolver> taxCodeResolver;
	private String datumPropertiesInKey = DATUM_PROPS_IN_KEY;
	private String datumOutKey = DATUM_OUT_KEY;
	private String datumDaysStoredKey = DATUM_DAYS_STORED_KEY;
	private String accountCreditKey = AccountBalance.ACCOUNT_CREDIT_KEY;
	private OptionalServiceCollection<SnfInvoiceDeliverer> deliveryServices;
	private OptionalServiceCollection<SnfInvoiceRendererResolver> rendererResolvers;
	private int deliveryTimeoutSecs = DEFAULT_DELIVERY_TIMEOUT;

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
	 * @param invoiceNodeUsageDao
	 *        the invoice node usage DAO
	 * @param usageDao
	 *        the usage DAO
	 * @param taxCodeDao
	 *        the tax code DAO
	 * @param messageDao
	 *        the message DAO
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public SnfBillingSystem(AccountDao accountDao, SnfInvoiceDao invoiceDao,
			SnfInvoiceItemDao invoiceItemDao, SnfInvoiceNodeUsageDao invoiceNodeUsageDao,
			NodeUsageDao usageDao, TaxCodeDao taxCodeDao, VersionedMessageDao messageDao) {
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
		if ( invoiceNodeUsageDao == null ) {
			throw new IllegalArgumentException("The invoiceNodeUsageDao argument must be provided.");
		}
		this.invoiceNodeUsageDao = invoiceNodeUsageDao;
		if ( usageDao == null ) {
			throw new IllegalArgumentException("The usageDao argument must be provided.");
		}
		this.usageDao = usageDao;
		if ( taxCodeDao == null ) {
			throw new IllegalArgumentException("The taxCodeDao argument must be provided.");
		}
		this.taxCodeDao = taxCodeDao;
		if ( messageDao == null ) {
			throw new IllegalArgumentException("The messageDao argument must be provided.");
		}
		this.messageDao = messageDao;
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
		// get account
		Account account = accountDao.getForUser(filter.getUserId());
		if ( account == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, filter.getUserId());
		}
		SnfInvoiceFilter invoiceFilter = SnfInvoiceFilter.forAccount(account);
		if ( filter.getUnpaid() != null ) {
			invoiceFilter.setUnpaidOnly(filter.getUnpaid());
		}
		net.solarnetwork.dao.FilterResults<SnfInvoice, UserLongPK> results = invoiceDao
				.findFiltered(invoiceFilter, SnfInvoiceDao.SORT_BY_INVOICE_DATE_DESCENDING, offset, max);
		List<InvoiceMatch> matches = StreamSupport.stream(results.spliterator(), false)
				.map(InvoiceImpl::new).collect(toList());
		return new BasicFilterResults<>(matches, results.getTotalResults(), results.getStartingOffset(),
				results.getReturnedResultCount());
	}

	private SnfInvoice getSnfInvoice(Long userId, String invoiceId) {
		final UserLongPK id = new UserLongPK(userId, Long.valueOf(invoiceId));
		final SnfInvoice invoice = invoiceDao.get(id);
		if ( invoice == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, invoiceId);
		}
		return invoice;
	}

	@Override
	public MessageSource messageSourceForInvoice(SnfInvoice invoice) {
		final Instant version = invoice.getStartDate().atStartOfDay(invoice.getTimeZone()).toInstant();
		return new VersionedMessageDaoMessageSource(messageDao, MESSAGE_BUNDLE_NAMES, version,
				messageCache);
	}

	private InvoiceImpl invoiceForSnfInvoice(SnfInvoice invoice, MessageSource messageSource,
			Locale locale) {
		List<InvoiceItem> invoiceItems = null;
		if ( locale != null ) {
			invoiceItems = invoice.getItems().stream().sorted(DEFAULT_ITEM_ORDER).map(e -> {
				String desc = messageSource.getMessage(e.getKey() + ".item", null, null, locale);
				InvoiceItemImpl item;
				UsageInfo usageInfo = e.getUsageInfo();
				if ( usageInfo != null ) {
					List<net.solarnetwork.central.user.billing.domain.NamedCost> tiers = usageInfo
							.getUsageTiers();
					String[] tierDescriptions = null;
					if ( tiers != null && !tiers.isEmpty() ) {
						tierDescriptions = new String[tiers.size()];
						for ( int i = 0; i < tiers.size(); i++ ) {
							tierDescriptions[i] = String.format("%s %d",
									messageSource.getMessage("invoiceItemTier", null, null, locale),
									i + 1);
						}
					}
					String unitTypeDesc = messageSource.getMessage(usageInfo.getUnitType() + ".unit",
							null, null, locale);
					LocalizedInvoiceItemUsageRecord locInfo = new LocalizedInvoiceItemUsageRecord(
							usageInfo, locale, unitTypeDesc, invoice.getCurrencyCode(),
							tierDescriptions);
					item = new InvoiceItemImpl(invoice, e, singletonList(locInfo));
				} else {
					if ( e.getItemType() == InvoiceItemType.Credit && e.getMetadata() != null
							&& e.getMetadata().containsKey(META_AVAILABLE_CREDIT) ) {
						Object availCreditVal = e.getMetadata().get(META_AVAILABLE_CREDIT);
						BigDecimal availCredit = (availCreditVal instanceof BigDecimal
								? (BigDecimal) availCreditVal
								: new BigDecimal(availCreditVal.toString()));
						e.getMetadata().put("localizedAvailableCredit",
								MoneyUtils.formattedMoneyAmountFormatWithSymbolCurrencyStyle(locale,
										invoice.getCurrencyCode(), availCredit));
					}
					item = new InvoiceItemImpl(invoice, e);
				}
				return new net.solarnetwork.central.user.billing.support.LocalizedInvoiceItem(item,
						locale, desc);

			}).collect(toList());
		} else {
			invoiceItems = invoice.getItems().stream().sorted(DEFAULT_ITEM_ORDER).map(e -> {
				return new InvoiceItemImpl(invoice, e);
			}).collect(toList());
		}

		return new InvoiceImpl(invoice, invoiceItems);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Invoice getInvoice(Long userId, String invoiceId, Locale locale) {
		final SnfInvoice invoice = getSnfInvoice(userId, invoiceId);
		final MessageSource messageSource = messageSourceForInvoice(invoice);
		return invoiceForSnfInvoice(invoice, messageSource, locale);
	}

	private TemplateRenderer renderer(SnfInvoice invoice, MimeType mimeType, Locale locale) {
		for ( SnfInvoiceRendererResolver resolver : rendererResolvers.services() ) {
			TemplateRenderer r = resolver.rendererForInvoice(invoice, mimeType, locale);
			if ( r != null ) {
				return r;
			}
		}
		String msg = String.format("MIME %s not supported for invoice rendering.", mimeType);
		throw new IllegalArgumentException(msg);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Resource renderInvoice(Long userId, String invoiceId, MimeType outputType, Locale locale) {
		SnfInvoice invoice = getSnfInvoice(userId, invoiceId);
		return renderInvoice(invoice, outputType, locale);
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	@Override
	public Resource renderInvoice(SnfInvoice invoice, MimeType outputType, Locale locale) {
		TemplateRenderer renderer = renderer(invoice, outputType, locale);
		VersionedMessageDaoMessageSource messageSource = (VersionedMessageDaoMessageSource) messageSourceForInvoice(
				invoice);
		LocalizedInvoice localizedInvoice = new LocalizedInvoice(
				invoiceForSnfInvoice(invoice, messageSource, locale), locale);
		Properties messages = messageSource.propertiesForLocale(locale);
		Map<String, Object> parameters = new LinkedHashMap<>(4);
		parameters.put("invoice", localizedInvoice);
		parameters.put("address", invoice.getAddress());
		parameters.put("messages", messages);
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(4096)) {
			renderer.render(locale, outputType, parameters, out);
			return invoiceResource(out.toByteArray(), invoice, messageSource, outputType, locale);
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	private Resource invoiceResource(byte[] data, SnfInvoice invoice, MessageSource messageSource,
			MimeType outputType, Locale locale) {
		String extension = ".txt";
		if ( outputType.isCompatibleWith(MimeType.valueOf("application/pdf")) ) {
			extension = ".pdf";
		} else {
			extension = "." + outputType.getSubtype();
		}
		Object[] filenameArgs = new Object[] {
				DRAFT_INVOICE_ID.equals(invoice.getId().getId())
						? messageSource.getMessage("draftInvoice", null, "DRAFT", locale)
						: SnfBillingUtils.invoiceNumForId(invoice.getId().getId()),
				YearMonth.from(invoice.getStartDate()).toString(), extension };
		String filename = messageSource.getMessage("invoice.filename", filenameArgs,
				"SolarNetwork Invoice {0} - {1}{2}", locale);
		return new ByteArrayResource(data) {

			@Override
			public String getFilename() {
				return filename;
			}

		};
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
		filter.setIgnoreCreditOnly(true);
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

	public static Map<String, Object> usageMetadata(Map<String, UsageInfo> usageData,
			Map<String, List<NamedCost>> tierData, String tierKey) {
		Map<String, Object> result = new LinkedHashMap<>(2);
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

	private SnfInvoice createPreviewInvoice(Long userId,
			net.solarnetwork.central.user.billing.domain.InvoiceGenerationOptions options,
			Locale locale) {
		Account account = accountDao.getForUser(userId);
		if ( account == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, userId);
		}

		LocalDate start;
		LocalDate end;
		if ( options != null && options.getMonth() != null ) {
			start = options.getMonth().atDay(1);
			end = start.plusMonths(1);
		} else {
			// find current month in account's time zone
			ZoneId zone = account.getTimeZone();
			if ( zone == null ) {
				zone = ZoneId.systemDefault();
			}
			start = LocalDate.now(zone).withDayOfMonth(1);
			end = start.plusMonths(1);
		}

		log.debug("Generating preview invoice for account {} (user {}) month {}", account.getId(),
				account.getUserId(), start);

		SnfInvoicingOptions opts = new SnfInvoicingOptions(true,
				options != null ? options.isUseAccountCredit() : false);
		SnfInvoice invoice = generateInvoice(userId, start, end, opts);
		return invoice;
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	@Override
	public Invoice getPreviewInvoice(Long userId,
			net.solarnetwork.central.user.billing.domain.InvoiceGenerationOptions options,
			Locale locale) {
		SnfInvoice invoice = createPreviewInvoice(userId, options, locale);
		final MessageSource messageSource = messageSourceForInvoice(invoice);
		return invoiceForSnfInvoice(invoice, messageSource, locale);
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	@Override
	public Resource previewInvoice(Long userId,
			net.solarnetwork.central.user.billing.domain.InvoiceGenerationOptions options,
			MimeType outputType, Locale locale) {
		SnfInvoice invoice = createPreviewInvoice(userId, options, locale);
		return renderInvoice(invoice, outputType, locale);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public SnfInvoice generateInvoice(Long userId, LocalDate startDate, LocalDate endDate,
			SnfInvoicingSystem.InvoiceGenerationOptions options) {
		// get account
		Account account = accountDao.getForUser(userId);
		if ( account == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, userId);
		}

		final boolean dryRun = (options != null ? options.isDryRun() : false);
		final boolean useCredit = (options != null ? options.isUseAccountCredit()
				: dryRun ? false : true);

		// query for usage
		final List<NodeUsage> usages = usageDao.findUsageForAccount(userId, startDate, endDate);
		if ( usages == null || usages.isEmpty() ) {
			// no invoice necessary
			return null;
		}

		// check for no-cost usage, and remove
		for ( Iterator<NodeUsage> itr = usages.iterator(); itr.hasNext(); ) {
			NodeUsage usage = itr.next();
			if ( usage.getTotalCost().compareTo(BigDecimal.ZERO) == 0 ) {
				itr.remove();
			}
		}
		if ( usages.isEmpty() ) {
			// only zero cost usage, no invoice necessary
			return null;
		}

		// turn usage into invoice items
		final SnfInvoice invoice = new SnfInvoice(account.getId().getId(), userId, Instant.now());
		invoice.setAddress(account.getAddress());
		invoice.setCurrencyCode(account.getCurrencyCode());
		invoice.setStartDate(startDate);
		invoice.setEndDate(endDate);

		// query for node usage counts
		final List<NodeUsage> nodeUsages = usageDao.findNodeUsageForAccount(userId, startDate, endDate);

		// for dryRun support, we generate a negative invoice ID
		final UserLongPK invoiceId = (dryRun ? new UserLongPK(userId, DRAFT_INVOICE_ID)
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
				item.setMetadata(usageMetadata(usageInfo, tiersBreakdown, DATUM_PROPS_IN_KEY));
				if ( !dryRun ) {
					invoiceItemDao.save(item);
				}
				items.add(item);
			}
			if ( usage.getDatumOut().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumOutKey,
						new BigDecimal(usage.getDatumOut()), usage.getDatumOutCost());
				item.setMetadata(usageMetadata(usageInfo, tiersBreakdown, DATUM_OUT_KEY));
				if ( !dryRun ) {
					invoiceItemDao.save(item);
				}
				items.add(item);
			}
			if ( usage.getDatumDaysStored().compareTo(BigInteger.ZERO) > 0 ) {
				SnfInvoiceItem item = newItem(invoiceId.getId(), Usage, datumDaysStoredKey,
						new BigDecimal(usage.getDatumDaysStored()), usage.getDatumDaysStoredCost());
				item.setMetadata(usageMetadata(usageInfo, tiersBreakdown, DATUM_DAYS_STORED_KEY));
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

		// claim credit, if available
		if ( useCredit ) {
			BigDecimal invoiceTotal = invoice.getTotalAmount();
			if ( invoiceTotal.compareTo(BigDecimal.ZERO) > 0 ) {
				BigDecimal credit = accountDao.claimAccountBalanceCredit(invoice.getAccountId(),
						invoiceTotal);
				if ( credit.compareTo(BigDecimal.ZERO) > 0 ) {
					AccountBalance balance = accountDao.getBalanceForUser(invoice.getUserId());
					SnfInvoiceItem creditItem = SnfInvoiceItem.newItem(invoice, InvoiceItemType.Credit,
							accountCreditKey, BigDecimal.ONE, credit.negate());
					creditItem.setMetadata(Collections.singletonMap(META_AVAILABLE_CREDIT,
							balance.getAvailableCredit().toPlainString()));
					if ( !dryRun ) {
						invoiceItemDao.save(creditItem);
					}
					invoice.getItems().add(creditItem);
				}
			}
		}

		// populate node usages
		if ( nodeUsages != null ) {
			List<SnfInvoiceNodeUsage> invoiceNodeUsages = new ArrayList<>(nodeUsages.size());
			for ( NodeUsage nodeUsage : nodeUsages ) {
				SnfInvoiceNodeUsage u = new SnfInvoiceNodeUsage(invoiceId.getId(), nodeUsage.getId(),
						invoice.getCreated(), nodeUsage.getDatumPropertiesIn(), nodeUsage.getDatumOut(),
						nodeUsage.getDatumDaysStored());
				invoiceNodeUsages.add(u);
				if ( !dryRun ) {
					invoiceNodeUsageDao.save(u);
				}
			}
			invoice.setUsages(new LinkedHashSet<>(invoiceNodeUsages));
		} else {
			invoice.setUsages(Collections.emptySet());
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

	private SnfInvoiceDeliverer invoiceDeliverer(Long userId) {
		OptionalServiceCollection<SnfInvoiceDeliverer> services = getDeliveryServices();
		Iterable<SnfInvoiceDeliverer> iterable = (services != null ? services.services() : null);
		Iterator<SnfInvoiceDeliverer> itr = (iterable != null ? iterable.iterator() : null);
		return (itr != null && itr.hasNext() ? itr.next() : null);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public boolean deliverInvoice(final UserLongPK invoiceId) {
		// get account
		final Account account = accountDao.getForUser(invoiceId.getUserId());
		if ( account == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, invoiceId.getUserId());
		}

		final SnfInvoice invoice = invoiceDao.get(invoiceId);
		if ( invoice == null ) {
			throw new AuthorizationException(Reason.UNKNOWN_OBJECT, invoiceId);
		}

		SnfInvoiceDeliverer deliverer = invoiceDeliverer(invoice.getUserId());
		if ( deliverer == null ) {
			String msg = format("No invoice delivery service available to delivery invoice %d",
					invoiceId.getId());
			log.error(msg);
			throw new RepeatableTaskException(msg);
		}

		// TODO: support configuration for account
		try {
			final int maxSeconds = getDeliveryTimeoutSecs();
			CompletableFuture<Result<Object>> future = deliverer.deliverInvoice(invoice, account, null);
			Result<Object> result;
			if ( maxSeconds > 0 ) {
				result = future.get(maxSeconds, TimeUnit.SECONDS);
			} else {
				result = future.get();
			}
			return (result != null && result.getSuccess() != null && result.getSuccess().booleanValue());
		} catch ( TimeoutException e ) {
			throw new RepeatableTaskException("Tiemout delivering invoice", e);
		} catch ( Exception e ) {
			Throwable root = e;
			while ( root.getCause() != null ) {
				root = root.getCause();
			}
			if ( root instanceof IOException ) {
				throw new RepeatableTaskException("Communication error delivering invoice.", e);
			}
			if ( !(e instanceof RuntimeException) ) {
				e = new RuntimeException(e);
			}
			throw (RuntimeException) e;
		}
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
	 * Get the item key for account credit.
	 * 
	 * @return the key; defaults to {@link AccountBalance#ACCOUNT_CREDIT_KEY}
	 */
	public String getAccountCreditKey() {
		return accountCreditKey;
	}

	/**
	 * Set the item key for account credit.
	 * 
	 * @param accountCreditKey
	 *        the accountCreditKey to set
	 * @throws IllegalArgumentException
	 *         if the argument is {@literal null}
	 */
	public void setAccountCreditKey(String accountCreditKey) {
		if ( accountCreditKey == null ) {
			throw new IllegalArgumentException("The accountCreditKey argumust must not be null.");
		}
		this.accountCreditKey = accountCreditKey;
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

	/**
	 * Get the optional message cache.
	 * 
	 * @return the cache
	 */
	public Cache<String, VersionedMessages> getMessageCache() {
		return messageCache;
	}

	/**
	 * Set the optional message cache.
	 * 
	 * @param messageCache
	 *        the cache to set
	 */
	public void setMessageCache(Cache<String, VersionedMessages> messageCache) {
		this.messageCache = messageCache;
	}

	/**
	 * Get the invoice delivery services.
	 * 
	 * @return the services
	 */
	public OptionalServiceCollection<SnfInvoiceDeliverer> getDeliveryServices() {
		return deliveryServices;
	}

	/**
	 * Set the invoice delivery services.
	 * 
	 * @param deliveryServices
	 *        the services to set
	 */
	public void setDeliveryServices(OptionalServiceCollection<SnfInvoiceDeliverer> deliveryServices) {
		this.deliveryServices = deliveryServices;
	}

	/**
	 * Get the invoice renderer resolver services.
	 * 
	 * @return the services
	 */
	public OptionalServiceCollection<SnfInvoiceRendererResolver> getRendererResolvers() {
		return rendererResolvers;
	}

	/**
	 * Set the invoice renderer resolver services.
	 * 
	 * @param rendererResolvers
	 *        the services to set
	 */
	public void setRendererResolvers(
			OptionalServiceCollection<SnfInvoiceRendererResolver> rendererResolvers) {
		this.rendererResolvers = rendererResolvers;
	}

	/**
	 * Get the maximum amount of time to wait for invoice delivery to complete,
	 * in seconds.
	 * 
	 * @return the timeout, in seconds
	 */
	public int getDeliveryTimeoutSecs() {
		return deliveryTimeoutSecs;
	}

	/**
	 * Set the maximum amount of time to wait for invoice delivery to complete.
	 * 
	 * @param deliveryTimeoutSecs
	 *        the timeout to set, in seconds, or {@literal 0} for no timeout
	 */
	public void setDeliveryTimeoutSecs(int deliveryTimeoutSecs) {
		this.deliveryTimeoutSecs = deliveryTimeoutSecs;
	}

}
