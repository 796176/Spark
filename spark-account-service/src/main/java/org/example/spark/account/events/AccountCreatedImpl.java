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

package org.example.spark.account.events;

import jakarta.annotation.Nonnull;

public class AccountCreatedImpl implements AccountCreated {

	private long accountId;

	private final String name;

	private final String encodedPassword;

	private final long[] roles;

	public AccountCreatedImpl(
		long accountId, @Nonnull String name, @Nonnull String encodedPassword, @Nonnull long[] roles
	) {
		this.accountId = accountId;
		this.name = name;
		this.encodedPassword = encodedPassword;
		this.roles = roles;
	}

	@Override
	public long getAccountId() {
		return accountId;
	}

	@Override
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getEncodedPassword() {
		return encodedPassword;
	}

	@Override
	public long[] getRoles() {
		return roles;
	}
}
