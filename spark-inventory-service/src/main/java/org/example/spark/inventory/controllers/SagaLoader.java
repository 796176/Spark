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
import org.example.spark.inventory.interactors.SagaDataAccess;
import org.example.spark.inventory.models.SagaProperties;
import org.example.spark.inventory.sagas.SagaManager;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.io.IOException;

public class SagaLoader implements SmartInitializingSingleton {

	private final SagaDataAccess sagaDataAccess;

	private final RMQSagaMessageConsumer rmqSagaMessageConsumer;

	private final SagaManager sagaManager;

	public SagaLoader(
		@Nonnull SagaDataAccess sagaDataAccess,
		@Nonnull RMQSagaMessageConsumer rmqSagaMessageConsumer,
		@Nonnull SagaManager sagaManager
	) {
		this.sagaDataAccess = sagaDataAccess;
		this.rmqSagaMessageConsumer = rmqSagaMessageConsumer;
		this.sagaManager = sagaManager;
	}

	@Override
	public void afterSingletonsInstantiated() {
		SagaProperties[] sagaProperties = sagaDataAccess.getSagas();
		for (SagaProperties sp: sagaProperties) {
			try {
				sagaManager.loadSaga(sp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sagaManager.setInitializationCompleted();
		try {
			rmqSagaMessageConsumer.getChannel().basicConsume("spark-inventory-service-saga", rmqSagaMessageConsumer);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
