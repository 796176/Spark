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
import org.example.spark.order.converters.OrderCommandParser.*;
import org.example.spark.order.converters.ResponseConverter;
import org.example.spark.order.models.LineItem;
import org.example.spark.order.models.RenderableOrder;

import java.util.Arrays;
import java.util.concurrent.Executor;

public abstract class AbstractCommandProcessor implements CommandProcessor {

	private final Executor executor;

	private final OrderCommandParser parser;

	private final ResponseConverter responseConverter;

	public AbstractCommandProcessor(
		@Nonnull Executor executor,
		@Nonnull OrderCommandParser orderCommandParser,
		@Nonnull ResponseConverter responseConverter
	) {
		this.executor = executor;
		this.parser = orderCommandParser;
		this.responseConverter = responseConverter;
	}

	@Override
	public void processCommand(
		@Nonnull String commandType,
		long callerId,
		@Nonnull long[] callerRoles,
		@Nonnull String commandId,
		@Nonnull String bodyContentType,
		@Nonnull String version,
		@Nonnull byte[] body,
		@Nonnull Response callback
	) {
		executor.execute(() -> {
			try {
				Role[] roles = Arrays.stream(callerRoles).mapToObj(Role::fromId).toArray(Role[]::new);
				ResponseConverter.ConvertedResponse emptyResponse = responseConverter.emptyResponse();
				switch (commandType) {
					case "org.example.spark.order.place-order" -> {
						PlacingOrderCommand command = parser.parsePlacingOrderCommand(bodyContentType, version, body);
						placeOrder(
							callerId,
							roles,
							command.accountId(),
							command.orderTimestamp(),
							command.lineItems(),
							commandId
						);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.order.accept-order" -> {
						UpdatingOrderStatusCommand command =
							parser.parseUpdatingOrderStatusCommand(bodyContentType, version, body);
						acceptOrder(callerId, roles, command.orderId(), command.orderVersion(), commandId);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.order.reject-order" -> {
						UpdatingOrderStatusCommand command =
							parser.parseUpdatingOrderStatusCommand(bodyContentType, version, body);
						rejectOrder(callerId, roles, command.orderId(), command.orderVersion(), commandId);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.order.cancel-order" -> {
						UpdatingOrderStatusCommand command =
							parser.parseUpdatingOrderStatusCommand(bodyContentType, version, body);
						cancelOrder(callerId, roles, command.orderId(), command.orderVersion(), commandId);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.order.restore-order" -> {
						UpdatingOrderStatusCommand command =
							parser.parseUpdatingOrderStatusCommand(bodyContentType, version, body);
						restoreOrder(callerId, roles, command.orderId(), command.orderVersion(), commandId);
						callback.send(
							0,
							emptyResponse.getContentType(),
							emptyResponse.getVersion(),
							emptyResponse.getBody()
						);
					}
					case "org.example.spark.order.get-order" -> {
						RetrievingOrderCommand command =
							parser.parseRetrievingOrderCommand(bodyContentType, version, body);
						RenderableOrder renderableOrder = getOrder(callerId, roles, command.orderId());
						ResponseConverter.ConvertedResponse response =
							responseConverter.convertRenderableOrders(renderableOrder);
						callback.send(
							0, response.getContentType(), response.getVersion(), response.getBody()
						);
					}
					case "org.example.spark.order.get-orders-by-account" -> {
						RetrievingOrdersByAccountCommand retrievingOrdersByAccountCommand =
							parser.parseRetrievingOrdersByAccountCommand(bodyContentType, version, body);
						RenderableOrder[] renderableOrders =
							getOrdersByAccount(callerId, roles, retrievingOrdersByAccountCommand.accountId());
						ResponseConverter.ConvertedResponse response =
							responseConverter.convertRenderableOrders(renderableOrders);
						callback.send(
							0, response.getContentType(), response.getVersion(), response.getBody()
						);
					}
					case "org.example.spark.order.invalidate-item" -> {
						InvalidatingItemCommand command =
							parser.parseInvalidatingItemCommand(bodyContentType, version, body);
						invalidateItem(callerId, roles, command.itemId());
						callback.send(
							0, emptyResponse.getContentType(), emptyResponse.getVersion(), emptyResponse.getBody()
						);
					}
					default -> {
						throw new IllegalArgumentException();
					}
				}
			} catch (Exception e) {
				try {
					ResponseConverter.ConvertedResponse exceptionResponse = responseConverter.convertThrowable(e);
					callback.send(
						1,
						exceptionResponse.getContentType(),
						exceptionResponse.getVersion(),
						exceptionResponse.getBody()
					);
				} catch (Exception ignored) { }
				e.printStackTrace();
			}
		});
	}

	protected abstract void placeOrder(
		long callerId,
		@Nonnull Role[] roles,
		long accountId,
		long orderTimestamp,
		@Nonnull LineItem[] lineItems,
		@Nonnull String idempotenceToken
	) throws Exception;

	protected abstract void rejectOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception;

	protected abstract void acceptOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception;

	protected abstract void cancelOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception;

	protected abstract void restoreOrder(
		long callerId, @Nonnull Role[] roles, long orderId, long version, @Nonnull String idempotenceToken
	) throws Exception;

	protected abstract RenderableOrder getOrder(long callerId, @Nonnull Role[] roles, long orderId) throws Exception;

	protected abstract RenderableOrder[] getOrdersByAccount(
		long callerId, @Nonnull Role[] roles, long accountId
	) throws Exception;

	protected abstract void invalidateItem(long callerId, @Nonnull Role[] roles, long itemId) throws Exception;
}
