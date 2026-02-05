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

package org.example.spark.inventory.events;

import jakarta.annotation.Nonnull;
import org.example.spark.inventory.models.Money;

public class ItemCreatedImpl implements ItemCreated {

	private final long itemId;

	private final String itemName;

	private final int amount;

	private final Money price;

	public ItemCreatedImpl(long itemId, @Nonnull String itemName, int amount, @Nonnull Money price) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.amount = amount;
		this.price = price;
	}
	@Override
	public long getItemId() {
		return itemId;
	}

	@Override
	public String getName() {
		return itemName;
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public Money getPrice() {
		return price;
	}
}
