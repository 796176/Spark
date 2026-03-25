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
import org.example.spark.gateway.web.exceptions.ServerError;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.Objects;

public class JsonErrorMessageParser implements ErrorMessageParser {

	@Override
	public ParsedError parse(@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body) {
		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			String exceptionType = null, exceptionMessage = null;
			while (jsonParser.nextToken() != null) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "exception_type" -> {
						jsonParser.nextToken();
						exceptionType = jsonParser.getValueAsString();
					}
					case "exception_message" -> {
						jsonParser.nextToken();
						exceptionMessage = jsonParser.getValueAsString();
					}
				}
			}

			Class<? extends Exception> errorType = null;
			if (exceptionType != null) {
				try {
					errorType = (Class<? extends Exception>) Class.forName(exceptionType);
				} catch (ClassNotFoundException | ClassCastException ignored) {
					errorType = ServerError.class;
				}
			}
			return new ParsedError(errorType, exceptionMessage);
		}
	}
}
