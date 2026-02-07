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
import jakarta.annotation.Nullable;
import org.example.spark.inventory.aggregates.ItemAggregate;
import org.example.spark.inventory.events.ItemEvent;
import org.example.spark.inventory.models.Money;

public interface ItemDataAccess {

	ItemAggregate getItem(long id);

	ItemAggregate[] getItems();

	void persist(@Nonnull ItemAggregate item, @Nullable String idempotenceToken, @Nonnull ItemEvent... itemEvents);

	ItemAggregate addItem(@Nonnull String name, @Nonnull Money price, int amount, @Nonnull String idempotenceToken);
}
