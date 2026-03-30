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
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;

public class JsonOrderServiceCommandEncoder implements OrderServiceCommandEncoder {
	@Override
	public EncodedCommand getOrders(long accountId) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(accountId));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand getOrder(long orderId) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("order_id", Long.toString(orderId));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand cancelOrder(long orderId, @Nonnull String version) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("order_id", Long.toString(orderId));
			jsonGenerator.writeStringProperty("version", version);
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand restoreOrder(long orderId, @Nonnull String version) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("order_id", Long.toString(orderId));
			jsonGenerator.writeStringProperty("version", version);
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand placeOrder(long accountId, long timestamp, @Nonnull LineItem[] lineItems) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(accountId));
			jsonGenerator.writeStringProperty("timestamp", Long.toString(timestamp));

			jsonGenerator.writeArrayPropertyStart("line_items");
			for (LineItem lineItem: lineItems) {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringProperty("item_id", Long.toString(lineItem.itemId()));
				jsonGenerator.writeStringProperty("amount", Integer.toString(lineItem.amount()));
				jsonGenerator.writeEndObject();
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}
}
