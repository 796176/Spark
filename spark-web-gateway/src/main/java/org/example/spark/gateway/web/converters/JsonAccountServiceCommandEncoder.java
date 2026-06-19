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
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JsonAccountServiceCommandEncoder implements AccountServiceCommandEncoder {

	@Override
	public EncodedCommand encodeCreatingAccountCommand(@Nonnull String name, @Nonnull String encodedPassword) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_name", name);
			jsonGenerator.writeStringProperty("encoded_password", encodedPassword);
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "2.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeDeletingAccountCommand(long id) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(id));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeCreatingAdministratorAccountCommand(@Nonnull String name, @Nonnull String password) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_name", name);
			jsonGenerator.writeStringProperty("encoded_password", password);
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "2.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeSuspendingAccountCommand(long id) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(id));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeRestoringAccountCommand(long id) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(id));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeGettingAccountsCommand() {
		return new EncodedCommand("application/json", "1.0", "{}".getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public EncodedCommand encodeGettingAccountCommand(long id) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(id));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public EncodedCommand encodeUpdatingRolesCommand(long id, @Nonnull Role[] roles) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(id));
			jsonGenerator.writeName("roles");
			jsonGenerator.writeArray(
				Arrays.stream(roles).map(role -> Long.toString(role.getId())).toArray(String[]::new),
				0,
				roles.length
			);
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();

			return new EncodedCommand("application/json", "1.0", os.toByteArray());
		}
	}
}
