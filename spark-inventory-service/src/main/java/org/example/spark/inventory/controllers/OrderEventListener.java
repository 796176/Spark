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
import org.example.spark.inventory.converters.OrderEventParser;
import org.example.spark.inventory.interactors.InventoryService;
import org.example.spark.inventory.models.RenderableItem;
import org.example.spark.inventory.utils.TokenGenerator;

public class OrderEventListener {

	private final InventoryService inventoryService;

	private final OrderEventParser orderEventParser;

	public OrderEventListener(@Nonnull InventoryService inventoryService, @Nonnull OrderEventParser orderEventParser) {
		this.inventoryService = inventoryService;
		this.orderEventParser = orderEventParser;
	}

	public void processEvent(
		@Nonnull String eventType,
		@Nonnull String contentType,
		@Nonnull String version,
		@Nonnull String messageId,
		@Nonnull byte[] eventBody
	) throws Exception {
		TokenGenerator tokenGenerator = new TokenGenerator(messageId);
		OrderEventParser.LineItem[] lineItems = orderEventParser.getLineItems(contentType, version, eventBody);
		switch (eventType) {
			case "org.example.spark.order.order-created":
				for (OrderEventParser.LineItem lineItem : lineItems) {
					String idempotenceToken = tokenGenerator.nextToken();
					while (true) {
						try {
							RenderableItem renderableItem = inventoryService.getItem(lineItem.itemId());
							inventoryService.updateAmount(
								renderableItem.getId(),
								renderableItem.getAmount() - lineItem.amount(),
								renderableItem.getVersion(),
								idempotenceToken
							);
							break;
						} catch (IllegalStateException ignored) { }
					}
				}
				break;
			case "org.example.spark.order.order-rejected":
			case "org.example.spark.order.order-canceled":
				for (OrderEventParser.LineItem lineItem : lineItems) {
					String idempotenceToken = tokenGenerator.nextToken();
					while (true) {
						try {
							RenderableItem renderableItem = inventoryService.getItem(lineItem.itemId());
							inventoryService.updateAmount(
								renderableItem.getId(),
								renderableItem.getAmount() + lineItem.amount(),
								renderableItem.getVersion(),
								idempotenceToken
							);
							break;
						} catch (IllegalStateException ignored) { }
					}
				}
				break;
		}
	}
}
