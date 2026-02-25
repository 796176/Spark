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

package org.example.spark.inventory.utils;

import jakarta.annotation.Nonnull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TokenGenerator {

	private long iterator = 0;

	private final byte[] namespaceAndName;

	public TokenGenerator(@Nonnull String seed) {
		byte[] byteRepresentation = seed.getBytes(StandardCharsets.UTF_8);
		namespaceAndName = new byte[byteRepresentation.length + Long.BYTES];
		System.arraycopy(byteRepresentation, 0, namespaceAndName, 0, byteRepresentation.length);
		fillName();
	}

	public String nextToken() {
		String nextToken = UUID.nameUUIDFromBytes(namespaceAndName).toString();
		iterator++;
		fillName();
		return nextToken;
	}

	private void fillName() {
		int namespaceLength = namespaceAndName.length - Long.BYTES;
		for (int i = 0; i < Long.BYTES; i++) {
			int nameOffset = i;
			int rightShift = 8 * (Long.BYTES - 1 - i);
			namespaceAndName[namespaceLength + nameOffset] = (byte) ((iterator >>> rightShift) % 256);
		}
	}
}
