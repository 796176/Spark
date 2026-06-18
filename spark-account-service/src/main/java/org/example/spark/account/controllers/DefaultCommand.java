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

public class DefaultCommand implements Command {

	private final String contentType;

	private final String version;

	private final byte[] body;

	public DefaultCommand(@Nonnull String contentType, @Nonnull String version, @Nonnull byte[] body) {
		this.contentType = contentType;
		this.version = version;
		this.body = body;
	}

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
		return body;
	}
}
