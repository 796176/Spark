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
import jakarta.annotation.Nullable;
import org.example.spark.order.aggregates.OrderAggregate;
import org.example.spark.order.aggregates.VersionedOrderAggregate;
import org.example.spark.order.events.OrderEvent;
import org.example.spark.order.models.LineItem;
import org.example.spark.order.sagas.Saga;

public interface OrderDataAccess {

	OrderAggregate getOrder(long id);

	VersionedOrderAggregate getVersionedOrder(long id);

	VersionedOrderAggregate[] getVersionedOrdersByAccount(long accountId);

	void persist(
		@Nonnull OrderAggregate order, long version, @Nullable String idempotenceToken, @Nonnull OrderEvent... events
	);

	Saga placeOrder(
		long accountId,
		long timestamp,
		@Nonnull String idempotenceToken,
		@Nonnull SagaDataAccess sagaDataAccess,
		@Nonnull LineItem... lineItems
	);

	Saga restoreOrder(
		@Nonnull OrderAggregate order,
		long version,
		@Nonnull String idempotenceToken,
		@Nonnull SagaDataAccess sagaDataAccess
	);

	boolean isItemOrdered(long itemId);

}
