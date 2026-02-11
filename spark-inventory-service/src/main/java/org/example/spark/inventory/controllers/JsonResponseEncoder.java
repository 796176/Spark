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

package org.example.spark.inventory.controllers;

import jakarta.annotation.Nonnull;
import org.example.spark.inventory.models.RenderableItem;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class JsonResponseEncoder implements ResponseEncoder {

	public static class JsonEncodedResponseProperties implements EncodedResponseProperties {

		private final String contentType;

		private final String version;

		private final byte[] body;

		private JsonEncodedResponseProperties(String contentType, String version, byte[] body) {
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
		public byte[] getBody() {
			return body;
		}
	}

	@Override
	public EncodedResponseProperties encodeRenderableItems(@Nonnull RenderableItem... renderableItems) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeArrayPropertyStart("items");
		for (RenderableItem renderableItem: renderableItems) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("item_id", Long.toString(renderableItem.getId()));
			jsonGenerator.writeStringProperty("item_name", renderableItem.getName());
			jsonGenerator.writeStringProperty("amount", Integer.toString(renderableItem.getAmount()));
			jsonGenerator.writeStringProperty("version", Long.toString(renderableItem.getVersion()));

			jsonGenerator.writeObjectPropertyStart("price");
			jsonGenerator.writeStringProperty(
				"currency_amount", Integer.toString(renderableItem.getPrice().currencyAmount())
			);
			jsonGenerator.writeStringProperty(
				"cent_amount", Integer.toString(renderableItem.getPrice().centAmount())
			);
			jsonGenerator.writeEndObject();

			jsonGenerator.writeEndObject();
		}
		jsonGenerator.writeEndArray();
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

		return new JsonEncodedResponseProperties("application/json", "1.0", os.toByteArray());
	}

	@Override
	public EncodedResponseProperties encodeThrowable(@Nonnull Throwable t) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringProperty("exception_type", t.getClass().getName());
		jsonGenerator.writeStringProperty("exception_message", t.getMessage());
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

		return new JsonEncodedResponseProperties("application/json", "1.0", os.toByteArray());
	}

	@Override
	public EncodedResponseProperties emptyResponse() {
		return new JsonEncodedResponseProperties(
			"application/json", "1.0", "{}".getBytes(StandardCharsets.UTF_8)
		);
	}
}
