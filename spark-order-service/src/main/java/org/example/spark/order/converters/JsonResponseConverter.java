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
import org.example.spark.order.models.RenderableOrder;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class JsonResponseConverter implements ResponseConverter {

	private record JsonConvertedResponse(String contentType, String version, byte[] body) implements ConvertedResponse {

		@Override
		public byte[] getBody() {
			return body;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public String getVersion() {
			return version;
		}
	}

	@Override
	public ConvertedResponse convertRenderableOrders(@Nonnull RenderableOrder... orders) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeArrayPropertyStart("orders");
			for (RenderableOrder renderableOrder: orders) {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringProperty("order_id", Long.toString(renderableOrder.getOrderId()));
				jsonGenerator
					.writeStringProperty("account_id", Long.toString(renderableOrder.getAccountId()));
				jsonGenerator
					.writeStringProperty("timestamp", Long.toString(renderableOrder.getTimestamp()));
				jsonGenerator.writeStringProperty("status", renderableOrder.getStatus());
				jsonGenerator.writeStringProperty("version", Long.toString(renderableOrder.getVersion()));

				jsonGenerator.writeArrayPropertyStart("line_items");
				for (LineItem lineItem: renderableOrder.getLineItems()) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringProperty("item_id", Long.toString(lineItem.itemId()));
					jsonGenerator.writeStringProperty("amount", Long.toString(lineItem.amount()));
					jsonGenerator.writeEndObject();
				}
				jsonGenerator.writeEndArray();

				jsonGenerator.writeEndObject();
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();
			return new JsonConvertedResponse("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public ConvertedResponse convertThrowable(@Nonnull Throwable throwable) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("exception_type", throwable.getClass().getName());
			jsonGenerator.writeStringProperty("exception_message", throwable.getMessage());
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();
			return new JsonConvertedResponse("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public ConvertedResponse emptyResponse() {
		return new JsonConvertedResponse(
			"application/json", "1.0", "{}".getBytes(StandardCharsets.UTF_8)
		);
	}
}
