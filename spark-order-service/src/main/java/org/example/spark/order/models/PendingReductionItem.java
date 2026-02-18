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
@Table(name = "pending_reduction_items")
public class PendingReductionItem {

	private long id;

	private ItemEntity item;

	private int amount;

	public PendingReductionItem() { }

	public PendingReductionItem(@Nonnull ItemEntity item, int amount) {
		this();
		this.setItem(item);
		this.setAmount(amount);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne
	public ItemEntity getItem() {
		return item;
	}

	public void setItem(@Nonnull ItemEntity item) {
		this.item = item;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
