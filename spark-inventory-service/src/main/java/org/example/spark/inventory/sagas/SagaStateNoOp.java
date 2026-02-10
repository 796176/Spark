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

public class SagaStateNoOp implements SagaState {

	private final long sagaId;

	public SagaStateNoOp(long sagaId) {
		this.sagaId = sagaId;
	}

	public SagaStateNoOp() {
		this(-1);
	}

	@Override
	public String getIdempotenceToken() {
		return "";
	}

	@Override
	public void setIdempotenceToken(@Nonnull String idempotenceToken) { }

	@Override
	public long getSagaId() {
		return sagaId;
	}

	@Override
	public SagaState initialize(@Nonnull Saga saga) {
		return this;
	}

	@Override
	public boolean canProcess(
		@Nonnull Saga saga,
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int StatusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) {
		return false;
	}

	@Override
	public SagaState executeNextStep(
		@Nonnull Saga saga,
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) {
		return this;
	}
}
