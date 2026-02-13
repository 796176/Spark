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

@Entity
@Table(name = "line_items")
@IdClass(LineItemEntity.LineItemEntityPK.class)
public class LineItemEntity {

	public record LineItemEntityPK(@Nonnull OrderEntity orderEntity, long itemId) { }

	private OrderEntity orderEntity;

	private long itemId;

	private int amount;

	public LineItemEntity() { }

	public LineItemEntity(@Nonnull OrderEntity orderEntity, long itemId, int amount) {
		this();
		this.setOrderEntity(orderEntity);
		this.setItemId(itemId);
		this.setAmount(amount);
	}

	@Id
	@ManyToOne
	@JoinColumn(name = "order_id")
	public OrderEntity getOrderEntity() {
		return orderEntity;
	}

	public void setOrderEntity(@Nonnull OrderEntity orderEntity) {
		this.orderEntity = orderEntity;
	}

	@Id
	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
