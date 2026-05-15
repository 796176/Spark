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

import org.example.spark.gateway.web.models.Order;
import org.example.spark.gateway.web.models.OrderManagementForm;
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

public class OrderManagementFormConverter implements HttpMessageConverter<OrderManagementForm> {

	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return clazz.equals(OrderManagementForm.class);
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
	public OrderManagementForm read(
		Class<? extends OrderManagementForm> clazz, HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), inputMessage.getBody())) {
			Order.Status previousStatus = null, currentStatus = null;
			String version = null;
			while (jsonParser.nextToken() != null) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "previous_order_status" -> {
						jsonParser.nextToken();
						try {
							previousStatus = Order.Status.fromId(Long.parseLong(jsonParser.getValueAsString()));
						} catch (IllegalArgumentException | NullPointerException ignored) { }
					}
					case "order_status" -> {
						jsonParser.nextToken();
						try {
							currentStatus = Order.Status.fromId(Long.parseLong(jsonParser.getValueAsString()));
						} catch (IllegalArgumentException | NullPointerException ignored) { }
					}
					case "order_version" -> {
						jsonParser.nextToken();
						version = jsonParser.getValueAsString();
					}
				}
			}
			return new OrderManagementForm(
				previousStatus, Objects.requireNonNullElse(currentStatus, previousStatus), version
			);
		}
	}

	@Override
	public void write(
		OrderManagementForm form, @Nullable MediaType contentType, HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException { }
}
