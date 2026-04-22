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
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CreatingItemFormConverter implements HttpMessageConverter<CreatingItemForm> {

	private final FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();

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
		return formHttpMessageConverter.getSupportedMediaTypes();
	}

	@Override
	public CreatingItemForm read(
		Class<? extends CreatingItemForm> clazz, HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		Map<String, String> map = formHttpMessageConverter.read(null, inputMessage).asSingleValueMap();

		String name = map.get("item_name") == null ? null : map.get("item_name").strip();

		Money price = null;
		if (map.containsKey("item_price")) {
			String[] itemPrice = map.get("item_price").split("\\.", 2);
			try {
				price = new Money(Integer.parseInt(itemPrice[0]), Integer.parseInt(itemPrice[1]));
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException ignored) { }
		}

		Integer amount = null;
		if (map.containsKey("item_amount")) {
			try {
				amount = Integer.parseInt(map.get("item_amount"));
			} catch (NullPointerException | NumberFormatException ignored) { }
		}
		return new CreatingItemForm(name, price, amount);
	}

	@Override
	public void write(
		CreatingItemForm creatingItemForm, @Nullable MediaType contentType, HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException { }
}
