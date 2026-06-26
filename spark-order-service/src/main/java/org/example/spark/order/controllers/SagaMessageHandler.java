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
import org.example.spark.order.sagas.Saga;
import org.example.spark.order.sagas.SagaManager;

import java.util.concurrent.Executor;

public class SagaMessageHandler {

	private final SagaManager sagaManager;

	private final Executor executor;

	public SagaMessageHandler(@Nonnull SagaManager sagaManager, @Nonnull Executor executor) {
		this.sagaManager = sagaManager;
		this.executor = executor;
	}

	void handleMessage(
		@Nonnull String correlationId,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body,
		Runnable acknowledgeRunnable
	) {
		for (Saga saga: sagaManager.getSagas()) {
			boolean canProcess = saga.canProcess(correlationId, contentType, statusCode, version, body);
			if (canProcess) {
				executor.execute(() -> {
					try {
						saga.concludeCurrentState(correlationId, contentType, statusCode, version, body);
						acknowledgeRunnable.run();
						do {
							if (saga.hasCompleted()){
								sagaManager.deleteSaga(saga);
								break;
							}
							else sagaManager.updateSagaState(saga, saga.getState());
						} while (saga.proceedNextState());
					} catch (Exception e) {
						sagaManager.deleteSaga(saga);
						acknowledgeRunnable.run();
						e.printStackTrace();
					}
				});
				return;
			}
		}
		acknowledgeRunnable.run();
	}
}
