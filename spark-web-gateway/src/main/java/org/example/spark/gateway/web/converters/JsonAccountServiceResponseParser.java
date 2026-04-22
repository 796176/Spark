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
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.AccountImpl;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.ArrayList;
import java.util.Objects;

public class JsonAccountServiceResponseParser implements AccountServiceResponseParser {
	@Override
	public Account[] parseGettingAccountsResponse(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0")))	throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			ArrayList<Account> accounts = null;
			while (jsonParser.nextToken() != null) {
				String key = jsonParser.currentName();
				if (Objects.equals(key, "accounts")) {
					accounts = new ArrayList<>();
					jsonParser.nextToken();
					while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
						String accountId = null, accountName = null, status = null;
						ArrayList<Role> roles = null;
						while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
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
								case "account_status" -> {
									jsonParser.nextToken();
									status = jsonParser.getValueAsString();
								}
								case "roles" -> {
									jsonParser.nextToken();
									roles = new ArrayList<>();
									while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
										try {
											roles.add(Role.fromId(Long.parseLong(jsonParser.getValueAsString())));
										} catch (NumberFormatException ignored) { }
									}
								}
							}
						}

						if (!anyNull(accountId, accountName, status, roles)) {
							try {
								accounts.add(
									new AccountImpl(
										Long.parseLong(accountId),
										"",
										accountName,
										roles.toArray(Role[]::new),
										Account.Status.valueOf(status)
									)
								);
							} catch (IllegalArgumentException ignored) { }
						}
					}
				}
			}

			if (accounts == null) throw new IllegalArgumentException();
			return accounts.toArray(Account[]::new);
		}
	}

	@Override
	public Account parseGettingAccountResponse(
		@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body
	) {
		if (!(contentType.equals("application/json") && version.equals("1.0")))	throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			while (jsonParser.nextToken() != null) {
				String key = jsonParser.currentName();
				if (Objects.equals(key, "accounts")) {
					jsonParser.nextToken();
					while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
						String accountId = null, accountName = null, status = null;
						ArrayList<Role> roles = null;
						while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
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
								case "account_status" -> {
									jsonParser.nextToken();
									status = jsonParser.getValueAsString();
								}
								case "roles" -> {
									jsonParser.nextToken();
									roles = new ArrayList<>();
									while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
										try {
											roles.add(Role.fromId(Long.parseLong(jsonParser.getValueAsString())));
										} catch (NumberFormatException ignored) { }
									}
								}
							}
						}

						if (!anyNull(accountId, accountName, status, roles)) {
							try {
								return new AccountImpl(
									Long.parseLong(accountId),
									"",
									accountName,
									roles.toArray(Role[]::new),
									Account.Status.valueOf(status)
								);
							} catch (IllegalArgumentException ignored) { }
						}
					}
				}
			}

			throw new IllegalArgumentException();
		}
	}

	private boolean anyNull(Object... objects) {
		for (Object o: objects) {
			if (o == null) return true;
		}
		return false;
	}
}
