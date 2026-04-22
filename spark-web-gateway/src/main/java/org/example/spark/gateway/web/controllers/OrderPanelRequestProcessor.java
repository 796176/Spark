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

package org.example.spark.gateway.web.controllers;

import jakarta.annotation.Nonnull;
import org.example.spark.authorization.exceptions.AuthorizationException;
import org.example.spark.gateway.web.converters.InventoryServiceResponseParser;
import org.example.spark.gateway.web.converters.OrderServiceResponseParser;
import org.example.spark.gateway.web.exceptions.AuthenticationException;
import org.example.spark.gateway.web.exceptions.ServerError;
import org.example.spark.gateway.web.interactors.SessionDataAccess;
import org.example.spark.gateway.web.models.*;
import org.example.spark.gateway.web.proxies.AdminInventoryServiceProxy;
import org.example.spark.gateway.web.proxies.AdminOrderServiceProxy;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;

public class OrderPanelRequestProcessor {

	private final AdminOrderServiceProxy orderService;

	private final AdminInventoryServiceProxy inventoryService;

	private final SessionDataAccess sessionDataAccess;

	private final OrderServiceResponseParser orderServiceResponseParser;

	private final InventoryServiceResponseParser inventoryServiceResponseParser;

	private final Executor executor;

	public OrderPanelRequestProcessor(
		@Nonnull AdminOrderServiceProxy orderService,
		@Nonnull AdminInventoryServiceProxy inventoryService,
		@Nonnull SessionDataAccess sessionDataAccess,
		@Nonnull OrderServiceResponseParser orderServiceResponseParser,
		@Nonnull InventoryServiceResponseParser inventoryServiceResponseParser,
		@Nonnull Executor executor
	) {
		this.orderService = orderService;
		this.inventoryService = inventoryService;
		this.sessionDataAccess = sessionDataAccess;
		this.orderServiceResponseParser = orderServiceResponseParser;
		this.inventoryServiceResponseParser = inventoryServiceResponseParser;
		this.executor = executor;
	}

	public Future<Order[]> getOrders(@Nonnull String sessionId, long accountId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<Order[]> completableFuture = new CompletableFuture<>();
		orderService.getOrders(session.getAccount(), accountId, rcr -> {
			String errorMessage =
				Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
			try {
				if (rcr.isSuccessful()) {
					Order[] orders = orderServiceResponseParser.parseGettingOrdersByAccountResponse(
						rcr.getContentType(), rcr.getVersion(), rcr.getResultBody()
					);
					completableFuture.complete(orders);
				} else {
					Class<? extends Exception> errorClass = rcr.getErrorType();
					Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
					completableFuture.completeExceptionally(error);
				}
			} catch (Exception e) {
				completableFuture.completeExceptionally(new ServerError(errorMessage));
			}
		});
		return completableFuture;
	}

	public Future<DetailedOrder> getOrder(@Nonnull String sessionId, long orderId) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthorizationException("Not authorized");

		CompletableFuture<DetailedOrder> completableFuture = new CompletableFuture<>();
		CompletableFuture<Item[]> itemsFuture = new CompletableFuture<>();
		orderService.getOrder(session.getAccount(), orderId, rcr -> {
			executor.execute(() -> {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					if (rcr.isSuccessful()) {
						while (true) {
							try {
								Item[] items = itemsFuture.get(100, TimeUnit.MILLISECONDS);
								Order order = orderServiceResponseParser.parseGettingOrderResponse(
									rcr.getContentType(), rcr.getVersion(), rcr.getResultBody()
								);
								Item[] orderedItems = Arrays.stream(order.lineItems()).map(lineItem -> {
									for (Item item: items) {
										if (item.itemId() == lineItem.itemId()) {
											return new Item(
												item.itemId(),
												item.name(),
												item.price(),
												lineItem.amount(),
												item.version()
											);
										}
									}
									throw new RuntimeException("Inconsistent Data");
								}).toArray(Item[]::new);
								completableFuture.complete(new DetailedOrder(
									order.orderId(), order.timestamp(), order.status(), order.version(), orderedItems
								));
								break;
							} catch (TimeoutException e) {
								if (completableFuture.isCancelled() || Thread.currentThread().isInterrupted()) return;
							} catch (InterruptedException e) {
								return;
							} catch (ExecutionException e) {
								if (e.getCause().getMessage() != null) errorMessage = e.getCause().getMessage();
								throw e.getCause();
							}
						}
					} else {
						Class<? extends Exception> errorClass = rcr.getErrorType();
						Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
						completableFuture.completeExceptionally(error);
					}
				} catch (Throwable e) {
					completableFuture.completeExceptionally(new ServerError(errorMessage));
				}
			});
		});

		inventoryService.getItems(
			session.getAccount(),
			rcr -> {
				String errorMessage =
					Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
				try {
					if (rcr.isSuccessful()) {
						Item[] items = inventoryServiceResponseParser.parseGettingItemsResponse(
							rcr.getContentType(), rcr.getVersion(), rcr.getResultBody()
						);
						itemsFuture.complete(items);
					} else {
						Class<? extends Exception> errorClass = rcr.getErrorType();
						Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
						itemsFuture.completeExceptionally(error);
					}
				} catch (Exception e) {
					itemsFuture.completeExceptionally(new ServerError(errorMessage));
				}
			}
		);

		return completableFuture;
	}

	public Future<?> saveOrder(
		@Nonnull String sessionId,
		long orderId,
		@Nonnull String version,
		@Nonnull Order.Status previousOrderStatus, @Nonnull Order.Status currentOrderStatus
	) throws Exception {
		Session session = sessionDataAccess.getSession(sessionId);
		if (session == null) throw new AuthenticationException();

		CompletableFuture<?> changingOrderStatusProcess = CompletableFuture.completedFuture(null);
		if (!previousOrderStatus.equals(currentOrderStatus)) {
			changingOrderStatusProcess = changeOrderStatus(session.getAccount(), orderId, version, currentOrderStatus);
		}

		return CompletableFuture.allOf(changingOrderStatusProcess);
	}

	private CompletableFuture<?> changeOrderStatus(
		Account account, long orderId, String version, Order.Status orderStatus
	) throws Exception {
		CompletableFuture<?> completableFuture = new CompletableFuture<>();
		switch (orderStatus) {
			case ACCEPTED -> {
				orderService.acceptOrder(account, orderId, version, rcr -> {
					if (rcr.isSuccessful()) {
						completableFuture.complete(null);
					} else {
						String errorMessage =
							Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
						try {
							Class<? extends Exception> errorClass =
								Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
							Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
							completableFuture.completeExceptionally(error);
						} catch (Exception e) {
							completableFuture.completeExceptionally(new ServerError(errorMessage));
						}
					}
				});
			}
			case REJECTED -> {
				orderService.rejectOrder(account, orderId, version, rcr -> {
					if (rcr.isSuccessful()) {
						completableFuture.complete(null);
					} else {
						String errorMessage =
							Objects.requireNonNullElse(rcr.getFormattedErrorMessage(), "Server Error");
						try {
							Class<? extends Exception> errorClass =
								Objects.requireNonNullElse(rcr.getErrorType(), ServerError.class);
							Exception error = errorClass.getConstructor(String.class).newInstance(errorMessage);
							completableFuture.completeExceptionally(error);
						} catch (Exception e) {
							completableFuture.completeExceptionally(new ServerError(errorMessage));
						}
					}
				});
			}
		}
		return completableFuture;
	}
}
