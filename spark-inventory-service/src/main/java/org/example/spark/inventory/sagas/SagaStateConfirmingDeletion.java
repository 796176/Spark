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

import java.util.UUID;

public class SagaStateConfirmingDeletion implements SagaState {

	private final long sagaId;

	private final InventoryServiceProxy inventoryService;

	private final String correlationId;

	private String idempotenceToken;

	public SagaStateConfirmingDeletion(long sagaId, @Nonnull InventoryServiceProxy inventoryService) {
		this.sagaId = sagaId;
		this.inventoryService = inventoryService;
		this.correlationId = UUID.randomUUID().toString();
	}

	@Override
	public String getIdempotenceToken() {
		return idempotenceToken;
	}

	@Override
	public void setIdempotenceToken(@Nonnull String idempotenceToken) {
		this.idempotenceToken = idempotenceToken;
	}

	@Override
	public long getSagaId() {
		return sagaId;
	}

	@Override
	public SagaState initialize(@Nonnull Saga saga) throws Exception {
		inventoryService.confirmDeletion(this, saga.getItemId(), correlationId);
		return this;
	}

	@Override
	public boolean canProcess(
		@Nonnull Saga saga,
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) {
		return this.correlationId.equals(correlationId);
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
		if (!this.correlationId.equals(correlationId)) throw new IllegalArgumentException();
		if (statusCode != 0) System.out.println("log error");

		saga.setCompleted();
		return new SagaStateNoOp(getSagaId());
	}
}
