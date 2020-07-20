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
import java.util.UUID;
import net.solarnetwork.central.user.billing.domain.InvoiceItem;
import net.solarnetwork.dao.BasicEntity;

/**
 * SNF invoice item entity.
 * 
 * @author matt
 * @version 1.0
 */
public class SnfInvoiceItem extends BasicEntity<UUID> {

	private final UUID invoiceId;
	private InvoiceItemType itemType;
	private BigDecimal amount;
	private BigDecimal quantity;
	private Map<String, Object> metadata;

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
	 * @param invoiceId
	 *        the invoice ID
	 * @param id
	 *        the invoice item ID
	 * @param created
	 *        the creation date
	 */
	public SnfInvoiceItem(UUID invoiceId, UUID id, Instant created) {
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
	 * @return the amount
	 */
	public BigDecimal getAmount() {
		return amount;
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
	 * Get the quantity.
	 * 
	 * @return the quantity
	 */
	public BigDecimal getQuantity() {
		return quantity;
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
