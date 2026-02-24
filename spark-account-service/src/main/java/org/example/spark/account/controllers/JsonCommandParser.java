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
import org.example.spark.authorization.Role;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.*;

public class JsonCommandParser implements CommandParser {

	public static class ParsedCommandImpl implements ParsedCommand {

		private final byte[] commandBody;

		private final HashMap<String, String> properties = new HashMap<>();

		private Password password;

		public ParsedCommandImpl(@Nonnull byte[] commandBody) {
			this.commandBody = commandBody;
		}

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

		@Override
		public void destroy() {
			Arrays.fill(commandBody, (byte) 0);
		}
	}

	@Override
	public ParsedCommand parse(@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		ParsedCommandImpl parsedMessage = new ParsedCommandImpl(body);
		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body);
		while (jsonParser.nextToken() != null) {
			String key = jsonParser.currentName();
			if (Objects.equals(key, "password")) {
				jsonParser.nextToken();
				Password password = new PasswordImpl(
					Arrays.copyOfRange(
						jsonParser.getStringCharacters(), jsonParser.getStringOffset(), jsonParser.getStringLength()
					)
				);
				parsedMessage.setPassword(password);
			} else if (key != null) {
				jsonParser.nextToken();
				parsedMessage.getProperties().put(key, jsonParser.getString());
			}
		}
		return parsedMessage;
	}

	@Override
	public ChangingRolesCommand parseChangingRolesCommand(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] bytes
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), bytes)) {
			String accountId = null;
			ArrayList<Role> roles = null;
			while (jsonParser.nextToken() != null) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "account_id" -> {
						jsonParser.nextToken();
						accountId = jsonParser.getValueAsString();
					}
					case "roles" -> {
						roles = new ArrayList<>();
						jsonParser.nextToken();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							roles.add(Role.fromId(Long.parseLong(jsonParser.getValueAsString())));
						}
					}
				}
			}
			if (anyNull(accountId, roles)) throw new IllegalArgumentException();

			return new ChangingRolesCommand(Long.parseLong(accountId), roles.toArray(new Role[0]));
		}
	}

	private boolean anyNull(Object... objects) {
		for (Object o: objects) {
			if (o == null) return true;
		}
		return false;
	}
}
