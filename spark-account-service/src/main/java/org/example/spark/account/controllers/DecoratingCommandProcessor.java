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

public class DecoratingCommandProcessor implements CommandProcessor {

	private final CommandProcessor commandProcessor;

	private final CreatingAccountCommandFilter creatingAccountCommandFilter;

	private final CreatingAdminAccountCommandFilter creatingAdminAccountCommandFilter;

	public DecoratingCommandProcessor(
		@Nonnull CommandProcessor commandProcessor,
		@Nonnull CreatingAccountCommandFilter creatingAccountCommandFilter,
		@Nonnull CreatingAdminAccountCommandFilter creatingAdminAccountCommandFilter
	) {
		this.commandProcessor = commandProcessor;
		this.creatingAccountCommandFilter = creatingAccountCommandFilter;
		this.creatingAdminAccountCommandFilter = creatingAdminAccountCommandFilter;
	}

	@Override
	public void processCommand(
		@Nonnull String commandType,
		long callerId,
		@Nonnull long[] callerRoles,
		@Nonnull String commandId,
		@Nonnull Command command,
		@Nonnull Response callback
	) {
		if (commandType.equals("org.example.spark.account.create-admin-account")) {
			command = creatingAdminAccountCommandFilter.wrap(command);
		} else if (commandType.equals("org.example.spark.account.create-account")) {
			command = creatingAccountCommandFilter.wrap(command);
		}
		commandProcessor.processCommand(commandType, callerId, callerRoles, commandId, command, callback);
	}
}
