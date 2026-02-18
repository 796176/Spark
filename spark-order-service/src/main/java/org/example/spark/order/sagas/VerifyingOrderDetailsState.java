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
import org.example.spark.order.models.LineItem;

import java.util.UUID;

public class VerifyingOrderDetailsState implements SagaState {

	private final InventoryServiceProxy inventoryService;

	private final long sagaId;

	private final LineItem[] lineItems;

	private final String correlationId;

	private String idempotenceToken;

	public VerifyingOrderDetailsState(
		@Nonnull InventoryServiceProxy inventoryService, long sagaId, @Nonnull LineItem[] lineItems
	) {
		this.inventoryService = inventoryService;
		this.sagaId = sagaId;
		this.lineItems = lineItems;
		correlationId = UUID.randomUUID().toString();
	}
	@Override
	public long getSagaId() {
		return sagaId;
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
	public boolean initialize(@Nonnull Saga saga) throws Exception {
		return inventoryService.verifyOrderDetails(this, saga, lineItems, correlationId);
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
	public void executeNextStep(
		@Nonnull Saga saga,
		@Nonnull String correlationId,
		@Nonnull String messageType,
		@Nonnull String contentType,
		int statusCode,
		@Nonnull String version,
		@Nonnull byte[] body
	) throws Exception {
		if (!this.correlationId.equals(correlationId)) throw new IllegalStateException();
		if (statusCode != 0) {
			System.out.println("log error");
			saga.setCompleted();
		}

		PlaceOrderSaga.State nextState;
		if ((new String(body).contains("true"))) {
			nextState = PlaceOrderSaga.State.CONFIRMING_PLACING;
		} else {
			nextState = PlaceOrderSaga.State.ABORTING_PLACING;
		}
		saga.setState(nextState, saga.getStateObjects().get(nextState));
	}
}
