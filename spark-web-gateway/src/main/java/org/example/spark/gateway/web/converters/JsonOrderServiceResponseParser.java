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

package org.example.spark.gateway.web.converters;

import jakarta.annotation.Nonnull;
import org.example.spark.gateway.web.models.LineItem;
import org.example.spark.gateway.web.models.Order;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.ArrayList;
import java.util.Objects;

public class JsonOrderServiceResponseParser implements OrderServiceResponseParser {
	@Override
	public Order[] parseGettingOrdersByAccountResponse(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try(JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			ArrayList<Order> orders = null;
			while (jsonParser.nextToken() != null) {
				if (Objects.equals(jsonParser.currentName(), "orders")) {
					jsonParser.nextToken();
					orders = new ArrayList<>();
					while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
						String orderId = null, timestamp = null, status = null, orderVersion = null;
						ArrayList<LineItem> lineItems = null;
						while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
							String key = jsonParser.currentName();
							switch (Objects.requireNonNullElse(key, "")) {
								case "order_id" -> {
									jsonParser.nextToken();
									orderId = jsonParser.getValueAsString();
								}
								case "timestamp" -> {
									jsonParser.nextToken();
									timestamp = jsonParser.getValueAsString();
								}
								case "status" -> {
									jsonParser.nextToken();
									status = jsonParser.getValueAsString();
								}
								case "version" -> {
									jsonParser.nextToken();
									orderVersion = jsonParser.getValueAsString();
								}
								case "line_items" -> {
									jsonParser.nextToken();
									lineItems = new ArrayList<>();
									while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
										String itemId = null, amount = null;
										while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
											key = jsonParser.currentName();
											switch (Objects.requireNonNullElse(key, "")) {
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
											lineItems.add(
												new LineItem(Long.parseLong(itemId), Integer.parseInt(amount))
											);
										}
									}
								}
							}
						}
						if (!anyNull(orderId, timestamp, status, lineItems, orderVersion)) {
							orders.add(
								new Order(
									Long.parseLong(orderId),
									Long.parseLong(timestamp),
									Order.Status.valueOf(status),
									orderVersion,
									lineItems.toArray(LineItem[]::new)
								)
							);
						}
					}
				}
			}
			if (orders == null) throw new IllegalArgumentException();
			return orders.toArray(Order[]::new);
		}
	}

	@Override
	public Order parseGettingOrderResponse(@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try(JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			while (jsonParser.nextToken() != null) {
				if (Objects.equals(jsonParser.currentName(), "orders")) {
					jsonParser.nextToken();
					while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
						String orderId = null, timestamp = null, status = null, orderVersion = null;
						ArrayList<LineItem> lineItems = null;
						while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
							String key = jsonParser.currentName();
							switch (Objects.requireNonNullElse(key, "")) {
								case "order_id" -> {
									jsonParser.nextToken();
									orderId = jsonParser.getValueAsString();
								}
								case "timestamp" -> {
									jsonParser.nextToken();
									timestamp = jsonParser.getValueAsString();
								}
								case "status" -> {
									jsonParser.nextToken();
									status = jsonParser.getValueAsString();
								}
								case "version" -> {
									jsonParser.nextToken();
									orderVersion = jsonParser.getValueAsString();
								}
								case "line_items" -> {
									jsonParser.nextToken();
									lineItems = new ArrayList<>();
									while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
										String itemId = null, amount = null;
										while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
											key = jsonParser.currentName();
											switch (Objects.requireNonNullElse(key, "")) {
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
											lineItems.add(
												new LineItem(Long.parseLong(itemId), Integer.parseInt(amount))
											);
										}
									}
								}
							}
						}
						if (!anyNull(orderId, timestamp, status, lineItems, orderVersion)) {
							return new Order(
									Long.parseLong(orderId),
									Long.parseLong(timestamp),
									Order.Status.valueOf(status),
									orderVersion,
									lineItems.toArray(LineItem[]::new)
							);
						}
					}
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
