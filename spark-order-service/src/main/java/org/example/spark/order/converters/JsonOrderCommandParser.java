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
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.ArrayList;
import java.util.Objects;

public class JsonOrderCommandParser implements OrderCommandParser {

	@Override
	public PlacingOrderCommand parsePlacingOrderCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			String accountId = null, timestamp = null;
			ArrayList<LineItem> lineItems = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				switch (Objects.requireNonNullElse(jsonParser.currentName(), "")) {
					case "account_id" -> {
						jsonParser.nextToken();
						accountId = jsonParser.getValueAsString();
					}
					case "timestamp" -> {
						jsonParser.nextToken();
						timestamp = jsonParser.getValueAsString();
					}
					case "line_items" -> {
						lineItems = new ArrayList<>();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							String itemId = null, amount = null;
							while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
								switch (Objects.requireNonNullElse(jsonParser.currentName(), "")) {
									case "item_id" -> {
										jsonParser.nextToken();
										itemId = jsonParser.getValueAsString();
									}
									case "amount" -> {
										jsonParser.nextToken();
										amount = jsonParser.getValueAsString();
									}
								}
							}
							if (!anyNull(itemId, amount)) {
								lineItems.add(new LineItem(Long.parseLong(itemId), Integer.parseInt(amount)));
							}
						}
					}
				}
			}
			if (anyNull(accountId, timestamp, lineItems)) throw new IllegalArgumentException();
			return new PlacingOrderCommand(
				Long.parseLong(accountId), Long.parseLong(timestamp), lineItems.toArray(new LineItem[0])
			);
		}
	}

	@Override
	public UpdatingOrderStatusCommand parseUpdatingOrderStatusCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			String orderId = null, orderVersion = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				switch (Objects.requireNonNullElse(jsonParser.currentName(), "")) {
					case "order_id" -> {
						jsonParser.nextToken();
						orderId = jsonParser.getValueAsString();
					}
					case "version" -> {
						jsonParser.nextToken();
						orderVersion = jsonParser.getValueAsString();
					}
				}
			}
			if (anyNull(orderId, orderVersion)) throw new IllegalArgumentException();
			return new UpdatingOrderStatusCommand(Long.parseLong(orderId), Long.parseLong(orderVersion));
		}
	}

	@Override
	public RetrievingOrderCommand parseRetrievingOrderCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				if (Objects.equals(jsonParser.currentName(), "order_id")) {
					jsonParser.nextToken();
					return new RetrievingOrderCommand(Long.parseLong(jsonParser.getValueAsString()));
				}
			}
			throw new IllegalArgumentException();
		}
	}

	@Override
	public RetrievingOrdersByAccountCommand parseRetrievingOrdersByAccountCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				if (Objects.equals(jsonParser.currentName(), "account_id")) {
					jsonParser.nextToken();
					return new RetrievingOrdersByAccountCommand(Long.parseLong(jsonParser.getValueAsString()));
				}
			}
			throw new IllegalArgumentException();
		}
	}

	private boolean anyNull(Object... objects) {
		for (Object o: objects) {
			if (o == null) return true;
		}
		return false;
	}
}
