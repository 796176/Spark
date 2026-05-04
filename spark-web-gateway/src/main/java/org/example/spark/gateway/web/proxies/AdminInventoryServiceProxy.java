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
import jakarta.annotation.Nullable;
import org.example.spark.gateway.web.models.Account;
import org.example.spark.gateway.web.models.Money;
import org.example.spark.gateway.web.models.RemoteCallResult;

import java.util.function.Consumer;

public interface AdminInventoryServiceProxy {

	void getItems(@Nonnull Account account, @Nonnull Consumer<RemoteCallResult> callResultConsumer) throws Exception;

	void getItem(
		@Nonnull Account account, long itemId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void addItem(
		@Nonnull Account account,
		@Nonnull String name,
		@Nonnull Money price,
		int amount,
		@Nullable String pictureName,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void deleteItem(
		@Nonnull Account account, long itemId, @Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;

	void updateItemAmount(
		@Nonnull Account account,
		long itemId,
		int amount,
		@Nonnull String version,
		@Nonnull Consumer<RemoteCallResult> callResultConsumer
	) throws Exception;
}
