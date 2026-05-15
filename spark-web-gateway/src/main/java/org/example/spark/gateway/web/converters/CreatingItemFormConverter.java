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

import org.example.spark.gateway.web.models.CreatingItemForm;
import org.example.spark.gateway.web.models.Money;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CreatingItemFormConverter implements HttpMessageConverter<CreatingItemForm> {

	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return clazz.equals(CreatingItemForm.class);
	}

	@Override
	public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
		return false;
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return List.of(MediaType.APPLICATION_JSON);
	}

	@Override
	public CreatingItemForm read(
		Class<? extends CreatingItemForm> clazz, HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), inputMessage.getBody())) {
			String name = null;
			Integer amount = null;
			Money price = null;
			while (jsonParser.nextToken() != null) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "item_name" -> {
						jsonParser.nextToken();
						name = jsonParser.getValueAsString();
					}
					case "item_price" -> {
						jsonParser.nextToken();
						try {
							String value = jsonParser.getValueAsString();
							price = new Money(
								Integer.parseInt(value.substring(0, value.indexOf('.'))),
								Integer.parseInt(value.substring(value.indexOf('.') + 1))
							);
						} catch (NullPointerException | IllegalArgumentException | IndexOutOfBoundsException _) { }
					}
					case "item_amount" -> {
						jsonParser.nextToken();
						try {
							amount = Integer.parseInt(jsonParser.getValueAsString());
						} catch (NullPointerException | IllegalArgumentException ignored) { }
					}
				}
			}
			return new CreatingItemForm(name, price, amount);
		}
	}

	@Override
	public void write(
		CreatingItemForm creatingItemForm, @Nullable MediaType contentType, HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException { }
}
