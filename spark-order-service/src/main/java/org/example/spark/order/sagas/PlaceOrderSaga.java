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

import jakarta.annotation.Nonnull;

import java.util.Map;

public class PlaceOrderSaga implements Saga {

	public enum State implements Saga.State {

		AUTHORIZING_ACCOUNT(0L),

		VERIFYING_ORDER_DETAILS(1L),

		CONFIRMING_PLACING(2L),

		ABORTING_PLACING(3L);

		private final long id;

		State(long id) {
			this.id = id;
		}

		@Override
		public long getId() {
			return id;
		}

		public static State fromId(long id) {
			if (id == 0) return AUTHORIZING_ACCOUNT;
			if (id == 1) return VERIFYING_ORDER_DETAILS;
			if (id == 2) return CONFIRMING_PLACING;
			if (id == 3) return ABORTING_PLACING;
			throw new IllegalArgumentException();
		}
	}

	private final long id;

	private final long orderId;

	private Saga.State state;

	private SagaState stateObject;

	private final Map<Saga.State, SagaState> stateObjects;

	private boolean hasCompleted = false;

	public PlaceOrderSaga(
		long id, long orderId, @Nonnull Saga.State currentState, @Nonnull Map<Saga.State, SagaState> stateObjects
	) {
		this.id = id;
		this.orderId = orderId;
		state = currentState;
		stateObject = stateObjects.get(state);
		this.stateObjects = stateObjects;
	}

	@Override
	public String getType() {
		return SagaTypes.PLACE_ORDERED;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getOrderId() {
		return orderId;
	}

	@Override
	public Map<Saga.State, SagaState> getStateObjects() {
		return stateObjects;
	}

	@Override
	public Saga.State getState() {
		return state;
	}

	@Override
	public SagaState getStateObject() {
		return stateObject;
	}

	@Override
	public void setState(@Nonnull Saga.State state, @Nonnull SagaState stateObject) {
		this.state = state;
		this.stateObject = stateObject;
	}

	@Override
	public void setCompleted() {
		hasCompleted = true;
	}

	@Override
	public boolean hasCompleted() {
		return hasCompleted;
	}

	@Override
	public boolean canProcess(
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) {
		return getStateObject()
			.canProcess(this, correlationId, messageType, contentType, statusCode, version, body);
	}

	@Override
	public void concludeCurrentState(
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) throws Exception {
		getStateObject().executeNextStep(this, correlationId, messageType, contentType, statusCode, version, body);
	}

	@Override
	public boolean proceedNextState() throws Exception {
		return getStateObject().initialize(this);
	}
}
