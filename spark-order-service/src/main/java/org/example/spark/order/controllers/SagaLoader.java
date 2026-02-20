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
import org.example.spark.order.interactors.OrderDataAccess;
import org.example.spark.order.interactors.SagaDataAccess;
import org.example.spark.order.sagas.Saga;
import org.example.spark.order.sagas.SagaManager;
import org.springframework.beans.factory.SmartInitializingSingleton;

public class SagaLoader implements SmartInitializingSingleton {

	private final SagaDataAccess sagaDataAccess;

	private final OrderDataAccess orderDataAccess;

	private final RMQSagaMessageConsumer rmqSagaMessageConsumer;

	private final SagaManager sagaManager;

	public SagaLoader(
		@Nonnull SagaDataAccess sagaDataAccess,
		@Nonnull OrderDataAccess orderDataAccess,
		@Nonnull RMQSagaMessageConsumer rmqSagaMessageConsumer,
		@Nonnull SagaManager sagaManager
	) {
		this.sagaDataAccess = sagaDataAccess;
		this.orderDataAccess = orderDataAccess;
		this.rmqSagaMessageConsumer = rmqSagaMessageConsumer;
		this.sagaManager = sagaManager;
	}

	@Override
	public void afterSingletonsInstantiated() {
		try {
			Saga[] sagas = sagaDataAccess.getSagas(orderDataAccess);
			for (Saga saga : sagas) {
				try {
					sagaManager.loadSaga(saga);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			sagaManager.setInitializationCompleted();
			rmqSagaMessageConsumer.getChannel().basicConsume("spark-order-service-saga", rmqSagaMessageConsumer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
