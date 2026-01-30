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
import org.example.spark.account.models.RenderableAccount;

import java.util.UUID;

public interface AccountService {

	void createAccount(@Nonnull String name, @Nonnull Password password, @Nonnull UUID commandId);

	void createAdminAccount(@Nonnull String name, @Nonnull Password password, @Nonnull UUID commandId);

	RenderableAccount getAccount(long id);

	RenderableAccount[] getAccounts();

	void deleteAccount(long id);

	void suspendAccount(long id);

	void restoreAccount(long id);

	void changeAccountRoles(long id, @Nonnull long... roles);
}
