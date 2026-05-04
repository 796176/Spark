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

package org.example.spark.inventory.controllers;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.example.spark.authorization.Role;
import org.example.spark.inventory.models.Money;
import org.example.spark.inventory.models.RenderableItem;

import java.util.Arrays;
import java.util.Objects;
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
					case "org.example.spark.inventory.add-item" -> {
						addItem(
							callerId,
							roles,
							Objects.requireNonNull(parsedCommand.getValue("item_name")),
							Objects.requireNonNull(parsedCommand.getPrice()),
							Integer.parseInt(Objects.requireNonNull(parsedCommand.getValue("amount"))),
							parsedCommand.getValue("item_picture_name"),
							commandId
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.inventory.delete-item" -> {
						deleteItem(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("item_id"))),
							commandId
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.inventory.update-item-amount" -> {
						updateAmount(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("item_id"))),
							Integer.parseInt(Objects.requireNonNull(parsedCommand.getValue("amount"))),
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("version"))),
							commandId
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.inventory.get-item" -> {
						RenderableItem renderableItem = getItem(
							callerId,
							roles,
							Long.parseLong(Objects.requireNonNull(parsedCommand.getValue("item_id")))
						);
						ResponseEncoder.EncodedResponseProperties response =
							responseEncoder.encodeRenderableItems(renderableItem);
						callback.send(
							0, response.getContentType(), response.getVersion(), response.getBody()
						);
					}
					case "org.example.spark.inventory.get-items" -> {
						RenderableItem[] renderableItems = getItems(callerId, roles);
						ResponseEncoder.EncodedResponseProperties response =
							responseEncoder.encodeRenderableItems(renderableItems);
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
			}
		});
	}

	protected abstract void addItem(
		long callerId,
		@Nonnull Role[] callerRoles,
		@Nonnull String name,
		@Nonnull Money price,
		int amount,
		@Nullable String pictureName,
		@Nonnull String idempotenceToken
	) throws Exception;

	protected abstract void deleteItem(
		long callerId, @Nonnull Role[] callerRoles, long itemId, @Nonnull String idempotenceToken
	) throws Exception;

	protected abstract void updateAmount(
		long callerId,
		@Nonnull Role[] callerRoles,
		long itemId,
		int amount,
		long version,
		@Nonnull String idempotenceToken
	) throws Exception;

	protected abstract RenderableItem getItem(long callerId, @Nonnull Role[] callerRoles, long itemId) throws Exception;

	protected abstract RenderableItem[] getItems(long callerId, @Nonnull Role[] callerRoles) throws Exception;
}
