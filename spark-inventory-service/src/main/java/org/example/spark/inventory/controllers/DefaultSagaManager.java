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
import org.example.spark.inventory.sagas.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class DefaultSagaManager implements SagaManager {

	private final List<Saga> sagas = new ArrayList<>();

	private final SagaDataAccess sagaDataAccess;

	private final Executor executor;

	private volatile boolean isInitializationCompleted = false;

	public DefaultSagaManager(
		@Nonnull SagaDataAccess sagaDataAccess,
		@Nonnull Executor executor
	) {
		this.sagaDataAccess = sagaDataAccess;
		this.executor = executor;
	}

	@Override
	public void newDeleteItemSaga(@Nonnull ItemAggregate item, @Nonnull String idempotenceToken) throws Exception {
		if (!isInitializationCompleted) throw new IllegalStateException();

		Saga saga = sagaDataAccess.newSaga(
			item, idempotenceToken, SagaType.ITEM_DELETED
		);
		loadSaga(saga);
	}

	@Override
	public void loadSaga(@Nonnull Saga saga) {
		synchronized (sagas) {
			sagas.add(saga);
		}
		executor.execute(() -> {
			try {
				while (saga.proceedNextState()) {
					if (saga.hasCompleted()) {
						deleteSaga(saga);
						break;
					} else {
						updateSagaState(saga, saga.getState());
					}
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

	public void deleteSaga(long sagaId) {
		synchronized (sagas) {
			deleteSaga(
				sagas.stream().filter(saga -> saga.getId() == sagaId).findFirst().orElse(null)
			);
		}
	}

	@Override
	public void updateSagaState(@Nullable Saga saga, @Nonnull Saga.StateEnumeration state) {
		if (saga == null) return;

		String idempotenceToken = sagaDataAccess.updateState(saga.getId(), state.getId());
		saga.getStateObject().setIdempotenceToken(idempotenceToken);
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
