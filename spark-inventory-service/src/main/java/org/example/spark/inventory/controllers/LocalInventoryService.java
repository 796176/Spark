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
import org.example.spark.inventory.events.ItemDeleted;
import org.example.spark.inventory.interactors.ItemDataAccess;
import org.example.spark.inventory.sagas.InventoryServiceProxy;
import org.example.spark.inventory.sagas.SagaManager;
import org.example.spark.inventory.sagas.SagaState;

public class LocalInventoryService implements InventoryServiceProxy {

	private final ItemDataAccess itemDataAccess;

	private final SagaManager sagaManager;

	public LocalInventoryService(@Nonnull ItemDataAccess itemDataAccess, @Nonnull SagaManager sagaManager) {
		this.itemDataAccess = itemDataAccess;
		this.sagaManager = sagaManager;
	}

	@Override
	public void confirmDeletion( @Nonnull SagaState state, long itemId, @Nonnull String correlationId ) {
		ItemAggregate item = itemDataAccess.getItem(itemId);
		ItemDeleted event = item.delete();
		itemDataAccess.persist(item, state.getIdempotenceToken(), event);
		sagaManager.deleteSaga(state.getSagaId());
	}

	@Override
	public void abortDeletion(@Nonnull SagaState state, long itemId, @Nonnull String correlationId) {
		ItemAggregate item = itemDataAccess.getItem(itemId);
		item.setStatus(ItemAggregate.Status.CREATED);
		itemDataAccess.persist(item, null);
		sagaManager.deleteSaga(state.getSagaId());
	}
}
