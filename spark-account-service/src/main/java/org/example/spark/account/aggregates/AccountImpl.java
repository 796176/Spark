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

package org.example.spark.account.aggregates;

import jakarta.annotation.Nonnull;
import org.example.spark.authorization.Role;
import org.example.spark.account.events.*;

import java.util.Arrays;

public class AccountImpl implements Account {

	private final long id;

	private final String name;

	private final String encodedPassword;

	private Role[] roles;

	private Status status;

	public AccountImpl(
		long id,
		@Nonnull String name,
		@Nonnull String encodedPassword,
		@Nonnull Role[] roles,
		@Nonnull Status status
	) {
		this.id = id;
		this.name = name;
		this.encodedPassword = encodedPassword;
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
	public AccountRolesUpdated setRoles(@Nonnull Role... roles) {
		this.roles = roles;
		return new AccountRolesUpdatedImpl(id, Arrays.stream(roles).mapToLong(Role::getId).toArray());
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public AccountStatusUpdated setStatus(@Nonnull Status status) {
		this.status = status;
		return switch (status) {
			case DELETED -> new AccountDeletedImpl(id);
			case SUSPENDED -> new AccountSuspendedImpl(id);
			case ACTIVE -> new AccountRestoredImpl(id);
		};
	}
}
