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
import org.example.spark.account.models.Password;
import org.springframework.security.crypto.password.AbstractValidatingPasswordEncoder;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

public class SpringPasswordEncoder implements PasswordEncoder {

	private final AbstractValidatingPasswordEncoder encoder;

	public SpringPasswordEncoder(@Nonnull AbstractValidatingPasswordEncoder encoder) {
		this.encoder = encoder;
	}

	@Override
	public String encode(Password password) {
		char[] chars = new char[password.getPassword().length];
		CharBuffer charBuffer = CharBuffer.wrap(chars);
		CharsetDecoder charsetDecoder = Charset.defaultCharset().newDecoder();
		CoderResult result = charsetDecoder.decode(ByteBuffer.wrap(password.getPassword()), charBuffer, true);
		if (result.isError()) throw new IllegalArgumentException();

		charBuffer.flip();
		String encodedPassword = encoder.encode(charBuffer);
		Arrays.fill(chars, '\u0000');

		return encodedPassword;
	}
}
