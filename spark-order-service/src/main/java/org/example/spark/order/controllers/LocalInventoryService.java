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
import org.example.spark.order.interactors.ItemDataAccess;
import org.example.spark.order.models.Item;
import org.example.spark.order.models.LineItem;
import org.example.spark.order.sagas.*;

import java.util.Arrays;

public class LocalInventoryService implements InventoryServiceProxy {

	private final ItemDataAccess itemDataAccess;

	public LocalInventoryService(@Nonnull ItemDataAccess itemDataAccess) {
		this.itemDataAccess = itemDataAccess;
	}

	@Override
	public boolean verifyOrderDetails(
		@Nonnull SagaState state, @Nonnull Saga saga, @Nonnull LineItem[] lineItems, @Nonnull String correlationId
	) throws Exception{
		for (LineItem lineItem: lineItems) {
			Item item = itemDataAccess.getItem(lineItem.itemId());
			int availableAmount = item.amount();
			if (availableAmount < lineItem.amount()) {
				Saga.State nextState = PlaceOrderSaga.State.ABORTING_PLACING;
				saga.setState(nextState, saga.getStateObjects().get(nextState));
				return true;
			}
		}

		Item[] itemsToReserve = new Item[lineItems.length];
		Arrays.setAll(
			itemsToReserve, i -> itemsToReserve[i] = new Item(lineItems[i].itemId(), lineItems[i].amount())
		);
		itemDataAccess.reserve(itemsToReserve, state.getIdempotenceToken());

		Saga.State nextState = PlaceOrderSaga.State.CONFIRMING_PLACING;
		saga.setState(nextState, saga.getStateObjects().get(nextState));
		return true;
	}
}
