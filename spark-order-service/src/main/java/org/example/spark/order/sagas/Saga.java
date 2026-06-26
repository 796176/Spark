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

public interface Saga {

	interface State {

		long getId();
	}

	String getType();

	long getId();

	long getOrderId();

	Map<State, SagaState> getStateObjects();

	State getState();

	SagaState getStateObject();

	void setState(@Nonnull State state, @Nonnull SagaState stateObject);

	void setCompleted();

	boolean hasCompleted();

	boolean canProcess(
		@Nonnull String correlationId,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	);

	void concludeCurrentState(
		@Nonnull String correlationId,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) throws Exception;

	boolean proceedNextState() throws Exception;
}
