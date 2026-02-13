/*
 * Spark - The inventory management application
 * Copyright (C) 2026 Yegore Vlussove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.example.spark.order.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.util.Collection;

@Entity
@Table(name = "orders")
public class OrderEntity {

	private long id;

	private long accountId;

	private long timestamp;

	private Collection<LineItemEntity> lineItems;

	private OrderStatus orderStatus;

	private long version;

	public OrderEntity() { }

	public OrderEntity(
		long accountId, long timestamp, @Nonnull Collection<LineItemEntity> lineItems, @Nonnull OrderStatus orderStatus
	) {
		this();
		this.setAccountId(accountId);
		this.setTimestamp(timestamp);
		this.setLineItems(lineItems);
		this.setOrderStatus(orderStatus);
		this.setVersion(0);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@OneToMany(mappedBy = "orderEntity", cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER)
	public Collection<LineItemEntity> getLineItems() {
		return lineItems;
	}

	public void setLineItems(@Nonnull Collection<LineItemEntity> lineItems) {
		this.lineItems = lineItems;
	}

	@ManyToOne
	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(@Nonnull OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
