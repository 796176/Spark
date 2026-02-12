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

package org.example.spark.order.events;

import jakarta.annotation.Nonnull;
import org.example.spark.order.models.LineItem;

public class OrderCreatedImpl implements OrderCreated {

	private final long orderId;

	private final long accountId;

	private final long timestamp;

	private final LineItem[] lineItems;

	public OrderCreatedImpl(long orderId, long accountId, long timestamp, @Nonnull LineItem[] lineItems) {
		this.orderId = orderId;
		this.accountId = accountId;
		this.timestamp = timestamp;
		this.lineItems = lineItems;
	}

	@Override
	public long getOrderId() {
		return orderId;
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
}
