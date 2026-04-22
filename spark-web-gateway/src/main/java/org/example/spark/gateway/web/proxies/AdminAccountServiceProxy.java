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

package org.example.spark.gateway.web.proxies;

import jakarta.annotation.Nonnull;
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.RemoteCallResult;

import java.util.function.Consumer;

public interface AdminAccountServiceProxy {

	void getAccounts(@Nonnull Account account, @Nonnull Consumer<RemoteCallResult> callResultConsumer) throws Exception;

	void getAccount(
		@Nonnull Account account, long accountId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void createAccount(
		@Nonnull Account account,
		@Nonnull String name,
		@Nonnull String password,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void createAdministratorAccount(
		@Nonnull Account account,
		@Nonnull String name,
		@Nonnull String password,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void suspendAccount(
		@Nonnull Account account, long accountId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void restoreAccount(
		@Nonnull Account account, long accountId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void updateRoles(
		@Nonnull Account account,
		long accountId,
		@Nonnull Role[] roles,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;
}
