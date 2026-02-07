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

package org.example.spark.inventory.converters;

import jakarta.annotation.Nonnull;
import org.example.spark.inventory.controllers.ItemEventConverter;
import org.example.spark.inventory.events.*;
import org.example.spark.inventory.models.Money;
import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class JsonItemEventConverter extends ItemEventConverter<String> {

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
	public <S extends ItemEvent> S convert(
		@Nonnull String contentType, @Nonnull String version, @Nonnull String s, @Nonnull Class<S> c
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), s);

		if (c.equals(ItemCreated.class)) {
			String itemId = null, name = null, amount = null;
			Money money = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "item_id" -> {
						jsonParser.nextToken();
						itemId = jsonParser.getValueAsString();
					}
					case "item_name" -> {
						jsonParser.nextToken();
						name = jsonParser.getValueAsString();
					}
					case "amount" -> {
						jsonParser.nextToken();
						amount = jsonParser.getValueAsString();
					}
					case "price" -> {
						String currencyAmount = null, centAmount = null;
						while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
							String nestedKey = jsonParser.currentName();
							switch (Objects.requireNonNullElse(nestedKey, "")) {
								case "currency_amount" -> {
									jsonParser.nextToken();
									currencyAmount = jsonParser.getValueAsString();
								}
								case "cent_amount" -> {
									jsonParser.nextToken();
									centAmount = jsonParser.getValueAsString();
								}
							}
						}
						if (anyNull(currencyAmount, centAmount)) throw new IllegalArgumentException();

						money = new Money(Integer.parseInt(currencyAmount), Integer.parseInt(centAmount));
					}
				}
			}
			if (anyNull(itemId, name, amount, money)) throw new IllegalArgumentException();

			return c.cast(
				new ItemCreatedImpl(
					Long.parseLong(itemId),
					name,
					Integer.parseInt(amount),
					money
				)
			);
		} else if (c.equals(ItemAmountUpdated.class)) {
			String itemId = null, delta = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "item_id" -> {
						jsonParser.nextToken();
						itemId = jsonParser.getValueAsString();
					}
					case "delta" -> {
						jsonParser.nextToken();
						delta = jsonParser.getValueAsString();
					}
				}
			}
			if (anyNull(itemId, delta)) throw new IllegalArgumentException();

			return c.cast(new ItemAmountUpdatedImpl(Long.parseLong(itemId), Integer.parseInt(delta)));
		} else if (c.equals(ItemDeleted.class)) {
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				if (Objects.equals(key, "item_id")) {
					jsonParser.nextToken();
					return c.cast(new ItemDeletedImpl(Long.parseLong(jsonParser.getValueAsString())));
				}
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
	public ItemEventConverter<String>.EncodedEventProperties convert(@Nonnull ItemEvent itemEvent) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		if (itemEvent instanceof ItemCreated itemCreated) {
			jsonGenerator.writeStringProperty("item_id", Long.toString(itemCreated.getItemId()));
			jsonGenerator.writeStringProperty("item_name", itemCreated.getName());
			jsonGenerator.writeStringProperty("amount", Integer.toString(itemCreated.getAmount()));
			jsonGenerator.writeName("price");
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty(
				"currency_amount", Integer.toString(itemCreated.getPrice().currencyAmount())
			);
			jsonGenerator.writeStringProperty(
				"cent_amount", Integer.toString(itemCreated.getPrice().centAmount())
			);
			jsonGenerator.writeEndObject();
		} else if (itemEvent instanceof ItemAmountUpdated itemAmountUpdated) {
			jsonGenerator.writeStringProperty("item_id", Long.toString(itemAmountUpdated.getItemId()));
			jsonGenerator.writeStringProperty("delta", Integer.toString(itemAmountUpdated.getDelta()));
		} else if (itemEvent instanceof ItemDeleted itemDeleted) {
			jsonGenerator.writeStringProperty("item_id", Long.toString(itemDeleted.getItemId()));
		}
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

		return new JsonEncodedEventProperties("application/json", "1.0", os.toString());
	}
}
