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

import jakarta.annotation.Nullable;

public class CreatingAccountForm {

	private final String username;

	private final String password;

	private final boolean admin;

	public CreatingAccountForm(@Nullable String username, @Nullable String password, boolean admin) {
		this.username = username;
		this.password = password;
		this.admin = admin;
	}

	@Nullable
	public String getUsername() {
		return username;
	}

	@Nullable
	public String getPassword() {
		return password;
	}

	public boolean isAdmin() {
		return admin;
	}
}
