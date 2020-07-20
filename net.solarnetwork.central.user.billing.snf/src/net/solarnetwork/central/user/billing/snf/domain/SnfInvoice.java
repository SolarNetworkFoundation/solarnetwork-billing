/* ==================================================================
 * SnfInvoice.java - 20/07/2020 9:37:06 AM
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.solarnetwork.central.user.billing.domain.Invoice;
import net.solarnetwork.central.user.dao.UserRelatedEntity;
import net.solarnetwork.central.user.domain.UserUuidPK;
import net.solarnetwork.dao.BasicEntity;

/**
 * SNF invoice entity.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfInvoice extends BasicEntity<UserUuidPK> implements UserRelatedEntity<UserUuidPK> {

	private final Long accountId;
	private Address address;
	private LocalDate startDate;
	private LocalDate endDate;
	private String currencyCode;

	/**
	 * Default constructor.
	 * 
	 * @param accountId
	 *        the account ID
	 */
	public SnfInvoice(Long accountId) {
		super(new UserUuidPK(), Instant.now());
		this.accountId = accountId;
	}

	/**
	 * Constructor.
	 * 
	 * @param accountId
	 *        the account ID
	 * @param id
	 *        the ID
	 * @param created
	 *        the creation date
	 */
	public SnfInvoice(Long accountId, UserUuidPK id, Instant created) {
		super(id, created);
		this.accountId = accountId;
	}

	/**
	 * Constructor.
	 * 
	 * @param accountId
	 *        the account ID
	 * @param userId
	 *        the user ID
	 * @param created
	 *        the creation date
	 */
	public SnfInvoice(Long accountId, Long userId, Instant created) {
		this(accountId, new UserUuidPK(userId, null), created);
	}

	/**
	 * Constructor.
	 * 
	 * @param accountId
	 *        the account ID
	 * @param id
	 *        the UUID ID
	 * @param userId
	 *        the user ID
	 * @param created
	 *        the creation date
	 */
	public SnfInvoice(Long accountId, UUID id, Long userId, Instant created) {
		this(accountId, new UserUuidPK(userId, id), created);
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

	@Override
	public boolean hasId() {
		UserUuidPK id = getId();
		return (id != null && id.getId() != null && id.getUserId() != null);
	}

	@Override
	public Long getUserId() {
		final UserUuidPK id = getId();
		return id != null ? id.getUserId() : null;
	}

	/**
	 * Set the user ID.
	 * 
	 * @param userId
	 *        the user ID
	 */
	public void setUserId(Long userId) {
		final UserUuidPK id = getId();
		if ( id != null ) {
			id.setUserId(userId);
		}
	}

	/**
	 * Get a billing {@link Invoice} from this entity.
	 * 
	 * @return the invoice, never {@literal null}
	 */
	public Invoice toInvoice() {
		// TODO
		return null;
	}

	/**
	 * Get the account ID.
	 * 
	 * @return the account ID
	 */
	public Long getAccountId() {
		return accountId;
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
	 * Get the starting date.
	 * 
	 * @return the starting date (inclusive)
	 */
	public LocalDate getStartDate() {
		return startDate;
	}

	/**
	 * Set the starting date.
	 * 
	 * @param startDate
	 *        the starting date to set (inclusive)
	 */
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	/**
	 * Get the ending date.
	 * 
	 * @return the ending date (exclusive)
	 */
	public LocalDate getEndDate() {
		return endDate;
	}

	/**
	 * Set the ending date.
	 * 
	 * @param endDate
	 *        the ending date to set (exclusive)
	 */
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	/**
	 * Get the currency code.
	 * 
	 * @return the currency code
	 */
	public String getCurrencyCode() {
		return currencyCode;
	}

	/**
	 * Set the currency code.
	 * 
	 * @param currencyCode
	 *        the currency code to set
	 */
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

}
