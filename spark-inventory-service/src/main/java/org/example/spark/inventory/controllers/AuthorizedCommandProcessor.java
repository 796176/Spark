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
import org.example.spark.authorization.exceptions.ConditionalAuthorizer;
import org.example.spark.inventory.interactors.InventoryService;
import org.example.spark.inventory.models.Money;
import org.example.spark.inventory.models.RenderableItem;

import java.util.concurrent.Executor;

public class AuthorizedCommandProcessor extends AbstractCommandProcessor {

	private final InventoryService inventoryService;

	public AuthorizedCommandProcessor(
		@Nonnull Executor executor,
		@Nonnull CommandParser commandParser,
		@Nonnull ResponseEncoder responseEncoder,
		@Nonnull InventoryService inventoryService
	) {
		super(executor, commandParser, responseEncoder);

		this.inventoryService = inventoryService;
	}

	@Override
	protected void addItem(
		long callerId,
		@Nonnull Role[] callerRoles,
		@Nonnull String name,
		@Nonnull Money price,
		int amount,
		@Nullable String pictureName,
		@Nonnull String idempotenceToken
	) throws Exception {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		inventoryService.addItem(name, price, amount, pictureName, idempotenceToken);
	}

	@Override
	protected void deleteItem(
		long callerId, @Nonnull Role[] callerRoles, long itemId, @Nonnull String idempotenceToken
	) throws Exception {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		inventoryService.deleteItem(itemId, idempotenceToken);
	}

	@Override
	protected void updateAmount(
		long callerId,
		@Nonnull Role[] callerRoles,
		long itemId,
		int amount,
		long version,
		@Nonnull String idempotenceToken
	) throws Exception {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.isAdministrator().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		inventoryService.updateAmount(itemId, amount, version, idempotenceToken);
	}

	@Override
	protected RenderableItem getItem(long callerId, @Nonnull Role[] callerRoles, long itemId) throws Exception {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.allowAny().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		return inventoryService.getItem(itemId);
	}

	@Override
	protected RenderableItem[] getItems(long callerId, @Nonnull Role[] callerRoles) throws Exception {
		ConditionalAuthorizer.Builder builder = ConditionalAuthorizer.builder();
		ConditionalAuthorizer conditionalAuthorizer = builder.allowAny().build();
		conditionalAuthorizer.authorize(callerId, callerRoles);
		return inventoryService.getItems();
	}
}
