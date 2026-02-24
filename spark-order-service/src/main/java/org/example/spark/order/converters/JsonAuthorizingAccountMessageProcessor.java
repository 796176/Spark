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

package org.example.spark.order.converters;

import jakarta.annotation.Nonnull;
import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class JsonAuthorizingAccountMessageProcessor implements AuthorizingAccountMessageProcessor {

	private record MessageDetailsImpl(String contentType, String version, byte[] body) implements MessageDetails {

		@Override
		public byte[] getBody() {
			return body;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public String getVersion() {
			return version;
		}
	}

	@Override
	public MessageDetails createRequest(long accountId) {
		JsonFactory jsonFactory = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(ObjectWriteContext.empty(), os)) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("account_id", Long.toString(accountId));
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();
			return new MessageDetailsImpl("application/json", "1.0", os.toByteArray());
		}
	}

	@Override
	public boolean isAuthorized(@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body) {
		if (!(contentType.equals("application/json") && version.equals("1.0"))) throw new IllegalArgumentException();

		JsonFactory jsonFactory = new JsonFactory();
		try (JsonParser jsonParser = jsonFactory.createParser(ObjectReadContext.empty(), body)) {
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				if (Objects.equals(jsonParser.currentName(), "authorized_placing_orders")) {
					jsonParser.nextToken();
					return Boolean.parseBoolean(jsonParser.getValueAsString());
				}
			}
			throw new IllegalArgumentException();
		}
	}
}
