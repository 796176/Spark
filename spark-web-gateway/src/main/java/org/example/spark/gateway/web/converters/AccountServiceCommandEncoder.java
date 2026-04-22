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

import jakarta.annotation.Nonnull;
import org.example.spark.authorization.Role;

public interface AccountServiceCommandEncoder {

	record EncodedCommand(String contentType, String version, byte[] body) { }

	EncodedCommand encodeCreatingAccountCommand(@Nonnull String name, @Nonnull String password);

	EncodedCommand encodeDeletingAccountCommand(long id);

	EncodedCommand encodeCreatingAdministratorAccountCommand(@Nonnull String name, @Nonnull String password);

	EncodedCommand encodeSuspendingAccountCommand(long id);

	EncodedCommand encodeRestoringAccountCommand(long id);

	EncodedCommand encodeGettingAccountsCommand();

	EncodedCommand encodeGettingAccountCommand(long id);

	EncodedCommand encodeUpdatingRolesCommand(long id, @Nonnull Role[] roles);
}
