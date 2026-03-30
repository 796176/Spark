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

package org.example.spark.gateway.web.models;

import jakarta.annotation.Nonnull;

public record Order(
	long orderId, long timestamp, @Nonnull Status status, @Nonnull String version, @Nonnull LineItem[] lineItems
) {
	public enum Status {
		PLACING(0L),
		PENDING_ACCEPTANCE(1L),
		ACCEPTED(2L),
		REJECTED(3L),
		CANCELED(4L),
		PLACING_ABORTED(5L);

		private final long id;

		Status(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public static Status fromId(long id) {
			if (id == 0) return PLACING;
			if (id == 1) return PENDING_ACCEPTANCE;
			if (id == 2) return ACCEPTED;
			if (id == 3) return REJECTED;
			if (id == 4) return CANCELED;
			if (id == 5) return PLACING_ABORTED;
			throw new IllegalArgumentException();
		}
	}
}
