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
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.Objects;

public class JsonErrorMessageParser implements ErrorMessageParser {

	@Nullable
	@Override
	public String parse(@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body) {
		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			while (jsonParser.nextToken() != null) {
				if (Objects.equals(jsonParser.currentName(), "exception_message")) {
					jsonParser.nextToken();
					return jsonParser.getValueAsString();
				}
			}
		}
		return null;
	}
}
