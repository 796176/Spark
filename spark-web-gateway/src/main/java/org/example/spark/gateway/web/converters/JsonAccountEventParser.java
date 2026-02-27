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
import org.example.spark.authorization.Role;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.ArrayList;
import java.util.Objects;

public class JsonAccountEventParser implements AccountEventParser {
	@Override
	public AccountCreatedEvent parseAccountCreatedEvent(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			String accountId = null, accountName = null, encodedPassword = null;
			ArrayList<Role> roles = null;
			String key;
			while (jsonParser.nextToken() != null) {
				key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "account_id" -> {
						jsonParser.nextToken();
						accountId = jsonParser.getValueAsString();
					}
					case "account_name" -> {
						jsonParser.nextToken();
						accountName = jsonParser.getValueAsString();
					}
					case "encoded_password" -> {
						jsonParser.nextToken();
						encodedPassword = jsonParser.getValueAsString();
					}
					case "roles" -> {
						jsonParser.nextToken();
						roles = new ArrayList<>();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							roles.add(Role.fromId(Long.parseLong(jsonParser.getValueAsString())));
						}
					}
				}
			}
			if (anyNull(accountId, accountName, encodedPassword, roles)) throw new IllegalArgumentException();
			return new AccountCreatedEvent(
				Long.parseLong(accountId), accountName, encodedPassword, roles.toArray(new Role[0])
			);
		}
	}

	@Override
	public AccountStatusUpdatedEvent parseAccountStatusUpdatedEvent(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			String key;
			while (jsonParser.nextToken() != null) {
				key = jsonParser.currentName();
				if (Objects.equals(key, "account_id")) {
					jsonParser.nextToken();
					return new AccountStatusUpdatedEvent(Long.parseLong(jsonParser.getValueAsString()));
				}
			}
			throw new IllegalArgumentException();
		}
	}

	@Override
	public AccountRolesUpdatedEvent parseAccountRolesUpdatedEvent(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			String accountId = null;
			ArrayList<Role> roles = null;
			String key;
			while (jsonParser.nextToken() != null) {
				key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "account_id" -> {
						jsonParser.nextToken();
						accountId = jsonParser.getValueAsString();
					}
					case "roles" -> {
						jsonParser.nextToken();
						roles = new ArrayList<>();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							roles.add(Role.fromId(Long.parseLong(jsonParser.getValueAsString())));
						}
					}
				}
			}
			if (anyNull(accountId, roles)) throw new IllegalArgumentException();
			return new AccountRolesUpdatedEvent(Long.parseLong(accountId), roles.toArray(new Role[0]));
		}
	}

	private boolean anyNull(Object... objects) {
		for (Object o: objects) {
			if (o == null) return true;
		}
		return false;
	}
}
