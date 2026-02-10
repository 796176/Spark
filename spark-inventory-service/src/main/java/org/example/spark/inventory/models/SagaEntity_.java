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

@StaticMetamodel(SagaEntity.class)
public class SagaEntity_ {
	public static volatile EntityType<SagaEntity> class_;

	public static volatile String ID = "id";
	public static volatile String IDEMPOTENCE_TOKEN = "idempotenceToken";
	public static volatile String STATE = "state";
	public static volatile String ITEM = "item";
	public static volatile String CLASS_NAME = "className";

	public static volatile SingularAttribute<SagaEntity, Long> id;
	public static volatile SingularAttribute<SagaEntity, String> idempotenceToken;
	public static volatile SingularAttribute<SagaEntity, Integer> state;
	public static volatile SingularAttribute<SagaEntity, ItemEntity> item;
	public static volatile SingularAttribute<SagaEntity, String> className;
}
