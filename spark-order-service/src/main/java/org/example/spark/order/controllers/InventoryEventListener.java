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

package org.example.spark.order.controllers;

import jakarta.annotation.Nonnull;
import org.example.spark.order.converters.InventoryEventParser;
import org.example.spark.order.converters.InventoryEventParser.*;
import org.example.spark.order.interactors.ItemRepositoryReplicaManager;

public class InventoryEventListener {

	private final ItemRepositoryReplicaManager itemRepositoryReplicaManager;

	private final InventoryEventParser inventoryEventParser;

	public InventoryEventListener(
		@Nonnull ItemRepositoryReplicaManager itemRepositoryReplicaManager,
		@Nonnull InventoryEventParser inventoryEventParser
	) {
		this.itemRepositoryReplicaManager = itemRepositoryReplicaManager;
		this.inventoryEventParser = inventoryEventParser;
	}

	public void processEvent(
		@Nonnull String eventType,
		@Nonnull String contentType,
		@Nonnull String version,
		@Nonnull byte[] body,
		@Nonnull String messageId
	) {
		switch (eventType) {
			case "org.example.spark.item-created" -> {
				ItemCreatedEvent event = inventoryEventParser.parseItemAddedEvent(contentType, version, body);
				itemRepositoryReplicaManager.addNewItem(event.itemId(), event.amount(), messageId);
			}
			case "org.example.spark.item-amount-updated" -> {
				ItemAmountUpdatedEvent event =
					inventoryEventParser.parseItemAmountUpdatedEvent(contentType, version, body);
				itemRepositoryReplicaManager.updateAmount(event.itemId(), event.delta(), messageId);
			}
		}
	}
}
