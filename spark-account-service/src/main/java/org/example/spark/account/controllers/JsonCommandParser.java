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

package org.example.spark.account.controllers;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.example.spark.account.models.Password;
import org.example.spark.account.models.PasswordImpl;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonCommandParser implements CommandParser {

	public static class ParsedCommandImpl implements ParsedCommand {

		private final HashMap<String, String> properties = new HashMap<>();

		private Password password;

		public Map<String, String> getProperties() {
			return properties;
		}

		@Nullable
		@Override
		public String getValue(@Nonnull String key) {
			return properties.get(key);
		}

		@Nullable
		@Override
		public Password getPassword() {
			return password;
		}

		public void setPassword(Password password) {
			this.password = password;
		}
	}

	@Override
	public ParsedCommand parse(@Nonnull String encodingFormat, @Nonnull String version, @Nonnull byte[] body) {
		if (!(encodingFormat.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		ParsedCommandImpl parsedMessage = new ParsedCommandImpl();
		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body);
		while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
			String key = jsonParser.currentName();
			if (Objects.equals(key, "password")) {
				jsonParser.nextToken();
				Password password = new PasswordImpl(
					new String(jsonParser.getStringCharacters(), jsonParser.getStringOffset(), jsonParser.getStringLength())
						.getBytes()
				);
				parsedMessage.setPassword(password);
			} else if (key != null) {
				jsonParser.nextToken();
				parsedMessage.getProperties().put(key, jsonParser.getString());
			}
		}
		return parsedMessage;
	}
}
