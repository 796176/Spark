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

import org.example.spark.authorization.Role;


public interface Account {

	enum Status {
		ACTIVE(0, "Active"), DELETED(1, "Deleted"), SUSPENDED(2, "Suspended");

		private final String formatedStatus;

		private final long id;

		Status(long id, String formatedStatus) {
			this.id = id;
			this.formatedStatus = formatedStatus;
		}

		public long getId() {
			return id;
		}

		public static Status fromId(long id) {
			if (id == 0) return ACTIVE;
			if (id == 1) return DELETED;
			if (id == 2) return SUSPENDED;
			throw new IllegalArgumentException();
		}

		@Override
		public String toString() {
			return formatedStatus;
		}
	}

	long getId();

	String getEncodedPassword();

	String getName();

	Role[] getRoles();

	Status getStatus();
}
