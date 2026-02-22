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
import org.example.spark.order.events.*;
import org.example.spark.order.models.LineItem;
import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class JsonOrderEventConverter extends OrderEventConverter<String> {

	public class JsonEncodedEventProperties extends EncodedEventProperties {

		private final String contentType;

		private final String version;

		private final String body;

		private JsonEncodedEventProperties(String contentType, String version, String body) {
			this.contentType = contentType;
			this.version = version;
			this.body = body;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public String getVersion() {
			return version;
		}

		@Override
		public String getBody() {
			return body;
		}
	}

	@Override
	public <S extends OrderEvent> S convert(
		@Nonnull String contentType, @Nonnull String version, @Nonnull String s, @Nonnull Class<S> c
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), s);
		if (c.equals(OrderCreated.class)) {
			String orderId = null, accountId = null, timestamp = null;
			LineItem[] lineItems = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "order_id" -> {
						jsonParser.nextToken();
						orderId = jsonParser.getValueAsString();
					}
					case "account_id" -> {
						jsonParser.nextToken();
						accountId = jsonParser.getValueAsString();
					}
					case "timestamp" -> {
						jsonParser.nextToken();
						timestamp = jsonParser.getValueAsString();
					}
					case "line_items" -> {
						ArrayList<LineItem> lineItemsArrayList = new ArrayList<>();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							String itemId = null, amount = null;
							while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
								String nestedKey = jsonParser.currentName();
								switch (Objects.requireNonNullElse(nestedKey, "")) {
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
							if (itemId != null & amount != null) {
								lineItemsArrayList.add(new LineItem(Long.parseLong(itemId), Integer.parseInt(amount)));
							}
						}
						lineItems = lineItemsArrayList.toArray(new LineItem[0]);
					}
				}
			}
			if (anyNull(orderId, accountId, timestamp, lineItems)) throw new IllegalArgumentException();

			return c.cast(new OrderCreatedImpl(
				Long.parseLong(orderId), Long.parseLong(accountId), Long.parseLong(timestamp), lineItems
			));
		} else if (OrderStatusUpdated.class.isAssignableFrom(c)) {
			String orderId = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				if (Objects.equals(jsonParser.currentName(), "order_id")) {
					jsonParser.nextToken();
					orderId = jsonParser.getValueAsString();
					break;
				}
			}
			if (orderId == null) throw new IllegalArgumentException();

			if (c.equals(OrderAccepted.class)) {
				return c.cast(new OrderAcceptedImpl(Long.parseLong(orderId)));
			} else if (c.equals(OrderCanceled.class)) {
				return c.cast(new OrderCanceledImpl(Long.parseLong(orderId)));
			} else if (c.equals(OrderRejected.class)) {
				return c.cast(new OrderRejectedImpl(Long.parseLong(orderId)));
			}
		}

		throw new IllegalArgumentException();
	}

	private boolean anyNull(Object... objects) {
		for (Object o: objects) {
			if (o == null) return true;
		}
		return false;
	}

	@Override
	public OrderEventConverter<String>.EncodedEventProperties convert(@Nonnull OrderEvent orderEvent) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		switch (orderEvent) {
			case OrderCreated orderCreated -> {
				jsonGenerator.writeStringProperty("order_id", Long.toString(orderCreated.getOrderId()));
				jsonGenerator.writeStringProperty("account_id", Long.toString(orderCreated.getAccountId()));
				jsonGenerator.writeStringProperty("timestamp", Long.toString(orderCreated.getTimestamp()));

				jsonGenerator.writeArrayPropertyStart("line_items");
				for (LineItem lineItem: orderCreated.getLineItems()) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringProperty("item_id", Long.toString(lineItem.itemId()));
					jsonGenerator.writeStringProperty("amount", Integer.toString(lineItem.amount()));
					jsonGenerator.writeEndObject();
				}
				jsonGenerator.writeEndArray();
			}
			case OrderStatusUpdated orderStatusUpdated -> {
				jsonGenerator
					.writeStringProperty("order_id", Long.toString(orderStatusUpdated.getOrderId()));
			}
			default -> {}
		}
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

		return new JsonEncodedEventProperties("application/json", "1.0", os.toString());
	}
}
