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
import org.example.spark.inventory.events.ItemAmountUpdated;
import org.example.spark.inventory.interactors.InventoryService;
import org.example.spark.inventory.interactors.ItemDataAccess;
import org.example.spark.inventory.models.Money;
import org.example.spark.inventory.models.RenderableItem;
import org.example.spark.inventory.sagas.SagaManager;

import java.util.Arrays;

public class InventoryServiceImpl implements InventoryService {

	private final ItemDataAccess itemDataAccess;

	private final SagaManager sagaManager;

	public InventoryServiceImpl(@Nonnull ItemDataAccess itemDataAccess, @Nonnull SagaManager sagaManager) {
		this.itemDataAccess = itemDataAccess;
		this.sagaManager = sagaManager;
	}

	@Override
	public void addItem(@Nonnull String name, @Nonnull Money price, int amount, @Nonnull String idempotenceToken) {
		itemDataAccess.addItem(name, price, amount, idempotenceToken);
	}

	@Override
	public void deleteItem(long itemId, @Nonnull String idempotenceToken) throws Exception {
		ItemAggregate itemAggregate = itemDataAccess.getItem(itemId);
		if (itemAggregate.getStatus() == ItemAggregate.Status.DELETED) return;
		if (itemAggregate.getStatus() == ItemAggregate.Status.BUSY) throw new IllegalStateException();
		sagaManager.newDeleteItemSaga(itemAggregate, idempotenceToken);
	}

	@Override
	public void updateAmount(long itemId, int amount, long version, @Nonnull String idempotenceToken) {
		ItemAggregate item = itemDataAccess.getItem(itemId);
		if (item.getStatus() == ItemAggregate.Status.DELETED) throw new IllegalArgumentException();
		if (item.getStatus() == ItemAggregate.Status.BUSY) throw new IllegalStateException();
		ItemAmountUpdated event = item.setAmount(amount);
		itemDataAccess.persist(item, version, idempotenceToken, event);
	}

	@Override
	public RenderableItem getItem(long itemId) {
		VersionedItemAggregate versionedItem = itemDataAccess.getVersionedItem(itemId);
		if (versionedItem.item().getStatus() != ItemAggregate.Status.CREATED) throw new IllegalArgumentException();
		return new RenderableItem(
			versionedItem.item().getId(),
			versionedItem.item().getName(),
			versionedItem.item().getPrice(),
			versionedItem.item().getAmount(),
			versionedItem.version()
		);
	}

	@Override
	public RenderableItem[] getItems() {
		VersionedItemAggregate[] items = itemDataAccess.getVersionedItems();
		return Arrays
			.stream(items)
			.filter(versionedItem -> {
				return versionedItem.item().getStatus() == ItemAggregate.Status.CREATED;
			})
			.map(versionedItem -> {
				return new RenderableItem(
					versionedItem.item().getId(),
					versionedItem.item().getName(),
					versionedItem.item().getPrice(),
					versionedItem.item().getAmount(),
					versionedItem.version()
				);
			})
			.toArray(RenderableItem[]::new);
	}
}
