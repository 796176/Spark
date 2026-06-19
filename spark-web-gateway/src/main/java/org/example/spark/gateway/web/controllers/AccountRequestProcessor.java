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
import org.example.spark.gateway.web.exceptions.AuthenticationException;
import org.example.spark.gateway.web.exceptions.ServerError;
import org.example.spark.gateway.web.interactors.AccountDataAccess;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.Session;
import org.example.spark.gateway.web.proxies.UserAccountServiceProxy;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class AccountRequestProcessor {

	private final SessionDataAccess sessionDataAccess;

	private final AccountDataAccess accountDataAccess;

	private final PasswordEncoder passwordEncoder;

	private final UserAccountServiceProxy userAccountService;

	private final Executor executor;

	public AccountRequestProcessor(
		@Nonnull SessionDataAccess sessionDataAccess,
		@Nonnull AccountDataAccess accountDataAccess,
		@Nonnull PasswordEncoder passwordEncoder,
		@Nonnull UserAccountServiceProxy userAccountService,
		@Nonnull Executor executor
	) {
		this.sessionDataAccess = sessionDataAccess;
		this.accountDataAccess = accountDataAccess;
		this.passwordEncoder = passwordEncoder;
		this.userAccountService = userAccountService;
		this.executor = executor;
	}

	public void logIn(
		@Nonnull String sessionId, @Nonnull String name, @Nonnull char[] password
	) throws AuthenticationException {
		try {
			Account account = accountDataAccess.getAccountByName(name);
			if (
				account == null ||
					!account.getStatus().equals(Account.Status.ACTIVE) ||
					!passwordEncoder.matches(password, account.getEncodedPassword())
			) {
				throw new AuthenticationException();
			}
			Session session = new Session(sessionId, account);
			sessionDataAccess.persist(session);
		} finally {
			Arrays.fill(password, (char)0);
		}
	}

	public Future<?> signIn(
		@Nonnull String sessionId, @Nonnull String name, @Nonnull char[] password, @Nonnull Role[] roles, long callerId
	) throws Exception {
		try {
			CompletableFuture<?> completableFuture = new CompletableFuture<>();
			userAccountService.createAccount(
				name,
				passwordEncoder.encode(password),
				rcr -> {
					executor.execute(() -> {
						if (rcr.isSuccessful()) {
							Account account;
							while (true) {
								account = accountDataAccess.getAccountByName(name);
								if (account != null) break;
								if (completableFuture.isCancelled() || Thread.currentThread().isInterrupted()) return;
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									return;
								}
							}
							Session session = new Session(sessionId, account);
							sessionDataAccess.persist(session);
							completableFuture.complete(null);
						} else {
							String errorMessage =
								Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
							Class<? extends Exception> errorClass =
								Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
							try {
								Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
								completableFuture.completeExceptionally(error);
							} catch (ReflectiveOperationException e) {
								completableFuture.completeExceptionally(new ServerError(errorMessage));
							}
						}
					});
				},
				roles,
				callerId
			);
			return completableFuture;
		} finally {
			Arrays.fill(password, (char) 0);
		}
	}

	public void logOut(@Nonnull String sessionId) {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session != null) sessionDataAccess.invalidateSession(session);
	}

	public Future<?> deleteAccount(@Nonnull String sessionId) throws Exception {
		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) {
			completableFuture.complete(null);
			return completableFuture;
		}
		userAccountService.deleteAccount(
			session.getAccount(),
			rcr -> {
				if (rcr.isSuccessful()) completableFuture.complete(null);
				else {
					String errorMessage =
						Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
					Class<? extends Exception> exception =
						Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
					try {
						completableFuture.completeExceptionally(
							exception.getConstructor(String.class).newInstance(errorMessage)
						);
					} catch (ReflectiveOperationException e) {
						completableFuture.completeExceptionally(new ServerError(errorMessage));
					}
				}
			}
		);
		return completableFuture;
	}

	public boolean isLoggedIn(@Nonnull String sessionId) {
		return sessionDataAccess.getSession(sessionId) != null;
	}

	public Account getAccount(@Nonnull String sessionId) {
		Session session = sessionDataAccess.getSession(sessionId);
		return session == null ? null : session.getAccount();
	}
}
