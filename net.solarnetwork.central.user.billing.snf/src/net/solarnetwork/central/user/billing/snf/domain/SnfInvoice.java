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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.solarnetwork.central.user.billing.domain.Invoice;
import net.solarnetwork.central.user.dao.UserRelatedEntity;
import net.solarnetwork.central.user.domain.UserUuidPK;
import net.solarnetwork.dao.BasicEntity;
import net.solarnetwork.domain.Differentiable;

/**
 * SNF invoice entity.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfInvoice extends BasicEntity<UserUuidPK>
		implements UserRelatedEntity<UserUuidPK>, Differentiable<SnfInvoice> {

	private final Long accountId;
	private Address address;
	private LocalDate startDate;
	private LocalDate endDate;
	private String currencyCode;
	private Set<SnfInvoiceItem> items;

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
	 * @param id
	 *        the ID
	 * @param accountId
	 *        the account ID
	 * @param created
	 *        the creation date
	 */
	public SnfInvoice(UserUuidPK id, Long accountId, Instant created) {
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
		this(new UserUuidPK(userId, null), accountId, created);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the UUID ID
	 * @param userId
	 *        the user ID
	 * @param accountId
	 *        the account ID
	 * @param created
	 *        the creation date
	 */
	public SnfInvoice(UUID id, Long userId, Long accountId, Instant created) {
		this(new UserUuidPK(userId, id), accountId, created);
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
	 * Test if the properties of another entity are the same as in this
	 * instance.
	 * 
	 * <p>
	 * The {@code id} and {@code created} properties are not compared by this
	 * method. The {@code address} and {@code items} values are compared by
	 * primary key values only.
	 * </p>
	 * 
	 * @param other
	 *        the other entity to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(SnfInvoice other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		boolean same = Objects.equals(accountId, other.accountId)
				&& Objects.equals(currencyCode, other.currencyCode)
				&& Objects.equals(startDate, other.startDate)
				&& Objects.equals(endDate, other.endDate);
		// @formatter:on
		if ( !same ) {
			return false;
		}
		if ( address != other.address ) {
			if ( address == null || other.address == null ) {
				return false;
			}
			if ( !Objects.equals(address.getId(), other.address.getId()) ) {
				return false;
			}
		}
		if ( items == other.items ) {
			return true;
		}
		if ( getItemCount() != other.getItemCount() ) {
			return false;
		}
		Map<UUID, SnfInvoiceItem> otherItems = other.itemMap();
		for ( SnfInvoiceItem item : items ) {
			otherItems.remove(item.getId());
		}
		return otherItems.isEmpty();
	}

	@Override
	public boolean differsFrom(SnfInvoice other) {
		return !isSameAs(other);
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
	 * Get a map of invoice items using their ID as map keys.
	 * 
	 * @return the map
	 */
	public Map<UUID, SnfInvoiceItem> itemMap() {
		if ( items == null ) {
			return Collections.emptyMap();
		}
		return items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
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

	/**
	 * Get the number of invoice items.
	 * 
	 * @return the number of items
	 */
	public int getItemCount() {
		return (items != null ? items.size() : 0);
	}

	/**
	 * Get the invoice items.
	 * 
	 * @return the items
	 */
	public Set<SnfInvoiceItem> getItems() {
		return items;
	}

	/**
	 * Set the invoice items.
	 * 
	 * @param items
	 *        the items to set
	 */
	public void setItems(Set<SnfInvoiceItem> items) {
		this.items = items;
	}

}
