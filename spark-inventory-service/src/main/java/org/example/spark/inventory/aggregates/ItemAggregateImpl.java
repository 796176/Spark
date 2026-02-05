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

package org.example.spark.inventory.aggregates;

import jakarta.annotation.Nonnull;
import org.example.spark.inventory.events.ItemAmountUpdated;
import org.example.spark.inventory.events.ItemAmountUpdatedImpl;
import org.example.spark.inventory.events.ItemDeleted;
import org.example.spark.inventory.events.ItemDeletedImpl;
import org.example.spark.inventory.models.Money;

public class ItemAggregateImpl implements ItemAggregate {

	private final long itemId;

	private final Money price;

	private int amount;

	private Status status;

	public ItemAggregateImpl(long itemId, @Nonnull Money price, int amount, @Nonnull Status status) {
		this.itemId = itemId;
		this.price = price;
		this.amount = amount;
		this.status = status;
	}

	@Override
	public long getId() {
		return itemId;
	}

	@Override
	public Money getPrice() {
		return price;
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public ItemAmountUpdated setAmount(int amount) {
		int delta = amount - this.amount;
		this.amount = amount;
		return new ItemAmountUpdatedImpl(getId(), delta);
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(@Nonnull Status status) {
		this.status = status;
	}

	@Override
	public ItemDeleted delete() {
		this.status = Status.DELETED;
		return new ItemDeletedImpl(getId());
	}
}
