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
		sagaManager.newDeleteItemSaga(itemAggregate, idempotenceToken);
	}

	@Override
	public void updateAmount(long itemId, int amount) {
		//TODO implement positive locking
		ItemAggregate item = itemDataAccess.getItem(itemId);
		ItemAmountUpdated event = item.setAmount(amount);
		itemDataAccess.persist(item, null, event);
	}

	@Override
	public RenderableItem getItem(long itemId) {
		ItemAggregate item = itemDataAccess.getItem(itemId);
		return new RenderableItem(item.getId(), item.getName(), item.getPrice(), item.getAmount());
	}

	@Override
	public RenderableItem[] getItems() {
		ItemAggregate[] items = itemDataAccess.getItems();
		return Arrays
			.stream(items)
			.map(i -> new RenderableItem(i.getId(), i.getName(), i.getPrice(), i.getAmount()))
			.toArray(RenderableItem[]::new);
	}
}
