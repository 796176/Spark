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

public interface Saga {

	interface StateEnumeration { }

	long getId();

	long getItemId();

	StateEnumeration getState();

	void setState(@Nonnull StateEnumeration state);

	Map<StateEnumeration, SagaState> getStateObjects();

	SagaState getStateObject();

	void setStateObject(@Nonnull SagaState deleteItemSagaState);

	void setCompleted();

	boolean hasCompleted();

	boolean canProcess(
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	);

	void concludeCurrentState(
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) throws Exception;

	void proceedNextState() throws Exception;
}
