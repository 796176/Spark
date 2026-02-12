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

package org.example.spark.order.aggregates;

import jakarta.annotation.Nonnull;
import org.example.spark.order.events.*;

public interface OrderAggregate {

	enum Status {
		CREATING(0L),
		CREATED(1L),
		ACCEPTED(2L),
		REJECTED(3L),
		CANCELLED(4L),
		RESTORING(5L),
		RESTORED(6L);

		private final long id;

		Status(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public static Status fromId(long id) {
			if (id == 0) return CREATING;
			if (id == 1) return CREATED;
			if (id == 2) return ACCEPTED;
			if (id == 3) return REJECTED;
			if (id == 4) return CANCELLED;
			if (id == 5) return RESTORING;
			if (id == 6) return RESTORED;
			throw new IllegalArgumentException();
		}
	}

	long getId();

	long getAccountId();

	long getTimestamp();

	Status getStatus();

	void setTransientStatus(@Nonnull Status status);

	OrderAccepted accept();

	OrderRejected reject();

	OrderCancelled cancel();

	OrderRestored restore();
}
