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
import jakarta.annotation.Nullable;
import org.example.spark.gateway.web.models.Money;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class JsonInventoryServiceCommandEncoder implements InventoryServiceCommandEncoder {
	@Override
	public EncodedCommand encodeGettingItemsCommand() {
		return new EncodedCommand("application/json", "1.0", "{}".getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public EncodedCommand encodeAddingItemCommand(
		@Nonnull String name, @Nonnull Money price, int amount, @Nullable String pictureName
	) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("item_name", name);
			jsonGenerator.writeStringProperty("amount", Integer.toString(amount));
			if (pictureName != null) {
				jsonGenerator.writeStringProperty("item_picture_name", pictureName);
			}

			jsonGenerator.writeObjectPropertyStart("price");
			jsonGenerator.writeStringProperty("currency_amount", Integer.toString(price.currencyAmount()));
			jsonGenerator.writeStringProperty("cent_amount", Integer.toString(price.centAmount()));
			jsonGenerator.writeEndObject();

			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeGettingItemCommand(long id) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("item_id", Long.toString(id));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeDeletingItemCommand(long id) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("item_id", Long.toString(id));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeUpdatingItemAmountCommand(long id, int newAmount, @Nonnull String version) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("item_id", Long.toString(id));
			jsonGenerator.writeStringProperty("amount", Integer.toString(newAmount));
			jsonGenerator.writeStringProperty("version", version);
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}
}
