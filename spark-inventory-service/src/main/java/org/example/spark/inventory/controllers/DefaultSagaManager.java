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
import org.example.spark.inventory.aggregates.ItemAggregate;
import org.example.spark.inventory.interactors.SagaDataAccess;
import org.example.spark.inventory.models.SagaProperties;
import org.example.spark.inventory.sagas.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DefaultSagaManager implements SagaManager {

	@FunctionalInterface
	public interface SagaStateFactory {
		SagaState get(@Nonnull Class<? extends SagaState> clazz, long sagaId);
	}

	private final List<Saga> sagas = new ArrayList<>();

	private final SagaDataAccess sagaDataAccess;

	private final SagaStateFactory sagaStateFactory;

	private volatile boolean isInitializationCompleted = false;

	public DefaultSagaManager(
		@Nonnull SagaDataAccess sagaDataAccess,
		@Nonnull SagaStateFactory sagaStateFactory
	) {
		this.sagaDataAccess = sagaDataAccess;
		this.sagaStateFactory = sagaStateFactory;
	}

	@Override
	public void newDeleteItemSaga(@Nonnull ItemAggregate item, @Nonnull String idempotenceToken) throws Exception {
		if (!isInitializationCompleted) throw new IllegalStateException();

		SagaProperties sagaProperties = sagaDataAccess.newSaga(
			item, DeleteItemSaga.State.INVALIDATING_ITEM.getId(), idempotenceToken, DeleteItemSaga.class
		);

		HashMap<Saga.StateEnumeration, SagaState> sagaStates = new HashMap<>();
		DeleteItemSaga.State initialState = DeleteItemSaga.State.INVALIDATING_ITEM;
		SagaState initialStateObject =
			sagaStateFactory.get(SagaStateInvalidatingItem.class, sagaProperties.id());
		initialStateObject.setIdempotenceToken(sagaProperties.idempotenceToken());
		sagaStates.put(initialState, initialStateObject);
		sagaStates.put(
			DeleteItemSaga.State.ABORTING_DELETION,
			sagaStateFactory.get(SagaStateAbortingDeletion.class, sagaProperties.id())
		);
		sagaStates.put(
			DeleteItemSaga.State.CONFIRMING_DELETION,
			sagaStateFactory.get(SagaStateConfirmingDeletion.class, sagaProperties.id())
		);

		Saga saga = new DeleteItemSaga(
			sagaProperties.id(), sagaProperties.itemId(), initialState, initialStateObject, sagaStates
		);
		synchronized (sagas) { sagas.add(saga); }
		saga.proceedNextState();
	}

	private void loadDeleteItemSaga(@Nonnull SagaProperties sagaProperties) throws Exception {
		HashMap<Saga.StateEnumeration, SagaState> sagaStates = new HashMap<>();
		sagaStates.put(
			DeleteItemSaga.State.INVALIDATING_ITEM,
			sagaStateFactory.get(SagaStateInvalidatingItem.class, sagaProperties.id())
		);
		sagaStates.put(
			DeleteItemSaga.State.ABORTING_DELETION,
			sagaStateFactory.get(SagaStateAbortingDeletion.class, sagaProperties.id())
		);
		sagaStates.put(
			DeleteItemSaga.State.CONFIRMING_DELETION,
			sagaStateFactory.get(SagaStateConfirmingDeletion.class, sagaProperties.id())
		);

		DeleteItemSaga.State currentState = DeleteItemSaga.State.fromId(sagaProperties.state());
		SagaState currentStateObject = sagaStates.get(currentState);
		currentStateObject.setIdempotenceToken(sagaProperties.idempotenceToken());

		Saga saga = new DeleteItemSaga(
			sagaProperties.id(), sagaProperties.itemId(), currentState, currentStateObject, sagaStates
		);
		synchronized (sagas) { sagas.add(saga); }
		saga.proceedNextState();
	}


	@Override
	public void loadSaga(@Nonnull SagaProperties sagaProperties) throws Exception {
		if (sagaProperties.sagaClass().equals(DeleteItemSaga.class)) {
			loadDeleteItemSaga(sagaProperties);
		}
	}

	@Override
	public void deleteSaga(@Nullable Saga saga) {
		if (saga == null) return;
		sagaDataAccess.deleteSaga(saga.getId());
		synchronized (sagas) { sagas.remove(saga); }
	}

	public void deleteSaga(long sagaId) {
		synchronized (sagas) {
			deleteSaga(
				sagas.stream().filter(saga -> saga.getId() == sagaId).findFirst().orElse(null)
			);
		}
	}

	@Override
	public Saga[] getSagas() {
		synchronized (sagas) {
			return sagas.toArray(Saga[]::new);
		}
	}

	@Override
	public void setInitializationCompleted() {
		isInitializationCompleted = true;
	}
}
