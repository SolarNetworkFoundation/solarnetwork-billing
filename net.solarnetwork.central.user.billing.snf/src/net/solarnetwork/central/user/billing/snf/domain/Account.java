/* ==================================================================
 * Account.java - 20/07/2020 11:14:01 AM
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

package net.solarnetwork.central.user.billing.snf.domain;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.solarnetwork.central.user.dao.UserRelatedEntity;
import net.solarnetwork.central.user.domain.UserLongPK;
import net.solarnetwork.dao.BasicEntity;

/**
 * Billing account entity.
 * 
 * @author matt
 * @version 1.0
 */
public class Account extends BasicEntity<UserLongPK> implements UserRelatedEntity<UserLongPK> {

	private Address address;
	private String currencyCode;
	private String locale;

	/**
	 * Default constructor.
	 */
	public Account() {
		super(new UserLongPK(), Instant.now());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the ID
	 * @param created
	 *        the creation date
	 */
	public Account(UserLongPK id, Instant created) {
		super(id, created);
	}

	/**
	 * Constructor.
	 * 
	 * @param userId
	 *        the user ID
	 * @param created
	 *        the creation date
	 */
	public Account(Long userId, Instant created) {
		this(new UserLongPK(userId, null), created);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the long ID
	 * @param userId
	 *        the user ID
	 * @param created
	 *        the creation date
	 */
	public Account(Long id, Long userId, Instant created) {
		this(new UserLongPK(userId, id), created);
	}

	@Override
	public boolean hasId() {
		UserLongPK id = getId();
		return (id != null && id.getId() != null && id.getUserId() != null);
	}

	@Override
	public Long getUserId() {
		final UserLongPK id = getId();
		return id != null ? id.getUserId() : null;
	}

	/**
	 * Set the user ID.
	 * 
	 * @param userId
	 *        the user ID
	 */
	public void setUserId(Long userId) {
		final UserLongPK id = getId();
		if ( id != null ) {
			id.setUserId(userId);
		}
	}

	/**
	 * Get the invoice time zone.
	 * 
	 * @return the time zone, or {@literal null} if not available
	 */
	@JsonIgnore
	public ZoneId getTimeZone() {
		Address addr = getAddress();
		if ( addr != null && addr.getTimeZoneId() != null ) {
			try {
				return ZoneId.of(addr.getTimeZoneId());
			} catch ( DateTimeException e ) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * Get the address.
	 * 
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * Set the address.
	 * 
	 * @param address
	 *        the address to set
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	/**
	 * Get the currency code.
	 * 
	 * @return the currencyCode
	 */
	public String getCurrencyCode() {
		return currencyCode;
	}

	/**
	 * Set the currency code.
	 * 
	 * @param currencyCode
	 *        the currencyCode to set
	 */
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	/**
	 * Get the locale.
	 * 
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * Set the locale.
	 * 
	 * @param locale
	 *        the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

}
