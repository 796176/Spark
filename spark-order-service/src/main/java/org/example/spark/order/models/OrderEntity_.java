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

@StaticMetamodel(OrderEntity.class)
public class OrderEntity_ {
	public static volatile EntityType<OrderEntity> class_;

	public static volatile String ID = "id";
	public static volatile String ACCOUNT_ID = "accountId";
	public static volatile String TIMESTAMP = "timestamp";
	public static volatile String LINE_ITEMS = "lineItems";
	public static volatile String ORDER_STATUS = "orderStatus";
	public static volatile String VERSION = "version";

	public static volatile SingularAttribute<OrderEntity, Long> id;
	public static volatile SingularAttribute<OrderEntity, Long> accountId;
	public static volatile SingularAttribute<OrderEntity, Long> timestamp;
	public static volatile CollectionAttribute<OrderEntity, LineItemEntity> lineItems;
	public static volatile SingularAttribute<OrderEntity, OrderStatus> orderStatus;
	public static volatile SingularAttribute<OrderEntity, Long> version;
}
