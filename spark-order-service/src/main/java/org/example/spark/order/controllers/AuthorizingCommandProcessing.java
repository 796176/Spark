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
import org.example.spark.authorization.Role;
import org.example.spark.order.converters.OrderCommandParser;
import org.example.spark.order.converters.ResponseConverter;
import org.example.spark.order.interactors.ItemRepositoryReplicaManager;
import org.example.spark.order.interactors.OrderDataAccess;
import org.example.spark.order.interactors.OrderService;
import org.example.spark.order.models.LineItem;
import org.example.spark.order.models.RenderableOrder;

import java.util.concurrent.Executor;

public class AuthorizingCommandProcessing extends AbstractCommandProcessor {

	private final OrderService orderService;

	private final OrderDataAccess orderDataAccess;

	private final ItemRepositoryReplicaManager itemRepositoryReplicaManager;

	public AuthorizingCommandProcessing(
		@Nonnull Executor executor,
		@Nonnull OrderCommandParser orderCommandParser,
		@Nonnull ResponseConverter responseConverter,
		@Nonnull OrderService orderService,
		@Nonnull OrderDataAccess orderDataAccess,
		@Nonnull ItemRepositoryReplicaManager itemRepositoryReplicaManager
	) {
		super(executor, orderCommandParser, responseConverter);

		this.orderService = orderService;
		this.orderDataAccess = orderDataAccess;
		this.itemRepositoryReplicaManager = itemRepositoryReplicaManager;
	}

	@Override
	protected void placeOrder(
		long callerId,
		@Nonnull Role[] roles,
		long accountId,
		long orderTimestamp,
		@Nonnull LineItem[] lineItems,
		@Nonnull String idempotenceToken
	) throws Exception {
		orderService.placeOrder(accountId, orderTimestamp, lineItems, idempotenceToken);
	}

	@Override
	protected void rejectOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception {
		orderService.rejectOrder(orderId, version, idempotenceToken);
	}

	@Override
	protected void acceptOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception {
		orderService.acceptOrder(orderId, version, idempotenceToken);
	}

	@Override
	protected void cancelOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception {
		orderService.cancelOrder(orderId, version, idempotenceToken);
	}

	@Override
	protected void restoreOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception {
		orderService.restoreOrder(orderId, version, idempotenceToken);
	}

	@Override
	protected RenderableOrder getOrder(long callerId, @Nonnull Role[] roles, long orderId) throws Exception {
		return orderService.getOrder(orderId);
	}

	@Override
	protected RenderableOrder[] getOrdersByAccount(
		long callerId, @Nonnull Role[] roles, long accountId
	) throws Exception {
		return orderService.getOrdersByAccount(accountId);
	}

	@Override
	protected void invalidateItem(long callerId, @Nonnull Role[] roles, long itemId) {
		if (orderDataAccess.isItemOrdered(itemId)) throw new IllegalStateException();
		itemRepositoryReplicaManager.deleteItem(itemId);
	}
}
