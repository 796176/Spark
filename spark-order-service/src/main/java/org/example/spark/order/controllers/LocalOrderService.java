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
import org.example.spark.order.aggregates.VersionedOrderAggregate;
import org.example.spark.order.events.OrderEvent;
import org.example.spark.order.interactors.OrderDataAccess;
import org.example.spark.order.sagas.OrderServiceProxy;
import org.example.spark.order.sagas.Saga;
import org.example.spark.order.sagas.SagaState;

public class LocalOrderService implements OrderServiceProxy {

	public final OrderDataAccess orderDataAccess;

	public LocalOrderService(@Nonnull OrderDataAccess orderDataAccess) {
		this.orderDataAccess = orderDataAccess;
	}

	@Override
	public boolean confirmPlacing(@Nonnull SagaState state, @Nonnull Saga saga, long orderId, @Nonnull String correlationId) throws Exception {
		VersionedOrderAggregate versionedOrder = orderDataAccess.getVersionedOrder(orderId);
		OrderEvent event = versionedOrder.order().confirmPlacement();
		orderDataAccess.persist(versionedOrder.order(), versionedOrder.version(), state.getIdempotenceToken(), event);
		saga.hasCompleted();
		return true;
	}

	@Override
	public boolean abortPlacing(@Nonnull SagaState state, @Nonnull Saga saga, long orderId, @Nonnull String correlationId) throws Exception {
		VersionedOrderAggregate versionedOrder = orderDataAccess.getVersionedOrder(orderId);
		versionedOrder.order().abortPlacing();
		orderDataAccess.persist(versionedOrder.order(), versionedOrder.version(), null);
		saga.setCompleted();
		return true;
	}
}
