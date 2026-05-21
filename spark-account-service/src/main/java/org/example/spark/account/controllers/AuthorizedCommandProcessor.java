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
import org.example.spark.account.models.Password;
import org.example.spark.account.models.PermissionList;
import org.example.spark.account.models.RenderableAccount;
import org.example.spark.authorization.Role;
import org.example.spark.authorization.exceptions.AuthorizationException;
import org.example.spark.authorization.exceptions.ConditionalAuthorizer;

import java.util.UUID;
import java.util.concurrent.Executor;

public class AuthorizedCommandProcessor extends AbstractCommandProcessor {

	private final AccountService accountService;

	public AuthorizedCommandProcessor(
		@Nonnull Executor executor,
		@Nonnull CommandParser converter,
		@Nonnull ResponseEncoder responseEncoder,
		@Nonnull AccountService accountService
	) {
		super(executor, converter, responseEncoder);

		this.accountService = accountService;
	}

	@Override
	protected void createAccount(
		long callerId,
		@Nonnull Role[] callerRoles,
		@Nonnull String name,
		@Nonnull Password password,
		@Nonnull UUID messageId
	) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder
			.any(
				builder.isAdministrator(),
				builder.not(builder.hasRoles())
			)
			.build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		accountService.createAccount(name, password, messageId);
	}

	@Override
	protected void createAdminAccount(
		long callerId,
		@Nonnull Role[] callerRoles,
		@Nonnull String name,
		@Nonnull Password password,
		@Nonnull UUID messageId
	) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		accountService.createAdminAccount(name, password, messageId);
	}

	@Override
	protected RenderableAccount getAccount(
		long callerId, @Nonnull Role[] callerRoles, long id
	) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder
			.any(
				builder.isAdministrator(),
				builder.matchesId(id)
			)
			.build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		return accountService.getAccount(id);
	}

	@Override
	protected RenderableAccount[] getAccounts(
		long callerId, @Nonnull Role[] callerRoles
	) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		return accountService.getAccounts();
	}

	@Override
	protected void deleteAccount(long callerId, @Nonnull Role[] callerRoles, long id) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.matchesId(id).build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		accountService.deleteAccount(id);
	}

	@Override
	protected void suspendAccount(long callerId, @Nonnull Role[] callerRoles, long id) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		accountService.suspendAccount(id);
	}

	@Override
	protected void restoreAccount(long callerId, @Nonnull Role[] callerRoles, long id) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		accountService.restoreAccount(id);
	}

	@Override
	protected void changeAccountRoles(
		long callerId, @Nonnull Role[] callerRoles, long id, @Nonnull Role[] roles
	) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		accountService.changeAccountRoles(id, roles);
	}

	@Override
	protected PermissionList getAccountPermissions(
		long callerId, @Nonnull Role[] callerRoles, long id
	) throws AuthorizationException {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder
			.any(
				builder.isAdministrator(),
				builder.isService(),
				builder.matchesId(id)
			)
			.build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		return accountService.getAccountPermissions(id);
	}
}
