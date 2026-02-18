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

package org.example.spark.order.models;

import jakarta.persistence.metamodel.*;

@StaticMetamodel(PendingReductionItem.class)
public class PendingReductionItem_ {
	public static volatile EntityType<PendingReductionItem> class_;

	public static volatile String ID = "id";
	public static volatile String ITEM = "item";
	public static volatile String AMOUNT = "amount";

	public static volatile SingularAttribute<PendingReductionItem, Long> id;
	public static volatile SingularAttribute<PendingReductionItem, ItemEntity> item;
	public static volatile SingularAttribute<PendingReductionItem, Integer> amount;
}
