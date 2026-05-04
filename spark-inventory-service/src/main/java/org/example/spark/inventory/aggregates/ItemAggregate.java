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

import jakarta.annotation.Nullable;
import org.example.spark.inventory.events.ItemAmountUpdated;
import org.example.spark.inventory.events.ItemDeleted;
import org.example.spark.inventory.models.Money;

public interface ItemAggregate {

	enum Status {
		CREATED(0), DELETED(1), BUSY(2);

		private long id;

		Status(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public static Status fromId(long id) {
			if (id == 0) return CREATED;
			if (id == 1) return DELETED;
			if (id == 2) return BUSY;
			throw new IllegalArgumentException();
		}
	}

	long getId();

	String getName();

	Money getPrice();

	@Nullable
	String getPictureName();

	int getAmount();

	ItemAmountUpdated setAmount(int amount);

	Status getStatus();

	void setStatus(Status status);

	ItemDeleted delete();
}
