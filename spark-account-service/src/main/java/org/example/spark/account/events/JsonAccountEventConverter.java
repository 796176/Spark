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

package org.example.spark.account.events;

import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class JsonAccountEventConverter implements AccountEventConverter<String> {
	@Override
	public <S extends AccountEvent> S convert(String s, Class<S> c) {
		JsonParser jsonParser = new JsonFactory().createParser(ObjectReadContext.empty(), s);
		if (c.equals(AccountCreated.class)) {
			String version = null, accountId = null, name = null, encodedPassword = null;
			String[] roles = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "version" -> version = jsonParser.getValueAsString();
					case "account_id" -> accountId = jsonParser.getValueAsString();
					case "name" -> name = jsonParser.getValueAsString();
					case "encoded_password" -> encodedPassword = jsonParser.getValueAsString();
					case "roles" -> {
						jsonParser.nextToken();
						ArrayList<String> arrayList = new ArrayList<>();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							arrayList.add(jsonParser.getValueAsString());
						}
						roles = arrayList.toArray(new String[0]);
					}
				}
			}
			if (anyNull(version, accountId, name, encodedPassword, roles)) throw new IllegalArgumentException();

			if (!version.equals("1.0")) throw new IllegalArgumentException();
			return c.cast(new AccountCreatedImpl(
				Long.parseLong(accountId),
				name,
				encodedPassword,
				Arrays.stream(roles).mapToLong(Long::parseLong).toArray()
			));
		} else if (c.equals(AccountDeleted.class)) {
			String version = null, accountId = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "version" -> version = jsonParser.getString();
					case "account_id" -> accountId = jsonParser.getString();
				}
			}
			if (anyNull(version, accountId)) throw new IllegalArgumentException();

			if (!version.equals("1.0")) throw new IllegalArgumentException();
			return c.cast(new AccountDeletedImpl(Long.parseLong(accountId)));
		} else if (c.equals(AccountSuspended.class)) {
			String version = null, accountId = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "version" -> version = jsonParser.getValueAsString();
					case "account_id" -> accountId = jsonParser.getValueAsString();
				}
			}
			if (anyNull(version, accountId)) throw new IllegalArgumentException();

			if (!version.equals("1.0")) throw new IllegalArgumentException();
			return c.cast(new AccountSuspendedImpl(Long.parseLong(accountId)));
		} else if (c.equals(AccountRestored.class)) {
			String version = null, accountId = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "version" -> version = jsonParser.getValueAsString();
					case "account_id" -> accountId = jsonParser.getValueAsString();
				}
			}
			if (anyNull(version, accountId)) throw new IllegalArgumentException();

			if (!version.equals("1.0")) throw new IllegalArgumentException();
			return c.cast(new AccountRestoredImpl(Long.parseLong(accountId)));
		} else if (c.equals(AccountRolesUpdated.class)) {
			String version = null, accountId = null;
			String[] roles = null;
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String key = jsonParser.currentName();
				switch (Objects.requireNonNullElse(key, "")) {
					case "version" -> version = jsonParser.getValueAsString();
					case "account_id" -> accountId = jsonParser.getValueAsString();
					case "roles" -> {
						jsonParser.nextToken();
						ArrayList<String> arrayList = new ArrayList<>();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							arrayList.add(jsonParser.getString());
						}
						roles = arrayList.toArray(new String[0]);
					}
				}
			}
			if (anyNull(version, accountId, roles)) throw new IllegalArgumentException();

			if (!version.equals("1.0")) throw new IllegalArgumentException();
			return c.cast(
				new AccountRolesUpdatedImpl(
					Long.parseLong(accountId),
					Arrays.stream(roles).mapToLong(Long::parseLong).toArray()
				)
			);
		}

		throw new IllegalArgumentException();
	}

	private boolean anyNull(Object... objects) {
		for (Object o: objects) {
			if (o == null) return true;
		}
		return false;
	}

	@Override
	public String convert(AccountEvent accountEvent) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringProperty("version", "1.0");
		if (accountEvent instanceof AccountCreated accountCreated) {
			jsonGenerator.writeStringProperty("account_id", Long.toString(accountCreated.getAccountId()));
			jsonGenerator.writeStringProperty("name", accountCreated.getName());
			jsonGenerator.writeStringProperty("encoded_password", accountCreated.getEncodedPassword());
			jsonGenerator.writeName("roles");
			jsonGenerator.writeArray(
				Arrays
					.stream(accountCreated.getRoles())
					.mapToObj(Long::toString)
					.toArray(String[]::new),
				0,
				accountCreated.getRoles().length
			);
		} else if (accountEvent instanceof AccountStatusUpdated accountStatusUpdated) {
			jsonGenerator
				.writeStringProperty("account_id", Long.toString(accountStatusUpdated.getAccountId()));
		} else if (accountEvent instanceof AccountRolesUpdated accountRolesUpdated) {
			jsonGenerator
				.writeStringProperty("account_id", Long.toString(accountRolesUpdated.getAccountId()));
			jsonGenerator.writeName("roles");
			jsonGenerator.writeArray(
				Arrays
					.stream(accountRolesUpdated.getRoles())
					.mapToObj(Long::toString)
					.toArray(String[]::new),
				0,
				accountRolesUpdated.getRoles().length
			);
		}
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

		return os.toString();
	}

	@Override
	public String getEncodingFormat() {
		return "application/json";
	}
}
