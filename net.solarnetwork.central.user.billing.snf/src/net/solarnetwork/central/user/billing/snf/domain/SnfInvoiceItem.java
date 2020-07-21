/* ==================================================================
 * SnfInvoiceItem.java - 20/07/2020 9:39:36 AM
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.solarnetwork.central.user.billing.domain.InvoiceItem;
import net.solarnetwork.dao.BasicEntity;
import net.solarnetwork.domain.Differentiable;

/**
 * SNF invoice item entity.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfInvoiceItem extends BasicEntity<UUID> implements Differentiable<SnfInvoiceItem> {

	private final UUID invoiceId;
	private InvoiceItemType itemType;
	private BigDecimal amount;
	private BigDecimal quantity;
	private Map<String, Object> metadata;

	/**
	 * Create a new invoice item.
	 * 
	 * @param invoice
	 *        the invoice to associate this item with; it must already have a
	 *        valid ID defined
	 * @param type
	 *        the type
	 * @param quantity
	 *        the quantity
	 * @param amount
	 *        the amount
	 * @return the new item, never {@literal null}
	 */
	public static SnfInvoiceItem newItem(SnfInvoice invoice, InvoiceItemType type, BigDecimal quantity,
			BigDecimal amount) {
		return newItem(invoice.getId().getId(), type, quantity, amount);
	}

	/**
	 * Create a new invoice item.
	 * 
	 * @param invoiceId
	 *        the invoice item ID
	 * @param type
	 *        the type
	 * @param quantity
	 *        the quantity
	 * @param amount
	 *        the amount
	 * @return the new item, never {@literal null}
	 */
	public static SnfInvoiceItem newItem(UUID invoiceId, InvoiceItemType type, BigDecimal quantity,
			BigDecimal amount) {
		return newItem(invoiceId, type, quantity, amount, Instant.now(), null);
	}

	/**
	 * Create a new invoice item.
	 * 
	 * @param invoiceId
	 *        the invoice item ID
	 * @param type
	 *        the type
	 * @param quantity
	 *        the quantity
	 * @param amount
	 *        the amount
	 * @param date
	 *        the date
	 * @return the new item, never {@literal null}
	 */
	public static SnfInvoiceItem newItem(UUID invoiceId, InvoiceItemType type, BigDecimal quantity,
			BigDecimal amount, Instant date) {
		return newItem(invoiceId, type, quantity, amount, date, null);
	}

	/**
	 * Create a new invoice item.
	 * 
	 * @param invoiceId
	 *        the invoice item ID
	 * @param type
	 *        the type
	 * @param quantity
	 *        the quantity
	 * @param amount
	 *        the amount
	 * @param date
	 *        the date
	 * @param metadata
	 *        the metadata
	 * @return the new item, never {@literal null}
	 */
	public static SnfInvoiceItem newItem(UUID invoiceId, InvoiceItemType type, BigDecimal quantity,
			BigDecimal amount, Instant date, Map<String, Object> metadata) {
		SnfInvoiceItem item = new SnfInvoiceItem(UUID.randomUUID(), invoiceId, date);
		item.setItemType(type);
		item.setAmount(amount);
		item.setQuantity(quantity);
		item.setMetadata(metadata);
		return item;
	}

	/**
	 * Constructor.
	 * 
	 * @param invoiceId
	 *        the invoice ID
	 */
	public SnfInvoiceItem(UUID invoiceId) {
		super(null, Instant.now());
		this.invoiceId = invoiceId;
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the invoice item ID
	 * @param invoiceId
	 *        the invoice ID
	 * @param created
	 *        the creation date
	 */
	public SnfInvoiceItem(UUID id, UUID invoiceId, Instant created) {
		super(id, created);
		this.invoiceId = invoiceId;
	}

	/**
	 * Get a billing {@link InvoiceItem} from this entity.
	 * 
	 * @return the invoice item, never {@literal null}
	 */
	public InvoiceItem toInvoiceItem() {
		// TODO
		return null;
	}

	/**
	 * Test if the properties of another entity are the same as in this
	 * instance.
	 * 
	 * <p>
	 * The {@code id} and {@code created} properties are not compared by this
	 * method.
	 * </p>
	 * 
	 * @param other
	 *        the other entity to compare to
	 * @return {@literal true} if the properties of this instance are equal to
	 *         the other
	 */
	public boolean isSameAs(SnfInvoiceItem other) {
		if ( other == null ) {
			return false;
		}
		// @formatter:off
		return Objects.equals(invoiceId, other.invoiceId)
				&& Objects.equals(itemType, other.itemType)
				&& Objects.equals(quantity, other.quantity)
				&& Objects.equals(metadata, other.metadata);
		// @formatter:on
	}

	@Override
	public boolean differsFrom(SnfInvoiceItem other) {
		return !isSameAs(other);
	}

	/**
	 * Get the invoice ID.
	 * 
	 * @return the invoice ID
	 */
	public UUID getInvoiceId() {
		return invoiceId;
	}

	/**
	 * Get the item type.
	 * 
	 * @return the type
	 */
	public InvoiceItemType getItemType() {
		return itemType;
	}

	/**
	 * Set the item type.
	 * 
	 * @param itemType
	 *        the type to set
	 */
	public void setItemType(InvoiceItemType itemType) {
		this.itemType = itemType;
	}

	/**
	 * Get the amount.
	 * 
	 * <p>
	 * This value represents the total cost for the associated
	 * {@link #getQuantity()}. Thus the individual quantity cost is derived via
	 * {@code amount / quantity}. The {@link #getUnitQuantityAmount()} returns
	 * the individual quantity cost.
	 * </p>
	 * 
	 * @return the amount, never {@literal null}
	 */
	public BigDecimal getAmount() {
		return amount != null ? amount : BigDecimal.ZERO;
	}

	/**
	 * Set the amount.
	 * 
	 * @param amount
	 *        the amount to set
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Get the individual quantity cost.
	 * 
	 * <p>
	 * This returns {@code amount / quantity}. If {@code quantity} is
	 * {@literal 0} then {@code amount} is returned.
	 * </p>
	 * 
	 * @return the cost per individual quantity
	 */
	public BigDecimal getUnitQuantityAmount() {
		BigDecimal amount = getAmount();
		BigDecimal quantity = getQuantity();
		if ( quantity.compareTo(BigDecimal.ZERO) == 0 ) {
			return amount;
		}
		return amount.divide(quantity);
	}

	/**
	 * Get the quantity.
	 * 
	 * @return the quantity, never {@literal null}
	 */
	public BigDecimal getQuantity() {
		return quantity != null ? quantity : BigDecimal.ONE;
	}

	/**
	 * Set the quantity.
	 * 
	 * @param quantity
	 *        the quantity to set
	 */
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	/**
	 * The item metadata.
	 * 
	 * @return the metadata
	 */
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	/**
	 * The item metadata.
	 * 
	 * @param metadata
	 *        the metadata to set
	 */
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
