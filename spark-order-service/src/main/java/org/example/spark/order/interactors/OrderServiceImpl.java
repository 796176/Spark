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

package org.example.spark.order.interactors;

import jakarta.annotation.Nonnull;
import org.example.spark.order.aggregates.OrderAggregate;
import org.example.spark.order.aggregates.VersionedOrderAggregate;
import org.example.spark.order.events.OrderEvent;
import org.example.spark.order.models.LineItem;
import org.example.spark.order.models.RenderableOrder;
import org.example.spark.order.sagas.SagaManager;

import java.util.Arrays;

public class OrderServiceImpl implements OrderService {

	private final OrderDataAccess orderDataAccess;

	private final SagaManager sagaManager;

	public OrderServiceImpl(@Nonnull OrderDataAccess orderDataAccess, @Nonnull SagaManager sagaManager) {
		this.orderDataAccess = orderDataAccess;
		this.sagaManager = sagaManager;
	}

	@Override
	public void placeOrder(
		long accountId, long orderTimestamp, @Nonnull LineItem[] lineItems, @Nonnull String idempotenceToken
	) throws Exception {
		sagaManager.newPlaceOrderSaga(accountId, orderTimestamp, orderDataAccess, idempotenceToken, lineItems);
	}

	@Override
	public void rejectOrder(long orderId, long version, @Nonnull String idempotenceToken) {
		OrderAggregate order = orderDataAccess.getOrder(orderId);
		if (order.getStatus() == OrderAggregate.Status.PLACING) throw new IllegalStateException();
		OrderEvent event = order.reject();
		orderDataAccess.persist(order, version, idempotenceToken, event);
	}

	@Override
	public void acceptOrder(long orderId, long version, @Nonnull String idempotenceToken) {
		OrderAggregate order = orderDataAccess.getOrder(orderId);
		if (order.getStatus() == OrderAggregate.Status.PLACING) throw new IllegalStateException();
		OrderEvent event = order.accept();
		orderDataAccess.persist(order, version, idempotenceToken, event);
	}

	@Override
	public void cancelOrder(long orderId, long version, @Nonnull String idempotenceToken) {
		OrderAggregate order = orderDataAccess.getOrder(orderId);
		if (order.getStatus() == OrderAggregate.Status.PLACING) throw new IllegalStateException();
		OrderEvent event = order.cancel();
		orderDataAccess.persist(order, version, idempotenceToken, event);
	}

	@Override
	public void restoreOrder(long orderId, long version, @Nonnull String idempotenceToken) {
		OrderAggregate order = orderDataAccess.getOrder(orderId);
		if (order.getStatus() == OrderAggregate.Status.PLACING) throw new IllegalStateException();
		sagaManager.newRestoreOrderSaga(order, version, idempotenceToken, orderDataAccess);
	}

	@Override
	public RenderableOrder getOrder(long orderId) {
		VersionedOrderAggregate versionedOrder = orderDataAccess.getVersionedOrder(orderId);
		return new RenderableOrder(
			versionedOrder.order().getId(),
			versionedOrder.order().getAccountId(),
			versionedOrder.order().getTimestamp(),
			versionedOrder.order().getLineItems(),
			versionedOrder.order().getStatus().name(),
			versionedOrder.version()
		);
	}

	@Override
	public RenderableOrder[] getOrdersByAccount(long accountId) {
		VersionedOrderAggregate[] versionedOrders = orderDataAccess.getVersionedOrdersByAccount(accountId);
		return Arrays
			.stream(versionedOrders)
			.map(versionedOrder-> {
				return new RenderableOrder(
					versionedOrder.order().getId(),
					versionedOrder.order().getAccountId(),
					versionedOrder.order().getTimestamp(),
					versionedOrder.order().getLineItems(),
					versionedOrder.order().getStatus().name(),
					versionedOrder.version()
				);
			})
			.toArray(RenderableOrder[]::new);
	}
}
