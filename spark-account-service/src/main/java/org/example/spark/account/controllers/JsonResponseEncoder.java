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
import org.example.spark.account.models.RenderableAccount;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JsonResponseEncoder implements ResponseEncoder {

	public record JsonEncodedResponseProperties(
		String contentType, String version, byte[] responseBody
	) implements EncodedResponseProperties {
		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public String getVersion() {
			return version;
		}

		@Override
		public byte[] getBody() {
			return responseBody;
		}
	}

	@Override
	public EncodedResponseProperties encodeRenderableAccounts(@Nonnull RenderableAccount... renderableAccounts) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeName("accounts");
		jsonGenerator.writeStartArray();
		for (RenderableAccount renderableAccount: renderableAccounts) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", renderableAccount.getId());
			jsonGenerator.writeStringProperty("account_name", renderableAccount.getName());
			jsonGenerator.writeName("roles");
			String[] roles = Arrays
				.stream(renderableAccount.getRoles())
				.mapToObj(Long::toString)
				.toArray(String[]::new);
			jsonGenerator.writeArray(roles, 0, roles.length);
			jsonGenerator.writeStringProperty("account_status", renderableAccount.getStatus());
			jsonGenerator.writeEndObject();
		}
		jsonGenerator.writeEndArray();
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

		return new JsonEncodedResponseProperties("application/json", "1.0", os.toByteArray());
	}

	@Override
	public EncodedResponseProperties encodeThrowable(@Nonnull Throwable t) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os);
		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringProperty("exception-type", t.getClass().getName());
		jsonGenerator.writeStringProperty("exception-message", t.getMessage());
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

		return new JsonEncodedResponseProperties("application/json", "1.0", os.toByteArray());
	}

	@Override
	public EncodedResponseProperties emptyResponse() {
		return new JsonEncodedResponseProperties(
			"application/json",
			"1.0",
			"{}".getBytes(StandardCharsets.UTF_8)
		);
	}
}
