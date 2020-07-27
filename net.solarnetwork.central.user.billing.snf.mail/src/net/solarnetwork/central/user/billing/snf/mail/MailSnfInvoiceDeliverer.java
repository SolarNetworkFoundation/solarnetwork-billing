/* ==================================================================
 * MailSnfInvoiceDeliverer.java - 26/07/2020 3:31:55 PM
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

package net.solarnetwork.central.user.billing.snf.mail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MimeTypeUtils;
import net.solarnetwork.central.domain.BaseStringIdentity;
import net.solarnetwork.central.mail.MailService;
import net.solarnetwork.central.mail.support.BasicMailAddress;
import net.solarnetwork.central.mail.support.SimpleMessageDataSource;
import net.solarnetwork.central.user.billing.snf.SnfInvoiceDeliverer;
import net.solarnetwork.central.user.billing.snf.SnfInvoicingSystem;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceImpl;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.domain.IdentifiableConfiguration;
import net.solarnetwork.domain.Result;

/**
 * Deliver invoices by mail.
 * 
 * @author matt
 * @version 1.0
 */
public class MailSnfInvoiceDeliverer extends BaseStringIdentity implements SnfInvoiceDeliverer {

	private static final long serialVersionUID = -2150380154034292953L;

	private final SnfInvoicingSystem invoicingSystem;
	private final MailService mailService;

	/**
	 * Constructor.
	 * 
	 * @param invoicingSystem
	 *        the invoicing system to render invoices with
	 * @param mailService
	 *        the mail service
	 * @param messageDao
	 *        the message DAO
	 * @param messageCache
	 *        the message cache
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public MailSnfInvoiceDeliverer(SnfInvoicingSystem invoicingSystem, MailService mailService) {
		super();
		setId(getClass().getName());
		if ( invoicingSystem == null ) {
			throw new IllegalArgumentException("The invoicingSystem argument must be provided.");
		}
		this.invoicingSystem = invoicingSystem;
		if ( mailService == null ) {
			throw new IllegalArgumentException("The mailService argument must be provided.");
		}
		this.mailService = mailService;
	}

	@Override
	public CompletableFuture<Result<Object>> deliverInvoice(SnfInvoice invoice, Account account,
			IdentifiableConfiguration configuration) {
		// TODO: support kicking to other thread
		Locale locale = account.locale();

		final CompletableFuture<Result<Object>> result = new CompletableFuture<>();

		// TODO: allow output type to be specified via configuration; for now assume HTML
		Resource content = invoicingSystem.renderInvoice(invoice, MimeTypeUtils.TEXT_HTML, locale);

		// TODO: allow rendering to attachment
		BasicMailAddress to = new BasicMailAddress(account.getAddress().getName(),
				account.getAddress().getEmail());

		// generate subject and pass invoice number and date as arguments
		InvoiceImpl invoiceImpl = new InvoiceImpl(invoice);
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
		String invoiceKey = invoiceImpl.getInvoiceNumber();
		String invoiceDate = formatter.format(invoice.getStartDate());
		Object[] subjectArgs = new Object[] { invoiceKey, invoiceDate };

		MessageSource messageSource = invoicingSystem.messageSourceForInvoice(invoice);
		String subject = messageSource.getMessage("invoice.mail.subject", subjectArgs,
				String.format("SolarNetwork invoice INV-%s (%s)", subjectArgs), locale);

		try {
			mailService.sendMail(to, new SimpleMessageDataSource(subject, FileCopyUtils
					.copyToString(new InputStreamReader(content.getInputStream(), "UTF-8"))));
		} catch ( IOException e ) {
			result.completeExceptionally(e);
		}

		result.complete(Result.result(null));
		return result;
	}

}
