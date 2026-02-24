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

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

public abstract class AbstractCommandProcessor implements CommandProcessor {

	private final Executor executor;

	private final CommandParser parser;

	private final ResponseEncoder responseEncoder;

	public AbstractCommandProcessor(
		@Nonnull Executor executor, @Nonnull CommandParser commandParser, @Nonnull ResponseEncoder responseEncoder
	) {
		this.executor = executor;
		this.parser = commandParser;
		this.responseEncoder = responseEncoder;
	}

	@Override
	public void processCommand(
		@Nonnull String commandType,
		long callerId,
		@Nonnull long[] callerRoles,
		@Nonnull String commandId,
		@Nonnull String bodyContentType,
		@Nonnull String version,
		@Nonnull byte[] body,
		@Nonnull Response callback
	) {
		executor.execute(() -> {
			Role[] roles = Arrays.stream(callerRoles).mapToObj(Role::fromId).toArray(Role[]::new);
			CommandParser.ParsedCommand parsedCommand = parser.parse(bodyContentType, version, body);
			try {
				ResponseEncoder.EncodedResponseProperties emptyResponse = responseEncoder.emptyResponse();
				switch (commandType) {
					case "org.example.spark.account.create-account" -> {
						createAccount(
							callerId,
							roles,
							Objects.requireNonNull(parsedCommand.getValue("account_name")),
							Objects.requireNonNull(parsedCommand.getPassword()),
							UUID.fromString(commandId)
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.account.create-admin-account" -> {
						createAdminAccount(
							callerId,
							roles,
							Objects.requireNonNull(parsedCommand.getValue("account_name")),
							Objects.requireNonNull(parsedCommand.getPassword()),
							UUID.fromString(commandId)
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.account.delete-account" -> {
						deleteAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.account.suspend-account" -> {
						suspendAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.account.restore-account" -> {
						restoreAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.account.get-account" -> {
						RenderableAccount renderableAccount = getAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						ResponseEncoder.EncodedResponseProperties response =
							responseEncoder.encodeRenderableAccounts(renderableAccount);
						callback.send(
							0, response.getContentType(), response.getVersion(), response.getBody()
						);
					}
					case "org.example.spark.account.get-accounts" -> {
						RenderableAccount[] renderableAccounts = getAccounts(
							callerId,
							roles
						);
						ResponseEncoder.EncodedResponseProperties response =
							responseEncoder.encodeRenderableAccounts(renderableAccounts);
						callback.send(
							0, response.getContentType(), response.getVersion(), response.getBody()
						);
					}
					case "org.example.spark.account.change-account-roles" -> {
						CommandParser.ChangingRolesCommand command =
							parser.parseChangingRolesCommand(bodyContentType, version, body);
						changeAccountRoles(callerId, roles, command.accountId(), command.newRoleList());
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.account.get-account-permissions" -> {
						PermissionList permissionList = getAccountPermissions(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						ResponseEncoder.EncodedResponseProperties response =
							responseEncoder.encodePermissionList(permissionList);
						callback.send(
							0, response.getContentType(), response.getVersion(), response.getBody()
						);
					}
					default -> {
						throw new IllegalStateException();
					}
				}
			} catch (Exception e) {
				try {
					ResponseEncoder.EncodedResponseProperties exceptionResponse = responseEncoder.encodeThrowable(e);
					callback.send(
						1,
						exceptionResponse.getContentType(),
						exceptionResponse.getVersion(),
						exceptionResponse.getBody()
					);
				} catch (Exception ignored) { }
				e.printStackTrace();
			} finally {
				parsedCommand.destroy();
				if (parsedCommand.getPassword() != null) parsedCommand.getPassword().destroy();
			}
		});
	}

	protected abstract void createAccount(
		long callerId,
		@Nonnull Role[] callerRoles,
		@Nonnull String name,
		@Nonnull Password password,
		@Nonnull UUID messageId
	);

	protected abstract void createAdminAccount(
		long callerId,
		@Nonnull Role[] callerRoles,
		@Nonnull String name,
		@Nonnull Password password,
		@Nonnull UUID messageId
	);

	protected abstract RenderableAccount getAccount(long callerId, @Nonnull Role[] callerRoles, long id);

	protected abstract RenderableAccount[] getAccounts(long callerId, @Nonnull Role[] callerRoles);

	protected abstract void deleteAccount(long callerId, @Nonnull Role[] callerRoles, long id);

	protected abstract void suspendAccount(long callerId, @Nonnull Role[] callerRoles, long id);

	protected abstract void restoreAccount(long callerId, @Nonnull Role[] callerRoles, long id);

	protected abstract void changeAccountRoles(
		long callerId, @Nonnull Role[] callerRoles, long id, @Nonnull Role[] roles
	);

	protected abstract PermissionList getAccountPermissions(
		long callerId, @Nonnull Role[] callerRoles, long id
	) throws Exception;
}
