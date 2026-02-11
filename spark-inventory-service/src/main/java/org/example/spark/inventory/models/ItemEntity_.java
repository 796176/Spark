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

package org.example.spark.inventory.models;

import jakarta.persistence.metamodel.*;

@StaticMetamodel(ItemEntity.class)
public class ItemEntity_ {
	public static volatile EntityType<ItemEntity> class_;

	public static volatile String ID = "id";
	public static volatile String NAME = "name";
	public static volatile String EMBEDDABLE_PRICE = "embeddablePrice";
	public static volatile String AMOUNT = "amount";
	public static volatile String ITEM_STATUS = "itemStatus";
	public static volatile String VERSION = "version";

	public static volatile SingularAttribute<ItemEntity, Long> id;
	public static volatile SingularAttribute<ItemEntity, String> name;
	public static volatile SingularAttribute<ItemEntity, EmbeddablePrice> embeddablePrice;
	public static volatile SingularAttribute<ItemEntity, Integer> amount;
	public static volatile SingularAttribute<ItemEntity, ItemStatus> itemStatus;
	public static volatile SingularAttribute<ItemEntity, Long> version;
}
