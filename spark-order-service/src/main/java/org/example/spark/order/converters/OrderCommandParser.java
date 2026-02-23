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

package org.example.spark.order.converters;

import jakarta.annotation.Nonnull;
import org.example.spark.order.models.LineItem;

public interface OrderCommandParser {

	record PlacingOrderCommand(long accountId, long orderTimestamp, LineItem[] lineItems) { }

	PlacingOrderCommand parsePlacingOrderCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	);

	record UpdatingOrderStatusCommand(long orderId, long orderVersion) { }

	UpdatingOrderStatusCommand parseUpdatingOrderStatusCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	);

	record RetrievingOrderCommand(long orderId) { }

	RetrievingOrderCommand parseRetrievingOrderCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	);

	record RetrievingOrdersByAccountCommand(long accountId) { }

	RetrievingOrdersByAccountCommand parseRetrievingOrdersByAccountCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	);

	record InvalidatingItemCommand(long itemId) { }

	InvalidatingItemCommand parseInvalidatingItemCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] bytes
	);
}
