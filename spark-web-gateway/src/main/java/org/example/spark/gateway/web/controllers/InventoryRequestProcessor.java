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
import org.example.spark.gateway.web.converters.InventoryServiceResponseParser;
import org.example.spark.gateway.web.exceptions.ServerError;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.models.Item;
import org.example.spark.gateway.web.models.Session;
import org.example.spark.gateway.web.proxies.UserInventoryServiceProxy;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class InventoryRequestProcessor {

	private final SessionDataAccess sessionDataAccess;

	private final UserInventoryServiceProxy userInventoryServiceProxy;

	private final InventoryServiceResponseParser inventoryServiceResponseParser;

	public InventoryRequestProcessor(
		@Nonnull SessionDataAccess sessionDataAccess,
		@Nonnull UserInventoryServiceProxy userInventoryServiceProxy,
		@Nonnull InventoryServiceResponseParser inventoryServiceResponseParser
	) {
		this.sessionDataAccess = sessionDataAccess;
		this.userInventoryServiceProxy = userInventoryServiceProxy;
		this.inventoryServiceResponseParser = inventoryServiceResponseParser;
	}

	public Future<Item[]> getInventory(@Nonnull String sessionId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		Role[] roles = session == null ? new Role[0] : session.getAccount().getRoles();
		long accountId = session == null ? -1 : session.getAccount().getId();

		CompletableFuture<Item[]> completableFuture = new CompletableFuture<>();
		userInventoryServiceProxy.getInventory(accountId, roles, rcr -> {
			try {
				if (rcr.isSuccessful()) {
					completableFuture.complete(
						inventoryServiceResponseParser.parseGettingItemsResponse(
							rcr.getContentType(), rcr.getVersion(), rcr.getResultBody()
						)
					);
				} else {
					String errorMessage =
						Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			} catch (Exception e) {
				String errorMessage = Objects.requireNonNullElse(e.getMessage(), "Server Error");
				completableFuture.completeExceptionally(new ServerError(errorMessage));
			}
		});
		return completableFuture;
	}
}
