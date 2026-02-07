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

package org.example.spark.inventory.interactors;

import jakarta.annotation.Nonnull;
import org.example.spark.inventory.models.Money;
import org.example.spark.inventory.models.RenderableItem;

public interface InventoryService {

	void addItem(@Nonnull String name, @Nonnull Money price, int amount, @Nonnull String idempotenceToken);

	void deleteItem(long itemId, @Nonnull String idempotenceToken);

	void updateAmount(long itemId, int amount);

	RenderableItem getItem(long itemId);

	RenderableItem[] getItems();
}
