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
import org.example.spark.gateway.web.models.LogInForm;
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

public class LogInFormConverter implements HttpMessageConverter<LogInForm> {

	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return clazz.equals(LogInForm.class);
	}

	@Override
	public boolean canWrite(@Nonnull Class<?> clazz, @Nullable MediaType mediaType) {
		return false;
	}

	@Nonnull
	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return List.of(MediaType.APPLICATION_JSON);
	}

	@Override
	public LogInForm read(
		@Nonnull Class<? extends LogInForm> clazz, @Nonnull HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), inputMessage.getBody())) {
			String username = null, password = null;
			while (jsonParser.nextToken() != null) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "password" -> {
						jsonParser.nextToken();
						password = jsonParser.getValueAsString();
						if (password != null) password = password.strip();
					}
					case "username" -> {
						jsonParser.nextToken();
						username = jsonParser.getValueAsString();
						if (username != null) username = username.strip();
					}
				}
			}
			return new LogInForm(username, password);
		}
	}

	@Override
	public void write(
		LogInForm logInForm, @Nullable MediaType contentType, @Nonnull HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException {

	}
}
