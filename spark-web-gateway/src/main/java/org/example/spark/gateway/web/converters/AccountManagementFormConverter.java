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

import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.AccountManagementForm;
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
import java.util.*;

public class AccountManagementFormConverter implements HttpMessageConverter<AccountManagementForm> {

	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return clazz.equals(AccountManagementForm.class);
	}

	@Override
	public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
		return false;
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return List.of(MediaType.APPLICATION_JSON);
	}

	@Override
	public AccountManagementForm read(
		Class<? extends AccountManagementForm> clazz, HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), inputMessage.getBody())) {
			Account.Status previousStatus = null, currentStatus = null;
			Role[] previouslyAssignedRoles = null;
			ArrayList<Role> currentlyAssignedRoles = new ArrayList<>();
			while (jsonParser.nextToken() != null) {
				String key = Objects.requireNonNullElse(jsonParser.currentName(), "");
				switch (key) {
					case "previously_assigned_roles" -> {
						jsonParser.nextToken();
						try {
							previouslyAssignedRoles = Arrays
								.stream(jsonParser.getValueAsString().split(","))
								.map(s -> {
									try {
										return Role.fromId(Long.parseLong(s));
									} catch (IllegalArgumentException | NullPointerException ignored) { }
									return null;
								})
								.filter(Objects::nonNull)
								.toArray(Role[]::new);
						} catch (NullPointerException | IllegalArgumentException ignored) { }
					}
					case "previous_status" -> {
						jsonParser.nextToken();
						try {
							previousStatus = Account.Status.fromId(Long.parseLong(jsonParser.getValueAsString()));
						} catch (NullPointerException | IllegalArgumentException ignored) { }
					}
					case "account_status" -> {
						jsonParser.nextToken();
						try {
							currentStatus = Account.Status.fromId(Long.parseLong(jsonParser.getValueAsString()));
						} catch (NullPointerException | IllegalArgumentException ignored) { }
					}
					default -> {
						if (key.startsWith("role:")) {
							try {
								currentlyAssignedRoles.add(
									Role.fromId(Long.parseLong(key.substring(key.indexOf(":") + 1)))
								);
							} catch (NullPointerException | IllegalArgumentException | IndexOutOfBoundsException _) { }
							jsonParser.nextToken();
						}
					}
				}
			}
			return new AccountManagementForm(
				previouslyAssignedRoles,
				currentlyAssignedRoles.toArray(Role[]::new),
				previousStatus,
				Objects.requireNonNullElse(currentStatus, previousStatus)
			);
		}
	}

	@Override
	public void write(
		AccountManagementForm accountManagementForm, @Nullable MediaType contentType, HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException { }
}
