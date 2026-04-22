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

import org.example.spark.gateway.web.models.CreatingAccountForm;
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

public class CreatingAccountFormConverter implements HttpMessageConverter<CreatingAccountForm> {

	private final FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return clazz.equals(CreatingAccountForm.class);
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
	public CreatingAccountForm read(
		Class<? extends CreatingAccountForm> clazz, HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		Map<String, String> map = formHttpMessageConverter.read(null, inputMessage).asSingleValueMap();
		String username = map.get("username") == null ? null : map.get("username").strip();
		String password = map.get("password") == null ? null : map.get("password").strip();
		return new CreatingAccountForm(username, password, map.containsKey("is_admin"));
	}

	@Override
	public void write(
		CreatingAccountForm creatingAccountForm, @Nullable MediaType contentType, HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException { }
}
