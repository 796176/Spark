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
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class AccountManagementFormConverter implements HttpMessageConverter<AccountManagementForm> {

	private final FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
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
		return formHttpMessageConverter.getSupportedMediaTypes();
	}

	@Override
	public AccountManagementForm read(
		Class<? extends AccountManagementForm> clazz, HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		Map<String, String> map = formHttpMessageConverter.read(null, inputMessage).asSingleValueMap();

		ArrayList<Role> currentlyAssignedRoles = new ArrayList<>();
		for (String key: map.keySet()) {
			if (key.startsWith("role:")) {
				try {
					currentlyAssignedRoles.add(
						Role.fromId(Long.parseLong(key.substring(key.indexOf(":") + 1)))
					);
				} catch (NullPointerException | IllegalArgumentException ignored) { }
			}
		}
		Role[] previouslyAssignedRoles = null;
		if (map.containsKey("previously_assigned_roles")) {
			try {
				previouslyAssignedRoles = Arrays
					.stream(map.get("previously_assigned_roles").split(","))
					.map(s -> Role.fromId(Long.parseLong(s)))
					.toArray(Role[]::new);
			} catch (NullPointerException | IllegalArgumentException ignored) { }
		}

		Account.Status previousStatus = null;
		if (map.containsKey("previous_status")) {
			try {
				previousStatus = Account.Status.fromId(Long.parseLong(map.get("previous_status")));
			} catch (NullPointerException | IllegalArgumentException ignored) { }
		}
		Account.Status currentStatus = null;
		if (map.containsKey("account_status") || previousStatus != null) {
			try {
				currentStatus = Account.Status.fromId(
					Long.parseLong(
						Objects.requireNonNullElse(map.get("account_status"), map.get("previous_status"))
					)
				);
			} catch (NullPointerException | IllegalArgumentException ignored) { }
		}

		return new AccountManagementForm(
			previouslyAssignedRoles,
			currentlyAssignedRoles.toArray(Role[]::new),
			previousStatus,
			currentStatus
		);
	}

	@Override
	public void write(
		AccountManagementForm accountManagementForm, @Nullable MediaType contentType, HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException { }
}
