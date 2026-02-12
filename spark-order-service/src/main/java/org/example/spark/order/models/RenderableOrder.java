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

package org.example.spark.order.models;

import jakarta.annotation.Nonnull;

public class RenderableOrder {

	private final long orderId;

	private final long accountId;

	private final long timestamp;

	private final LineItem[] lineItems;

	private final String status;

	private final long version;

	public RenderableOrder(
		long orderId,
		long accountId,
		long timestamp,
		@Nonnull LineItem[] lineItems,
		@Nonnull String status,
		long version
	) {
		this.orderId = orderId;
		this.accountId = accountId;
		this.timestamp = timestamp;
		this.lineItems = lineItems;
		this.status = status;
		this.version = version;
	}

	public long getOrderId() {
		return orderId;
	}

	public long getAccountId() {
		return accountId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public LineItem[] getLineItems() {
		return lineItems;
	}

	public String getStatus() {
		return status;
	}

	public long getVersion() {
		return version;
	}
}
