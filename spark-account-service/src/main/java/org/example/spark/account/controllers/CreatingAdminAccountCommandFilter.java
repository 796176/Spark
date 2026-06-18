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
import org.example.spark.account.converters.AccountServiceCommandEncoder;

import java.util.Objects;

public class CreatingAdminAccountCommandFilter implements CommandFilter {

	private final String latestVersion = "2.0";

	private final CommandParser commandParser;

	private final AccountServiceCommandEncoder accountServiceCommandEncoder;

	private final PasswordEncoder passwordEncoder;

	public CreatingAdminAccountCommandFilter(
		@Nonnull AccountServiceCommandEncoder accountServiceCommandEncoder,
		@Nonnull CommandParser commandParser,
		@Nonnull PasswordEncoder passwordEncoder
	) {
		this.commandParser = commandParser;
		this.accountServiceCommandEncoder = accountServiceCommandEncoder;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Command wrap(Command command) {
		return new Command() {
			@Override
			public String getContentType() {
				return command.getContentType();
			}

			@Override
			public String getVersion() {
				return latestVersion;
			}

			@Override
			public byte[] getBody() {
				if (command.getVersion().equals(latestVersion)) {
					return command.getBody();
				} else {
					CommandParser.ParsedCommand creatingAccountCommand = commandParser.parse(
						command.getContentType(), command.getVersion(), command.getBody()
					);
					String encodedPassword = passwordEncoder.encode(creatingAccountCommand.getPassword());
					creatingAccountCommand.getPassword().destroy();
					creatingAccountCommand.destroy();
					return accountServiceCommandEncoder
						.encodeCreatingAdministratorAccountCommand2(
							Objects.requireNonNull(creatingAccountCommand.getValue("account_name")),
							encodedPassword
						)
						.body();
				}
			}
		};
	}
}
