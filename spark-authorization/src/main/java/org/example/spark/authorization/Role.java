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

package org.example.spark.authorization;

public enum Role {
	USER(0), ADMIN(1);

	private final long id;

	Role(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public static Role fromId(long id) {
		if (id == 0L) return Role.USER;
		if (id == 1L) return Role.ADMIN;
		throw new IllegalArgumentException();
	}
}
