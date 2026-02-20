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

package org.example.spark.order.sagas;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import jakarta.annotation.Nonnull;
import org.example.spark.order.aggregates.OrderAggregate;
import org.example.spark.order.controllers.LocalInventoryService;
import org.example.spark.order.controllers.LocalOrderService;
import org.example.spark.order.controllers.RMQAccountService;
import org.example.spark.order.converters.AuthorizingAccountMessageProcessor;
import org.example.spark.order.interactors.ItemDataAccess;
import org.example.spark.order.interactors.OrderDataAccess;

import java.util.Map;

public class DefaultSagaFactory implements SagaFactory {

	private final ItemDataAccess itemDataAccess;

	private final OrderDataAccess orderDataAccess;

	private final Connection connection;

	private final String replyChannel;

	private final AuthorizingAccountMessageProcessor authorizingAccountMessageProcessor;

	public DefaultSagaFactory(
		@Nonnull ItemDataAccess itemDataAccess,
		@Nonnull OrderDataAccess orderDataAccess,
		@Nonnull Connection connection,
		@Nonnull String replyChannel,
		@Nonnull AuthorizingAccountMessageProcessor authorizingAccountMessageProcessor
	) {
		this.itemDataAccess = itemDataAccess;
		this.orderDataAccess = orderDataAccess;
		this.connection = connection;
		this.replyChannel = replyChannel;
		this.authorizingAccountMessageProcessor = authorizingAccountMessageProcessor;
	}

	@Override
	public Saga instantiateSaga(
		long sagaId, @Nonnull OrderAggregate order, @Nonnull String idempotenceToken, @Nonnull String sagaType
	) throws Exception {
		return switch (sagaType) {
			case SagaTypes.PLACE_ORDERED -> {
				yield instantiateSaga(sagaId, order, idempotenceToken, sagaType, getInitialState(sagaType).getId());
			}
			default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public Saga.State getInitialState(@Nonnull String sagaType) {
		return switch (sagaType) {
			case SagaTypes.PLACE_ORDERED -> PlaceOrderSaga.State.AUTHORIZING_ACCOUNT;
			default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public Saga instantiateSaga(
		long sagaId, @Nonnull OrderAggregate order, @Nonnull String idempotenceToken, @Nonnull String sagaType, long sagaStateId
	) throws Exception {
		return switch (sagaType) {
			case SagaTypes.PLACE_ORDERED -> {

				Channel ch = connection.createChannel();
				ch.confirmSelect();
				RMQAccountService rmqAccountService =
					new RMQAccountService(ch, replyChannel, authorizingAccountMessageProcessor);
				AuthorizingAccountState authorizingAccountState =
					new AuthorizingAccountState(
						rmqAccountService, sagaId, order.getAccountId(), authorizingAccountMessageProcessor
					);

				LocalInventoryService localInventoryService = new LocalInventoryService(itemDataAccess);
				VerifyingOrderDetailsState verifyingOrderDetailsState =
					new VerifyingOrderDetailsState(localInventoryService, sagaId, order.getLineItems());

				LocalOrderService localOrderService =
					new LocalOrderService(orderDataAccess);
				ConfirmingPlacingState confirmingPlacingState =
					new ConfirmingPlacingState(localOrderService, sagaId);
				AbortingPlacingState abortingPlacingState =
					new AbortingPlacingState(localOrderService, sagaId);

				Map<Saga.State, SagaState> stateObjects = Map.of(
					PlaceOrderSaga.State.AUTHORIZING_ACCOUNT, authorizingAccountState,
					PlaceOrderSaga.State.VERIFYING_ORDER_DETAILS, verifyingOrderDetailsState,
					PlaceOrderSaga.State.CONFIRMING_PLACING, confirmingPlacingState,
					PlaceOrderSaga.State.ABORTING_PLACING, abortingPlacingState
				);

				PlaceOrderSaga.State state = PlaceOrderSaga.State.fromId(sagaStateId);
				SagaState sagaState = stateObjects.get(state);
				sagaState.setIdempotenceToken(idempotenceToken);
				yield new PlaceOrderSaga(sagaId, order.getId(), state, stateObjects);
			}
			default -> throw new IllegalArgumentException();
		};
	}
}
