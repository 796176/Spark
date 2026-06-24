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

package org.example.spark.inventory.sagas;

import jakarta.annotation.Nonnull;

import java.util.Map;

public class DeleteItemSaga implements Saga {

	public enum State implements StateEnumeration {
		INVALIDATING_ITEM(0), CONFIRMING_DELETION(1), ABORTING_DELETION(2);

		private final int id;

		State(int id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public static State fromId(long id) {
			if (id == 0) return INVALIDATING_ITEM;
			if (id == 1) return CONFIRMING_DELETION;
			if (id == 2) return ABORTING_DELETION;
			throw new IllegalArgumentException();
		}
	}

	private final long sagaId;

	private final long itemId;

	private State state;

	private SagaState stateObject;

	private boolean hasCompleted = false;

	private final Map<StateEnumeration, SagaState> stateObjects;

	public DeleteItemSaga(
		long sagaId,
		long itemId,
		@Nonnull State state,
		@Nonnull Map<StateEnumeration, SagaState> stateObjects
	) {
		this.sagaId = sagaId;
		this.itemId = itemId;
		this.state = state;
		this.stateObjects = stateObjects;
		this.stateObject = stateObjects.get(state);
	}

	@Override
	public long getId() {
		return sagaId;
	}

	@Override
	public long getItemId() {
		return itemId;
	}

	@Override
	public Saga.StateEnumeration getState() {
		return state;
	}

	@Override
	public void setState(@Nonnull StateEnumeration stateEnumeration) {
		if (stateEnumeration instanceof DeleteItemSaga.State deleteItemSagaState) {
			this.state = deleteItemSagaState;
			this.stateObject = stateObjects.get(getState());
		}
	}

	@Override
	public SagaState getStateObject() {
		return stateObject;
	}

	@Override
	public Map<StateEnumeration, SagaState> getStateObjects() {
		return stateObjects;
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
		return stateObject.canProcess(this, correlationId, messageType, contentType, statusCode, version, body);
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
		stateObject.executeNextStep(this, correlationId, messageType, contentType, statusCode, version, body);
	}

	@Override
	public boolean proceedNextState() throws Exception {
		return stateObject.initialize(this);
	}
}
