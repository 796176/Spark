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
import org.example.spark.account.models.RenderableAccount;
import org.example.spark.authorization.Role;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

public abstract class AbstractCommandProcessor implements CommandProcessor {

	private final Executor executor;

	private final CommandParser parser;

	public AbstractCommandProcessor(@Nonnull Executor executor, @Nonnull CommandParser commandParser) {
		this.executor = executor;
		this.parser = commandParser;
	}

	@Override
	public void processCommand(
		@Nonnull String commandType,
		long callerId,
		@Nonnull long[] callerRoles,
		@Nonnull String commandId,
		@Nonnull String bodyEncodingFormat,
		@Nonnull String version,
		@Nonnull byte[] body,
		@Nonnull Response callback
	) {
		executor.execute(() -> {
			Role[] roles = Arrays.stream(callerRoles).mapToObj(Role::fromId).toArray(Role[]::new);
			CommandParser.ParsedCommand parsedCommand = parser.parse(bodyEncodingFormat, version, body);
			try {
				byte[] emptyJson = "{}".getBytes(StandardCharsets.UTF_8);
				switch (commandType) {
					case "org.example.spark.account.create-account" -> {
						createAccount(
							callerId,
							roles,
							Objects.requireNonNull(parsedCommand.getValue("name")),
							Objects.requireNonNull(parsedCommand.getPassword()),
							UUID.fromString(commandId)
						);
						callback.send(0, "application/json", "1.0", emptyJson);
					}
					case "org.example.spark.account.create-admin-account" -> {
						createAdminAccount(
							callerId,
							roles,
							Objects.requireNonNull(parsedCommand.getValue("name")),
							Objects.requireNonNull(parsedCommand.getPassword()),
							UUID.fromString(commandId)
						);
						callback.send(0, "application/json", "1.0", emptyJson);
					}
					case "org.example.spark.account.delete-account" -> {
						deleteAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						callback.send(0, "application/json", "1.0", emptyJson);
					}
					case "org.example.spark.account.suspend-account" -> {
						suspendAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						callback.send(0, "application/json", "1.0", emptyJson);
					}
					case "org.example.spark.account.restore-account" -> {
						restoreAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						callback.send(0, "application/json", "1.0", emptyJson);
					}
					case "org.example.spark.account.get-account" -> {
						RenderableAccount renderableAccount = getAccount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("account_id")))
						);
						byte[] jsonOutput = toJson(renderableAccount).getBytes(StandardCharsets.UTF_8);
						callback.send(0, "application/json", "1.0", jsonOutput);
					}
					case "org.example.spark.account.get-accounts" -> {
						RenderableAccount[] renderableAccounts = getAccounts(
							callerId,
							roles
						);
						byte[] jsonOutput = toJson(renderableAccounts).getBytes(StandardCharsets.UTF_8);
						callback.send(0, "application/json", "1.0", jsonOutput);
					}
				}
			} catch (Exception e) {
				try {
					byte[] jsonOutput = toJson(e).getBytes(StandardCharsets.UTF_8);
					callback.send(1, "application/json", "1.0", jsonOutput);
				} catch (Exception ignored) { }
				e.printStackTrace();
			}
		});
	}

	private String toJson(Exception e) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringProperty("exception-type", e.getClass().getName());
		jsonGenerator.writeStringProperty("exception-message", e.getMessage());
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();
		return os.toString();
	}

	private String toJson(RenderableAccount... renderableAccounts) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeName("accounts");
		jsonGenerator.writeStartArray();
		for (RenderableAccount renderableAccount: renderableAccounts) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", renderableAccount.getId());
			jsonGenerator.writeStringProperty("name", renderableAccount.getName());
			jsonGenerator.writeName("roles");
			String[] roles = Arrays
				.stream(renderableAccount.getRoles())
				.mapToObj(Long::toString)
				.toArray(String[]::new);
			jsonGenerator.writeArray(roles, 0, roles.length);
			jsonGenerator.writeStringProperty("account_status", renderableAccount.getStatus());
			jsonGenerator.writeEndObject();
		}
		jsonGenerator.writeEndArray();
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();
		return os.toString();
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
}
