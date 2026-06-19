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

package org.example.spark.gateway.web.controllers;

import jakarta.annotation.Nonnull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.CharBuffer;

public class SpringPasswordEncoder implements PasswordEncoder {

	private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

	@Override
	public String encode(@Nonnull char[] password) {
		return bCryptPasswordEncoder.encode(CharBuffer.wrap(password));
	}

	@Override
	public boolean matches(@Nonnull char[] rawPassword, @Nonnull String encodedPassword) {
		return bCryptPasswordEncoder.matches(CharBuffer.wrap(rawPassword), encodedPassword);
	}

	@Override
	public boolean matches(@Nonnull String rawPassword, @Nonnull String encodedPassword) {
		return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
	}
}
