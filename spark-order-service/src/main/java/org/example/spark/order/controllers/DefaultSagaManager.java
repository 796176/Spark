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
import jakarta.annotation.Nullable;
import org.example.spark.order.aggregates.OrderAggregate;
import org.example.spark.order.interactors.OrderDataAccess;
import org.example.spark.order.interactors.SagaDataAccess;
import org.example.spark.order.models.LineItem;
import org.example.spark.order.sagas.Saga;
import org.example.spark.order.sagas.SagaManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class DefaultSagaManager implements SagaManager {

	private final List<Saga> sagas = new ArrayList<>();

	private final SagaDataAccess sagaDataAccess;

	private final Executor executor;

	private volatile boolean isInitializationCompleted = false;

	public DefaultSagaManager(@Nonnull SagaDataAccess sagaDataAccess, @Nonnull Executor executor) {
		this.sagaDataAccess = sagaDataAccess;
		this.executor = executor;
	}

	@Override
	public void newPlaceOrderSaga(
		long accountId,
		long timestamp,
		@Nonnull OrderDataAccess orderDataAccess,
		@Nonnull String idempotenceToken,
		@Nonnull LineItem... lineItems
	) throws Exception {
		if (!isInitializationCompleted) throw new IllegalStateException();

		Saga saga = orderDataAccess.placeOrder(accountId, timestamp, idempotenceToken, sagaDataAccess, lineItems);
		loadSaga(saga);
	}

	@Override
	public void newRestoreOrderSaga(
		@Nonnull OrderAggregate order,
		long version,
		@Nonnull String idempotenceToken,
		@Nonnull OrderDataAccess orderDataAccess
	) {
		if (!isInitializationCompleted) throw new IllegalStateException();

		Saga saga = orderDataAccess.restoreOrder(order, version, idempotenceToken, sagaDataAccess);
		loadSaga(saga);
	}

	@Override
	public void loadSaga(@Nonnull Saga saga) {
		synchronized (sagas) { sagas.add(saga); }
		executor.execute(() -> {
			try {
				while (saga.proceedNextState()) {
					if (saga.hasCompleted()) {
						deleteSaga(saga);
						break;
					} else updateSagaState(saga, saga.getState());
				}
			} catch (Exception e) {
				deleteSaga(saga);
				e.printStackTrace();
			}
		});
	}

	@Override
	public void deleteSaga(@Nullable Saga saga) {
		if (saga == null) return;
		sagaDataAccess.deleteSaga(saga.getId());
		synchronized (sagas) { sagas.remove(saga); }
	}

	@Override
	public void deleteSaga(long sagaId) {
		synchronized (sagas) { deleteSaga(getSaga(sagaId)); }
	}

	@Override
	public void updateSagaState(@Nullable Saga saga, @Nonnull Saga.State state) {
		if (saga == null) return;

		String idempotenceToken = sagaDataAccess.updateSagaState(saga.getId(), state.getId());
		saga.setState(state, saga.getStateObjects().get(state));
		saga.getStateObject().setIdempotenceToken(idempotenceToken);
	}

	@Override
	public void updateSagaState(long sagaId, @Nonnull Saga.State state) {
		synchronized (sagas) { updateSagaState(getSaga(sagaId), state); }
	}

	@Override
	public Saga[] getSagas() {
		synchronized (sagas) {
			return sagas.toArray(new Saga[0]);
		}
	}

	@Override
	public Saga getSaga(long sagaId) {
		return sagas.stream().filter(saga -> saga.getId() == sagaId).findFirst().orElse(null);
	}

	@Override
	public void setInitializationCompleted() {
		isInitializationCompleted = true;
	}
}
