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
import org.example.spark.inventory.aggregates.ItemAggregate;
import org.example.spark.inventory.aggregates.VersionedItemAggregate;
import org.example.spark.inventory.events.ItemDeleted;
import org.example.spark.inventory.interactors.ItemDataAccess;
import org.example.spark.inventory.sagas.InventoryServiceProxy;
import org.example.spark.inventory.sagas.Saga;

public class LocalInventoryService implements InventoryServiceProxy {

	private final ItemDataAccess itemDataAccess;

	public LocalInventoryService(@Nonnull ItemDataAccess itemDataAccess) {
		this.itemDataAccess = itemDataAccess;
	}

	@Override
	public boolean confirmDeletion(Saga saga, @Nonnull String correlationId) {
		VersionedItemAggregate versionedItem = itemDataAccess.getVersionedItem(saga.getItemId());
		ItemDeleted event = versionedItem.item().delete();
		itemDataAccess.persist(
			versionedItem.item(), versionedItem.version(), saga.getStateObject().getIdempotenceToken(), event
		);
		saga.setCompleted();
		return true;
	}

	@Override
	public boolean abortDeletion(Saga saga, @Nonnull String correlationId) {
		VersionedItemAggregate versionedItem = itemDataAccess.getVersionedItem(saga.getItemId());
		versionedItem.item().setStatus(ItemAggregate.Status.CREATED);
		itemDataAccess.persist(versionedItem.item(), versionedItem.version(), null);
		saga.setCompleted();
		return true;
	}
}
