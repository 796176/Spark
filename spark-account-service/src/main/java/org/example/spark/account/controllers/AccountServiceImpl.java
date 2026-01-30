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

package org.example.spark.account.controllers;

import jakarta.annotation.Nonnull;
import org.example.spark.account.events.AccountEvent;
import org.example.spark.account.adapters.PasswordEncoder;
import org.example.spark.account.aggregates.Account;
import org.example.spark.authorization.Role;
import org.example.spark.account.intaractors.AccountDataAccess;
import org.example.spark.account.models.Password;
import org.example.spark.account.models.RenderableAccount;

import java.util.Arrays;

public class AccountServiceImpl implements AccountService {

	public final AccountDataAccess accountDataAccess;

	public final PasswordEncoder passwordEncoder;

	public AccountServiceImpl(
		@Nonnull AccountDataAccess accountDataAccess,
		@Nonnull PasswordEncoder passwordEncoder
	) {
		this.accountDataAccess = accountDataAccess;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void createAccount(@Nonnull String name, @Nonnull Password password) {
		accountDataAccess.createAccount(name, passwordEncoder.encode(password), Role.USER);
	}

	@Override
	public void createAdminAccount(@Nonnull String name, @Nonnull Password password) {
		accountDataAccess.createAccount(name, passwordEncoder.encode(password), Role.USER, Role.ADMIN);
	}

	@Override
	public RenderableAccount getAccount(long id) {
		Account account = accountDataAccess.getAccount(id);
		return new RenderableAccount(account.getId(), account.getName(), account.getRoles());
	}

	@Override
	public RenderableAccount[] getAccounts() {
		Account[] accounts = accountDataAccess.getAccounts();
		return Arrays
			.stream(accounts)
			.map(account -> new RenderableAccount(account.getId(), account.getName(), account.getRoles()))
			.toArray(RenderableAccount[]::new);
	}

	@Override
	public void deleteAccount(long id) {
		Account account = accountDataAccess.getAccount(id);
		AccountEvent accountEvent = account.setStatus(Account.Status.DELETED);
		accountDataAccess.persist(account, accountEvent);
	}

	@Override
	public void suspendAccount(long id) {
		Account account = accountDataAccess.getAccount(id);
		AccountEvent accountEvent = account.setStatus(Account.Status.SUSPENDED);
		accountDataAccess.persist(account, accountEvent);
	}

	@Override
	public void restoreAccount(long id) {
		Account account = accountDataAccess.getAccount(id);
		AccountEvent accountEvent = account.setStatus(Account.Status.ACTIVE);
		accountDataAccess.persist(account, accountEvent);
	}

	@Override
	public void changeAccountRoles(long id, long[] roles) {
		Account account = accountDataAccess.getAccount(id);
		AccountEvent accountEvent = account.setRoles(Arrays.stream(roles).mapToObj(Role::fromId).toArray(Role[]::new));
		accountDataAccess.persist(account, accountEvent);
	}
}
