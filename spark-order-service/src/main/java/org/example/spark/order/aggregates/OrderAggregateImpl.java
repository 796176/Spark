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

package org.example.spark.order.aggregates;

import jakarta.annotation.Nonnull;
import org.example.spark.order.events.*;
import org.example.spark.order.models.LineItem;

public class OrderAggregateImpl implements OrderAggregate {

	private final long id;

	private final long accountId;

	private final long timestamp;

	private final LineItem[] lineItems;

	private Status status;

	public OrderAggregateImpl(
		long id, long accountId, long timestamp, @Nonnull LineItem[] lineItems, @Nonnull Status status
	) {
		this.id = id;
		this.accountId = accountId;
		this.timestamp = timestamp;
		this.lineItems = lineItems;
		this.status = status;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public long getAccountId() {
		return accountId;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public LineItem[] getLineItems() {
		return lineItems;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setTransientStatus(@Nonnull Status status) {
		if (status != Status.PLACING && status != Status.RESTORING) throw new IllegalArgumentException();

		this.status = status;
	}

	@Override
	public OrderCreated confirmPlacement() {
		status = Status.PENDING_ACCEPTANCE;
		return new OrderCreatedImpl(getId(), getAccountId(), getTimestamp(), getLineItems());
	}

	@Override
	public OrderAccepted accept() {
		status = Status.ACCEPTED;
		return new OrderAcceptedImpl(getId());
	}

	@Override
	public OrderRejected reject() {
		status = Status.REJECTED;
		return new OrderRejectedImpl(getId());
	}

	@Override
	public OrderCancelled cancel() {
		status = Status.CANCELLED;
		return new OrderCancelledImpl(getId());
	}

	@Override
	public void abortPlacing() {
		status = Status.PLACING_ABORTED;
	}
}
