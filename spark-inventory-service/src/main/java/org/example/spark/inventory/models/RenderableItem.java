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

package org.example.spark.inventory.models;

import jakarta.annotation.Nonnull;

public class RenderableItem {

	private final long id;

	private final String name;

	private final Money price;

	private final int amount;

	private final long version;

	public RenderableItem(long id, @Nonnull String name, @Nonnull Money price, int amount, long version) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.amount = amount;
		this.version = version;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Money getPrice() {
		return price;
	}

	public int getAmount() {
		return amount;
	}

	public long getVersion() {
		return version;
	}
}
