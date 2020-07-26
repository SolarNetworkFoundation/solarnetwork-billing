/* ==================================================================
 * VersionedMessageSourceSnfInvoiceRendererResolver.java - 26/07/2020 5:05:00 PM
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

package net.solarnetwork.central.user.billing.snf.st4;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.cache.Cache;
import org.springframework.context.MessageSource;
import org.springframework.util.MimeType;
import net.solarnetwork.central.dao.VersionedMessageDao;
import net.solarnetwork.central.dao.VersionedMessageDao.VersionedMessages;
import net.solarnetwork.central.support.VersionedMessageDaoMessageSource;
import net.solarnetwork.central.user.billing.snf.SnfInvoiceRendererResolver;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.common.tmpl.st4.MessageSourceGroup;
import net.solarnetwork.common.tmpl.st4.ST4TemplateRenderer;
import net.solarnetwork.support.TemplateRenderer;

/**
 * {@link SnfInvoiceRendererResolver} that resolves {@link ST4TemplateRenderer}
 * renderers using a {@link MessageSourceGroup} for templates.
 * 
 * @author matt
 * @version 1.0
 */
public class VersionedMessageSourceSnfInvoiceRendererResolver implements SnfInvoiceRendererResolver {

	private final String[] bundleNames;
	private final String rootTemplateName;
	private final List<MimeType> mimeTypes;
	private final VersionedMessageDao messageDao;
	private final Cache<String, VersionedMessages> messageCache;
	private final ConcurrentMap<String, ST4TemplateRenderer> templateCache;

	/**
	 * Constructor.
	 * 
	 * @param bundleName
	 *        the message bundle name to use
	 * @param rootTemplateName
	 *        the root template name
	 * @param mimeType
	 *        the supported MIME type
	 * @param messageDao
	 *        the message DAO
	 * @param messageCache
	 *        the message cache
	 * @throws IllegalArgumentException
	 *         if any argument is {@literal null}
	 */
	public VersionedMessageSourceSnfInvoiceRendererResolver(String bundleName, String rootTemplateName,
			MimeType mimeType, VersionedMessageDao messageDao,
			Cache<String, VersionedMessages> messageCache) {
		super();
		if ( bundleName == null ) {
			throw new IllegalArgumentException("The bundleName argument must be provided.");
		}
		this.bundleNames = new String[] { bundleName };
		if ( rootTemplateName == null ) {
			throw new IllegalArgumentException("The rootTemplateName argument must be provided.");
		}
		this.rootTemplateName = rootTemplateName;
		if ( messageDao == null ) {
			throw new IllegalArgumentException("The messageDao argument must be provided.");
		}
		this.messageDao = messageDao;
		if ( messageCache == null ) {
			throw new IllegalArgumentException("The messageCache argument must be provided.");
		}
		this.messageCache = messageCache;
		if ( mimeType == null ) {
			throw new IllegalArgumentException("The mimeType argument must be provided.");
		}
		this.mimeTypes = Collections.singletonList(mimeType);
		this.templateCache = new ConcurrentHashMap<>(16, 0.9f, 1);
	}

	@Override
	public TemplateRenderer rendererForInvoice(SnfInvoice invoice, MimeType mimeType, Locale locale) {
		final Instant version = invoice.getStartDate().atStartOfDay(invoice.getTimeZone()).toInstant();
		MessageSource messageSource = new VersionedMessageDaoMessageSource(messageDao, bundleNames,
				version, messageCache);
		String templateVersion = messageSource.getMessage("version", null, "", locale);
		return templateCache.computeIfAbsent(templateVersion, k -> {
			return new ST4TemplateRenderer(bundleNames[0],
					new MessageSourceGroup(bundleNames[0], messageSource, '$', '$'), rootTemplateName,
					mimeTypes, ST4TemplateRenderer.UTF8);
		});
	}

}
