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
import org.example.spark.order.models.LineItem;
import org.example.spark.order.models.RenderableOrder;

public interface OrderService {

	void placeOrder(
		long accountId, long orderTimestamp, @Nonnull LineItem[] lineItems, @Nonnull String idempotenceToken
	) throws Exception;

	void rejectOrder(long orderId, long version, @Nonnull String idempotenceToken) throws Exception;

	void acceptOrder(long orderId, long version, @Nonnull String idempotenceToken) throws Exception;

	void cancelOrder(long orderId, long version, @Nonnull String idempotenceToken) throws Exception;

	void restoreOrder(long orderId, long version, @Nonnull String idempotenceToken) throws Exception;

	RenderableOrder getOrder(long orderId);

	RenderableOrder[] getOrdersByAccount(long accountId);
}
