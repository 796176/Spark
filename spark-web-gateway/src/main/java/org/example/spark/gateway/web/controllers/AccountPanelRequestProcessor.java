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

package org.example.spark.gateway.web.controllers;

import jakarta.annotation.Nonnull;
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.converters.AccountServiceResponseParser;
import org.example.spark.gateway.web.exceptions.AuthenticationException;
import org.example.spark.gateway.web.exceptions.ServerError;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.Session;
import org.example.spark.gateway.web.proxies.AdminAccountServiceProxy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class AccountPanelRequestProcessor {

	private final AdminAccountServiceProxy accountService;

	private final SessionDataAccess sessionDataAccess;

	private final AccountServiceResponseParser accountServiceResponseParser;

	public AccountPanelRequestProcessor(
		@Nonnull AdminAccountServiceProxy accountService,
		@Nonnull SessionDataAccess sessionDataAccess,
		@Nonnull AccountServiceResponseParser accountServiceResponseParser
	) {
		this.accountService = accountService;
		this.sessionDataAccess = sessionDataAccess;
		this.accountServiceResponseParser = accountServiceResponseParser;
	}

	public Future<Account[]> getAccounts(@Nonnull String sessionId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<Account[]> completableFuture = new CompletableFuture<>();
		accountService.getAccounts(session.getAccount(), rcr -> {
			String errorMessage =
				Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
			try {
				if (rcr.isSuccessful()) {
					Account[] accounts = accountServiceResponseParser
						.parseGettingAccountsResponse(rcr.getContentType(), rcr.getVersion(), rcr.getResultBody());
					completableFuture.complete(accounts);
				} else {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				}
			} catch (Exception e) {
				completableFuture.completeExceptionally(new ServerError(errorMessage));
			}
		});
		return completableFuture;
	}

	public Future<Account> getAccount(@Nonnull String sessionId, long accountId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<Account> completableFuture = new CompletableFuture<>();
		accountService.getAccount(session.getAccount(), accountId, rcr -> {
			String errorMessage =
				Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
			try {
				if (rcr.isSuccessful()) {
					Account account = accountServiceResponseParser
						.parseGettingAccountResponse(rcr.getContentType(), rcr.getVersion(), rcr.getResultBody());
					completableFuture.complete(account);
				} else {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				}
			} catch (Exception e) {
				completableFuture.completeExceptionally(new ServerError(errorMessage));
			}
		});
		return completableFuture;
	}

	public Future<?> createAccount(
		@Nonnull String sessionId, @Nonnull String name, @Nonnull String password
	) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		accountService.createAccount(session.getAccount(), name, password, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}

	public Future<?> createAdministratorAccount(
		@Nonnull String sessionId, @Nonnull String name, @Nonnull String password
	) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		accountService.createAdministratorAccount(session.getAccount(), name, password, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}

	public Future<?> saveAccount(
		@Nonnull String sessionId,
		long accountId,
		@Nonnull Account.Status previousStatus, @Nonnull Account.Status currentStatus,
		@Nonnull Role[] previouslyAssignedRoles, @Nonnull Role[] currentlyAssignedRoles
	) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<?> updatingRolesProcess = CompletableFuture.completedFuture(null);
		if (!equalRoles(previouslyAssignedRoles, currentlyAssignedRoles)) {
			updatingRolesProcess = updateRoles(session.getAccount(), accountId, currentlyAssignedRoles);
		}

		CompletableFuture<?> updatingStatusProcess = CompletableFuture.completedFuture(null);
		if (!previousStatus.equals(currentStatus)) {
			switch (currentStatus) {
				case ACTIVE -> {
					updatingStatusProcess = restoreAccount(session.getAccount(), accountId);
				}
				case SUSPENDED -> {
					updatingStatusProcess = suspendAccount(session.getAccount(), accountId);
				}
			}
		}

		return CompletableFuture.allOf(updatingRolesProcess, updatingStatusProcess);
	}

	private boolean equalRoles(Role[] roles1, Role[] roles2) {
		if (roles1.length != roles2.length) return false;

		Comparator<Role> roleComparator = Comparator.nullsLast((r1, r2) -> (int)(r1.getId() - r2.getId()));
		Arrays.sort(roles1, roleComparator);
		for (Role role: roles2) {
			if (Arrays.binarySearch(roles1, role, roleComparator) < 0) return false;
		}
		return true;
	}

	private CompletableFuture<?> suspendAccount(Account account, long accountId) throws Exception {
		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		accountService.suspendAccount(account, accountId, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}

	private CompletableFuture<?> restoreAccount(Account account, long accountId) throws Exception {
		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		accountService.restoreAccount(account, accountId, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}

	private CompletableFuture<?> updateRoles(Account account, long accountId, Role[] roles) throws Exception {
		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		accountService.updateRoles(account, accountId, roles, rcr -> {
			if (rcr.isSuccessful()) {
				completableFuture.complete(null);
			} else {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					Class<? extends Exception> errorClass =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				} catch (Exception e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		});
		return completableFuture;
	}
}
