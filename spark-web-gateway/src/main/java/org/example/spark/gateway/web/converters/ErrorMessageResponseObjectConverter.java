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

import org.example.spark.gateway.web.models.ErrorFormSubmissionResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.ObjectWriteContext;
import tools.jackson.core.json.JsonFactory;

import java.io.IOException;
import java.util.List;

public class ErrorMessageResponseObjectConverter implements HttpMessageConverter<ErrorFormSubmissionResponse> {

	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return false;
	}

	@Override
	public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
		return clazz.equals(ErrorFormSubmissionResponse.class);
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return List.of(MediaType.APPLICATION_JSON);
	}

	@Override
	public ErrorFormSubmissionResponse read(
		Class<? extends ErrorFormSubmissionResponse> clazz,
		HttpInputMessage inputMessage
	) throws IOException, HttpMessageNotReadableException {
		return null;
	}

	@Override
	public void write(
		ErrorFormSubmissionResponse errorFormSubmissionResponse,
		@Nullable MediaType contentType,
		HttpOutputMessage outputMessage
	) throws IOException, HttpMessageNotWritableException {
		JsonFactory jsonFactory = new JsonFactory();
		try (JsonGenerator jsonGenerator =
				 jsonFactory.createGenerator(ObjectWriteContext.empty(), outputMessage.getBody())
		) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("error", errorFormSubmissionResponse.errorMessage());
			jsonGenerator.writeEndObject();
			jsonGenerator.flush();
		}
	}
}
