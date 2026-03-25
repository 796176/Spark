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

package org.example.spark.gateway.web.models;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class RemoteCallResult {

	private final boolean successful;

	private final byte[] resultBody;

	private final String version;

	private final String contentType;

	private final Class<? extends Exception> errorType;

	private final String errorMessage;

	public RemoteCallResult(
		boolean successful,
		@Nonnull byte[] resultBody,
		@Nonnull String version,
		@Nonnull String contentType,
		@Nullable Class<? extends Exception> errorType,
		@Nullable String errorMessage
	) {
		this.successful = successful;
		this.resultBody = resultBody;
		this.version = version;
		this.contentType = contentType;
		this.errorType = errorType;
		this.errorMessage = errorMessage;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public byte[] getResultBody() {
		return resultBody;
	}

	public String getVersion() {
		return version;
	}

	public String getContentType() {
		return contentType;
	}

	@Nullable
	public Class<? extends Exception> getErrorType() {
		return errorType;
	}

	@Nullable
	public String getFormattedErrorMessage() {
		return errorMessage;
	}
}
