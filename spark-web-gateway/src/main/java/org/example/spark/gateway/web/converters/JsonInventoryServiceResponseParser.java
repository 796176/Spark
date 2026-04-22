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
import org.example.spark.gateway.web.models.Item;
import org.example.spark.gateway.web.models.Money;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.ArrayList;
import java.util.Objects;

public class JsonInventoryServiceResponseParser implements InventoryServiceResponseParser {

	@Override
	public Item[] parseGettingItemsResponse(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			ArrayList<Item> items = null;
			while (jsonParser.nextToken() != null) {
				if (Objects.equals(jsonParser.currentName(), "items")) {
					jsonParser.nextToken();
					items = new ArrayList<>();
					while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
						String itemId = null, name = null, amount = null, itemVersion = null;
						Money price = null;
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
								case "version" -> {
									jsonParser.nextToken();
									itemVersion = jsonParser.getValueAsString();
								}
								case "price" -> {
									String currencyAmount = null, centAmount = null;
									while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
										key = jsonParser.currentName();
										switch (Objects.requireNonNullElse(key, "")) {
											case "currency_amount" -> {
												jsonParser.nextToken();
												currencyAmount = jsonParser.getValueAsString();
											}
											case "cent_amount" -> {
												jsonParser.nextToken();
												centAmount = jsonParser.getValueAsString();
											}
										}
										if (!anyNull(currencyAmount, centAmount)) {
											price = new Money(
												Integer.parseInt(currencyAmount), Integer.parseInt(centAmount)
											);
										}
									}
								}
							}
						}
						if (!anyNull(itemId, name, amount, price, itemVersion)) {
							try {
								items.add(new Item(
									Long.parseLong(itemId), name, price, Integer.parseInt(amount), itemVersion
								));
							} catch (IllegalArgumentException ignored) { }
						}
					}
				}
			}
			if (items == null) throw new IllegalArgumentException();
			return items.toArray(Item[]::new);
		}
	}

	@Override
	public Item parseGettingItemResponse(@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			while (jsonParser.nextToken() != null) {
				if (Objects.equals(jsonParser.currentName(), "items")) {
					jsonParser.nextToken();
					while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
						String itemId = null, name = null, amount = null, itemVersion = null;
						Money price = null;
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
								case "version" -> {
									jsonParser.nextToken();
									itemVersion = jsonParser.getValueAsString();
								}
								case "price" -> {
									String currencyAmount = null, centAmount = null;
									while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
										key = jsonParser.currentName();
										switch (Objects.requireNonNullElse(key, "")) {
											case "currency_amount" -> {
												jsonParser.nextToken();
												currencyAmount = jsonParser.getValueAsString();
											}
											case "cent_amount" -> {
												jsonParser.nextToken();
												centAmount = jsonParser.getValueAsString();
											}
										}
										if (!anyNull(currencyAmount, centAmount)) {
											price = new Money(
												Integer.parseInt(currencyAmount), Integer.parseInt(centAmount)
											);
										}
									}
								}
							}
						}
						if (!anyNull(itemId, name, amount, price, itemVersion)) {
							try {
								return new Item(
									Long.parseLong(itemId), name, price, Integer.parseInt(amount), itemVersion
								);
							} catch (IllegalArgumentException ignored) { }
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
