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
import org.example.spark.authorization.Role;

public class AccountImpl implements Account {

	private final long id;

	private final String encodedPassword;

	private final String name;

	private final Role[] roles;

	private final Status status;

	public AccountImpl(
		long id, @Nonnull String encodedPassword, @Nonnull String name, @Nonnull Role[] roles, @Nonnull Status status
	) {
		this.id = id;
		this.encodedPassword = encodedPassword;
		this.name = name;
		this.roles = roles;
		this.status = status;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getEncodedPassword() {
		return encodedPassword;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Role[] getRoles() {
		return roles;
	}

	@Override
	public Status getStatus() {
		return status;
	}
}
