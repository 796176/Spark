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

import org.example.spark.gateway.web.models.LineItem;
import org.example.spark.gateway.web.models.NewOrderForm;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewOrderFormConverter implements HttpMessageConverter<NewOrderForm> {

	private final FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();

	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return clazz.equals(NewOrderForm.class);
	}

	@Override
	public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
		return false;
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return formHttpMessageConverter.getSupportedMediaTypes();
	}

	@Override
	public NewOrderForm read(
		Class<? extends NewOrderForm> clazz, HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		Map<String, String> map = formHttpMessageConverter.read(null, inputMessage).asSingleValueMap();
		Long timestamp = null;
		ArrayList<LineItem> lineItems = new ArrayList<>();
		for (Map.Entry<String, String> entry: map.entrySet()) {
			switch (entry.getKey()) {
				case "timestamp" -> {
					try {
						timestamp = Long.parseLong(entry.getValue());
					} catch (NumberFormatException ignored) { }
				}
				default -> {
					try {
						LineItem lineItem =
							new LineItem(Long.parseLong(entry.getKey()), Integer.parseInt(entry.getValue()));
						if (lineItem.amount() > 0) lineItems.add(lineItem);
					} catch (NumberFormatException ignored) { }
				}
			}
		}
		return new NewOrderForm(timestamp, lineItems.toArray(LineItem[]::new));
	}

	@Override
	public void write(
		NewOrderForm newOrderForm, @Nullable MediaType contentType, HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException {

	}
}
