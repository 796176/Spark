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

package org.example.spark.inventory.sagas;

import jakarta.annotation.Nonnull;
import org.example.spark.inventory.aggregates.ItemAggregate;

import java.util.HashMap;
import java.util.Map;

public class SagaFactoryImpl implements SagaFactory {

	private final OrderServiceProxy orderService;

	private final InventoryServiceProxy inventoryService;

	public SagaFactoryImpl(@Nonnull OrderServiceProxy orderService, @Nonnull InventoryServiceProxy inventoryService) {
		this.orderService = orderService;
		this.inventoryService = inventoryService;
	}

	@Override
	public Saga instantiateSaga(
		long sagaId, @Nonnull ItemAggregate item, @Nonnull String idempotenceToken, @Nonnull SagaType sagaType
	) {
		return instantiateSaga(sagaId, item, idempotenceToken, sagaType, getInitialState(sagaType).getId());
	}

	@Override
	public Saga.StateEnumeration getInitialState(@Nonnull SagaType sagaType) {
		return switch (sagaType) {
			case ITEM_DELETED -> DeleteItemSaga.State.INVALIDATING_ITEM;
		};
	}

	@Override
	public Saga instantiateSaga(
		long sagaId,
		@Nonnull ItemAggregate item,
		@Nonnull String idempotenceToken,
		@Nonnull SagaType sagaType,
		long sagaStateId
	) {
		return switch (sagaType) {
			case ITEM_DELETED -> {
				SagaStateInvalidatingItem sagaStateInvalidatingItem =
					new SagaStateInvalidatingItem(orderService);
				SagaStateConfirmingDeletion sagaStateConfirmingDeletion =
					new SagaStateConfirmingDeletion(inventoryService);
				SagaStateAbortingDeletion sagaStateAbortingDeletion = new SagaStateAbortingDeletion(inventoryService);
				Map<Saga.StateEnumeration, SagaState> stateObjects = new HashMap<>();
				stateObjects.put(DeleteItemSaga.State.ABORTING_DELETION, sagaStateAbortingDeletion);
				stateObjects.put(DeleteItemSaga.State.CONFIRMING_DELETION, sagaStateConfirmingDeletion);
				stateObjects.put(DeleteItemSaga.State.INVALIDATING_ITEM, sagaStateInvalidatingItem);

				yield new DeleteItemSaga(sagaId, item.getId(), DeleteItemSaga.State.fromId(sagaStateId), stateObjects);
			}
		};
	}
}
